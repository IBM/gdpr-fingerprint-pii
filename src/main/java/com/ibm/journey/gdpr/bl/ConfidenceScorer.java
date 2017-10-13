package com.ibm.journey.gdpr.bl;

import com.ibm.journey.gdpr.config.GDPRConfig;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class ConfidenceScorer {
	
	
	public JSONObject getConfidenceScore(JSONObject inputData) throws Exception{
		
		try{
			JSONObject outputJSON = new JSONObject();
			
			// get entities node from NLU data
			JSONArray entities = (JSONArray)inputData.get("entities");
			
			// for each PII in entities node, 
			// 		1. check what is it's category
			// 		2. Get weightage for that category
			// Create a new JSONObject for output purpose which will have
			// 		1. Categories that the PII belong to
			//		2. attach PII (along with weight) to respective categories
			// Calculate overall weight
			JSONArray categoriesArray = new JSONArray(); 
			if( entities == null ){
				return outputJSON;
			}
			for( int i = 0; i < entities.size(); i++ ){
				JSONObject entity = (JSONObject)entities.get(i);
				String piiType = entity.get("type").toString();
				String pii = entity.get("text").toString();
				String category = GDPRConfig.getCategoryForPII(piiType);
				if( category == null ){
					continue;
				}
				float weight = GDPRConfig.getWeightForCategory(category);
				
				JSONObject attributeWeight = new JSONObject();
				attributeWeight.put("piitype", piiType);
				attributeWeight.put("pii", pii);
				attributeWeight.put("weight", weight);
				
				boolean foundCategory = false; // To check if a PII's category is already added to outputData or not
				JSONObject categoryEntry = null; // individual category (e.g. Medium category)
				JSONArray attributesArray = null; // pii array (nodes containing piitype, piitext and weight)
				// If a category is already added in output data, then no need to add again
				// otherwise add a category entry to output data
				for( int j = 0; j < categoriesArray.size(); j++ ){
					categoryEntry = (JSONObject)categoriesArray.get(j);
					attributesArray = (JSONArray)categoryEntry.get(category);
					if( attributesArray != null ){
						foundCategory = true; // category is already added
						break;
					}
				}
				if( !foundCategory ){
					attributesArray = new JSONArray(); // category not found.. new array of pii attributes
				}
				attributesArray.add(attributeWeight); // add attributes node
				
				if( categoryEntry == null ){ // category was not found
					categoryEntry = new JSONObject();
				}
				categoryEntry.put(category, attributesArray); // add catgeory specific array
				if( !foundCategory && categoriesArray.size() <= 0 ){
					categoriesArray.add(categoryEntry); // this is done only once so that reference is added
				}
				
				outputJSON.put("categories",categoriesArray); // add categories node
			}
			outputJSON.put("PIIConfidenceScore", calculateConfidence(categoriesArray));
			return outputJSON;			
		}catch( Exception e){
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	private float calculateConfidence(JSONArray categoriesArray) throws Exception{
		// Get categories in order of priority
		float currentConfidence = 0;
		try{
			String[] configCategories = GDPRConfig.getCategories();
			for( int i = 0; i < configCategories.length; i++ ){ // categories in order of weightage
				String category = configCategories[i];
				System.out.println("Category = " + category);
				// calculate weightage for this category
				for( int j = 0; j < categoriesArray.size(); j++ ){
					JSONObject piiDataCategoryNode = (JSONObject)categoriesArray.get(j);
					JSONArray attributesArray = (JSONArray)piiDataCategoryNode.get(category);
					if( attributesArray != null ){
						// calculate weightage for all PII entries in this node
						for( int k = 0; k < attributesArray.size(); k++ ){
							JSONObject piiNode = (JSONObject)attributesArray.get(k);
							String piiType = piiNode.get("piitype").toString();
							String pii = piiNode.get("pii").toString();
							float weight = ((Float)piiNode.get("weight")).floatValue();
							System.out.println("PII Node: piitype = " + piiType + ", pii = " + pii + ", weight = " + weight);
							currentConfidence = getUpdatedConfidence(weight, currentConfidence);
						}
					}
				}
			}
			return currentConfidence;
			
		}catch( Exception e){
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
			throw e;
		}
	}
	
	
	private float getUpdatedConfidence(float weight, float currentConfidence){
		// current_confidence = current_confidence + (weight% of remaining confidence)
		Float remainingConfidence = 100 - currentConfidence;
		Float confidenceToAdd = (weight/100)*remainingConfidence;
		currentConfidence = currentConfidence + confidenceToAdd;
		return currentConfidence;
	}

}

package com.ibm.journey.gdpr.bl;

import org.apache.log4j.Logger;

import com.ibm.journey.gdpr.config.GDPRConfig;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * This class implements the algorithm for scoring logic
 *
 * Scoring logic:

Let us divide PII data into, say, 3 categories. We can divide into how many ever categories we want, but for explanation sake, let us consider 3 categories. Let's name them as high, medium and low. Let us assign weightages to these categories as follows (again weightages here are for explanation sake and we can assign any weightage we feel would make sense)
high - 60%
medium - 30%
low - 10%

There can be zero or more number of occurrences of each category of PII data. And we want confidence scoring to be between 0 and 1 always.

Algorithm goes as follows:
- Consider all PII data in high->medium->low order only. Any change in order might result in a different score
- Let us begin with current_score of 0 (zero)
- Let us say we have 1 high, 2 medium and 1 low PII data
- we start with high. It's weightage is 70%. So let us assign current_score as 70%
- we are now left with 30 (100 - current_score). and the next PII attribute's weightage is 50% (the first of the 2 medium ones)
- Next let's take weightage percentage of this remaining score, aka 50% of 30 which comes to 15. Add this 15 to current score. So now the current_score will be 70+15 = 85
- Next weightage is again 50% (the second of medium). Remaining score that can be added is 15. So the new score now becomes 50% of 15 which is 7.5. current_score = 85+7.5 = 92.5
- The last weightage is 30%. remaining score is 100-92.5 = 7.5. So 30% of 7.5 is 2.25. current_score = 92.5 + 2.25 = 94.75

So the document score is 0.9475
With this logic the document scores will always be between 0 and 1. And since we consider weightages in the order of high->medium->low the scoring is consistent. We can try documents, whose score we know already, and check what will be the score using this algorithm. We can then tweak weightages so that the algorithm score matches the actual score. This will help us arrive at optimal weightage values.
 *
 */

public class ConfidenceScorer {

	final static Logger logger = Logger.getLogger(ConfidenceScorer.class);

	/**
	 * Get the confidence score on Personal data based on the logic explain in class comments
	 * @param inputData The JSON data on which scoring algorithm is applied
	 * @return outputJSON JSON data with scoring details
	 */

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
				piiType = piiType.substring(0, 1).toUpperCase() + piiType.substring(1); // make first letter caps
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
							currentConfidence = getUpdatedConfidence(weight, currentConfidence);
						}
					}
				}
			}
			return currentConfidence/100;

		}catch( Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 *
	 * @param weight weightage of the category
	 * @param currentConfidence Confidence score of the document. This gets updated as and when each of the personal data occurances are processed
	 * @return currentConfidence The updated confidence score
	 */

	private float getUpdatedConfidence(float weight, float currentConfidence){
		// current_confidence = current_confidence + (weight% of remaining confidence)
		Float remainingConfidence = 100 - currentConfidence;
		Float confidenceToAdd = (weight/100)*remainingConfidence;
		currentConfidence = currentConfidence + confidenceToAdd;
		return currentConfidence;
	}

}

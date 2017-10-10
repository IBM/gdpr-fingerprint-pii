package com.ibm.journey.gdpr.bl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;

public class PIIDataExtractor {
	
	public JSONObject getPII(JSONObject inputJSON) throws Exception{
		try{
			// pass text to NLU and get entities.. NLU uses a custom model build using WKS
			JSONObject nluOuput = invokeNLU(inputJSON.get("text").toString());
			System.out.println("NLU Output = " + nluOuput);
			
			// Now parse for regular expressions from config
			JSONObject regexOutput = parseForRegularExpression(inputJSON.get("text").toString(), nluOuput);
			System.out.println("regexOutput = " + regexOutput);
			
			// pass NLU output for adding weights and scoring the document
			JSONObject scorerOutput = invokeScorer(regexOutput);
			System.out.println("scorerOutput = " + scorerOutput);

			return scorerOutput;
		}catch( Exception e){
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
			throw e;
		}
	}
	
	public JSONObject getPIIForD3(JSONObject inputJSON) throws Exception{
		try{
			JSONObject nfD3OutputWithScore = new JSONObject();
			JSONObject nfD3Output = new JSONObject(); // new format output
			
			// Change format for D3 display
			String nfNameL1Str = "Categories";
			nfD3Output.put("name", nfNameL1Str); // new format - parent node
			JSONArray nfChildrenL1Array = new JSONArray(); // new format - children of categories
			nfD3Output.put("children", nfChildrenL1Array);
			
			JSONObject ofScorerOutput = getPII(inputJSON); // old format output
			nfD3OutputWithScore.put("score", ofScorerOutput.get("PIIConfidenceScore"));
			nfD3OutputWithScore.put("treeData", nfD3Output);
			JSONArray ofCategoriesArray = (JSONArray)ofScorerOutput.get("categories"); // old format parent node
			if( ofCategoriesArray.size() > 0 ){
				JSONObject ofCategories = (JSONObject)ofCategoriesArray.get(0); // old format - top level Object in categories Array
				
				for (Object ofKey : ofCategories.keySet()) { // Old format For each category
			        //based on you key types
			        String ofKeyStr = (String)ofKey; // Category name.. e.g. Very_High
			        // Add this key as name
			        JSONObject nfNameL2 = new JSONObject(); // new format - Category name
			        nfNameL2.put("name", ofKeyStr);
			        JSONArray nfChildrenL2Array = new JSONArray(); // 
			        nfNameL2.put("children", nfChildrenL2Array);
			        nfChildrenL1Array.add(nfNameL2);
			        
			        // add children to nfChildrenL2Array
			        JSONArray ofPIIArrayForGivenCategory = (JSONArray)ofCategories.get(ofKeyStr); // Old format - PII Array for a given Category
			        if( ofPIIArrayForGivenCategory != null && ofPIIArrayForGivenCategory.size() > 0 ){
			        	for( int i = 0; i < ofPIIArrayForGivenCategory.size(); i++ ){
			        		JSONObject ofPIIObject = (JSONObject)ofPIIArrayForGivenCategory.get(i);
					        String ofPIIType = (String)ofPIIObject.get("piitype");
					        String ofPII = (String)ofPIIObject.get("pii");
					        double ofWeight = ((Double)ofPIIObject.get("weight")).doubleValue();
					        
					        JSONObject nfNameL3 = new JSONObject();
					        nfNameL3.put("name", ofPIIType);
					        JSONArray nfChildrenL3Array = new JSONArray(); // 
					        nfNameL3.put("children", nfChildrenL3Array);
					        nfChildrenL2Array.add(nfNameL3);
					        
					        
					        // add children to nfChildrenL3Array
					        JSONObject nfNameL4 = new JSONObject();
					        nfNameL4.put("name", ofPII);
					        JSONArray nfChildrenL4Array = new JSONArray(); // 
					        nfNameL4.put("children", nfChildrenL4Array);
					        nfChildrenL3Array.add(nfNameL4);
					        
					        // add children to nfChildrenL4Array
					        JSONObject nfNameL5 = new JSONObject();
					        nfNameL5.put("name", "Weightage " + ofWeight);
					        nfChildrenL4Array.add(nfNameL5);
			        	}
			        }
			    }
			}

			return nfD3OutputWithScore;
		}catch( Exception e){
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
			throw e;
		}
	}
	
	// Invoke NLU to capture NLU output
	private JSONObject invokeNLU(String text) throws Exception{
		try{
			String user = System.getenv("username");
			String password = System.getenv("password");
			String model = System.getenv("wks_model");
			System.out.println(user + ":" + password + ":" + model);
			NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
					NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, user, password);
			
			EntitiesOptions entitiesOptions = new EntitiesOptions.Builder().model(model).emotion(true).sentiment(true).limit(20).build();
			KeywordsOptions keywordsOptions = new KeywordsOptions.Builder().emotion(true).sentiment(true).limit(20).build();
			Features features = new Features.Builder().entities(entitiesOptions).keywords(keywordsOptions).build();
			AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(text).features(features).build();
			AnalysisResults response = service.analyze(parameters).execute();
			
			return JSONObject.parse(response.toString());
		}catch( Exception e){
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}
	
	
	private JSONObject parseForRegularExpression(String text, JSONObject nluJSON){
		// get, from config, what entity types to be used for regular expression
		String regexEntityTypesConfig = System.getenv("regex_params");
		String[] regexEntityTypes = regexEntityTypesConfig.split(",");
		
		// extract string from text for each regex and build JSONObjects for them
		JSONArray entities = (JSONArray) nluJSON.get("entities");
		if( regexEntityTypes != null && regexEntityTypes.length > 0 ){
			for( int i = 0; i < regexEntityTypes.length; i++ ){
				String regexEntityType = regexEntityTypes[i];
				// Get regular expression got this entity type
				String regex = System.getenv(regexEntityType + "_regex");
				
				// Now get extracted values from text
				// getting is as a list
				List<String> matchResultList = getRegexMatchingWords(text, regex);
				
				// Add entries in this regex to entities in nluOutput, for each result
				// First build a JSONObject
				if( matchResultList != null && matchResultList.size() > 0 ){
					for(int j = 0; j < matchResultList.size(); j++ ){
						String matchResult = matchResultList.get(j);
						
						JSONObject entityEntry = new JSONObject();
						entityEntry.put("type", regexEntityType);
						entityEntry.put("text", matchResult);
						entities.add(entityEntry);
					}
				}
			}
		}
		
		return nluJSON;
	}
	
	
	private List<String> getRegexMatchingWords(String text, String patternStr){
		Pattern p = Pattern.compile(patternStr);
									
		Matcher m = p.matcher(text);
		
		List<String> matchResultList = new ArrayList<String>();
		while(m.find()) {
            matchResultList.add(m.group());
        }
		return matchResultList;
	}
	
	
	// Invoke Document Scorer.. 
	// Document scorer provides a confidence score based on configuration settings
	private JSONObject invokeScorer(JSONObject nluOuput) throws Exception{
		
		try{
			// Send "is" to scorer
			String scorerURL = System.getenv("scorerurl");
			URL url = new URL(scorerURL);
			HttpURLConnection conn = (HttpURLConnection)
			url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
			OutputStream os = conn.getOutputStream();
			os.write(nluOuput.toString().getBytes());
			os.flush();
			
			if (conn.getResponseCode() != 200 ) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
			}
			
			JSONObject outputJSON = JSONObject.parse(conn.getInputStream());

			conn.disconnect();
			
			return outputJSON;
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}
	
}

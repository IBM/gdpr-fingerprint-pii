package com.ibm.journey.gdpr.bl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;


/**
 * Implementation classes for the REST interfaces
 */
public class PIIDataExtractor {

	final static Logger logger = Logger.getLogger(PIIDataExtractor.class);

	/**
	 * This method extracts personal data and scores those personal data.
	 * Output data is more generic, targetted to be consumed by other applications
	 *
	 * @param inputJSON Unstructired text in a JSON object
	 * @return scorerOutput Personal data and score output as JSON
	 */
	public JSONObject getPersonalDataInGenericFormat(JSONObject inputJSON) throws Exception {
		try {
			// pass text to NLU and get entities.. NLU uses a custom model build
			// using WKS
			JSONObject nluOuput = getNLUOutput(inputJSON.get("text").toString());
			if (logger.isInfoEnabled()) {
				logger.info("NLU Response data");
				logger.info(nluOuput);
			}

			// Now parse for regular expressions from config
			DataExtractorUsingRegex xtractorUsingRegex = new DataExtractorUsingRegex();
			JSONObject regexOutput = xtractorUsingRegex.parseForRegularExpression(inputJSON.get("text").toString(), nluOuput);
			if (logger.isInfoEnabled()) {
				logger.info("Entities data after parsing for regular expressions");
				logger.info(regexOutput);
			}

			// pass NLU output for adding weights and scoring the document
			JSONObject scorerOutput = new ConfidenceScorer().getConfidenceScore(regexOutput);
			if (logger.isInfoEnabled()) {
				logger.info("Entities data after assigning scores");
				logger.info(scorerOutput);
			}

			return scorerOutput;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * This method extracts personal data and scores those personal data.
	 * Output data is formatted so that it can be consumed by D3 tree viewer library
	 * This method used Generic Format to create a veiw format.
	 *
	 * @param payload Text document from which personal data needs to be extracted
	 * @return nfD3OutputWithScore Personal data and score output as JSON to be used by D3 tree
	 */
	public JSONObject getPersonalDataForViewer(JSONObject inputJSON) throws Exception {
		try {
			JSONObject nfD3OutputWithScore = new JSONObject();
			JSONObject nfD3Output = new JSONObject(); // view format output

			// Change format for D3 display
			String nfNameL1Str = "Categories";
			nfD3Output.put("name", nfNameL1Str); // view format - parent node
			// view format - children of categories
			JSONArray nfChildrenL1Array = new JSONArray();
			nfD3Output.put("children", nfChildrenL1Array);

			JSONObject ofScorerOutput = getPersonalDataInGenericFormat(inputJSON); // generic format output
			nfD3OutputWithScore.put("score", ofScorerOutput.get("PIIConfidenceScore"));
			nfD3OutputWithScore.put("treeData", nfD3Output);
			// generic format parent node
			JSONArray ofCategoriesArray = (JSONArray) ofScorerOutput.get("categories");
			if (ofCategoriesArray.size() > 0) {
				// generic format - top level Object in categories Array
				JSONObject ofCategories = (JSONObject) ofCategoriesArray.get(0);

				// generic format For each category
				for (Object ofKey : ofCategories.keySet()) {
					// based on you key types
					// Category name.. e.g. Very_High
					String ofKeyStr = (String) ofKey;
					// Add this key as name
					// view format - Category name
					JSONObject nfNameL2 = new JSONObject();
					String categoryWeight = "";
					JSONArray nfChildrenL2Array = new JSONArray(); //
					nfNameL2.put("children", nfChildrenL2Array);
					nfChildrenL1Array.add(nfNameL2);

					// add children to nfChildrenL2Array
					// generic format - PII Array for a given Category
					JSONArray ofPIIArrayForGivenCategory = (JSONArray) ofCategories.get(ofKeyStr);
					if (ofPIIArrayForGivenCategory != null && ofPIIArrayForGivenCategory.size() > 0) {
						for (int i = 0; i < ofPIIArrayForGivenCategory.size(); i++) {
							JSONObject ofPIIObject = (JSONObject) ofPIIArrayForGivenCategory.get(i);
							String ofPIIType = (String) ofPIIObject.get("piitype");
							String ofPII = (String) ofPIIObject.get("pii");
							categoryWeight = ((Float) ofPIIObject.get("weight")).floatValue() + "";

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
							/*
							 * JSONObject nfNameL5 = new JSONObject();
							 * nfNameL5.put("name", "Weightage " + ofWeight);
							 * nfChildrenL4Array.add(nfNameL5);
							 */
						}
					}
					if (categoryWeight.length() > 0) {
						nfNameL2.put("name", ofKeyStr + " (Weightage: " + categoryWeight + ")");
					} else {
						nfNameL2.put("name", ofKeyStr);
					}
				}
			}

			return nfD3OutputWithScore;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// Invoke NLU to capture NLU output
	/**
	  * Invoke NLU to extract keywords (entities)
	  *
	  * @param text Text document from which personal data needs to be extracted
	  * @return response.toString() NLU output as a String
	  */
	private JSONObject getNLUOutput(String text) throws Exception {
		try {
			Map<String, String> nluCreds = getNLUCredentials();
//			String user = System.getenv("username");
//			String password = System.getenv("password");
			String model = System.getenv("wks_model");
//			if (logger.isInfoEnabled()) {
//				logger.info("NLU Instance details");
//				logger.info("user: " + user + ", modelid: " + model + "  For password, refer env variables");
//			}

			NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
					NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, nluCreds.get("username"), nluCreds.get("password"));

			EntitiesOptions entitiesOptions = new EntitiesOptions.Builder().model(model).emotion(true).sentiment(true)
					.limit(20).build();
			KeywordsOptions keywordsOptions = new KeywordsOptions.Builder().emotion(true).sentiment(true).limit(20)
					.build();
			Features features = new Features.Builder().entities(entitiesOptions).keywords(keywordsOptions).build();
			AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(text).features(features).build();
			AnalysisResults response = service.analyze(parameters).execute();

			return JSONObject.parse(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}


	private Map<String, String> getNLUCredentials() throws Exception{

		Map<String, String> nluCredentialsMap = new HashMap<String, String>();

		try{
			String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
			//String VCAP_SERVICES = "{\"natural-language-understanding\":[{\"credentials\":{\"url\":\"https://gateway.watsonplatform.net/natural-language-understanding/api\",\"username\":\"8ea86c6c-c681-4186-bf91-e766413145ad\",\"password\":\"XaQHFNhhpnKX\"},\"syslog_drain_url\":null,\"volume_mounts\":[],\"label\":\"natural-language-understanding\",\"provider\":null,\"plan\":\"free\",\"name\":\"NLU - GDPR\",\"tags\":[\"watson\",\"ibm_created\",\"ibm_dedicated_public\",\"lite\"]}]}";

			if (VCAP_SERVICES != null) {
				// parse the VCAP JSON structure
				JSONObject obj = (JSONObject) JSONObject.parse(VCAP_SERVICES);
				JSONArray nluArray = (JSONArray)obj.get("natural-language-understanding");
				if( nluArray != null && nluArray.size() > 0 ){
					for( int i = 0; i < nluArray.size(); i++ ){
						JSONObject o = (JSONObject)nluArray.get(i);
						if( o.get("credentials") != null ){
							JSONObject credsObject = (JSONObject)o.get("credentials");
							nluCredentialsMap.put("username", (String)credsObject.get("username"));
							nluCredentialsMap.put("password", (String)credsObject.get("password"));
							nluCredentialsMap.put("nluURL", (String)credsObject.get("url"));
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		if( nluCredentialsMap.get("username") == null ){
			throw new Exception("NLU Credentials not found");
		}
		return nluCredentialsMap;
	}




}

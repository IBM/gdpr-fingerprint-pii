package com.ibm.journey.gdpr.bl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class DataExtractorUsingRegex {

	/**
	  * This method extracts personal data using regular expressions
	  *
	  * @param text Text document from which text matching regular expressions need to be extracted
	  * @param nluJSON NLUOutput
	  * @return nluJSON Regular expression parsing results are appended to the input NLU output and returned
	  */
	public JSONObject parseForRegularExpression(String text, JSONObject nluJSON) {
		// get, from config, what entity types to be used for regular expression
		String regexEntityTypesConfig = System.getenv("regex_params");
		String[] regexEntityTypes = regexEntityTypesConfig.split(",");

		// extract string from text for each regex and build JSONObjects for
		// them
		JSONArray entities = (JSONArray) nluJSON.get("entities");
		if (entities == null) {
			return new JSONObject();
		}
		if (regexEntityTypes != null && regexEntityTypes.length > 0) {
			for (int i = 0; i < regexEntityTypes.length; i++) {
				String regexEntityType = regexEntityTypes[i];
				// Get regular expression got this entity type
				String regex = System.getenv(regexEntityType + "_regex");

				// Now get extracted values from text
				// getting is as a list
				List<String> matchResultList = getRegexMatchingWords(text, regex);

				// Add entries in this regex to entities in nluOutput, for each
				// result
				// First build a JSONObject
				if (matchResultList != null && matchResultList.size() > 0) {
					for (int j = 0; j < matchResultList.size(); j++) {
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


	/**
	  * This method matches text for a given regular expression
	  *
	  * @param text input text data on which regular expression is run
	  * @param patternStr regular expression string
	  * @return matchResultList results of regular expression matching
	  */
	private List<String> getRegexMatchingWords(String text, String patternStr) {
		System.out.println("text = " + text);
		System.out.println("patternStr = " + patternStr);

		Pattern p = Pattern.compile(patternStr);

		Matcher m = p.matcher(text);

		List<String> matchResultList = new ArrayList<String>();
		while (m.find()) {
			matchResultList.add(m.group());
		}
		return matchResultList;
	}
}

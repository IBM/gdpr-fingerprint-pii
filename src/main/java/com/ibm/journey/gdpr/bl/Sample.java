package com.ibm.journey.gdpr.bl;

import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.watson.natural_language_understanding.v1.model.KeywordsOptions;

public class Sample {

	public static void main(String[] args) {
		try {

			IamOptions options = new IamOptions.Builder()
				    .apiKey("tWlQZ68lVhvrWCItIkaYLIEqPG4yQTQ9D9XL0R6fsSiL")
				    .build();
			
			NaturalLanguageUnderstanding nluservice = new NaturalLanguageUnderstanding("2018-11-16", options);
			nluservice.setEndPoint("https://gateway.watsonplatform.net/natural-language-understanding/api");
			
//			NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
//					NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, nluCreds.get("username"), nluCreds.get("password"));

			EntitiesOptions entitiesOptions = new EntitiesOptions.Builder().emotion(true).sentiment(true)
					.limit(20).build();
			KeywordsOptions keywordsOptions = new KeywordsOptions.Builder().emotion(true).sentiment(true).limit(20)
					.build();
			Features features = new Features.Builder().entities(entitiesOptions).keywords(keywordsOptions).build();
			AnalyzeOptions parameters = new AnalyzeOptions.Builder().text("Hello, I am murali.. who are you?").features(features).build();
			
			AnalysisResults response = nluservice
					  .analyze(parameters)
					  .execute()
					  .getResult();
			
				System.out.println(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}

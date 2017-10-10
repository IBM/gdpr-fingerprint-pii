package com.ibm.journey.gdpr.scorer.rest;

import java.io.InputStream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.journey.gdpr.scorer.bl.ConfidenceScorer;
import com.ibm.json.java.JSONObject;
/*
 * Confidence scorer REST interface
 */
@ApplicationPath("/rest")
@Path("confidencescore")
public class ScorerRest extends Application{
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response scoreDocument(InputStream is){

		try {
			JSONObject inputJSON = JSONObject.parse(is);
			System.out.println("starting scorer rest");
			ConfidenceScorer confidenceScorer = new ConfidenceScorer();
			JSONObject outputJSON = confidenceScorer.getConfidenceScore(inputJSON);
			
			return Response.ok(outputJSON, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
					.header("Access-Control-Allow-Headers", "Access-Control-Allow-Origin")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Content-Type", "application/json")
					.build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testMethod(){
		JSONObject inputJSON = new JSONObject();
		inputJSON.put("Message", "Scorer Get operation is success");
		
		return Response.ok(inputJSON, MediaType.APPLICATION_JSON)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
				.header("Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token, x-requested-with")
				.build();
	}

	

}

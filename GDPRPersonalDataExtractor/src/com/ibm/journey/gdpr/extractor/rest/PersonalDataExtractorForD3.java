
package com.ibm.journey.gdpr.extractor.rest;

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

import com.ibm.journey.gdpr.bl.PIIDataExtractor;
import com.ibm.json.java.JSONObject;

/*
 * Document PII data extractor REST interface
 */

@ApplicationPath("/rest")
@Path("piiextractorford3")
public class PersonalDataExtractorForD3 extends Application {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response scoreDocument(InputStream is) {
		System.out.println("Request came to PII D3");
		try {
			if (is == null) {
				return Response.serverError().entity("Input data not available").build();
			}
			JSONObject inputJSON = JSONObject.parse(is);
			
			PIIDataExtractor piiDataExtractor = new PIIDataExtractor();
			JSONObject response = piiDataExtractor.getPIIForD3(inputJSON);
			
			
			return Response.ok(response, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testMethod() {
		JSONObject inputJSON = new JSONObject();
		inputJSON.put("Message", "D3 Get operation was successful");
		
		return Response.ok(inputJSON, MediaType.APPLICATION_JSON)
				.header("Access-Control-Allow-Origin", "*")
				.build();
	}
	

}

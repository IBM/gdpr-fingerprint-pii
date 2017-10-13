package com.ibm.journey.gdpr.rest;

import java.io.InputStream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
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
@Path("/personaldata")
public class PersonalDataExtractor extends Application {

	
	@POST
	@Path("/forviewer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonalDataForViewer(InputStream payload) {
		// TODO logging
		try {
			if (payload == null) {
				return Response.serverError().entity("Input data not available").build();
			}
			JSONObject payloadJSON = JSONObject.parse(payload);
			
			PIIDataExtractor piiExtractor = new PIIDataExtractor();
			JSONObject response = piiExtractor.getPIIForD3(payloadJSON);
			
			
			return Response.ok(response, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	@POST
	@Path("/forconsumer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonalDataForConsumer(InputStream is) {
		try {
			if (is == null) {
				return Response.serverError().entity("Input data not available").build();
			}
			JSONObject inputJSON = JSONObject.parse(is);
			
			PIIDataExtractor piiDataExtractor = new PIIDataExtractor();
			JSONObject response = piiDataExtractor.getPII(inputJSON);
			
			return Response.ok(response, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

}

package com.ibm.journey.gdpr.viewer.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.json.java.JSONObject;

@ApplicationPath("/rest")
@Path("getpiidata")
public class PIIDataGetter extends Application {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPIIData(InputStream is) {
		try {
			if (is == null) {
				return Response.serverError().entity("Input data not available").build();
			}
			JSONObject inputJSON = JSONObject.parse(is);
			
			JSONObject outputJSON = invokeDataExtractor(inputJSON);
			
			return Response.ok(outputJSON, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.build();
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	
	// Invoke Document Scorer.. 
	// Document scorer provides a confidence score based on configuration settings
	private JSONObject invokeDataExtractor(JSONObject inputJSON) throws MalformedURLException, IOException{
		
		try{
			// Send "is" to data extractor
			String extractorURL = System.getenv("wrapperurl");
			System.out.println("wrapperurl = " + extractorURL);
			URL url = new URL(extractorURL);
			HttpURLConnection conn = (HttpURLConnection)
			url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
			OutputStream os = conn.getOutputStream();
			os.write(inputJSON.toString().getBytes());
			os.flush();
			int responseCode = conn.getResponseCode();
			if ( responseCode != 200 ) {
				System.out.println("Response code = " + responseCode);

				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
			}
			
			JSONObject outputJSON = JSONObject.parse(conn.getInputStream());

			conn.disconnect();
			
			return outputJSON;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
}

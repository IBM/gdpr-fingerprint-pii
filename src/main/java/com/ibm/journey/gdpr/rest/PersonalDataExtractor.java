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

import org.apache.log4j.Logger;

import com.ibm.journey.gdpr.bl.PIIDataExtractor;
import com.ibm.json.java.JSONObject;

/**
 * Personal Data Extractor and scorer REST interface. It has two POST methods.
 * "/forviewer" method's response is intended to be consumed by UI (D3), which
 * requires a particular format of data. "/forconsumer" method's response is
 * more generic and can be consumed by other applications which can then use the
 * data for other purposes as needed by calling applications
 */


@ApplicationPath("/rest")
@Path("/personaldata")
public class PersonalDataExtractor extends Application {

	final static Logger logger = Logger.getLogger(PersonalDataExtractor.class);

	/**
	 * This method extracts personal data and scores those personal data.
	 * Output data is formatted so that it can be consumed by D3 tree viewer library
	 *
	 * @param payload Text document from which personal data needs to be extracted
	 * @return Response Personal data and score output as JSON
	 */
	@POST
	@Path("/forviewer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonalDataForViewer(InputStream payload) {
		try {
			if (payload == null) {
				logger.error("Input data not available");
				return Response.serverError().entity("Input data not available").build();
			}

			JSONObject payloadJSON = JSONObject.parse(payload);
			if(logger.isInfoEnabled()){
			    logger.info("Input text provided by user");
			    logger.info(payloadJSON.toString());
			}

			PIIDataExtractor piiExtractor = new PIIDataExtractor();
			JSONObject response = piiExtractor.getPersonalDataForViewer(payloadJSON);

			if(logger.isInfoEnabled()){
			    logger.info("Output for D3 viewer format");
			    logger.info(response.toString());
			}

			return Response.ok(response, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}


	/**
	 * This method extracts personal data and scores those personal data.
	 * Output data is more generic, targetted to be consumed by other applications
	 *
	 * @param payload Text document from which personal data needs to be extracted
	 * @return Response Personal data and score output as JSON
	 */
	@POST
	@Path("/forconsumer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonalDataForConsumer(InputStream payload) {
		try {
			if (payload == null) {
				return Response.serverError().entity("Input data not available").build();
			}
			JSONObject payloadJSON = JSONObject.parse(payload);
			if(logger.isInfoEnabled()){
			    logger.info("Input text provided by user");
			    logger.info(payloadJSON.toString());
			}


			PIIDataExtractor piiDataExtractor = new PIIDataExtractor();
			JSONObject response = piiDataExtractor.getPersonalDataInGenericFormat(payloadJSON);

			if(logger.isInfoEnabled()){
			    logger.info("Output for consumer format");
			    logger.info(response.toString());
			}


			return Response.ok(response, MediaType.APPLICATION_JSON)
					.header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

}

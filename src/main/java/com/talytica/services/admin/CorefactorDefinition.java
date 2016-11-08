package com.talytica.services.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.util.CorefactorUtil;

@Path("corefactor")
public class CorefactorDefinition {

	private static final Logger log = LoggerFactory.getLogger(CorefactorDefinition.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorefactors() {
		log.debug("Fetching corefactor definitions");

		JSONArray jsonCorefactors = CorefactorUtil.getJsonCorefactors();

		return Response.status(Response.Status.OK).entity(jsonCorefactors.toString()).build();
	}



	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upsertCoreFactors(String corefactorDefinitions) {
		log.debug("Received request for corefactor definitions upsert");
		JSONObject resultEntity = new JSONObject();
		ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST)
				.entity(resultEntity.put("message", "Bad Request - Incorrect Corefactors Definition").toString());
		
		if(null != corefactorDefinitions && !corefactorDefinitions.isEmpty()) {
			try {
				log.debug("Proceeding with corefactor upserts");
				CorefactorUtil.persistCorefactors(corefactorDefinitions);
				responseBuilder = Response.status(Response.Status.OK)
						.entity(resultEntity.put("message", "Corefactor definitions updated successfully").toString());;
			} catch (Exception e) {
				log.debug("Failed to persist corefactor definitions. " + e);
				responseBuilder = Response.status(Response.Status.OK)
						.entity(resultEntity.put("message", e.getMessage()).toString());
			}			
		}
		
		return responseBuilder.build();
	}
		
}

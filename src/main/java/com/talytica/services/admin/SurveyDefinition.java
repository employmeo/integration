package com.talytica.services.admin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONObject;

import com.employmeo.objects.Survey;
import com.employmeo.util.SurveyUtil;

@Path("definition")
public class SurveyDefinition {

	private static final Logger log = LoggerFactory.getLogger(SurveyDefinition.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSurveyDefinition(@QueryParam("survey_id") Long surveyId) {
		log.debug("processing with survey_id: " + surveyId);
		
		// initialize as Not Found
		ResponseBuilder responseBuilder = Response.status(Response.Status.NOT_FOUND);
		
		JSONObject json = new JSONObject();
		Survey survey = Survey.getSurveyById(surveyId);
		if(null != survey) {
			survey.refreshMe();
			json.put("survey", survey.getJSON());
			responseBuilder = Response.status(Response.Status.OK).entity(survey.getJSONString());
		}
		
		return responseBuilder.build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSurveyDefinition(String surveyDefinition) {
		log.debug("Creating new survey definition");
		JSONObject resultEntity = new JSONObject();
		ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST)
				.entity(resultEntity.put("message", "Bad Request - Incorrect Survey Definition").toString());
		
		if(null != surveyDefinition && !surveyDefinition.isEmpty()) {
			JSONObject json = new JSONObject(surveyDefinition);
			//log.debug("Hydrated JSONObject: " + json);
			
			Survey survey = Survey.fromJSON(json);
			log.debug("processing new survey definition with id: " + survey.getSurveyId());
			
			try {
				SurveyUtil.persistSurvey(survey);
				responseBuilder = Response.status(Response.Status.OK)
						.entity(resultEntity.put("message", "Survey definition persisted successfully").toString());;
			} catch (Exception e) {
				log.debug("Failed to persist survey. " + e);
				responseBuilder = Response.status(Response.Status.OK)
						.entity(resultEntity.put("message", e.getMessage()).toString());
			}			
		}
		
		return responseBuilder.build();
	}
	
}

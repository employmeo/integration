package com.talytica.services.admin;


import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.employmeo.objects.Survey;
import com.employmeo.util.DBUtil;

@Path("list")
public class GetSurveyList {

	private static final Logger log = LoggerFactory.getLogger(GetSurveyList.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getSurveyList() {
		log.debug("Fetching survey list");

		JSONArray response = new JSONArray();	
		List<Survey> surveys = getSurveys();
		
		surveys.forEach(survey-> {
			JSONObject surveyJson = new JSONObject();
			surveyJson.put("survey_name", survey.getSurveyName());
			surveyJson.put("survey_id", survey.getSurveyId());
			response.put(surveyJson);
		});

		return response.toString();		
	}

	private List<Survey> getSurveys() {
		return DBUtil.getEntityManager().createNamedQuery("Survey.findAll").getResultList();
	}

}

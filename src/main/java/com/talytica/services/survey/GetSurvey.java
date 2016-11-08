package com.talytica.services.survey;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.employmeo.objects.AccountSurvey;
import com.employmeo.objects.Respondant;
import com.employmeo.objects.Response;

@Path("getsurvey")
public class GetSurvey {

	private static final Logger log = LoggerFactory.getLogger(GetSurvey.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String doPost(
			// @FormParam("start_time") TimeStamp startTime,
			@Context final HttpServletRequest reqt,
			@FormParam("respondant_uuid") UUID respondantUuid) {

		log.debug("processing with: " + respondantUuid);
		
		JSONObject json = new JSONObject();
		Respondant respondant = Respondant.getRespondantByUuid(respondantUuid);

		if (respondant != null) {
			respondant.refreshMe(); // make sure to get latest and greatest from
			if (respondant.getRespondantStatus() < Respondant.STATUS_STARTED) {
				respondant.setRespondantStatus(Respondant.STATUS_STARTED);
				respondant.setRespondantStartTime(new Timestamp(new Date().getTime()));
				respondant.setRespondantUserAgent(reqt.getHeader("User-Agent"));
				respondant.mergeMe();
			} else if (respondant.getRespondantStatus() >= Respondant.STATUS_COMPLETED) {
				// TODO put in better error handling here.
				json.put("message", "This assessment has already been completed and submitted");
			}

			AccountSurvey aSurvey = respondant.getAccountSurvey();
			json.put("survey", aSurvey.getJSON());
			json.put("respondant", respondant.getJSON());
			List<Response> responses = respondant.getResponses();
			for (int i = 0; i < responses.size(); i++)
				json.accumulate("responses", responses.get(i).getJSON());
		} else {
			// TODO put in better error handling here.
			json.put("message", "Unable to associate this link with an assessment.");
		}

		return json.toString();
	}

}
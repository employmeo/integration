package com.talytica.integration.partners;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;

import jersey.repackaged.com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class WorkablePartnerUtil extends BasePartnerUtil {


	public WorkablePartnerUtil() {
	}

	@Override
	public JSONArray formatSurveyList(Set<AccountSurvey> surveys) {
		JSONArray response = new JSONArray();
		for (AccountSurvey as : surveys) {
			JSONObject survey = new JSONObject();
			survey.put("name", as.getDisplayName());
			survey.put("id", as.getId());
			response.put(survey);
		}
		return response;
	}
	
	@Override
	public JSONObject getScoresMessage(Respondant resp) {
		Respondant respondant = respondantService.getRespondantById(resp.getId());// Doing this to refresh predictions, etc
		JSONObject message = new JSONObject();
		JSONObject assessment = new JSONObject();
		JSONArray attachments = new JSONArray();
		
		try {
			if (respondant.getRespondantStatus() >= Respondant.STATUS_PREDICTED) {
				message.put("status","complete");
				message.put("results_url", externalLinksService.getRenderLink(respondant));

				assessment.put("score",respondant.getCompositeScore().toString());
				assessment.put("grade",respondant.getAccount().getCustomProfile().getName(respondant.getProfileRecommendation()));
				StringBuffer pred = new StringBuffer();
				for (Prediction prediction : respondant.getPredictions()) {
					pred.append(String.format("%.1f", 100*prediction.getPredictionScore()));
					pred.append("% chance that ");
					pred.append(respondant.getPerson().getFirstName());
					pred.append(" ");
					pred.append(prediction.getPositionPredictionConfig().getPredictionTarget().getLabel());
					pred.append(". ");
				}
				assessment.put("summary",pred.toString());
				
				HashMap<String,JSONObject> detailsTable = Maps.newHashMap();
				for (RespondantScore rs : respondant.getRespondantScores()) {
					Corefactor cf = corefactorService.findCorefactorById(rs.getId().getCorefactorId());
					if ((null == cf.getDisplayGroup()) || ("Hidden".equalsIgnoreCase(cf.getDisplayGroup()))) continue;
					if (!detailsTable.containsKey(cf.getDisplayGroup())) detailsTable.put(cf.getDisplayGroup(), new JSONObject());
					detailsTable.get(cf.getDisplayGroup()).put(cf.getName(), rs.getValue());
				}
				JSONObject details = new JSONObject();
				for (Map.Entry<String, JSONObject> pair : detailsTable.entrySet()) {
					details.put(pair.getKey(), pair.getValue());
				}
				assessment.put("details", details);
				message.put("assessment",assessment);
				message.put("attachments",attachments);
			} else {
				message.put("status","pending");
			}
		} catch (JSONException jse) {
			log.error("JSON Exception: {}", jse.getMessage());
		}
		return message;

	}
	
	@Override
	public void postScoresToPartner(String postmethod, JSONObject message) {
		Client client = getPartnerClient();
		if (interceptOutbound) {
			log.info("Not Intercepting Post to {}", postmethod);
			//postmethod = externalLinksService.getIntegrationEcho();
			//client = integrationClientFactory.newInstance();
		}

		WebTarget target = client.target(postmethod);
		Response result = null;
		try {
			result = target.request(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + partner.getApiKey())
					.put(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));

			Boolean success = (null == result || result.getStatus() >= 300) ? false : true;
			String serviceResponse = result.readEntity(String.class);
			if (success) {
				log.debug("Put message {} to {}. Server response: {}", message, postmethod, serviceResponse);
			} else {
				log.warn("Failed to put message {} to {}. Server response: {}", message, postmethod, serviceResponse);
			}

		} catch (Exception e) {
			log.error("PUT to {} failed with: {}", postmethod, e.getMessage());
		}

	}
}

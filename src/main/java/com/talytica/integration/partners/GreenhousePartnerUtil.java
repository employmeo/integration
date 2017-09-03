package com.talytica.integration.partners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.CustomProfile;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.PredictionTarget;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class GreenhousePartnerUtil extends BasePartnerUtil {

	public GreenhousePartnerUtil() {
	}

	@Override
	public JSONArray formatSurveyList(Set<AccountSurvey> surveys) {
		JSONArray response = new JSONArray();
		for (AccountSurvey as : surveys) {
			JSONObject survey = new JSONObject();
			survey.put("partner_test_name", as.getDisplayName());
			survey.put("partner_test_id", as.getId());
			response.put(survey);
		}

		return response;
	}
	
	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {

		inviteCandidate(respondant);	
		JSONObject output = new JSONObject();
		try {
			// Assemble the response object to notify that action is complete
			output.put("partner_interview_id", respondant.getRespondantUuid());
		} catch (JSONException jse) {
			log.error("JSON Exception: {}", jse.getMessage());
		}
		
		return output;
	}
}

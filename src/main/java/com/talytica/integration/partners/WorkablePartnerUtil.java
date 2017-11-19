package com.talytica.integration.partners;

import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Respondant;

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
}

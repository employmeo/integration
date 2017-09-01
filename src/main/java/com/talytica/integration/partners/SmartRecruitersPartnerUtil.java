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
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
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
import com.employmeo.data.model.CustomProfile;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.PredictionTarget;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.Survey;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersAssessmentNotification;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersAssessmentOrder;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersOffer;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersResults;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersStatusUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class SmartRecruitersPartnerUtil extends BasePartnerUtil {

	@Value("${partners.smartrecruiters.api:https://api.smartrecruiters.com/v1/}")
	String API;

	@Value("${partners.smartrecruiters.apitoken:28b2c4a41719f5158c26fcbf2a6f4d317c284bf30e01234bdcde131bd30ae37b}")
	String API_KEY;
	
	
	public SmartRecruitersPartnerUtil() {
	}

	public List<SmartRecruitersAssessmentOrder> pollNewAssessmentOrders() {
		return null;
	}
	
	public SmartRecruitersAssessmentOrder fetchIndividualOrder(SmartRecruitersAssessmentNotification notification) {
		Client client = getPartnerClient();
		String service = API + "assessments/" + notification.getAssmentOrderId();

		Response response = client.target(service).request().header("X-SmartToken", API_KEY).get();
		Boolean success = (null == response || response.getStatus() >= 300) ? false : true;
		if (success) return response.readEntity(SmartRecruitersAssessmentOrder.class);
		log.error("failed to retrieve order {}: {}",response.getStatusInfo().getReasonPhrase(), response.readEntity(String.class));
		return null;
	}

	@Override
	public void changeCandidateStatus(Respondant respondant, String message) {
		SmartRecruitersStatusUpdate status = new SmartRecruitersStatusUpdate();
				
	}

	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {
			
		SmartRecruitersResults scores = new SmartRecruitersResults();
				
	}

	public String submitOffer(Survey survey) {
		Client client = getPartnerClient();
		String service = API + "offers";
		SmartRecruitersOffer offer = new SmartRecruitersOffer();
		offer.setCatalogId(survey.getId().toString());
		offer.setName(survey.getName());
		offer.setDescription(survey.getDescription());
		Response response = client.target(service).request().header("X-SmartToken", API_KEY).post(Entity.entity(offer, MediaType.APPLICATION_JSON));
		
		return null;
	}
	
}

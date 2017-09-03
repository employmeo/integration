package com.talytica.integration.partners;

import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Respondant;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersAssessmentNotification;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersAssessmentOrder;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersResults;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersStatusUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class SmartRecruitersPartnerUtil extends BasePartnerUtil {

	@Value("${partners.smartrecruiters.api:https://api.smartrecruiters.com/v1/}")
	public String API;

	@Value("${partners.smartrecruiters.apitoken:28b2c4a41719f5158c26fcbf2a6f4d317c284bf30e01234bdcde131bd30ae37b}")
	public String API_KEY;
	
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
	public AccountSurvey getSurveyFrom(JSONObject assessment, Account account) {
		String offerCatalogId = assessment.getString("offer_catalog_id");
		AccountSurvey aSurvey = accountSurveyService.getAccountSurveyBySurveyIdForAccount(Long.valueOf(offerCatalogId),account.getId());
		if (aSurvey == null) aSurvey = accountSurveyService.getAccountSurveyById(account.getDefaultAsId());
		return aSurvey;
	}
	
	@Override
	public void inviteCandidate(Respondant respondant) {
		emailService.sendEmailInvitation(respondant);
		
		SmartRecruitersStatusUpdate status = new SmartRecruitersStatusUpdate();	
		String link = externalLinksService.getAssessmentLink(respondant);
		status.setAssessmentURL(link);
		status.setMessage("Candidate invited by Talytica at: " + new Date());
		status.setMessageToCandidate("Please click the following link to access this questionnaire: " + link);
		String atsId = this.trimPrefix(respondant.getAtsId());
		String service = API + "assessments/"+atsId+"/accept";
		Client client = getPartnerClient();
		if (interceptOutbound) {
			log.info("Intercepting Post to {}", service);
			service = externalLinksService.getIntegrationEcho();
			client = integrationClientFactory.newInstance();
		}		
		Response response = client.target(service).request().header("X-SmartToken", API_KEY)
				.post(Entity.entity(status, MediaType.APPLICATION_JSON+";charset=utf-8"));
		if ((response.getStatus() >=200) && (response.getStatus() < 300)) {
			// Success
			log.debug("Posted Accept to smartrecruiters: {}", trimPrefix(respondant.getAtsId()));
		} else {
			// fail
			log.error("Failed to signal accept: {} {}", response.getStatusInfo().getReasonPhrase(), response.readEntity(String.class));		
		}
	}

	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {		
		SmartRecruitersResults scores = new SmartRecruitersResults();
		String link = externalLinksService.getPortalLink(respondant);
		String desc = respondant.getAccount().getCustomProfile().getName(respondant.getProfileRecommendation());
		scores.setTitle(respondant.getAccountSurvey().getDisplayName() + " results");
		scores.setScore(respondant.getCompositeScore().toString());
		scores.setDescription(desc);
		scores.setPassed(respondant.getCompositeScore() > 50);
		scores.setResult(link);
		scores.setResultType("URL");
		
		String service = respondant.getScorePostMethod();
		Client client = getPartnerClient();
		
		if (interceptOutbound) {
			log.info("Intercepting Post to {}", service);
			service = externalLinksService.getIntegrationEcho();
			client = integrationClientFactory.newInstance();
		}
		
		Response response = client.target(service).request().header("X-SmartToken", API_KEY)
				.post(Entity.entity(scores, MediaType.APPLICATION_JSON+";charset=utf-8"));
		if ((response.getStatus() >=200) && (response.getStatus() < 300)) {
			// Success
			log.debug("Posted Accept to smartrecruiters: {}", trimPrefix(respondant.getAtsId()));
		} else {
			// fail
			log.error("Failed to signal accept: {} {}", response.getStatusInfo().getReasonPhrase(), response.readEntity(String.class));		
		}
				
	}
}

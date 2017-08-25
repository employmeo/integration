package com.talytica.integration.partners;

import java.util.Set;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.service.*;
import com.talytica.common.service.EmailService;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.common.service.AddressService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public abstract class BasePartnerUtil implements PartnerUtil {

	@Setter
	@Getter
	protected Partner partner = null;

	@Autowired
	ExternalLinksService externalLinksService;

	@Autowired
	EmailService emailService;

	@Autowired
	AddressService addressService;

	@Autowired
	PartnerService partnerService;

	@Autowired
	AccountService accountService;

	@Autowired
	AccountSurveyService accountSurveyService;

	@Autowired
	RespondantService respondantService;

	@Autowired
	PersonService personService;

	@Autowired
	CorefactorService corefactorService;

	@Autowired
	PredictionModelService predictionModelService;

	@Value("${partners.default.intercept.outbound:true}")
	Boolean interceptOutbound;

	public BasePartnerUtil() {
	}

	@Override
	public String getPrefix() {
		return partner.getPrefix();
	}

	@Override
	public String addPrefix(String id) {
		if (partner.getPrefix() == null) {
			return id;
		}
		return partner.getPrefix() + id;
	}

	@Override
	public String trimPrefix(String id) {
		if (id == null) {
			return null;
		}
		return id.substring(id.indexOf(getPrefix()) + getPrefix().length());
	}

	@Override
	public Account getAccountFrom(JSONObject jAccount) {
		Account account = null;
		String accountAtsId = jAccount.optString("account_ats_id");
		if (accountAtsId != null) {
			account = accountService.getAccountByAtsId(addPrefix(accountAtsId));
		} else {
			account = accountService.getAccountById(jAccount.optLong("account_id"));
		}
		return account;
	}

	@Override
	public Location getLocationFrom(JSONObject jLocation, Account account) {
		Location location = null;

		if (jLocation != null) {
			if (jLocation.has("location_id")) {
				return accountService.getLocationById(jLocation.optLong("location_id"));
			}

			if ((jLocation != null) && (jLocation.has("location_ats_id"))) {
				location = accountService.getLocationByAtsId(account.getId(),
						addPrefix(jLocation.optString("location_ats_id")));
				if (location != null) {
					return location;
				}

				// otherwise create a new location from address
				location = new Location();
				JSONObject address = jLocation.optJSONObject("address");
				addressService.validate(address);
				location.setAtsId(partner.getPrefix() + jLocation.optString("location_ats_id"));
				if (jLocation.has("location_name")) {
					location.setLocationName(jLocation.optString("location_name"));
				}
				if (address.has("street")) {
					location.setStreet1(address.optString("street"));
				}
				if (address.has("formatted_address")) {
					location.setStreet2(address.optString("formatted_address"));
				}
				if (address.has("city")) {
					location.setCity(address.optString("city"));
				}
				if (address.has("state")) {
					location.setState(address.optString("state"));
				}
				if (address.has("zip")) {
					location.setZip(address.optString("zip"));
				}
				if (address.has("lat")) {
					location.setLatitude(address.optDouble("lat"));
				}
				if (address.has("lng")) {
					location.setLongitude(address.optDouble("lng"));
				}
				location.setAccount(account);

				return accountService.save(location);
			}
		}
		return accountService.getLocationById(account.getDefaultLocationId());
	}

	@Override
	public Position getPositionFrom(JSONObject position, Account account) {

		Position pos = null;
		if ((position != null) && (position.has("position_id"))) {
			pos = accountService.getPositionById(position.optLong("position_id"));
		}
		if (pos == null) {
			pos = accountService.getPositionById(account.getDefaultPositionId());
		}
		return pos;
	}

	@Override
	public AccountSurvey getSurveyFrom(JSONObject assessment, Account account) {

		AccountSurvey aSurvey = null;
		Long asId = assessment.optLong("assessment_asid");
		if (asId != null) {
			aSurvey = accountSurveyService.getAccountSurveyById(asId);
		}

		return aSurvey;
	}

	@Override
	public Respondant getRespondantFrom(JSONObject applicant, Account account) {
		Respondant respondant = null;
		String applicantAtsId = applicant.optString("applicant_ats_id");
		if (applicantAtsId != null) {
			respondant = respondantService.getRespondantByAtsId(addPrefix(applicantAtsId));
		} else {
			// Try to grab account by employmeo respondant_id
			respondant = respondantService.getRespondantById(applicant.optLong("applicant_id"));
		}
		return respondant;
	}

	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		Person person = new Person();
		Respondant respondant = new Respondant();
		respondant.setAccountId(account.getId());

		JSONObject applicant = json.optJSONObject("applicant");
		String appAtsId = applicant.optString("applicant_ats_id");
		respondant.setAtsId(this.getPrefix() + appAtsId);
		person.setAtsId(this.getPrefix() + appAtsId);
		person.setEmail(applicant.optString("email"));
		person.setFirstName(applicant.optString("fname"));
		person.setLastName(applicant.optString("lname"));
		JSONObject personAddress = applicant.optJSONObject("address");
		addressService.validate(personAddress);
		person.setAddress(personAddress.optString("formatted_address"));
		person.setLatitude(personAddress.optDouble("lat"));
		person.setLongitude(personAddress.optDouble("lng"));

		Location location = this.getLocationFrom(json.optJSONObject("location"), account);
		Position position = this.getPositionFrom(json.optJSONObject("position"), account);
		AccountSurvey aSurvey = this.getSurveyFrom(json.optJSONObject("assessment"), account);

		JSONObject delivery = json.optJSONObject("delivery");
		// get the redirect method, score posting and email handling for results
		if (delivery.has("scores_email_address")) {
			respondant.setEmailRecipient(delivery.optString("scores_email_address"));
		}
		if (delivery.has("scores_redirect_url")) {
			respondant.setRedirectUrl(delivery.optString("scores_redirect_url"));
		}
		if (delivery.has("scores_post_url")) {
			respondant.setScorePostMethod(delivery.optString("scores_post_url"));
		}

		respondant.setAccountId(account.getId());
		respondant.setAccount(account);
		respondant.setAccountSurveyId(aSurvey.getId());
		respondant.setLocationId(location.getId());
		respondant.setPositionId(position.getId());
		respondant.setAccountSurvey(aSurvey);
		respondant.setLocation(location);
		respondant.setPosition(position);
		respondant.setPartner(this.partner);
		respondant.setPartnerId(this.partner.getId());

		// Create Person & Respondant in database.
		Person savedPerson = personService.save(person);
		respondant.setPerson(savedPerson);
		respondant.setPersonId(savedPerson.getId());

		return respondantService.save(respondant);
	}

	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {

		JSONObject delivery = json.optJSONObject("delivery");
		if (delivery.has("email_applicant") && delivery.optBoolean("email_applicant")) {
			emailService.sendEmailInvitation(respondant);
		}
		JSONObject output = new JSONObject();

		try {
			// Assemble the response object to notify that action is complete
			JSONObject jAccount = json.optJSONObject("account");
			jAccount.put("account_ats_id", this.trimPrefix(respondant.getAccount().getAtsId()));
			jAccount.put("account_id", respondant.getAccount().getId());
			jAccount.put("account_name", respondant.getAccount().getAccountName());

			JSONObject jApplicant = new JSONObject();
			jApplicant.put("applicant_ats_id", this.trimPrefix(respondant.getAtsId()));
			jApplicant.put("applicant_id", respondant.getId());

			delivery = new JSONObject();
			delivery.put("assessment_url", externalLinksService.getAssessmentLink(respondant));

			output.put("account", jAccount);
			output.put("applicant", jApplicant);
			output.put("delivery", delivery);
		} catch (JSONException jse) {
			log.error("JSON Exception: {}", jse.getMessage());
		}
		// get the redirect method, score posting and email handling for results
		return output;
	}

	@Override
	public JSONObject getScoresMessage(Respondant respondant) {

		JSONObject message = new JSONObject();

		Account account = respondant.getAccount();
		JSONObject jAccount = new JSONObject();
		JSONObject applicant = new JSONObject();
		try {
			jAccount.put("account_ats_id", trimPrefix(account.getAtsId()));
			jAccount.put("account_id", account.getId());
			jAccount.put("account_name", account.getAccountName());
			applicant.put("applicant_ats_id", trimPrefix(respondant.getAtsId()));
			applicant.put("applicant_id", respondant.getId());

			if (respondant.getRespondantStatus() >= Respondant.STATUS_SCORED) {
				CustomProfile customProfile = account.getCustomProfile();
				Set<RespondantScore> scores = respondant.getRespondantScores();
				applicant.put("applicant_profile", respondant.getProfileRecommendation());
				applicant.put("applicant_composite_score", respondant.getCompositeScore());
				applicant.put("applicant_profile_label", customProfile.getName(respondant.getProfileRecommendation()));
				//applicant.put("applicant_profile_a", respondant.getProfileA());
				//applicant.put("applicant_profile_b", respondant.getProfileB());
				//applicant.put("applicant_profile_c", respondant.getProfileC());
				//applicant.put("applicant_profile_d", respondant.getProfileD());
				applicant.put("label_profile_a", customProfile.getName(ProfileDefaults.PROFILE_A));
				applicant.put("label_profile_b", customProfile.getName(ProfileDefaults.PROFILE_B));
				applicant.put("label_profile_c", customProfile.getName(ProfileDefaults.PROFILE_C));
				applicant.put("label_profile_d", customProfile.getName(ProfileDefaults.PROFILE_D));
				JSONArray scoreset = new JSONArray();
				for (RespondantScore score : scores) {
					Corefactor cf = corefactorService.findCorefactorById(score.getId().getCorefactorId());
					JSONObject item = new JSONObject();
					item.put("corefactor_name", cf.getName());
					item.put("corefactor_score", score.getValue());
					scoreset.put(item);
				}

				applicant.put("scores", scoreset);
				applicant.put("portal_link", externalLinksService.getPortalLink(respondant));
				applicant.put("render_link", externalLinksService.getRenderLink(respondant));
			}

			message.put("account", jAccount);
			message.put("applicant", applicant);
		} catch (JSONException jse) {
			log.error("JSON Exception: {}", jse.getMessage());
		}
		return message;

	}

	@Override
	public JSONObject getScreeningMessage(Respondant respondant) {
		JSONObject message = new JSONObject();
		try {
			Account account = respondant.getAccount();
			JSONObject jAccount = new JSONObject();
			JSONObject applicant = new JSONObject();
			jAccount.put("account_ats_id", trimPrefix(account.getAtsId()));
			jAccount.put("account_id", account.getId());
			jAccount.put("account_name", account.getAccountName());
			applicant.put("applicant_ats_id", trimPrefix(respondant.getAtsId()));
			applicant.put("applicant_id", respondant.getId());
			Set<Prediction> predictions = respondant.getPredictions();

			CustomProfile customProfile = account.getCustomProfile();
			applicant.put("applicant_profile", respondant.getProfileRecommendation());
			applicant.put("applicant_composite_score", respondant.getCompositeScore());
			applicant.put("applicant_profile_label", customProfile.getName(respondant.getProfileRecommendation()));
			JSONArray predset = new JSONArray();

			for (Prediction prediction : predictions) {
				PredictionTarget target = predictionModelService.getTargetById(prediction.getTargetId());
				JSONObject item = new JSONObject();
				item.put("target_name", target.getLabel());
				item.put("target_probabilty", prediction.getPredictionScore());
				item.put("precentile_rank", prediction.getScorePercentile());
				predset.put(item);
			}

			applicant.put("predictions", predset);
			applicant.put("portal_link", externalLinksService.getPortalLink(respondant));

			message.put("account", jAccount);
			message.put("applicant", applicant);
		} catch (JSONException jse) {
			log.error("JSON Exception: {}", jse.getMessage());
		}
		return message;

	}

	@Override
	public void changeCandidateStatus(Respondant respondant, String status) {
		log.warn("No method for changing ATS status for respondant {} ", respondant.getId());
	}
	
	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {

		String postmethod = respondant.getScorePostMethod();
		if (postmethod == null || postmethod.isEmpty()) {
			return;
		} 
		postScoresToPartner(postmethod, message);
	}

	@Override
	public void postScoresToPartner(String postmethod, JSONObject message) {

		if (interceptOutbound) {
			log.info("Intercepting Post to {}", postmethod);
			postmethod = externalLinksService.getIntegrationEcho();
		}

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(postmethod);
		Response result = null;
		try {
			result = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));

			Boolean success = (null == result || result.getStatus() >= 300) ? false : true;
			String serviceResponse = result.readEntity(String.class);
			if (success) {
				log.info("Posted message {} to {}. Server response: {}", message, postmethod, serviceResponse);
			} else {
				log.warn("Failed to post message {} to {}. Server response: {}", message, postmethod, serviceResponse);
			}

		} catch (Exception e) {
			log.warn("failed posting scores to {}: ", postmethod);
		}

	}
	
	public void inviteCandidate(Respondant respondant) {
		emailService.sendEmailInvitation(respondant);
	}

}

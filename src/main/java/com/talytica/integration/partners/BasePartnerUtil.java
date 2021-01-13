package com.talytica.integration.partners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import com.employmeo.data.repository.CorefactorRepository;
import com.employmeo.data.service.*;
import com.talytica.common.service.EmailService;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.IntegrationClientFactory;
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
	
	@Autowired
	GraderService graderService;

	@Autowired
	IntegrationClientFactory integrationClientFactory;
	
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
		log.debug("get location for {}",jLocation);
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
				if (null != address) addressService.validate(address);
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
				location.setAccountId(account.getId());
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

		Long asId = null;
		if (null != assessment)	asId = assessment.optLong("assessment_asid");
		if (null == asId) asId = account.getDefaultAsId();

		return accountSurveyService.getAccountSurveyById(asId);
	}

	@Override
	public Respondant getRespondantFrom(JSONObject applicant, Account account) {
		Respondant respondant = null;
		String appAtsId = applicant.optString("applicant_ats_id");
		if (appAtsId != null) {
			respondant = respondantService.getRespondantByAtsId(this.addPrefix(appAtsId));
			log.debug("Match = {} for ats id: {}", (null == respondant), this.addPrefix(appAtsId));
		} else if (applicant.optLong("applicant_id") != 0) {
			// Try to grab account by employmeo respondant_id
			respondant = respondantService.getRespondantById(applicant.optLong("applicant_id"));
		}
		return respondant;
	}

	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		Person person = null;
		JSONObject applicant = json.optJSONObject("applicant");
		Respondant respondant = getRespondantFrom(applicant, account);
		if (respondant != null) return respondant;
		
		respondant = new Respondant();
		respondant.setAccountId(account.getId());
		respondant.setRespondantStatus(Respondant.STATUS_CREATED);
		String appAtsId = applicant.optString("applicant_ats_id");
		if (appAtsId != null) respondant.setAtsId(this.addPrefix(appAtsId));		
		if (applicant.has("person_ats_id")) person = personService.getPersonByAtsId(applicant.optString("person_ats_id"));
		
		if (null == person) {
			person = new Person();
			person.setAtsId(this.getPrefix() + appAtsId);
			if (applicant.has("person_ats_id")) person.setAtsId(this.addPrefix(applicant.optString("person_ats_id")));
			person.setEmail(applicant.optString("email"));
			person.setFirstName(applicant.optString("fname"));
			person.setLastName(applicant.optString("lname"));
			person.setPhone(applicant.optString("phone"));
			JSONObject personAddress = applicant.optJSONObject("address");
			if (personAddress != null) {
			addressService.validate(personAddress);
				person.setAddress(personAddress.optString("formatted_address"));
				person.setLatitude(personAddress.optDouble("lat"));
				person.setLongitude(personAddress.optDouble("lng"));
			}
		}
		Location location = this.getLocationFrom(json.optJSONObject("location"), account);
		Position position = this.getPositionFrom(json.optJSONObject("position"), account);
		AccountSurvey aSurvey = this.getSurveyFrom(json.optJSONObject("assessment"), account);

		JSONObject delivery = json.optJSONObject("delivery");
		if (delivery != null) {
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
		if ((delivery != null) && 
				delivery.has("email_applicant") && 
				delivery.optBoolean("email_applicant") &&
				respondant.getRespondantStatus()<Respondant.STATUS_INVITED) {
			emailService.sendEmailInvitation(respondant);
			respondant.setRespondantStatus(Respondant.STATUS_INVITED);
			respondantService.save(respondant);
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
	public String getScoreNotesFormat(Respondant respondant) {
	
		StringBuffer notes = new StringBuffer();
		
		List<String> warnings = respondantService.getWarningMessages(respondant);
		for (String warning : warnings) {
			notes.append("WARNING: ");
			notes.append(warning);
			notes.append("\n");
		}
		
		CustomProfile customProfile = respondant.getAccount().getCustomProfile();
		notes.append("Category: ");
		notes.append(customProfile.getName(respondant.getProfileRecommendation()));
		notes.append(" (");
		notes.append(respondant.getCompositeScore());
		notes.append(")\n");
		
		if (respondant.getPredictions().size() > 0) {
			notes.append("Summary Scores:\n");		
			for (Prediction prediction : respondant.getPredictions()) {
				PredictionTarget target = predictionModelService.getTargetById(prediction.getTargetId());
				notes.append(target.getLabel());
				notes.append(": ");
				notes.append(String.format("%.2f", prediction.getPredictionScore()));
				notes.append("\n");
			}
		}
		
		List<RespondantScore> scores = new ArrayList<RespondantScore>( respondant.getRespondantScores());		
		scores.sort(new Comparator<RespondantScore>() {
			public int compare (RespondantScore a, RespondantScore b) {
				Corefactor corefactorA = corefactorService.findCorefactorById(a.getId().getCorefactorId());
				Corefactor corefactorB = corefactorService.findCorefactorById(b.getId().getCorefactorId());
				double aCoeff = 1d;
				double bCoeff = 1d;
				if (corefactorA.getDefaultCoefficient() != null) aCoeff = Math.abs(corefactorA.getDefaultCoefficient());
				if (corefactorB.getDefaultCoefficient() != null) bCoeff = Math.abs(corefactorB.getDefaultCoefficient());
				// first sort by coefficient - descending
				if (aCoeff != bCoeff) return (int)(bCoeff - aCoeff);
				// otherwise just sort by name
				return corefactorA.getName().compareTo(corefactorB.getName());
			}
		});
		notes.append("Summary Scores:\n");		
		for (RespondantScore score : scores) {
			Corefactor cf = corefactorService.findCorefactorById(score.getId().getCorefactorId());
			notes.append(cf.getName());
			notes.append(" : ");
			
//			this is awful but, for now, always format as two digit leading zero
//			if ((int) score.getValue().doubleValue() == score.getValue()) {
				notes.append(String.format("%02d",(int) score.getValue().doubleValue()));
//			} else {
//				notes.append(String.format("%.1f",score.getValue()));
//			}
			notes.append("\n");
		}

		Set<RespondantNVP> customQuestions = respondantService.getDisplayNVPsForRespondant(respondant.getId());
		if (!customQuestions.isEmpty()) {
			notes.append("Candidate Questions:\n");
			for (RespondantNVP nvp : customQuestions) {
				notes.append(nvp.getName());
				notes.append(" : ");
				notes.append(nvp.getValue());
				notes.append("\n");
			}
		}
		
		List<Grader> graders = graderService.getGradersByRespondantId(respondant.getId());
		if (!graders.isEmpty()) {
			StringBuffer references = new StringBuffer();
			StringBuffer evaluators = new StringBuffer();
			for (Grader grader : graders) {
				switch (grader.getType()) {
				case Grader.TYPE_PERSON:
					references.append(grader.getPerson().getFirstName());
					references.append(" ");
					references.append(grader.getPerson().getLastName());
					if ((null != grader.getRelationship()) && (!grader.getRelationship().isEmpty()))
					  references.append(" ("+grader.getRelationship()+")");
					references.append(" : ");
					references.append(grader.getSummaryScore());
					references.append("\n");
					break;
				case Grader.TYPE_SUMMARY_USER:
				case Grader.TYPE_USER:
				default:
					evaluators.append(grader.getUser().getFirstName());
					evaluators.append(" ");
					evaluators.append(grader.getUser().getLastName());
					evaluators.append(" : ");
					evaluators.append(grader.getSummaryScore());
					evaluators.append("\n");
					break;
				}
			}
			if (references.length() > 0) {
				notes.append("References:\n");
				notes.append(references);
			}
			if (evaluators.length() > 0) {
				notes.append("Evaluated By:\n");
				notes.append(evaluators);
			}
		}
		
		return notes.toString();
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
		Client client = getPartnerClient();
		if (interceptOutbound) {
			log.info("Intercepting Post to {}", postmethod);
			postmethod = externalLinksService.getIntegrationEcho();
			client = integrationClientFactory.newInstance();
		}

		WebTarget target = client.target(postmethod);
		Response result = null;
		try {
			result = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));

			Boolean success = (null == result || result.getStatus() >= 300) ? false : true;
			String serviceResponse = result.readEntity(String.class);
			if (success) {
				log.debug("Posted message {} to {}. Server response: {}", message, postmethod, serviceResponse);
			} else {
				log.warn("Failed to post message {} to {}. Server response: {}", message, postmethod, serviceResponse);
			}

		} catch (Exception e) {
			log.warn("failed posting scores to {}: ", postmethod);
		}

	}
	
	@Override
	public Client getPartnerClient() {
		return ClientBuilder.newClient();
	}
	
	@Override
	public void inviteCandidate(Respondant respondant) {
		emailService.sendEmailInvitation(respondant);
	}
	
	@Override
	public JSONArray formatSurveyList(Set<AccountSurvey> surveys) {
		JSONArray response = new JSONArray();
		for (AccountSurvey as : surveys) {
			if (as.getType() != AccountSurvey.TYPE_APPLICANT) continue;
			JSONObject survey = new JSONObject();
			try {
				survey.put("assessment_name", as.getDisplayName());
				survey.put("assessment_asid", as.getId());
				response.put(survey);
			} catch (JSONException e) {
				log.error("Unexpected JSON exception {}", e);
			}
		}

		return response;
	}
}

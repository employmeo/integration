package com.talytica.integration.util;

import java.util.Set;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.*;
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
public class DefaultPartnerUtil implements PartnerUtil {

	@Setter @Getter
	private Partner partner = null;

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
	LocationRepository locationRepository;

	@Autowired
	PositionRepository positionRepository;

	@Autowired
	CorefactorRepository corefactorRepository;

	public DefaultPartnerUtil() {
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
		return id.substring(id.indexOf(getPrefix())+getPrefix().length());
	}

	@Override
	public Account getAccountFrom(JSONObject jAccount) {
		log.debug("get account called with {} by {}", jAccount, partner);
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
				return locationRepository.findOne(jLocation.getLong("location_id"));
			}

			if ((jLocation != null) && (jLocation.has("location_ats_id"))) {
				location = locationRepository.findByAccountIdAndAtsId(account.getId(), partner.getPrefix() + jLocation.getString("location_ats_id"));
				if (location != null) {
					return location;
				}

				// otherwise create a new location from address
				location = new Location();
				JSONObject address = jLocation.getJSONObject("address");
				addressService.validate(address);
				location.setAtsId(partner.getPrefix() + jLocation.getString("location_ats_id"));
				if (jLocation.has("location_name")) {
					location.setLocationName(jLocation.getString("location_name"));
				}
				if (address.has("street")) {
					location.setStreet1(address.getString("street"));
				}
				if (address.has("formatted_address")) {
					location.setStreet2(address.getString("formatted_address"));
				}
				if (address.has("city")) {
					location.setCity(address.getString("city"));
				}
				if (address.has("state")) {
					location.setState(address.getString("state"));
				}
				if (address.has("zip")) {
					location.setZip(address.getString("zip"));
				}
				if (address.has("lat")) {
					location.setLatitude(address.getDouble("lat"));
				}
				if (address.has("lng")) {
					location.setLongitude(address.getDouble("lng"));
				}
				location.setAccount(account);

				return locationRepository.save(location);
			}
		}
		return locationRepository.findOne(account.getDefaultLocationId());
	}

	@Override
	public Position getPositionFrom(JSONObject position, Account account) {

		Position pos = null;
		if ((position != null) && (position.has("position_id"))) {
			pos = positionRepository.findOne(position.getLong("position_id"));
		}
		if (pos == null) {
			pos = positionRepository.findOne(account.getDefaultPositionId());
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
	public Respondant getRespondantFrom(JSONObject applicant) {
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


		JSONObject applicant = json.getJSONObject("applicant");
		String appAtsId = applicant.getString("applicant_ats_id");
		respondant.setAtsId(this.getPrefix() + appAtsId);
		person.setAtsId(this.getPrefix() + appAtsId);
		person.setEmail(applicant.getString("email"));
		person.setFirstName(applicant.getString("fname"));
		person.setLastName(applicant.getString("lname"));
		JSONObject personAddress = applicant.getJSONObject("address");
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
		if (delivery.has("email_applicant") && delivery.getBoolean("email_applicant")) {
			emailService.sendEmailInvitation(respondant);
		}

		// Assemble the response object to notify that action is complete
		JSONObject jAccount = json.getJSONObject("account");
		jAccount.put("account_ats_id", this.trimPrefix(respondant.getAccount().getAtsId()));
		jAccount.put("account_id", respondant.getAccount().getId());
		jAccount.put("account_name", respondant.getAccount().getAccountName());

		JSONObject jApplicant = new JSONObject();
		jApplicant.put("applicant_ats_id", this.trimPrefix(respondant.getAtsId()));
		jApplicant.put("applicant_id", respondant.getId());

		delivery = new JSONObject();
		delivery.put("assessment_url", externalLinksService.getAssessmentLink(respondant));

		JSONObject output = new JSONObject();
		output.put("account", jAccount);
		output.put("applicant", jApplicant);
		output.put("delivery", delivery);

		// get the redirect method, score posting and email handling for results
		return output;
	}

	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		
		
		Account account = respondant.getAccount();
		JSONObject jAccount = new JSONObject();
		JSONObject applicant = new JSONObject();
		jAccount.put("account_ats_id", trimPrefix(account.getAtsId()));
		jAccount.put("account_id", account.getId());
		jAccount.put("account_name", account.getAccountName());
		applicant.put("applicant_ats_id", trimPrefix(respondant.getAtsId()));
		applicant.put("applicant_id", respondant.getId());
		
		if (respondant.getRespondantStatus() >= Respondant.STATUS_SCORED) {
							
			Set<RespondantScore> scores = respondant.getRespondantScores();		
			applicant.put("applicant_profile", respondant.getProfileRecommendation());
			applicant.put("applicant_composite_score", respondant.getCompositeScore());
			applicant.put("applicant_profile_label",
					PositionProfile.getProfileDefaults(respondant.getProfileRecommendation()).getString("profile_name"));
			applicant.put("applicant_profile_a", respondant.getProfileA());
			applicant.put("applicant_profile_b", respondant.getProfileB());
			applicant.put("applicant_profile_c", respondant.getProfileC());
			applicant.put("applicant_profile_d", respondant.getProfileD());
			applicant.put("label_profile_a",
					PositionProfile.getProfileDefaults(PositionProfile.PROFILE_A).getString("profile_name"));
			applicant.put("label_profile_b",
					PositionProfile.getProfileDefaults(PositionProfile.PROFILE_B).getString("profile_name"));
			applicant.put("label_profile_c",
					PositionProfile.getProfileDefaults(PositionProfile.PROFILE_C).getString("profile_name"));
			applicant.put("label_profile_d",
					PositionProfile.getProfileDefaults(PositionProfile.PROFILE_D).getString("profile_name"));
			JSONArray scoreset = new JSONArray();
			for (RespondantScore score : scores) {
				Corefactor cf = corefactorRepository.findOne(score.getId().getCorefactorId());
				JSONObject item = new JSONObject();
				item.put("corefactor_name", cf.getName());
				item.put("corefactor_score", score.getValue());
				scoreset.put(item);
			}
	
			applicant.put("scores", scoreset);
			applicant.put("portal_link", externalLinksService.getPortalLink(respondant));
			applicant.put("render_link", externalLinksService.getRenderLink(respondant));
		}
		
		JSONObject message = new JSONObject();
		message.put("account", jAccount);
		message.put("applicant", applicant);
	
		return message;

	}

	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {

		String postmethod = respondant.getScorePostMethod();
		if (postmethod == null || postmethod.isEmpty()) {
			return;
		}

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(postmethod);
		try {
			String result = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON), String.class);
			log.debug("posted scores to echo with result:\n" + result);
		} catch (Exception e) {
			log.warn("failed posting scores to: " + postmethod);
		}

	}

}

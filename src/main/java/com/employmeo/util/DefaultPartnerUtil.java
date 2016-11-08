package com.employmeo.util;

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Account;
import com.employmeo.objects.AccountSurvey;
import com.employmeo.objects.Location;
import com.employmeo.objects.Partner;
import com.employmeo.objects.Person;
import com.employmeo.objects.Position;
import com.employmeo.objects.PositionProfile;
import com.employmeo.objects.Respondant;

public class DefaultPartnerUtil implements PartnerUtil {
	private static final Logger log = LoggerFactory.getLogger(DefaultPartnerUtil.class);
	private final Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private Partner partner = null;
	
	public DefaultPartnerUtil(Partner partner) {
		this.partner = partner;
	}
	@Override	
	public String getPrefix() {
		return partner.getPartnerPrefix();
	}

	@Override
	public String trimPrefix(String id) {
		if (id == null) return null;
		return id.substring(id.indexOf(getPrefix())+getPrefix().length());
	}
	
	@Override
	public Account getAccountFrom(JSONObject jAccount) {
		Account account = null;
		String accountAtsId = jAccount.optString("account_ats_id");
		if (accountAtsId != null) {
			// lookup account by ATS ID
			EntityManager em = DBUtil.getEntityManager();
			TypedQuery<Account> q = em.createQuery("SELECT a FROM Account a WHERE a.accountAtsId = :accountAtsId",
					Account.class);
			q.setParameter("accountAtsId", partner.getPartnerPrefix() + accountAtsId);
			try {
				account = q.getSingleResult();
			} catch (NoResultException nre) {
				log.warn("Can't find account with atsId: " + accountAtsId);
				throw new WebApplicationException(
						Response.status(Status.PRECONDITION_FAILED).entity(jAccount.toString()).build());
			}
		} else {
			// Try to grab account by account_id
			account = Account.getAccountById(jAccount.optLong("account_id"));
		}
		return account;
	}

	@Override
	public Location getLocationFrom(JSONObject jLocation, Account account) {
		Location location = null;
		String locationAtsId = null;
		if ((jLocation != null) && (jLocation.has("location_ats_id"))) 
			locationAtsId = jLocation.getString("location_ats_id");
		if (locationAtsId != null) {
			EntityManager em = DBUtil.getEntityManager();
			TypedQuery<Location> q = em.createQuery(
					"SELECT l FROM Location l WHERE l.locationAtsId = :locationAtsId AND l.locationAccountId = :accountId",
					Location.class);
			q.setParameter("locationAtsId", partner.getPartnerPrefix() + locationAtsId);
			q.setParameter("accountId", account.getAccountId());
			try {
				location = q.getSingleResult();
			} catch (NoResultException nre) {
				location = new Location();
				// Create a new location from address
				JSONObject address = jLocation.getJSONObject("address");
				AddressUtil.validate(address);
				location.setLocationAtsId(partner.getPartnerPrefix() + locationAtsId);
				if (jLocation.has("location_name"))
					location.setLocationName(jLocation.getString("location_name"));
				if (address.has("street"))
					location.setLocationStreet1(address.getString("street"));
				if (address.has("formatted_address"))
					location.setLocationStreet2(address.getString("formatted_address"));
				if (address.has("city"))
					location.setLocationCity(address.getString("city"));
				if (address.has("state"))
					location.setLocationState(address.getString("state"));
				if (address.has("zip"))
					location.setLocationZip(address.getString("zip"));
				if (address.has("lat"))
					location.setLocationLat(address.getDouble("lat"));
				if (address.has("lng"))
					location.setLocationLong(address.getDouble("lng"));
				location.setAccount(account);
				location.persistMe();
			}

		} else {
			if ((jLocation != null) && (jLocation.has("location_id"))) {
				location = Location.getLocationById(jLocation.getLong("location_id"));
			} else {
			location = account.getDefaultLocation();
			}
		}
		return location;
	}
	
	@Override
	public Position getPositionFrom(JSONObject position, Account account) {

		Position pos = null;
		Long positionId = null;
		if (position != null) positionId = position.optLong("position_id");
		if (positionId != null) pos = Position.getPositionById(positionId);
		if (pos == null) pos = account.getDefaultPosition();
		return pos;
	}

	@Override
	public AccountSurvey getSurveyFrom(JSONObject assessment, Account account) {

		AccountSurvey aSurvey = null;
		Long asId = assessment.optLong("assessment_asid");
		if (asId != null) aSurvey = AccountSurvey.getAccountSurveyByASID(asId);
		
		return aSurvey;
	}
	
	@Override
	public Respondant getRespondantFrom(JSONObject applicant) {
		Respondant respondant = null;
		String applicantAtsId = applicant.optString("applicant_ats_id");
		if (applicantAtsId != null) {
			// lookup account by ATS ID
			EntityManager em = DBUtil.getEntityManager();
			TypedQuery<Respondant> q = em.createQuery(
					"SELECT r FROM Respondant r WHERE r.respondantAtsId = :respondantAtsId", Respondant.class);
			q.setParameter("respondantAtsId", partner.getPartnerPrefix() + applicantAtsId);
			try {
				respondant = q.getSingleResult();
			} catch (NoResultException nre) {
			}
		} else {
			// Try to grab account by employmeo respondant_id
			respondant = Respondant.getRespondantById(applicant.optLong("applicant_id"));
		}
		return respondant;
	}
	
	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		Person person = new Person();
		Respondant respondant = new Respondant();
		respondant.setRespondantAccountId(account.getAccountId());
		
		try { // the required parameters
			JSONObject applicant = json.getJSONObject("applicant");
			String appAtsId = applicant.getString("applicant_ats_id");
			respondant.setRespondantAtsId(this.getPrefix() + appAtsId);
			person.setPersonAtsId(this.getPrefix() + appAtsId);
			person.setPersonEmail(applicant.getString("email"));
			person.setPersonFname(applicant.getString("fname"));
			person.setPersonLname(applicant.getString("lname"));
			JSONObject personAddress = applicant.getJSONObject("address");
			AddressUtil.validate(personAddress);
			person.setPersonAddress(personAddress.optString("formatted_address"));
			person.setPersonLat(personAddress.optDouble("lat"));
			person.setPersonLong(personAddress.optDouble("lng"));
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			log.warn(e.toString());
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}
		
		Location location = this.getLocationFrom(json.optJSONObject("location"), account);
		Position position = this.getPositionFrom(json.optJSONObject("position"), account);
		AccountSurvey aSurvey = this.getSurveyFrom(json.optJSONObject("assessment"), account);

		JSONObject delivery = json.optJSONObject("delivery");
		// get the redirect method, score posting and email handling for results
		if (delivery.has("scores_email_address"))
			respondant.setRespondantEmailRecipient(delivery.optString("scores_email_address"));
		if (delivery.has("scores_redirect_url"))
			respondant.setRespondantRedirectUrl(delivery.optString("scores_redirect_url"));
		if (delivery.has("scores_post_url"))
			respondant.setRespondantScorePostMethod(delivery.optString("scores_post_url"));

		respondant.setRespondantAccountId(account.getAccountId());
		respondant.setRespondantAsid(aSurvey.getAsId());
		respondant.setRespondantLocationId(location.getLocationId());
		respondant.setRespondantPositionId(position.getPositionId());
		respondant.setPartner(this.partner);

		// Create Person & Respondant in database.
		person.persistMe();
		respondant.setPerson(person);
		respondant.persistMe();
		respondant.refreshMe(); // gets the remaining auto-gen-fields
				
		return respondant;
	}
	
	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {

		JSONObject delivery = json.optJSONObject("delivery");
		if (delivery.has("email_applicant") && delivery.getBoolean("email_applicant"))
			EmailUtility.sendEmailInvitation(respondant);
		
		// Assemble the response object to notify that action is complete
		JSONObject jAccount = json.getJSONObject("account");
		jAccount.put("account_ats_id", this.trimPrefix(respondant.getRespondantAccount().getAccountAtsId()));
		jAccount.put("account_id", respondant.getRespondantAccount().getAccountId());
		jAccount.put("account_name", respondant.getRespondantAccount().getAccountName());

		JSONObject jApplicant = new JSONObject();
		jApplicant.put("applicant_ats_id", this.trimPrefix(respondant.getRespondantAtsId()));
		jApplicant.put("applicant_id", respondant.getRespondantId());

		delivery = new JSONObject();
		delivery.put("assessment_url", ExternalLinksUtil.getAssessmentLink(respondant));

		JSONObject output = new JSONObject();
		output.put("account", jAccount);
		output.put("applicant", jApplicant);
		output.put("delivery", delivery);

		// get the redirect method, score posting and email handling for results
		return output;
	}
	
	@Override
	public JSONObject getScoresMessage(Respondant respondant) {

		JSONObject scores = respondant.getAssessmentScore();
		PredictionUtil.predictRespondant(respondant);

		Account account = respondant.getRespondantAccount();
		JSONObject jAccount = new JSONObject();
		JSONObject applicant = new JSONObject();

		jAccount.put("account_ats_id", trimPrefix(account.getAccountAtsId()));
		jAccount.put("account_id", account.getAccountId());
		jAccount.put("account_name", account.getAccountName());

		applicant.put("applicant_ats_id", trimPrefix(respondant.getRespondantAtsId()));
		applicant.put("applicant_id", respondant.getRespondantId());
		applicant.put("applicant_profile", respondant.getRespondantProfile());
		applicant.put("applicant_composite_score", respondant.getCompositeScore());
		applicant.put("applicant_profile_label", respondant.getProfileLabel());
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

		Iterator<String> it = scores.keys();
		JSONArray scoreset = new JSONArray();
		while (it.hasNext()) {
			String label = it.next();
			JSONObject cf = new JSONObject();
			cf.put("corefactor_name", label);
			cf.put("corefactor_score", scores.getDouble(label));
			scoreset.put(cf);
		}

		applicant.put("scores", scoreset);
		applicant.put("portal_link", ExternalLinksUtil.getPortalLink(respondant));
		applicant.put("render_link", ExternalLinksUtil.getRenderLink(applicant));

		JSONObject message = new JSONObject();
		message.put("account", jAccount);
		message.put("applicant", applicant);

		return message;

	}
	
	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {

		String postmethod = respondant.getRespondantScorePostMethod();
		if (postmethod == null || postmethod.isEmpty()) return;

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

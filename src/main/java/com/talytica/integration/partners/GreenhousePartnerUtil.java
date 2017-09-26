package com.talytica.integration.partners;

import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Respondant;
import com.talytica.integration.partners.greenhouse.GreenhouseApplication;
import com.talytica.integration.partners.greenhouse.GreenhouseCandidate;

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
	
	
	@Override
	public Client getPartnerClient() {
		ClientConfig cc = new ClientConfig();
//		cc.property(ApacheClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION, true);	
//		cc.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "BUFFERED");
//		cc.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
//		cc.property("sslProtocol", "TLSv1.2");
//		cc.connectorProvider(new ApacheConnectorProvider());
		Client client = ClientBuilder.newClient(cc);
		String user = this.partner.getApiKey();
//		String pass = this.partner.getApiPass();	
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user,"");
		client.register(feature);

		return client;
	}

	public GreenhouseApplication getApplicationDetail(Long id) {
		Client client = getPartnerClient();
		GreenhouseApplication appl = client.target("https://harvest.greenhouse.io/v1/applications/"+id)
				.request().get(GreenhouseApplication.class);

		return appl;
	}
	
	public GreenhouseCandidate getCandidateDetail(Long id) {
		Client client = getPartnerClient();
		GreenhouseCandidate candidate = client.target("https://harvest.greenhouse.io/v1/candidates/"+id)
				.request().get(GreenhouseCandidate.class);

		return candidate;
	}

	
	public Respondant getRespondant(GreenhouseApplication app) {
		return respondantService.getRespondantByAtsId(addPrefix(app.getId().toString()));
	}

	public Respondant createPrescreenCandidate(GreenhouseApplication app, Account account) {
		Respondant respondant = getRespondant(app);
		if (respondant != null) return respondant;
		respondant = new Respondant();
		respondant.setType(Respondant.TYPE_APPLICANT);
		respondant.setRespondantStatus(Respondant.STATUS_PRESCREEN);

		return createRespondantFrom(app, account, respondant);
	}
	public Respondant createPriorDataCandidate(GreenhouseApplication app, Account account) {
		Respondant respondant = getRespondant(app);
		if (respondant != null) return respondant;
		respondant = new Respondant();
		respondant.setType(Respondant.TYPE_PRIOR_DATA);
		switch (app.getStatus()) {
			case "rejected":
				respondant.setRespondantStatus(Respondant.STATUS_REJECTED);
				break;
			case "hired":
				respondant.setRespondantStatus(Respondant.STATUS_HIRED);
				break;
			case "active":
			default:
				respondant.setRespondantStatus(Respondant.STATUS_STARTED);
				break;
		}		
		return createRespondantFrom(app, account, respondant);
	}
	
	public Respondant createRespondantFrom(GreenhouseApplication app, Account account, Respondant respondant) {

		
		Long candidateId = app.getCandidate_id(); // person ATS ID
		Person person = personService.getPersonByAtsId(addPrefix(candidateId.toString()));
		if (person == null) {
			GreenhouseCandidate candidate = getCandidateDetail(candidateId);
			Person newPerson = new Person();
			newPerson.setFirstName(candidate.getFirst_name());
			newPerson.setLastName(candidate.getLast_name());
			if (!candidate.getEmail_addresses().isEmpty()) newPerson.setEmail(candidate.getEmail_addresses().get(0).getValue());
			if (!candidate.getPhone_numbers().isEmpty()) newPerson.setEmail(candidate.getPhone_numbers().get(0).getValue());
			if (!candidate.getAddresses().isEmpty()) newPerson.setEmail(candidate.getAddresses().get(0).getValue());
			newPerson.setAtsId(addPrefix(candidateId.toString()));
			person = personService.save(newPerson);
		}
		
		respondant.setPerson(person);
		respondant.setPersonId(person.getId());
		respondant.setAtsId(addPrefix(app.getId().toString()));
		
		respondant.setAccount(account);
		respondant.setAccountId(account.getId());
		respondant.setAccountSurveyId(account.getDefaultAsId()); // lets see if this is ok...
		respondant.setLocationId(account.getDefaultLocationId());
		String jobId = null;
		if (!app.getJobs().isEmpty()) jobId = addPrefix(app.getJobs().get(0).getId().toString());
		Position position = accountService.getPositionByAtsId(account.getId(), jobId);
		if (position != null) {
			respondant.setPosition(position);
			respondant.setPositionId(position.getId());
		} else {
			respondant.setPositionId(account.getDefaultPositionId());
		}
		respondant.setPartner(getPartner());
		respondant.setPartnerId(getPartner().getId());

		Respondant savedRespondant = respondantService.save(respondant);
		
		if  ((app.getAnswers() != null) && (!app.getAnswers().isEmpty())) {
			for (GreenhouseApplication.GHAnswer ans : app.getAnswers()) {
				if (ans.getAnswer() != null) respondantService.addNVPToRespondant(savedRespondant, ans.getQuestion(), ans.getAnswer());
			}
		}
		if (app.getCustom_fields() != null) {
			log.debug("got custom fields from app: {}", app.getCustom_fields());
		}
		
		return savedRespondant;
	}
}

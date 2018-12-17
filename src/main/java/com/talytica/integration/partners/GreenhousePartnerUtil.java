package com.talytica.integration.partners;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.CustomProfile;
import com.employmeo.data.model.Grade;
import com.employmeo.data.model.Grader;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.PredictionTarget;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.RespondantScore;
import com.talytica.integration.partners.greenhouse.GreenhouseApplication;
import com.talytica.integration.partners.greenhouse.GreenhouseCandidate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class GreenhousePartnerUtil extends BasePartnerUtil {

	@Value("https://harvest.greenhouse.io/v1/")
	private String HARVEST_API;
	
	private final String REJECT_STATUS = "reject";
		
	public GreenhousePartnerUtil() {
	}

	@Override
	public JSONArray formatSurveyList(Set<AccountSurvey> surveys) {
		JSONArray response = new JSONArray();
		for (AccountSurvey as : surveys) {
			JSONObject survey = new JSONObject();	
			try {
				survey.put("partner_test_name", as.getDisplayName());
				survey.put("partner_test_id", as.getId());
				response.put(survey);
			} catch (JSONException e) {
				log.error("Unexpected JSON exception {}", e);
			}
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
	public void changeCandidateStatus(Respondant respondant, String status) {
		String appId = trimPrefix(respondant.getAtsId());
		GreenhouseApplication response;
		JSONObject move = new JSONObject();
		if (REJECT_STATUS.equalsIgnoreCase(status)) {
			response = getPartnerClient().target(HARVEST_API+"applications/"+appId+ "/reject")
				.request().header("On-Behalf-Of:", partner.getApiLogin())
				.post(Entity.entity(move.toString(), MediaType.APPLICATION_JSON),GreenhouseApplication.class);
		} else {
			try {
				move.put("from_stage", Integer.valueOf(status));
				response = getPartnerClient().target(HARVEST_API+"applications/"+appId+"/move")
					.request().header("On-Behalf-Of:", partner.getApiLogin())
					.post(Entity.entity(move.toString(), MediaType.APPLICATION_JSON),GreenhouseApplication.class);
			} catch (JSONException e) {
				log.error("Unexpected JSON error {}",e);
				response = null;
			}
		}
		log.debug("Respondant {} Change resulted in: {}",respondant.getId(),response);
	}
	
	@Override
	public void postScoresToPartner(String method, JSONObject message) {

		Client client =  getPartnerClient();
		if (interceptOutbound) {
			log.info("Intercepting Post to {}", method);
			method = externalLinksService.getIntegrationEcho();
			client = integrationClientFactory.newInstance();
			client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		}
		Response response = client.target(method).request()
				.header("On-Behalf-Of:", partner.getApiLogin())
				.method("PATCH", Entity.entity(message.toString(), MediaType.APPLICATION_JSON));
		
		if (response.getStatus() <= 300) {
			GreenhouseApplication app = response.readEntity(GreenhouseApplication.class);
			log.debug("Success posting as: {}", app);	
		} else {
			log.error("Error from posting {}\n to {}\n with response of: {}", message, method, response.readEntity(String.class));				
		}
	}
	
	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		JSONObject message = new JSONObject();
		JSONObject customFields = new JSONObject();
		try {
			customFields.put("Talytica_Scores", getScoreNotesFormat(respondant));
			customFields.put("Talytica_Link", externalLinksService.getPortalLink(respondant));
			message.put("custom_fields", customFields);
		} catch (JSONException e) {
			log.error("Unexpected JSON error {}",e);
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
		
		if (respondant.getPredictions().size() > 0) notes.append("Analytics:\n");		
		for (Prediction prediction : respondant.getPredictions()) {
			PredictionTarget target = predictionModelService.getTargetById(prediction.getTargetId());
			notes.append(target.getLabel());
			notes.append(": ");
			notes.append(String.format("%.2f", prediction.getPredictionScore()));
			notes.append("\n");
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
		if (scores.size() > 0) notes.append("Summary Scores:\n");		
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
					if (null != grader.getSummaryScore()) references.append(" : ").append(grader.getSummaryScore());
					references.append("\n");
					List<Grade> grades = graderService.getGradesByGraderId(grader.getId());
					int num=0;
					for (Grade grade : grades) {
						num++;
						references.append(num).append(". ");
						references.append(grade.getQuestion().getQuestionText()).append(": ");
						if ((null != grade.getGradeText()) && (!grade.getGradeText().isEmpty())) {
							references.append(grade.getGradeText()).append("\n");						
						} else if (null != grade.getGradeValue()) {
							references.append(grade.getGradeValue()).append("\n");
						} else {
							references.append("No Answer").append("\n");
						}
					}
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
				notes.append("Reference Responses:\n");
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
	public Client getPartnerClient() {
		ClientConfig cc = new ClientConfig();
		cc.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		Client client = ClientBuilder.newClient(cc);
		String user = this.partner.getApiKey();	
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user,"");
		client.register(feature);

		return client;
	}

	public GreenhouseApplication getApplicationDetail(Long id) {
		return getPartnerClient().target(HARVEST_API+"applications/"+id)
		.request().get(GreenhouseApplication.class);
	}
	
	public GreenhouseCandidate getCandidateDetail(Long id) {
		Client client = getPartnerClient();
		GreenhouseCandidate candidate = client.target(HARVEST_API+"candidates/"+id)
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
		String scorePostMethod = HARVEST_API + "applications/" + app.getId();
		respondant.setScorePostMethod(scorePostMethod);
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

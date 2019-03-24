package com.talytica.integration.partners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.CustomProfile;
import com.employmeo.data.model.Grade;
import com.employmeo.data.model.Grader;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.PredictionTarget;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.RespondantScore;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class FountainPartnerUtil extends BasePartnerUtil {

	@Value("https://api.fountain.com/v2/")
	private String FOUNTAIN_API;
	
	@Value("applicants/")
	private String APPLICANT_ENDPOINT;
		
	@Value("/advance")
	private String STAGE_ADVANCE;
	
	public FountainPartnerUtil() {
	}
	
	@Override
	public void changeCandidateStatus(Respondant respondant, String status) {
		String method = respondant.getScorePostMethod()+STAGE_ADVANCE;
		Client client = null;
		if (interceptOutbound) {
			log.info("Intercepting Post to {}", method);
			method = externalLinksService.getIntegrationEcho();
			client = integrationClientFactory.newInstance();
		} else {
			client = getPartnerClient();
		}
		try {
			JSONObject advance = new JSONObject();
			advance.put("stage_id", status);
			advance.put("skip_automated_action", false);
			client.target(method)
				.request().header("X-ACCESS-TOKEN:", partner.getApiLogin())
				.put(Entity.entity(advance.toString(), MediaType.APPLICATION_JSON));
		} catch (JSONException e) {
			log.error("Unexpected JSON error {}",e);
		} finally {
			client.close();
		}
	}
	
	@Override
	public void postScoresToPartner(String method, JSONObject message) {

		Client client =  getPartnerClient();
		if (interceptOutbound) {
			log.info("Intercepting Post to {}", method);
			method = externalLinksService.getIntegrationEcho();
			client = integrationClientFactory.newInstance();
		}
		Response response = client.target(method).request()
				.header("X-ACCESS-TOKEN:", partner.getApiLogin())
				.put(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));
		
		if (response.getStatus() <= 300) {
			log.debug("Success posting to {}: {}", method, message);
		} else {
			log.error("Error from posting {}\n to {}\n with response of: {}", message, method, response.readEntity(String.class));				
		}
	}
	
	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		JSONObject message = new JSONObject();
		JSONObject data = new JSONObject();
		try {

			data.put("talytica_link", externalLinksService.getPortalLink(respondant));
			int status = respondant.getRespondantStatus();
			String talyticastatus = "created";
			if (status >= Respondant.STATUS_STARTED) talyticastatus = "incomplete";
			if (status >= Respondant.STATUS_COMPLETED) {
				talyticastatus = "completed";
				addResponseFields(data, respondant);
			}
			if (status >= Respondant.STATUS_SCORED) {
				if(respondant.getCompositeScore() > 0) data.put("talytica_scores", getScoreNotesFormat(respondant));
				talyticastatus = "scored";
			}
			data.put("talytica_status", talyticastatus);
			message.put("data", data);
		} catch (JSONException e) {
			log.error("Unexpected JSON error {}",e);
		}
		
		return message;		
	}
	
	public void addResponseFields(JSONObject data, Respondant respondant) throws JSONException {
		Set<com.employmeo.data.model.Response> audioResponses = respondantService.getAudioResponses(respondant.getId());
		List<String> responseUrls = Lists.newArrayList();
		for (com.employmeo.data.model.Response resp : audioResponses) responseUrls.add(resp.getResponseMedia());
		if (!responseUrls.isEmpty()) data.put("talytica_recordings", responseUrls);
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
		Client client = ClientBuilder.newClient(cc);
		return client;
	}


	public String getApplicantUpdateMethod(String appId) {
		String scorePostMethod = FOUNTAIN_API + APPLICANT_ENDPOINT + appId;
		return scorePostMethod;
	}
	
}

package com.employmeo.util;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Corefactor;
import com.employmeo.objects.Question;
import com.employmeo.objects.Respondant;
import com.employmeo.objects.RespondantScore;
import com.employmeo.objects.Response;

public class ScoringUtil {

	private static final Logger log = LoggerFactory.getLogger(ScoringUtil.class);
	private static String MERCER_PREFIX = "Mercer";
	private static String MERCER_SERVICE = System.getenv("MERCER_SERVICE");
	private static String MERCER_USER = "employmeo";
	private static String MERCER_PASS = "employmeo";
	private static int MERCER_COREFACTOR = 34;

	
	public static void scoreAssessment(Respondant respondant) {
		log.debug("Scoring assessment for respondant {}", respondant);
		List<Response> responses = respondant.getResponses();
		if ((responses == null) || (responses.size() == 0))	return; // return nothing

		mercerScore(respondant); // for questions with corefactor 34
		defaultScore(respondant);

		if (respondant.getRespondantScores().size() > 0) {
			respondant.setRespondantStatus(Respondant.STATUS_SCORED);
			respondant.mergeMe();
		}
		return;
	}

	private static void defaultScore(Respondant respondant) {
		List<Response> responses = respondant.getResponses();
		int[] count = new int[50];
		int[] score = new int[50];

		for (int i = 0; i < responses.size(); i++) {
			Response response = responses.get(i);
			Integer cfId = Question.getQuestionById(response.getResponseQuestionId()).getQuestionCorefactorId();
			if (cfId != MERCER_COREFACTOR) {
				count[cfId]++;
				score[cfId] += response.getResponseValue();
			}
		}
		for (int i = 0; i < 50; i++) {
			if (count[i] > 0) {
				RespondantScore rs = new RespondantScore();
				rs.setPK(i, respondant.getRespondantId());
				rs.setRsQuestionCount(count[i]);
				rs.setRsValue((double) score[i] / (double) count[i]);
				rs.mergeMe();
				respondant.addRespondantScore(rs);
			}
		}
	}
	
	
	private static void mercerScore(Respondant respondant) {
		log.debug("Requesting Mercer Score for respondant_id: " + respondant.getRespondantId());
		List<Response> responses = respondant.getResponses();
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(MERCER_USER, MERCER_PASS);
		client.register(feature);

		JSONArray answers = new JSONArray();
		for (int i=0;i<responses.size();i++) {
			Question question = Question.getQuestionById(responses.get(i).getResponseQuestionId());
			if (question.getQuestionCorefactorId() == MERCER_COREFACTOR) {
				String testname = question.getQuestionForeignSource();
				if (testname.equalsIgnoreCase("behavior_b")) {
					String[] priorities = Integer.toString(responses.get(i).getResponseValue()).split("(?!^)");
					for (int j=0;j<priorities.length;j++) {
						int value = Integer.valueOf(priorities[j]);
						String quesId = question.getQuestionForeignId() + "_" + j;
						JSONObject jResp = new JSONObject();
						jResp.put("response_value", value);
						jResp.put("question_id", quesId);
						jResp.put("test_name", testname);
						answers.put(jResp);	
					}
				} else {
					JSONObject jResp = new JSONObject();			
					jResp.put("response_value", responses.get(i).getResponseValue());
					jResp.put("question_id", question.getQuestionForeignId());
					jResp.put("test_name", testname);
					answers.put(jResp);
				}
			}
		}
		if (answers.length() > 0) {
			JSONObject applicant = new JSONObject();
			JSONObject message = new JSONObject();
			applicant.put("applicant_id", respondant.getRespondantId());
			applicant.put("applicant_account_name", respondant.getRespondantAccount().getAccountName());
			message.put("applicant", applicant);
			message.put("responses", answers);
	
			JSONArray result;
			javax.ws.rs.core.Response resp = null;
			String output = null;
			try {
				WebTarget target = client.target(MERCER_SERVICE);
				resp = target.request(MediaType.APPLICATION_JSON)
							.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));
				output = resp.readEntity(String.class);
				result = new JSONArray(output);
			} catch (Exception e) {
				log.warn("Failed to get results from mercer: " + e.getMessage());
				log.debug("Failed to get results from mercer: " + message.toString());
				if (resp != null) {
					log.debug("Response status: " + resp.getStatus() + " " + resp.getStatusInfo().getReasonPhrase());
					log.debug("Failed to get results from mercer: " + output);
				}
				return;
			}
	
			for (int i = 0; i < result.length(); i++) {
				JSONObject data = result.getJSONObject(i);
				int score = data.getInt("score");
				RespondantScore rs = new RespondantScore();
				try {
						String foreignId = MERCER_PREFIX + data.getString("id");
						log.debug("Finding corefactor by foreign id '{}'", foreignId);
						Corefactor cf = Corefactor.getCorefactorByForeignId(foreignId);
						rs.setPK(cf.getCorefactorId(), respondant.getRespondantId());
						rs.setRsQuestionCount(responses.size());
						rs.setRsValue((double) score);
						rs.mergeMe();
						respondant.addRespondantScore(rs);
				} catch (Exception e) {
						log.warn("Failed to record score: " + data + " for repondant " + respondant.getJSONString(), e);
				}
			}
		}
		
		return;
	}

}

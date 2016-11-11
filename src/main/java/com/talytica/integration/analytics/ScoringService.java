package com.talytica.integration.analytics;

import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.service.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ScoringService {
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;


	private static String MERCER_PREFIX = "Mercer";
	private static String MERCER_SERVICE = System.getenv("MERCER_SERVICE");
	private static String MERCER_USER = "employmeo";
	private static String MERCER_PASS = "employmeo";
	private static int MERCER_COREFACTOR = 34;


	public void scoreAssessment(Respondant respondant) {
		log.debug("Scoring assessment for respondant {}", respondant);
		Set<Response> responses = respondant.getResponses();
		if ((responses == null) || (responses.size() == 0))
		 {
			return; // return nothing
		}

		mercerScore(respondant); // for questions with corefactor 34
		defaultScore(respondant);

		if (respondant.getRespondantScores().size() > 0) {
			respondant.setRespondantStatus(Respondant.STATUS_SCORED);
			respondantService.save(respondant);
		}
		return;
	}

	private void defaultScore(Respondant respondant) {
		Set<Response> responses = respondantService.getResponses(respondant.getRespondantUuid());
		//List<Response> responses = respondant.getResponses();
		int[] count = new int[50];
		int[] score = new int[50];

		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Integer cfId = question.getCorefactorId();
			if (cfId != MERCER_COREFACTOR) {
				count[cfId]++;
				score[cfId] += response.getResponseValue();
			}
		});

		for (int i = 0; i < 50; i++) {
			if (count[i] > 0) {
				RespondantScore rs = new RespondantScore();
				rs.setId(new RespondantScorePK((long) i, respondant.getId()));
				rs.setQuestionCount(count[i]);
				rs.setValue((double) score[i] / (double) count[i]);
				rs.setRespondant(respondant);
				respondant.getRespondantScores().add(rs);

				respondantService.save(respondant);
			}
		}
	}


	private void mercerScore(Respondant respondant) {
		log.debug("Requesting Mercer Score for respondant_id: " + respondant.getId());
		Set<Response> responses = respondant.getResponses();
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(MERCER_USER, MERCER_PASS);
		client.register(feature);

		JSONArray answers = new JSONArray();
		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			//Question question = Question.getQuestionById(responses.get(i).getResponseQuestionId());
			if (question.getCorefactorId() == MERCER_COREFACTOR) {
				String testname = question.getForeignSource();
				if (testname.equalsIgnoreCase("behavior_b")) {
					String[] priorities = Integer.toString(response.getResponseValue()).split("(?!^)");
					for (int j=0;j<priorities.length;j++) {
						int value = Integer.valueOf(priorities[j]);
						String quesId = question.getForeignId() + "_" + j;
						JSONObject jResp = new JSONObject();
						jResp.put("response_value", value);
						jResp.put("question_id", quesId);
						jResp.put("test_name", testname);
						answers.put(jResp);
					}
				} else {
					JSONObject jResp = new JSONObject();
					jResp.put("response_value", response.getResponseValue());
					jResp.put("question_id", question.getForeignId());
					jResp.put("test_name", testname);
					answers.put(jResp);
				}
			}
		});

		if (answers.length() > 0) {
			JSONObject applicant = new JSONObject();
			JSONObject message = new JSONObject();
			applicant.put("applicant_id", respondant.getId());
			applicant.put("applicant_account_name", respondant.getAccount().getAccountName());
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
						Corefactor cf =  corefactorService.getByForeignId(foreignId);
						//Corefactor cf = Corefactor.getCorefactorByForeignId(foreignId);
						rs.setId(new RespondantScorePK(cf.getId(), respondant.getId()));
						rs.setQuestionCount(responses.size());
						rs.setValue((double) score);
						rs.setRespondant(respondant);
						respondant.getRespondantScores().add(rs);

						respondantService.save(respondant);
				} catch (Exception e) {
						log.warn("Failed to record score: " + data + " for repondant " + respondant, e);
				}
			}
		}

		return;
	}

}

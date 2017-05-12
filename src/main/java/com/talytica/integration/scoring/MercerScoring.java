package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Question;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.RespondantScorePK;
import com.employmeo.data.model.Response;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.QuestionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MercerScoring implements ScoringModelEngine {

	@Value("${com.talytica.apis.mercer.url}")
	private String MERCER_SERVICE;
	private static String MERCER_PREFIX = "Mercer";
	private static String MERCER_USER = "employmeo";
	private static String MERCER_PASS = "employmeo";

	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;

	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>();
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(MERCER_USER, MERCER_PASS);
		client.register(feature);

		JSONArray answers = new JSONArray();
		responses.forEach(response -> {
			try {
				Question question = questionService.getQuestionById(response.getQuestionId());
				String testname = question.getForeignSource();
				if (testname.equalsIgnoreCase("behavior_b")) {
					String[] priorities = Integer.toString(response.getResponseValue()).split("(?!^)");
					for (int j = 0; j < priorities.length; j++) {
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
			} catch (Exception e) {
				log.error("Failed to convert response");
			}
		});
		if (answers.length() > 0) {
			JSONObject applicant = new JSONObject();
			JSONObject message = new JSONObject();

			JSONArray result;
			javax.ws.rs.core.Response resp = null;
			String output = null;
			log.debug("Requesting Mercer Score for respondant {} ", respondant);
			try {
				applicant.put("applicant_id", respondant.getId());
				applicant.put("applicant_account_name", respondant.getAccount().getAccountName());
				message.put("applicant", applicant);
				message.put("responses", answers);
				WebTarget target = client.target(MERCER_SERVICE);
				resp = target.request(MediaType.APPLICATION_JSON)
						.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));
				output = resp.readEntity(String.class);
				result = new JSONArray(output);
			} catch (Exception e) {
				if (resp != null) {
					log.debug("Mercer Failure {} {}: " + resp.getStatus(), resp.getStatusInfo().getReasonPhrase());
				}
				log.error("Mercer Failure Exception {} {}", e.getMessage(), message.toString());
				return null;
			}

			for (int i = 0; i < result.length(); i++) {
				JSONObject data = result.optJSONObject(i);
				int score = data.optInt("score");
				RespondantScore rs = null;
				Corefactor cf = null;
				try {
					String foreignId = MERCER_PREFIX + data.getString("id");
					log.debug("Finding corefactor by foreign id '{}'", foreignId);
					cf = corefactorService.getByForeignId(foreignId);
					rs = new RespondantScore();
					rs.setId(new RespondantScorePK(cf.getId(), respondant.getId()));
					rs.setQuestionCount(responses.size());
					rs.setValue((double) score);
					rs.setRespondant(respondant);
					scores.add(rs);
				} catch (Exception e) {
					log.error("Failed to record score {}, {} for respondant {}", rs, cf, respondant);
				}
			}

		}

		return scores;
	}

	@Override
	public String getModelName() {
		return ScoringModelType.MERCER.getValue();
	}

	@Override
	public void initialize(String modelName) {

	}

}

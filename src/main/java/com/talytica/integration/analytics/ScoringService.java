package com.talytica.integration.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PersonRepository;
import com.employmeo.data.repository.RespondantScoreRepository;
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
	@Autowired
	private GraderService graderService;
	@Autowired
	private UserService userService;

	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private RespondantScoreRepository respondantScoreRepository;
	
	@Value("${com.talytica.apis.mercer.url}")
	private String MERCER_SERVICE;
	private static String MERCER_PREFIX = "Mercer";
	private static String MERCER_USER = "employmeo";
	private static String MERCER_PASS = "employmeo";
	private static final int MERCER_COREFACTOR = 34;
	private static final int AUDIO_COREFACTOR = 42;
	private static final int REFERENCE_COREFACTOR = 43; /// doesn't exist yet!!

	public Respondant scoreAssessment(Respondant respondant) {
		log.debug("Scoring assessment for respondant {}", respondant);
		Set<Response> responses = respondantService.getResponses(respondant.getRespondantUuid());
		
		List<Response> mercer = new ArrayList<Response>();
		List<Response> audio = new ArrayList<Response>();
		List<Response> reference = new ArrayList<Response>();
		List<Response> others = new ArrayList<Response>();

		if ((responses == null) || (responses.size() == 0))
		{
			log.debug("No responses found for respondant {}", respondant);
			return respondant; // return nothing
		}

		for (Response response : responses) {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Integer cfId = question.getCorefactorId();
			switch (cfId.intValue()) {
				case MERCER_COREFACTOR:
					mercer.add(response);
					break;
				case AUDIO_COREFACTOR:
					audio.add(response);
					break;
				case REFERENCE_COREFACTOR:
					reference.add(response);
					break;
				default:
					others.add(response);
					break;
			}
		}

		mercerScore(respondant, mercer); // for questions with corefactor 34
		defaultScore(respondant, others);
		
		boolean gradesNeeded = audioGraderLaunch(respondant, audio); // not ready yet
		boolean referencesNeeded = referenceLaunch(respondant, reference);
		
		if (respondant.getRespondantScores().size() > 0) {
			respondantScoreRepository.save(respondant.getRespondantScores());
			log.debug("Saved Scores for respondant {}", respondant.getRespondantScores());
		}
		if (gradesNeeded || referencesNeeded) {
			respondant.setRespondantStatus(Respondant.STATUS_UNGRADED);// GRADES INCOMPLETE?
		} else {
			respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		}
		return respondantService.save(respondant);
	}

	private void defaultScore(Respondant respondant, List<Response> responses) {
		int[] count = new int[50];
		int[] score = new int[50];

		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Integer cfId = question.getCorefactorId();
			count[cfId]++;
			score[cfId] += response.getResponseValue();
		});

		for (int i = 0; i < 50; i++) {
			if (count[i] > 0) {
				RespondantScore rs = new RespondantScore();
				rs.setId(new RespondantScorePK((long) i, respondant.getId()));
				rs.setQuestionCount(count[i]);
				rs.setValue((double) score[i] / (double) count[i]);
				rs.setRespondant(respondant);
				respondant.getRespondantScores().add(rs);
			}
		}
		
	}

	private boolean referenceLaunch(Respondant respondant, List<Response> responses) {
		boolean referenceSent = false;
		
		for (Response response : responses) {
			String email = response.getResponseText();
			Person person = new Person();
			person.setEmail(email);
			Person reference = personRepository.save(person);

			Grader grader = new Grader();
			grader.setType(Grader.TYPE_PERSON);
			grader.setStatus(Grader.STATUS_NEW);
			grader.setPerson(reference);
			grader.setRespondantId(respondant.getId());
			grader.setPersonId(reference.getId());
			grader.setResponse(response);
			grader.setResponseId(response.getId());
			grader.setQuestionId(response.getQuestionId());
			graderService.save(grader);
				
			//emailService.sendReferenceCheck();
			referenceSent = true;
		}	
		return referenceSent;
	}
	
	private boolean audioGraderLaunch(Respondant respondant, List<Response> responses) {
		boolean graderSaved = false;
		Set<User> users = userService.getUsersForAccount(respondant.getAccountId());// creating a grader for every user?		

		log.debug("Found {} Users to grade Respondant {}",users.size(),respondant.getId());
		
		for (Response response : responses) {
			for (User user : users) {
				Grader grader = new Grader();
				grader.setType(Grader.TYPE_USER);
				grader.setStatus(Grader.STATUS_NEW);
				grader.setUser(user);
				grader.setRespondantId(respondant.getId());
				grader.setUserId(user.getId());
				grader.setResponse(response);
				grader.setResponseId(response.getId());
				grader.setQuestionId(response.getQuestionId());
				graderService.save(grader);
				graderSaved = true;
			}
		}	
		return graderSaved;
	}
	
	private void mercerScore(Respondant respondant, List<Response> responses) {

		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(MERCER_USER, MERCER_PASS);
		client.register(feature);

		JSONArray answers = new JSONArray();
		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
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
			log.debug("Requesting Mercer Score for respondant {} ", respondant);
			try {
				WebTarget target = client.target(MERCER_SERVICE);
				resp = target.request(MediaType.APPLICATION_JSON)
							.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));
				output = resp.readEntity(String.class);
				result = new JSONArray(output);
			} catch (Exception e) {
				if (resp != null) {
					log.debug("Mercer Failure {} {}: " + resp.getStatus(), resp.getStatusInfo().getReasonPhrase());
				}
				log.error("Mercer Failure Exception {} {}", e.getMessage(),message.toString());
				return;
			}

			for (int i = 0; i < result.length(); i++) {
				JSONObject data = result.getJSONObject(i);
				int score = data.getInt("score");
				RespondantScore rs = null;
				Corefactor cf = null;
				try {
					String foreignId = MERCER_PREFIX + data.getString("id");
					log.debug("Finding corefactor by foreign id '{}'", foreignId);
					cf =  corefactorService.getByForeignId(foreignId);
					rs = new RespondantScore();
					rs.setId(new RespondantScorePK(cf.getId(), respondant.getId()));
					rs.setQuestionCount(responses.size());
					rs.setValue((double) score);
					rs.setRespondant(respondant);
					respondant.getRespondantScores().add(rs);
				} catch (Exception e) {
					log.error("Failed to record score {}, {} for respondant {}", rs, cf, respondant);
				}
			}
		}

		return;
	}

}

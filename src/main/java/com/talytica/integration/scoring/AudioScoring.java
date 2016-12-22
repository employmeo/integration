package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Grader;
import com.employmeo.data.model.GraderConfig;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;

import com.employmeo.data.model.Response;
import com.employmeo.data.model.User;
import com.employmeo.data.service.AccountSurveyService;
import com.employmeo.data.service.GraderService;
import com.employmeo.data.service.UserService;
import com.talytica.common.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AudioScoring implements ScoringModelEngine {
	
	@Autowired
	private UserService userService;
	@Autowired
	private GraderService graderService;
	@Autowired
	private AccountSurveyService accountSurveyService;
	@Autowired
	private EmailService emailService;

	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		boolean graderSaved = false;
		Set<GraderConfig> configs = accountSurveyService.getGraderConfigsForSurvey(respondant.getAccountSurveyId());

		if (configs.isEmpty()) {
			Set<User> users = userService.getUsersForAccount(respondant.getAccountId());// creating a grader for every user?
			users.forEach(user -> {
				GraderConfig config = new GraderConfig();
				config.setUser(user);
				config.setUserId(user.getId());
				configs.add(config);
				});
			log.info("No Configs Found. Addeded {} users to grade Respondant {}",configs.size(),respondant.getId());
		} 
		
		for (GraderConfig config : configs) {
			Grader savedGrader = null;
			for (Response response : responses) {	
				Grader grader = new Grader();
				grader.setStatus(Grader.STATUS_NEW);
				grader.setUser(config.getUser());
				grader.setUserId(config.getUserId());
				grader.setRespondant(respondant);
				grader.setRespondantId(respondant.getId());
				grader.setResponse(response);
				grader.setResponseId(response.getId());
				grader.setQuestionId(response.getQuestionId());			
				grader.setType(Grader.TYPE_USER);;
				if (config.getSummarize()) grader.setType(Grader.TYPE_SUMMARY_USER);;
				savedGrader = graderService.save(grader);
				graderSaved = true;
				if (config.getSummarize()) break;
			}
			log.debug("Grader {} created with {} responses to grade, notify = {}", savedGrader, responses.size(),config.getNotify());
			if ((savedGrader != null) && (config.getNotify())) emailService.sendGraderRequest(savedGrader);
		}
		if (graderSaved) return null;
		return new ArrayList<RespondantScore>();
	}

	@Override
	public String getModelName() {
		return ScoringModelType.AUDIO.getValue();
	}

	@Override
	public void initialize(String modelName) {
		// TODO Auto-generated method stub
		
	}
}

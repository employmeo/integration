package com.talytica.integration.analytics;

import java.util.*;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.RespondantScoreRepository;
import com.employmeo.data.service.*;
import com.talytica.integration.scoring.ScoringModelEngine;
import com.talytica.integration.scoring.ScoringModelRegistry;

import lombok.NonNull;
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
	private GraderService graderService;
	@Autowired
	private ScoringModelRegistry scoringModelRegistry;
	@Autowired
	private RespondantScoreRepository respondantScoreRepository;

	public Respondant scoreAssessment(@NonNull Respondant respondant) {
		log.debug("Scoring assessment for respondant {}", respondant);
		Set<Response> responses = respondantService.getResponses(respondant.getRespondantUuid());
		HashMap<String, List<Response>> responseTable = new HashMap<String, List<Response>>();
		
		if ((responses == null) || (responses.size() == 0))
		{
			log.debug("No responses found for respondant {}", respondant);
			return respondant; // return nothing
		}

		for (Response response : responses) {
			List<Response> responseSet;
			Question question = questionService.getQuestionById(response.getQuestionId());
			String scoringModel = question.getScoringModel();
			if (responseTable.containsKey(scoringModel)) {
				responseSet = responseTable.get(scoringModel);
			} else {
				responseSet = new ArrayList<Response>();
				responseTable.put(scoringModel, responseSet);
			}
			responseSet.add(response);
		}
		
		boolean complete = true;
		for (Map.Entry<String, List<Response>> pair : responseTable.entrySet()) {
			Optional<ScoringModelEngine> result = scoringModelRegistry.getScoringModelEngineByName(pair.getKey());
			if (!result.isPresent()) {
				log.error("Didn't find {} scoring model for responses", pair.getKey());
				complete = false;
				continue;
			}
			ScoringModelEngine scoringModelEngine = result.get();
			
			List<RespondantScore> scores = scoringModelEngine.scoreResponses(respondant, pair.getValue());
			if (scores == null) {
				complete = false;
				continue;
			}
			respondant.getRespondantScores().addAll(scores);
		}


		if (respondant.getRespondantScores().size() > 0) {
			respondantScoreRepository.save(respondant.getRespondantScores());
			log.debug("Saved Scores for respondant {}", respondant.getRespondantScores());
		}
		if (complete) {
			respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		} else {
			respondant.setRespondantStatus(Respondant.STATUS_UNGRADED);// GRADES INCOMPLETE?
		}
		return respondantService.save(respondant);
	}

	
	public Respondant scoreGraders(@NonNull Respondant respondant) {
		List<Grader> graders = graderService.getGradersByRespondantId(respondant.getId());
		List<Grade> grades = new ArrayList<Grade>();

		for (Grader grader : graders) {
		    grades.addAll(graderService.getGradesByGraderId(grader.getId()));
		    log.debug("Respondant {} has grader {}", respondant.getId(), grader);
		}
		HashMap<String, List<Response>> responseTable = new HashMap<String, List<Response>>();
		
		grades.forEach(grade -> {
			Question question = questionService.getQuestionById(grade.getQuestionId());
			Response response = new Response();
			response.setRespondant(respondant);
			response.setRespondantId(respondant.getId());
			response.setQuestionId(grade.getQuestionId());
			response.setResponseValue(grade.getGradeValue());
			List<Response> responseSet;
			String scoringModel = question.getScoringModel();
			if (responseTable.containsKey(scoringModel)) {
				responseSet = responseTable.get(scoringModel);
			} else {
				responseSet = new ArrayList<Response>();
				responseTable.put(scoringModel, responseSet);
			}
			responseSet.add(response);
		});
		
		for (Map.Entry<String, List<Response>> pair : responseTable.entrySet()) {
			Optional<ScoringModelEngine> result = scoringModelRegistry.getScoringModelEngineByName(pair.getKey());
			if (!result.isPresent()) {
				log.error("Didn't find {} scoring model for responses", pair.getKey()); continue;
			}
			ScoringModelEngine scoringModelEngine = result.get();
			List<RespondantScore> scores = scoringModelEngine.scoreResponses(respondant, pair.getValue());
			if (scores == null)	continue;
			respondant.getRespondantScores().addAll(scores);
		}

		// then update respondant status to Scored		
		respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		respondantScoreRepository.save(respondant.getRespondantScores());
		return respondantService.save(respondant);
	}
}

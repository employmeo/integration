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
	@Autowired
	private CorefactorService corefactorService;

	public Respondant scoreAssessment(@NonNull Respondant respondant) {
		log.debug("Scoring assessment for respondant {}", respondant);
		Set<Response> responses = respondantService.getResponses(respondant.getRespondantUuid());
		HashMap<String, List<Response>> responseTable = new HashMap<String, List<Response>>();
		HashMap<Corefactor, List<RespondantScore>> parents = new HashMap<Corefactor, List<RespondantScore>>();
		
		if ((responses == null) || (responses.size() == 0))
		{
			log.debug("No responses found for respondant {}", respondant);
			return respondant; // return nothing
		}

		// This loop associates each response with a scoring model
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
		
		// This loop sends sets of responses to each "scoring model"
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

		// Loop identifies "parent" factors - rolling up factors with a parent id to their parents
		for (RespondantScore rs : respondant.getRespondantScores()) {
			Corefactor cf = corefactorService.findCorefactorById(rs.getId().getCorefactorId());
			if (null != cf.getParentId()) {
				List<RespondantScore> rsList;
				Corefactor parent = corefactorService.findCorefactorById(cf.getParentId());
				if (!parents.containsKey(parent)) {
					rsList = new ArrayList<RespondantScore>();
					parents.put(parent,rsList);
				} else {
					rsList = parents.get(parent);
				}
				rsList.add(rs);
			}
		}
		
		// Loop creates and average of children scores for each parent
		for (Map.Entry<Corefactor, List<RespondantScore>> pair : parents.entrySet()) {
			RespondantScore score = new RespondantScore();
			score.setId(new RespondantScorePK(pair.getKey().getId(), respondant.getId()));
			double total = 0;
			for (RespondantScore rs : pair.getValue()) {
				total += rs.getValue();
			}
			score.setValue(total/pair.getValue().size());
			respondant.getRespondantScores().add(score);
		}

		// Section below saves all scores to the DB.
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

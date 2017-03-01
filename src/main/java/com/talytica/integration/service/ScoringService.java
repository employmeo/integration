package com.talytica.integration.service;

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
		log.debug("Scoring assessment for respondant {}", respondant.getId());
		Set<Response> responses = cleanseResponses(respondantService.getResponses(respondant.getRespondantUuid()));
		HashMap<String, List<Response>> responseTable = new HashMap<String, List<Response>>();
		HashMap<Corefactor, List<RespondantScore>> parents = new HashMap<Corefactor, List<RespondantScore>>();
		
		if ((responses == null) || (responses.size() == 0))
		{
			log.debug("No responses found for respondant {}", respondant.getId());
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
			log.debug("Saved {} Scores for respondant {}", respondant.getRespondantScores().size(), respondant.getId());
		}
		if (complete) {
			respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		} else {
			respondant.setRespondantStatus(Respondant.STATUS_UNGRADED);// GRADES INCOMPLETE?
		}
		return respondantService.save(respondant);
	}

	
	private Set<Response> cleanseResponses(Set<Response> responses) {
		Set<Response> returnSet = new HashSet<Response>();
		Set<Response> duplicates = new HashSet<Response>();
		for (Response response : responses) {
			Optional<Response> conflict = findResponse(response.getQuestionId(),returnSet);
			if(conflict.isPresent()) {
				Response orig = conflict.get();
				log.debug("Duplicate found: {} vs {}",response,orig);
				if (response.getLastModDate().after(orig.getLastModDate())) {
					returnSet.remove(orig);
					duplicates.add(orig);
					returnSet.add(response);
				} else {
					duplicates.add(response);					
				}
			} else {
				returnSet.add(response);
			}	
		}
		return returnSet;
	}
	
	private Optional<Response> findResponse(Long questionId, Set<Response> responses) {
		return responses.stream().filter(response -> questionId.equals(response.getQuestionId())).findFirst();
	}

	public Respondant scoreGraders(@NonNull Respondant respondant) {
		List<Grader> graders = graderService.getGradersByRespondantId(respondant.getId());
		List<Grade> grades = new ArrayList<Grade>();

		for (Grader grader : graders) {
		    grades.addAll(graderService.getGradesByGraderId(grader.getId()));
		    log.debug("Respondant {} has grader {} with {} total grades", respondant.getId(), grader.getId(), grades.size());
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
		
		Set<RespondantScore> gradedScores = new HashSet<RespondantScore>();
		for (Map.Entry<String, List<Response>> pair : responseTable.entrySet()) {
			Optional<ScoringModelEngine> result = scoringModelRegistry.getScoringModelEngineByName(pair.getKey());
			if (!result.isPresent()) {
				log.error("Didn't find {} scoring model for responses", pair.getKey()); continue;
			}
			ScoringModelEngine scoringModelEngine = result.get();
			List<RespondantScore> scores = scoringModelEngine.scoreResponses(respondant, pair.getValue());
			log.debug("Scoring model {} produced {} for respondant {}", pair.getKey(), scores, respondant.getId());
			if (scores != null)	gradedScores.addAll(scores);
		}

		// then update respondant status to Scored		
		respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		if (gradedScores.size() > 0) respondantScoreRepository.save(gradedScores);
		return respondantService.save(respondant);
	}
}

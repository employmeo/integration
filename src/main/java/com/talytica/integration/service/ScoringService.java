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

import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ScoringService {
	private static final Long RANKER_CFID = 99l;
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
	@Autowired
	private AccountSurveyService accountSurveyService;

	@Deprecated
	public Respondant scoreAssessment(@NonNull Respondant respondant) {
		Boolean incomplete = scoreAssessmentResponses(respondant);
		if (incomplete) {
			respondant.setRespondantStatus(Respondant.STATUS_UNGRADED);
		} else {
			respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		}
		if (respondant.getRespondantScores().size() > 0) {
			respondantScoreRepository.save(respondant.getRespondantScores());
			log.debug("Saved {} Scores for respondant {}", respondant.getRespondantScores().size(), respondant.getId());
		}

		return respondantService.save(respondant);
	}
	
	@Deprecated
	public Respondant scoreGraders(@NonNull Respondant respondant) {
		Set<RespondantScore> gradedScores = computeGraders(respondant);
		respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		if (gradedScores.size() > 0) respondantScoreRepository.save(gradedScores);
		return respondantService.save(respondant);
	}
	
	
	public Boolean scoreAssessmentResponses(@NonNull Respondant respondant) {
		log.debug("Scoring assessment for respondant {}", respondant.getId());
		HashMap<String, List<Response>> responseTable = new HashMap<String, List<Response>>();
		HashMap<Corefactor, List<RespondantScore>> parents = new HashMap<Corefactor, List<RespondantScore>>();
		Set<RespondantScore> allScores = Sets.newHashSet();
		Set<Response> responses = cleanseResponses(respondantService.getResponses(respondant.getRespondantUuid()));
		if (respondant.getRespondantStatus() == Respondant.STATUS_ADVCOMPLETED) {
			// filter down responses to 2nd stage survey only.
			AccountSurvey as = accountSurveyService.getAccountSurveyById(respondant.getSecondStageSurveyId());
			Set<SurveyQuestion> questions = as.getSurvey().getSurveyQuestions();
			Set<Response> secondStageResponses = Sets.newHashSet();
			for (Response response : responses) {
				for (SurveyQuestion sq : questions) {
					if (sq.getQuestionId().equals(response.getQuestionId())) {
						secondStageResponses.add(response); 
						continue;
					}
				}
			}
			log.debug("Filtered respondant {} to {} second stage responses", respondant.getId(), secondStageResponses.size());
			responses = secondStageResponses;
		}
		
		if ((responses == null) || (responses.size() == 0))
		{
			log.debug("No responses found for respondant {}", respondant.getId());
			return null; // return nothing to signify that no "scores" have come back.
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
		RespondantScore gradesNeeded = null; 
		for (Map.Entry<String, List<Response>> pair : responseTable.entrySet()) {
			Optional<ScoringModelEngine> result = scoringModelRegistry.getScoringModelEngineByName(pair.getKey());
			if (!result.isPresent()) {
				log.error("Didn't find {} scoring model for responses - responses ignored", pair.getKey());
				continue;
			}
			ScoringModelEngine scoringModelEngine = result.get();			
			List<RespondantScore> scores = scoringModelEngine.scoreResponses(respondant, pair.getValue());
			allScores.addAll(scores);
		}

		// Loop identifies "parent" factors - rolling up factors with a parent id to their parents
		for (RespondantScore rs : allScores) {
			Corefactor cf = corefactorService.findCorefactorById(rs.getId().getCorefactorId());
			if (cf.getId() == RANKER_CFID) { // Graders + Audio add "ranker corefactors" to signify incomplete scoring
				gradesNeeded = rs;
				continue;
			}
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
			allScores.add(score);
		}
		
		Boolean outstandingGrades = Boolean.FALSE;
		if (null != gradesNeeded) {
			respondant.getRespondantScores().remove(gradesNeeded);			
			outstandingGrades = Boolean.TRUE;
		}
		respondant.getRespondantScores().addAll(allScores);
		return outstandingGrades;
	}

	
	public Set<RespondantScore> computeGraders(@NonNull Respondant respondant) {
		List<Grader> graders = graderService.getGradersByRespondantId(respondant.getId());	
		if (respondant.getRespondantStatus() == Respondant.STATUS_ADVUNGRADED) {
			// filter down responses to 2nd stage survey only.
			AccountSurvey as = accountSurveyService.getAccountSurveyById(respondant.getSecondStageSurveyId());
			Set<SurveyQuestion> questions = as.getSurvey().getSurveyQuestions();
			List<Grader> secondStageGraders = Lists.newArrayList();
			for (Grader grader : graders) {
				for (SurveyQuestion sq : questions) {
					if (sq.getQuestionId().equals(grader.getQuestionId())) {
						secondStageGraders.add(grader); 
						continue;
					}
				}
			}
			log.debug("Filtered respondant {} to {} second stage graders", respondant.getId(), secondStageGraders.size());
			graders = secondStageGraders;
		}	
			
		List<Grade> grades = new ArrayList<Grade>();

		for (Grader grader : graders) {
			if (grader.getStatus() == Grader.STATUS_IGNORED) continue;
		    grades.addAll(graderService.getGradesByGraderId(grader.getId()));
		    log.debug("Respondant {} has grader {} with {} cumulative total grades", respondant.getId(), grader.getId(), grades.size());
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
		return gradedScores;
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

}

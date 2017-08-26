package com.talytica.integration.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.CustomWorkflow;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.EmailService;
import com.talytica.integration.objects.GradingResult;
import com.talytica.integration.objects.PredictionResult;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PipelineService {

	@Autowired
	private RespondantService respondantService;
	@Autowired
	private ScoringService scoringService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private PredictionService predictionService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	
	public void preScreen(@NonNull Respondant respondant) {
		
		try {
			respondant.setRespondantStatus(Respondant.STATUS_CREATED);
			List<PredictionResult> results = predictionService.runPreAssessmentPredictions(respondant);
			if ((null != results) && (results.size() > 0)) {
				GradingResult grade = gradingService.gradeRespondantByPredictions(respondant, results);
				respondant.setCompositeScore(grade.getCompositeScore());
				respondant.setProfileRecommendation(grade.getRecommendedProfile());
				log.debug("Pre-screen completed for respondant {} with results: ", respondant.getId(), results);
				if ((respondant.getPartner() != null) && (respondant.getScorePostMethod()!=null)) {
					PartnerUtil pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
					pu.postScoresToPartner(respondant, pu.getScreeningMessage(respondant));
					log.debug("Posted results to: {}", respondant.getScorePostMethod());
				}
			}
			executePreScreenWorkflows(respondant);
			respondantService.save(respondant);
		} catch (Exception e) {
			log.warn("Failed to prescreen respondant {} {}", respondant.getId(), e);
		}

	}

	public void executePreScreenWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		PartnerUtil pu = null;
		for (CustomWorkflow workflow : workflows) {
			if ((workflow.getProfile() != null) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (CustomWorkflow.TRIGGER_POINT_CREATION == workflow.getTriggerPoint()) {
				switch (workflow.getType()) {
					case CustomWorkflow.TYPE_ATSUPDATE:
						if (respondant.getPartner() == null) break;
						pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
						pu.changeCandidateStatus(respondant, workflow.getAtsId());
						log.debug("WORKFLOW: Changed respondant {} status to {}", respondant.getId(), workflow.getText());
						break;
					case CustomWorkflow.TYPE_EMAIL:
						if (respondant.getPartner() != null) {
							pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
							pu.inviteCandidate(respondant);
						} else {
							emailService.sendEmailInvitation(respondant);												
						}
						log.debug("WORKFLOW: Sent email to respondant {}", respondant.getId());
						respondant.setRespondantStatus(Respondant.STATUS_INVITED);
						break;
					default:
						log.warn("WORKFLOW: No action at creation trigger point for: {}", workflow);
						break;
				}
			} else {
				log.debug("Different Trigger Point: {}", workflow.getTriggerPoint());
			}
		}	
	}
	
	public void scoreAssessment(@NonNull Respondant respondant) {	
		log.debug("Respondant {} has status = {} - not scored yet.", respondant.getId(), respondant.getRespondantStatus());
		Boolean stageTwo = (respondant.getRespondantStatus() >= Respondant.STATUS_ADVANCED);
		if (respondant.getType() == Respondant.TYPE_SAMPLE) {
			log.debug("Respondant Id#{} is a sample - will not score", respondant.getId());
			respondant.setRespondantStatus(Respondant.STATUS_PREDICTED); //skip the rest of the pipeline
			respondantService.save(respondant);
		} else {
			Boolean incomplete = scoringService.scoreAssessmentResponses(respondant);
			if (incomplete) {
				respondant.setRespondantStatus(Respondant.STATUS_UNGRADED);
				if (stageTwo) respondant.setRespondantStatus(Respondant.STATUS_ADVUNGRADED);
			} else {
				respondant.setRespondantStatus(Respondant.STATUS_SCORED);
				if (stageTwo) respondant.setRespondantStatus(Respondant.STATUS_ADVSCORESADDED);
			}
			respondantService.save(respondant);
			log.debug("{} Respondant scores saved for respondant {}", respondant.getRespondantScores().size(), respondant);				
		}
	}

	public void computeGrades(@NonNull Respondant respondant) {
		Boolean stageTwo = (respondant.getRespondantStatus() >= Respondant.STATUS_ADVANCED);
		log.debug("Respondant {} has status = {} - ready to grade.", respondant.getId(), respondant.getRespondantStatus());
		Set<RespondantScore> gradedScores = scoringService.computeGraders(respondant);
		respondant.setRespondantStatus(Respondant.STATUS_SCORED);
		if (stageTwo) respondant.setRespondantStatus(Respondant.STATUS_ADVSCORESADDED);
		respondant.getRespondantScores().addAll(gradedScores);
		respondantService.save(respondant);
	}

	public void predictRespondant(@NonNull Respondant respondant) {
		Boolean stageTwo = (respondant.getRespondantStatus() >= Respondant.STATUS_ADVANCED);
		log.debug("Respondant {} has status = {} - not predicted yet.", respondant.getId(), respondant.getRespondantStatus());
		if (respondant.getType() == Respondant.TYPE_BENCHMARK) {
			log.debug("Respondant Id#{} is a benchmark - will not predict", respondant.getId());
			respondant.setRespondantStatus(Respondant.STATUS_HIRED); 
			respondantService.save(respondant);
			return; //skip the rest of the pipeline
		}
		
		// Todo - deal with stage two predictions later (if we ever need them).	
		if ((!stageTwo) && (respondant.getPosition().getPositionPredictionConfigurations().size() >= 0)) {
			try {
				// Stage 1
				List<PredictionResult> predictions = predictionService.runPostAssessmentPredictions(respondant);
				// Stage 2
				GradingResult gradingResult = gradingService.gradeRespondantByPredictions(respondant, predictions);

				// Assimilate results, Update respondant lifecycle, and persist state
				respondant.setProfileRecommendation(gradingResult.getRecommendedProfile());
				respondant.setCompositeScore(gradingResult.getCompositeScore());
				respondant.setRespondantStatus(Respondant.STATUS_PREDICTED);
				Respondant savedRespondant = respondantService.save(respondant);
				sendNotifications(savedRespondant);
			} catch(Exception e) {
				log.warn("Transaction Rollback for assessment analysis. Failed to run predictions/grading for respondant " + respondant.getId(), e);
				throw e;
			}
		} else {
			// No predictions configured for this respondant... skip change status and break
			log.debug("Skipping prediction portion for respondant {}", respondant.getId());
			respondant.setRespondantStatus(Respondant.STATUS_PREDICTED);
			Respondant savedRespondant = respondantService.save(respondant);
			sendNotifications(savedRespondant);
		}
		log.debug("Assessment analysis for respondant {} complete", respondant.getId());
	}
	

	public void sendNotifications(Respondant respondant) {
		// Notifications Logic.
		Partner partner = respondant.getPartner();
		if (partner != null) {
			PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
			JSONObject message = pu.getScoresMessage(respondant);
			pu.postScoresToPartner(respondant, message);
		}

		if ((respondant.getEmailRecipient() != null) && (!respondant.getEmailRecipient().isEmpty())) {
			emailService.sendResults(respondant);
		}
	}

}

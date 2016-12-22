package com.talytica.integration.analytics;

import java.util.List;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.EmailService;
import com.talytica.integration.objects.GradingResult;
import com.talytica.integration.objects.PredictionResult;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AssessmentPipeline {

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

	public void initiateAssessmentAnalysis(@NonNull Respondant respondant) {
		log.debug("Assessment analysis requested for respondant {}", respondant.getId());

		if (respondant.getRespondantStatus() < Respondant.STATUS_SCORED) {
			log.debug("Respondant {} has status = {} - not scored yet.", respondant.getId(), respondant.getRespondantStatus());
			scoringService.scoreAssessment(respondant);
			log.debug("{} Respondant scores saved for respondant {}", respondant.getRespondantScores().size(), respondant);
		}

		if (respondant.getRespondantStatus() == Respondant.STATUS_SCORED) {

			log.debug("Respondant {} has status = {} - not predicted yet.", respondant.getId(), respondant.getRespondantStatus());
			if (respondant.getPosition().getPositionPredictionConfigurations().size() >= 0) {
				try {
					// Stage 1
					List<PredictionResult> predictions = predictionService.runPredictionsStageForAllTargets(respondant);
	
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

		return;
	}

	public void assessGraderFulfillmentAndScore(@NonNull Respondant respondant) {
		if (Respondant.STATUS_UNGRADED == respondant.getRespondantStatus()) {
			scoringService.scoreGraders(respondant);
		} else {
			log.warn("Respondant {} not eligible for grader assessment with status = {}", respondant.getId(), respondant.getRespondantStatus());
		}
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

package com.talytica.integration.triggers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.objects.GradingResult;
import com.talytica.integration.objects.PredictionResult;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.service.AssessmentPipelineService;
import com.talytica.integration.service.GradingService;
import com.talytica.integration.service.PredictionService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PreScreenPredictionScheduleTrigger {
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private PredictionService predictionService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	PartnerUtilityRegistry partnerUtilityRegistry;
	
	@Value(value = "${jobs.prescreenprediction.enabled:false}")
	private Boolean preScreenPredictionJobEnabled;

	@Scheduled(initialDelayString = "${jobs.prescreenprediction.trigger.init.seconds:60}000", fixedDelayString = "${jobs.prescreenprediction.trigger.delay.seconds:60}000")
	public void triggerEligibleRespondantSubmissionAnalysis() {
		if (preScreenPredictionJobEnabled) {

			List<Respondant> eligibleRespondants = respondantService.getAllRespondantsByStatus(Respondant.STATUS_PRESCREEN);
			
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to process for prescreen predictions.");
			} else {
				log.info("Scheduled trigger: Analyzing {} prescreen candidates", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					try {
						List<PredictionResult> results = predictionService.runPreAssessmentPredictions(respondant);
						GradingResult grade = gradingService.gradeRespondantByPredictions(respondant, results);
						respondant.setCompositeScore(grade.getCompositeScore());
						respondant.setProfileRecommendation(grade.getRecommendedProfile());
						respondant.setRespondantStatus(Respondant.STATUS_CREATED);
						Respondant updatedRespondant = respondantService.save(respondant);
						if ((updatedRespondant.getPartner() != null) && (updatedRespondant.getScorePostMethod()!=null)) {
							PartnerUtil pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
							pu.postScoresToPartner(updatedRespondant, pu.getScoresMessage(updatedRespondant));							
						}
					} catch (Exception e) {
						log.warn("Failed to process prescreen for respondant {}", respondant.getId(), e);
					}
				});
			}	
		}
	}
	
}

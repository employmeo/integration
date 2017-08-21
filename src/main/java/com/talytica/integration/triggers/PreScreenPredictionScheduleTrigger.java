package com.talytica.integration.triggers;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.CustomWorkflow;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.EmailService;
import com.talytica.integration.objects.GradingResult;
import com.talytica.integration.objects.PredictionResult;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.service.GradingService;
import com.talytica.integration.service.PredictionService;

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
	private EmailService emailService;
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
							List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
							log.debug("WORKFLOW: {} workflows found", workflows.size());
							Collections.sort(workflows);
							for (CustomWorkflow workflow : workflows) {
								if ((workflow.getProfile() != null) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
								if (CustomWorkflow.TRIGGER_POINT_CREATION == workflow.getTriggerPoint()) {
									switch (workflow.getType()) {
										case CustomWorkflow.TYPE_ATSUPDATE:
											PartnerUtil pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
											pu.changeCandidateStatus(respondant, workflow.getAtsId());
											log.debug("WORKFLOW: Changed respondant {} status to {}", respondant.getId(), workflow.getText());
											break;
										case CustomWorkflow.TYPE_EMAIL:
											emailService.sendEmailInvitation(respondant);
											log.debug("WORKFLOW: Email to respondant {}", respondant.getId());
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
						respondantService.save(respondant);
					} catch (Exception e) {
						log.warn("Failed to prescreen respondant {} {}", respondant.getId(), e);
					}
				});
			}	
		}
	}
	
}

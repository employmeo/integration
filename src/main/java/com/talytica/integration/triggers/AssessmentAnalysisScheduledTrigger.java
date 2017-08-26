package com.talytica.integration.triggers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.service.AssessmentPipelineService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Deprecated
public class AssessmentAnalysisScheduledTrigger {
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private AssessmentPipelineService assessmentPipeline;

	@Value(value = "${jobs.submissionanalysis.enabled:false}")
	private Boolean respondantSubmissionAnalysisJobEnabled;
	
	@Value(value = "${jobs.graderscoring.enabled:false}")
	private Boolean graderFulfilledScoringJobEnabled;

	//@Scheduled(initialDelayString = "${jobs.submissionanalysis.trigger.init.seconds:60}000", fixedDelayString = "${jobs.submissionanalysis.trigger.delay.seconds:60}000")
	public void triggerEligibleRespondantSubmissionAnalysis() {
		if (respondantSubmissionAnalysisJobEnabled) {

			List<Respondant> eligibleRespondants = respondantService.getAnalysisPendingRespondants();
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to process for submission analysis.");
			} else {
				log.info("Scheduled trigger: Analyzing {} survey submissions", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					try {
						assessmentPipeline.initiateAssessmentAnalysis(respondant);
					} catch (Exception e) {
						log.warn("Failed to process submission for respondant {}", respondant.getId(), e);
					}
				});
			}	
		}
	}

	//@Scheduled(initialDelayString = "${jobs.graderscoring.trigger.init.seconds:90}000", fixedDelayString = "${jobs.graderscoring.trigger.delay.seconds:900}000")
	public void triggerGraderFulfilledScoring() {
		if (graderFulfilledScoringJobEnabled) {

			List<Respondant> eligibleRespondants = respondantService.getGraderBasedScoringPendingRespondants();

			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to promote from grader fulfilled to scoring");
			} else {
				log.info("Scheduled trigger: Grading {} eligible respondants", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					try {
						log.debug("Assessing grader fulfillment for respondant: {}", respondant.getId());
						assessmentPipeline.assessGraderFulfillmentAndScore(respondant);
					} catch (Exception e) {
						log.warn("Failed to process respondant while promoting from ungraded to scored: {}",
								respondant.getId(), e);
					}
				});
			}
		}
	}
}

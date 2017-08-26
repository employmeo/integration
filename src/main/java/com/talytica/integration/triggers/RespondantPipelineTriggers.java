package com.talytica.integration.triggers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.service.PipelineService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RespondantPipelineTriggers {
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private PipelineService pipelineService;

	@Value(value = "${jobs.submissionanalysis.enabled:false}")
	private Boolean respondantSubmissionAnalysisJobEnabled;
	
	@Value(value = "${jobs.graderscoring.enabled:false}")
	private Boolean graderFulfilledScoringJobEnabled;

	@Value(value = "${jobs.prescreenprediction.enabled:false}")
	private Boolean preScreenPredictionJobEnabled;
	
	@Value(value = "${jobs.predictions.enabled:false}")
	private Boolean predictionScoringJobEnabled;

	@Scheduled(initialDelayString = "${jobs.prescreenprediction.trigger.init.seconds:60}000", fixedDelayString = "${jobs.prescreenprediction.trigger.delay.seconds:60}000")
	public void triggerRespondantPreScreen() {
		if (preScreenPredictionJobEnabled) {
			List<Respondant> eligibleRespondants = respondantService.getAllRespondantsByStatus(Respondant.STATUS_PRESCREEN);
			
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to process for prescreen predictions.");
			} else {
				log.info("Scheduled trigger: Analyzing {} prescreen candidates", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {	
						pipelineService.preScreen(respondant);
				});
			}	
		}
	}
		
	@Scheduled(initialDelayString = "${jobs.submissionanalysis.trigger.init.seconds:60}000", fixedDelayString = "${jobs.submissionanalysis.trigger.delay.seconds:60}000")
	public void triggerRespondantAssessmentScoring() {
		if (respondantSubmissionAnalysisJobEnabled) {
			List<Respondant> eligibleRespondants = respondantService.getAnalysisPendingRespondants();
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to process for submission analysis.");
			} else {
				log.info("Scheduled trigger: Analyzing {} survey submissions", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					try {
						pipelineService.scoreAssessment(respondant);
					} catch (Exception e) {
						log.warn("Failed to process submission for respondant {}", respondant.getId(), e);
					}
				});
			}	
		}
	}

	@Scheduled(initialDelayString = "${jobs.graderscoring.trigger.init.seconds:90}000", fixedDelayString = "${jobs.graderscoring.trigger.delay.seconds:900}000")
	public void triggerGraderCompute() {
		if (graderFulfilledScoringJobEnabled) {
			List<Respondant> eligibleRespondants = respondantService.getGraderBasedScoringPendingRespondants();
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to promote from grader fulfilled to scoring");
			} else {
				log.info("Scheduled trigger: Grading {} eligible respondants", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					try {
						log.debug("Assessing grader fulfillment for respondant: {}", respondant.getId());
						pipelineService.computeGrades(respondant);
					} catch (Exception e) {
						log.warn("Failed to process respondant while promoting from ungraded to scored: {}",
								respondant.getId(), e);
					}
				});
			}
		}
	}
	
	@Scheduled(initialDelayString = "${jobs.predictions.trigger.init.seconds:90}000", fixedDelayString = "${jobs.predictions.trigger.delay.seconds:900}000")
	public void triggerPredictions() {
		if (predictionScoringJobEnabled) {
			List<Respondant> eligibleRespondants = respondantService.getPredictionPendingRespondants();
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to predict");
			} else {
				log.info("Scheduled trigger: Predicting {} eligible respondants", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					try {
						log.debug("Predicting for respondant: {}", respondant.getId());
						pipelineService.predictRespondant(respondant);
					} catch (Exception e) {
						log.warn("Failed to process respondant while promoting from scored to predicted: {}",
								respondant.getId(), e);
					}
				});
			}
		}
	}
}

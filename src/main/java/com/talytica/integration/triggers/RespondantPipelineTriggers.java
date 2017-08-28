package com.talytica.integration.triggers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
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
				log.debug("Pipeline: No prescreen candidates.");
			} else {
				log.info("Pipeline: Analyzing {} prescreen candidates", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {	
					if (respondant.getErrorStatus()) {
						log.warn("Skipping prescreening for problem respondant: {}", respondant.getId());
						return;
					}
					try {
						pipelineService.preScreen(respondant);
					} catch (Exception e) {
						log.error("Failed to process prescreening for respondant {}", respondant.getId(), e);
						respondantService.markError(respondant);
					}											
				});
			}	
		}
	}
		
	@Scheduled(initialDelayString = "${jobs.submissionanalysis.trigger.init.seconds:60}000", fixedDelayString = "${jobs.submissionanalysis.trigger.delay.seconds:60}000")
	public void triggerRespondantAssessmentScoring() {
		if (respondantSubmissionAnalysisJobEnabled) {
			List<Respondant> eligibleRespondants = respondantService.getAnalysisPendingRespondants();
			if (eligibleRespondants.isEmpty()) {
				log.debug("Pipeline: No submissions for analysis.");
			} else {
				log.info("Pipeline: Analyzing {} survey submissions", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					if (respondant.getErrorStatus()) {
						log.warn("Skipping scoring for problem respondant: {}", respondant.getId());
						return;
					}
					try {
						pipelineService.scoreAssessment(respondant);
					} catch (Exception e) {
						log.error("Failed to process submission for respondant {}", respondant.getId(), e);
						respondantService.markError(respondant);
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
				log.debug("Pipeline: No graders to process.");
			} else {
				log.info("Pipeline: Grading {} eligible respondants", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					if (respondant.getErrorStatus()) {
						log.warn("Skipping grades for problem respondant: {}", respondant.getId());
						return;
					}
					try {
						log.debug("Grading for respondant: {}", respondant.getId());
						pipelineService.computeGrades(respondant);
					} catch (Exception e) {
						log.error("Failed to grade respondant: {}", respondant.getId(), e);
						respondantService.markError(respondant);
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
				log.debug("Pipeline: No eligible respondants to predict");
			} else {
				log.info("Pipeline Predicting {} eligible respondants", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {
					if (respondant.getErrorStatus()) {
						log.warn("Skipping prediction for problem respondant: {}", respondant.getId());
						return;
					}
					try {
						log.debug("Predicting for respondant: {}", respondant.getId());
						pipelineService.predictRespondant(respondant);
					} catch (Exception e) {
						log.warn("Failed to process respondant while promoting from scored to predicted: {}",
								respondant.getId(), e);
						respondantService.markError(respondant);
					}
				});
			}
		}
	}
}

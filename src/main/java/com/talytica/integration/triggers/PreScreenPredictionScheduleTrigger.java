package com.talytica.integration.triggers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.service.PipelineService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Deprecated
public class PreScreenPredictionScheduleTrigger {
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private PipelineService pipelineService;

	
	@Value(value = "${jobs.prescreenprediction.enabled:false}")
	private Boolean preScreenPredictionJobEnabled;

	//@Scheduled(initialDelayString = "${jobs.prescreenprediction.trigger.init.seconds:60}000", fixedDelayString = "${jobs.prescreenprediction.trigger.delay.seconds:60}000")
	public void triggerEligibleRespondantSubmissionAnalysis() {
		if (preScreenPredictionJobEnabled) {

			List<Respondant> eligibleRespondants = respondantService.getAllRespondantsByStatus(Respondant.STATUS_PRESCREEN);
			
			if (eligibleRespondants.isEmpty()) {
				log.debug("Scheduled trigger: No eligible respondants to process for prescreen predictions.");
			} else {
				log.info("Scheduled trigger: Analyzing {} prescreen candidates", eligibleRespondants.size());
				eligibleRespondants.forEach(respondant -> {	
					
					try {
						log.debug("Prescreening respondant: {}", respondant.getId());
						pipelineService.preScreen(respondant);
					} catch (Exception e) {
						log.error("Failed to grade respondant: {}", respondant.getId(), e);
						respondantService.markError(respondant);
					}			
				});
			}	
		}
	}
	
}

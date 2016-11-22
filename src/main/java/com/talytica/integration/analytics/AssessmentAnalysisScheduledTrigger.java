package com.talytica.integration.analytics;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AssessmentAnalysisScheduledTrigger {
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private AssessmentSubmissionPipeline assessmentSubmissionPipeline;

	@Scheduled(initialDelayString="${scheduled.assessment.trigger.init.seconds:60}000",
				fixedDelayString = "${scheduled.assessment.trigger.delay.seconds:60}000")
    private void triggerEligibleRespondantSubmissionAnalysis() {
        log.debug("Scheduled trigger: Assessing eligible respondant submissions");

        List<Respondant> eligibleRespondants = respondantService.getAnalysisPendingRespondants();

        if(eligibleRespondants.isEmpty()) {
        	log.debug("No eligible respondants to process for submission analysis.");
        } else {
        	eligibleRespondants.forEach(respondant -> {
        		try {
        			assessmentSubmissionPipeline.initiateAssessmentAnalysis(respondant);
        		} catch(Exception e) {
        			log.warn("Failed to process submission for respondant {}", respondant.getId(), e);
        		}
        	});
        }

    }
}

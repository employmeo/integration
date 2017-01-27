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
	private AssessmentPipeline assessmentPipeline;

	@Scheduled(initialDelayString="${scheduled.assessment.trigger.init.seconds:60}000",
				fixedDelayString = "${scheduled.assessment.trigger.delay.seconds:60}000")
    public void triggerEligibleRespondantSubmissionAnalysis() {
        log.debug("Scheduled trigger: Assessing eligible respondant submissions");

        List<Respondant> eligibleRespondants = respondantService.getAnalysisPendingRespondants();

        if(eligibleRespondants.isEmpty()) {
        	log.debug("No eligible respondants to process for submission analysis.");
        } else {
        	eligibleRespondants.forEach(respondant -> {
        		try {
        			assessmentPipeline.initiateAssessmentAnalysis(respondant);
        		} catch(Exception e) {
        			log.warn("Failed to process submission for respondant {}", respondant.getId(), e);
        		}
        	});
        }

    }

	@Scheduled(initialDelayString="${scheduled.grader.trigger.init.seconds:90}000",
			fixedDelayString = "${scheduled.grader.trigger.delay.seconds:900}000")
	public void triggerGraderFulfilledScoring() {
    log.debug("Scheduled trigger: Assessing eligible respondants whose graders are fulfilled for scoring");

    List<Respondant> eligibleRespondants = respondantService.getGraderBasedScoringPendingRespondants();

    if(eligibleRespondants.isEmpty()) {
    	log.debug("No eligible respondants to promote from grader fulfilled to scoring");
    } else {
    	eligibleRespondants.forEach(respondant -> {
    		try {
    			log.debug("Assessing grader fulfillment for respondant: {}", respondant);

    			assessmentPipeline.assessGraderFulfillmentAndScore(respondant);
    		} catch(Exception e) {
    			log.warn("Failed to process respondant while promoting from ungraded to scored: {}", respondant.getId(), e);
    		}
    	});
    }

}
}

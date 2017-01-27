package com.talytica.integration.applicanttracking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.talytica.integration.util.JazzPartnerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerApplicantTrackingScheduledTrigger {
	
	@Autowired
	private JazzPartnerUtil jazzPartnerUtil;
	

	/**
	 * Runs at server startup and then every 6 hours thereafter.
	 * Should put in a determinate schedule subsequently.
	 * 
	 * Presently, caters to only one account (specific to SalesRoad's integration of Jazz ATS)
	 */
	@Scheduled(initialDelayString="${scheduled.applicanttracking.jazz.trigger.init.seconds:10}000",
			fixedDelayString = "${scheduled.applicanttracking.jazz.trigger.delay.seconds:21600}000")
	public void trackJazzedApplicants() {
	    log.debug("Scheduled trigger (Jazz): Tracking partner applicants");
	    
	    // Supported account for now is SalesRoad, with apiKey as below 
	    final String SALESROAD_JAZZ_ACCOUNT_PRODUCTION_API_KEY = "qNBh5onDQGu00yeHA4dHW8Fxb4r9m9G9";
	    jazzPartnerUtil.orderNewCandidateAssessments(SALESROAD_JAZZ_ACCOUNT_PRODUCTION_API_KEY);	
	    
	    log.debug("Jazzed applicants assessment ordering complete.");
	}

}

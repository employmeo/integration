package com.talytica.integration.applicanttracking;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.PartnerApplicant;
import com.employmeo.data.service.PartnerService;
import com.talytica.integration.util.JazzPartnerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerApplicantTrackingScheduledTrigger {
	
	@Autowired
	private JazzPartnerUtil jazzPartnerUtil;
	
	@Autowired
	private PartnerService partnerService;

	@Scheduled(initialDelayString="${scheduled.applicanttracking.jazz.trigger.init.seconds:05}000",
			fixedDelayString = "${scheduled.applicanttracking.jazz.trigger.delay.seconds:120}000")
	public void trackJazzedApplicants() {
	    log.debug("Scheduled trigger (Jazz): Tracking partner applicants");
	    
	    List<PartnerApplicant> applicants = jazzPartnerUtil.fetchPartnerApplicants(null, null);	
	    
	    partnerService.saveNewApplicants(applicants);
	    
	    log.debug("Jazzed partner applicants fetch complete.");
	}

}

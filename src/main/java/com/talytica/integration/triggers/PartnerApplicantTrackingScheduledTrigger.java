package com.talytica.integration.triggers;


import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.talytica.integration.partners.JazzPolling;
import com.talytica.integration.partners.jazz.JazzApplicantPollConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerApplicantTrackingScheduledTrigger {

	@Autowired
	private JazzPolling jazzPolling;

	@Value(value = "${partners.jazz.polling.enabled:false}")
	private Boolean jazzPartnerTrackingJobEnabled;

	/**
	 * Runs polling task(s) every 180 minutes.
	 * Presently, caters to only one account 
	 */
	@Scheduled(initialDelayString = "${jobs.frequent.trigger.init.seconds:1800}000", fixedDelayString = "${jobs.frequent.trigger.delay.seconds:10800}000")
	public void frequentPolling() {
		if (jazzPartnerTrackingJobEnabled) {
			log.info("Scheduled Frequent Polling Launched");
			Set<JazzApplicantPollConfiguration> configs = jazzPolling.getFrequentPollingConfigs();
			for (JazzApplicantPollConfiguration config : configs) {
				jazzPolling.pollJazzApplicants(config);
			}
		} else {
			log.info("Jazz frequent polling disabled.");				
		}	
	}
	
	/**
	 * Runs polling task(s) every day.
	 * Presently, caters to only one account 
	 */
	@Scheduled(initialDelayString = "${jobs.daily.trigger.init.seconds:3600}000", fixedDelayString = "${jobs.daily.trigger.delay.seconds:86400}000")
	public void dailyPolling() {
		if (jazzPartnerTrackingJobEnabled) {
			log.info("Scheduled Daily Jazz Polling Launched");
			Set<JazzApplicantPollConfiguration> configs = jazzPolling.getDailyPollingConfigs();
			for (JazzApplicantPollConfiguration config : configs) {
				if ("hired".equalsIgnoreCase(config.getStatus())) {
					jazzPolling.pollJazzHires(config);
				} else {
					jazzPolling.pollJazzApplicantsByStatus(config);		
				}
			} 
		} else {
			log.info("Jazz Daily polling disabled.");				
		}
	}
		


}

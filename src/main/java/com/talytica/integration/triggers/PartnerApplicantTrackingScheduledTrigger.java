package com.talytica.integration.triggers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.service.AccountService;
import com.google.common.collect.Sets;
import com.talytica.integration.objects.JazzApplicantPollConfiguration;
import com.talytica.integration.partners.JazzPolling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerApplicantTrackingScheduledTrigger {

	@Autowired
	private JazzPolling jazzPolling;
	
	@Autowired
	private AccountService accountService;

	@Value(value = "${jobs.applicanttracking.jazz.enabled:false}")
	private Boolean jazzPartnerTrackingJobEnabled;

	/**
	 * Runs polling task(s) every 180 minutes.
	 * Presently, caters to only one account 
	 */
	@Scheduled(initialDelayString = "${frequent.polling.trigger.init.seconds:1800}000", fixedDelayString = "${frequent.polling.trigger.delay.seconds:10800}000")
	public void frequentPolling() {
		log.info("Scheduled Frequent Polling Launched");
		Set<JazzApplicantPollConfiguration> configs = getFrequentPollingConfigs();
		for (JazzApplicantPollConfiguration config : configs) {
			if (jazzPartnerTrackingJobEnabled) jazzPolling.pollJazzApplicants(config);
				
			//jazzPolling.pollJazzApplicantsByStatus(getSalesRoadsInterviewConfig());
			//jazzPolling.pollJazzHires(getSalesRoadsHiredConfig());
			
			log.info("Jazz applicants assessment ordering complete.");
		} 
	}

	private Set<JazzApplicantPollConfiguration> getFrequentPollingConfigs() {
		Set<JazzApplicantPollConfiguration> configs = Sets.newHashSet();
		configs.add(getSalesRoadsApplicantsConfig());
		return configs;
	}
	
	/**
	 * Runs polling task(s) every 180 minutes.
	 * Presently, caters to only one account 
	 */
	@Scheduled(initialDelayString = "${daily.polling.trigger.init.seconds:3600}000", fixedDelayString = "${daily.polling.trigger.delay.seconds:86400}000")
	public void dailyPolling() {
		log.info("Scheduled Daily Polling Launched");
		Set<JazzApplicantPollConfiguration> configs = getDailyPollingConfigs();
		for (JazzApplicantPollConfiguration config : configs) {
			if (jazzPartnerTrackingJobEnabled) {
				if ("hired".equalsIgnoreCase(config.getStatus())) jazzPolling.pollJazzHires(config);
				if ("invited".equalsIgnoreCase(config.getStatus())) jazzPolling.pollJazzApplicantsByStatus(config);				
			} else {
				log.info("Jazz applicants daily polling disabled.");				
			}
			log.info("Jazz applicants daily status updating complete.");
		} 
	}
	
	private Set<JazzApplicantPollConfiguration> getDailyPollingConfigs() {
		Set<JazzApplicantPollConfiguration> configs = Sets.newHashSet();
		configs.add(getSalesRoadsInterviewConfig());
		configs.add(getSalesRoadsHiredConfig());
		return configs;
	}
	
	private JazzApplicantPollConfiguration getSalesRoadsApplicantsConfig() {
		JazzApplicantPollConfiguration config = new JazzApplicantPollConfiguration();
		Account salesRoads = accountService.getAccountByName("Sales Roads");
		config.setAccount(salesRoads);

		Range<Date> lookbackPeriod = getRangeInDaysFromToday(1);
		final SimpleDateFormat JazzDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		config.setLookbackBeginDate(JazzDateFormat.format(lookbackPeriod.getLowerBound()));
		config.setLookbackEndDate(JazzDateFormat.format(lookbackPeriod.getUpperBound()));
		config.setSendEmail(Boolean.FALSE);

		return config;
	}

	private JazzApplicantPollConfiguration getSalesRoadsInterviewConfig() {
		JazzApplicantPollConfiguration config = new JazzApplicantPollConfiguration();
		Account salesRoads = accountService.getAccountByName("Sales Roads");
		config.setAccount(salesRoads);
		config.setWorkFlowIds(Arrays.asList("2827415", "2886659", "2827450", "2878899", "2878947"));

		Range<Date> lookbackPeriod = getRangeInDaysFromToday(90);
		final SimpleDateFormat JazzDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		config.setLookbackBeginDate(JazzDateFormat.format(lookbackPeriod.getLowerBound()));
		config.setLookbackEndDate(JazzDateFormat.format(lookbackPeriod.getUpperBound()));
		config.setStatus("invited");
		config.setSendEmail(Boolean.FALSE);

		return config;
	}

	private JazzApplicantPollConfiguration getSalesRoadsHiredConfig() {
		JazzApplicantPollConfiguration config = new JazzApplicantPollConfiguration();
		Account salesRoads = accountService.getAccountByName("Sales Roads");
		config.setAccount(salesRoads);

		Range<Date> lookbackPeriod = getRangeInDaysFromToday(90);
		final SimpleDateFormat JazzDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		config.setLookbackBeginDate(JazzDateFormat.format(lookbackPeriod.getLowerBound()));
		config.setLookbackEndDate(JazzDateFormat.format(lookbackPeriod.getUpperBound()));
		config.setStatus("hired");
		config.setSendEmail(Boolean.FALSE);

		return config;
	}
	
	
	private Range<Date> getRangeInDaysFromToday(int numDays) {
		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();

		cal.add(Calendar.DAY_OF_MONTH, -numDays);
		Date beginDate = cal.getTime();

		Range<Date> window = new Range<Date>(beginDate, endDate);
		return window;
	}

}

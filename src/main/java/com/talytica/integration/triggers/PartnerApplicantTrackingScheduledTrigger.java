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

	@Value(value = "${partners.jazz.polling.enabled:false}")
	private Boolean jazzPartnerTrackingJobEnabled;

	/**
	 * Runs polling task(s) every 180 minutes.
	 * Presently, caters to only one account 
	 */
	@Scheduled(initialDelayString = "${jobs.frequent.trigger.init.seconds:1800}000", fixedDelayString = "${jobs.frequent.trigger.delay.seconds:10800}000")
	public void frequentPolling() {
		Set<JazzApplicantPollConfiguration> configs = getFrequentPollingConfigs();
		if (jazzPartnerTrackingJobEnabled) {
			log.info("Scheduled Frequent Polling Launched");
			for (JazzApplicantPollConfiguration config : configs) {
				jazzPolling.pollJazzApplicants(config);
			}
		} else {
			log.info("Jazz frequent polling disabled.");				
		}	
	}

	private Set<JazzApplicantPollConfiguration> getFrequentPollingConfigs() {
		Set<JazzApplicantPollConfiguration> configs = Sets.newHashSet();
		configs.add(getSalesRoadsApplicantsConfig());
		return configs;
	}
	
	/**
	 * Runs polling task(s) every day.
	 * Presently, caters to only one account 
	 */
	@Scheduled(initialDelayString = "${jobs.daily.trigger.init.seconds:3600}000", fixedDelayString = "${jobs.daily.trigger.delay.seconds:86400}000")
	public void dailyPolling() {
		Set<JazzApplicantPollConfiguration> configs = getDailyPollingConfigs();
		if (jazzPartnerTrackingJobEnabled) {
			log.info("Scheduled Daily Jazz Polling Launched");
			for (JazzApplicantPollConfiguration config : configs) {
				if ("hired".equalsIgnoreCase(config.getStatus())) jazzPolling.pollJazzHires(config);
				if ("invited".equalsIgnoreCase(config.getStatus())) jazzPolling.pollJazzApplicantsByStatus(config);				
			} 
		} else {
			log.info("Jazz Daily polling disabled.");				
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

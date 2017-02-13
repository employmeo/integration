package com.talytica.integration.applicanttracking;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Range;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.talytica.integration.objects.JazzApplicantPollConfiguration;
import com.talytica.integration.util.JazzPartnerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerApplicantTrackingScheduledTrigger {

	@Value("${jobs.applicanttracking.jazz.schedule.days}")
	Integer DAYSFROMTODAY;
	
	@Autowired
	private JazzPartnerUtil jazzPartnerUtil;

	@Value(value = "${jobs.applicanttracking.jazz.enabled:false}")
	private Boolean jazzPartnerTrackingJobEnabled;

	/**
	 * Runs at 8:00am, 1:00pm and 6:00pm every weekday on Eastern timezone.
	 * 
	 * Presently, caters to only one account (specific to SalesRoad's
	 * integration of Jazz ATS)
	 */
	@Scheduled(cron = "${jobs.applicanttracking.jazz.schedule.cron}", zone = "${jobs.applicanttracking.jazz.schedule.timezone}")
	//@ConditionalOnProperty(name="jobs.applicanttracking.jazz.enabled") 
	public void trackJazzedApplicants() {
		if (jazzPartnerTrackingJobEnabled) {
			log.info("Scheduled trigger (Jazz): Tracking partner applicants");

			// Supported account for now is SalesRoad, with specific configs as
			// below
			JazzApplicantPollConfiguration pollConfiguration = getSalesRoadSpecificPollConfiguration();
			jazzPartnerUtil.orderNewCandidateAssessments(pollConfiguration);

			log.info("Jazzed applicants assessment ordering complete.");
		} 
	}

	private JazzApplicantPollConfiguration getSalesRoadSpecificPollConfiguration() {
		JazzApplicantPollConfiguration config = new JazzApplicantPollConfiguration();
		config.setApiKey("qNBh5onDQGu00yeHA4dHW8Fxb4r9m9G9");
		config.setWorkFlowIds(Arrays.asList("2827415", "2886659", "2827450", "2878899", "2878947"));

		Range<Date> lookbackPeriod = getRangeInDaysFromToday(DAYSFROMTODAY);
		final SimpleDateFormat JazzDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		config.setLookbackBeginDate(JazzDateFormat.format(lookbackPeriod.getLowerBound()));
		config.setLookbackEndDate(JazzDateFormat.format(lookbackPeriod.getUpperBound()));
		config.setSendEmail(Boolean.FALSE);

		log.info("JazzApplicantPollConfiguration: {}", config);
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

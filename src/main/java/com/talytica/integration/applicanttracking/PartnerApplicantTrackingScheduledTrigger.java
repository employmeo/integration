package com.talytica.integration.applicanttracking;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.talytica.integration.objects.JazzApplicantPollConfiguration;
import com.talytica.integration.util.JazzPartnerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerApplicantTrackingScheduledTrigger {
	
	@Autowired
	private JazzPartnerUtil jazzPartnerUtil;

	

	/**
	 * Runs at 8:00am, 1:00pm and 6:00pm every weekday on Eastern timezone.
	 * 
	 * Presently, caters to only one account (specific to SalesRoad's integration of Jazz ATS)
	 */
	@Scheduled(cron="0 0 8,13,18 ? * MON-FRI", zone="America/New_York")
	public void trackJazzedApplicants() {
	    log.debug("Scheduled trigger (Jazz): Tracking partner applicants");
	    
	    // Supported account for now is SalesRoad, with apiKey as below 
	    JazzApplicantPollConfiguration pollConfiguration = getSalesRoadSpecificPollConfiguration();
	    jazzPartnerUtil.orderNewCandidateAssessments(pollConfiguration);	
	    
	    log.debug("Jazzed applicants assessment ordering complete.");
	}
	
	private JazzApplicantPollConfiguration getSalesRoadSpecificPollConfiguration() {
		JazzApplicantPollConfiguration config = new JazzApplicantPollConfiguration();
		config.setApiKey("qNBh5onDQGu00yeHA4dHW8Fxb4r9m9G9");
		config.setWorkFlowIds(Arrays.asList("2827415","2886659","2827450","2878899","2878947"));
		
		Range<Date> lookbackPeriod = getRangeInDaysFromToday(45);
		final SimpleDateFormat JazzDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		config.setLookbackBeginDate(JazzDateFormat.format(lookbackPeriod.getLowerBound()));
		config.setLookbackEndDate(JazzDateFormat.format(lookbackPeriod.getUpperBound()));
		
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

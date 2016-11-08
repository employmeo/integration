package com.talytica.services.survey;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.employmeo.objects.Partner;
import com.employmeo.objects.Respondant;
import com.employmeo.util.EmailUtility;
import com.employmeo.util.PartnerUtil;
import com.employmeo.util.PredictionUtil;

@Path("submitassessment")
public class SubmitAssessment {
	private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();
	private static final Logger log = LoggerFactory.getLogger(SubmitAssessment.class);

	@PermitAll
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String doPost(
			// @FormParam("finish_time") TimeStamp finishTime,
			@FormParam("respondant_id") Long respondantId) {
		log.debug("Survey Submitted for Respondant: " + respondantId);

		Respondant respondant = Respondant.getRespondantById(respondantId);
		if (respondant.getRespondantStatus() < Respondant.STATUS_COMPLETED) {
			respondant.setRespondantStatus(Respondant.STATUS_COMPLETED);
			respondant.setRespondantFinishTime(new Timestamp(new Date().getTime()));
			respondant.mergeMe();
		}

		postScores(respondant.getRespondantId());

		return respondant.getJSONString();
	}

	private static void postScores(Long respondantId) {
		TASK_EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				
				Respondant respondant = Respondant.getRespondantById(respondantId);
				// Check if integrated:
				Partner partner = respondant.getPartner();
				if (partner != null) {
					PartnerUtil pu = partner.getPartnerUtil();
					JSONObject message = pu.getScoresMessage(respondant);
					pu.postScoresToPartner(respondant, message);
				} else {
					respondant.getAssessmentScore();
					PredictionUtil.predictRespondant(respondant);
				}

				if (respondant.getRespondantEmailRecipient() != null
						&& !respondant.getRespondantEmailRecipient().isEmpty()) {
					EmailUtility.sendResults(respondant);
				}
			}
		});
	}
}
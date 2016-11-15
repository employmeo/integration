package com.talytica.integration.analytics;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.objects.GradingResult;
import com.talytica.integration.objects.PredictionResult;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AssessmentSubmissionPipeline {

	@Autowired
	private RespondantService respondantService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private PredictionService predictionService;

	public void initiateAssessmentAnalysis(@NonNull Respondant respondant) {
		log.debug("Assessment analysis requested for respondant {}", respondant.getId());

		if (respondant.getRespondantStatus() <= Respondant.STATUS_SCORED) {
			//respondant = refresh(respondant);
			log.debug("Respondant {} has status = {}, no predictions have been run yet.", respondant.getId(), respondant.getRespondantStatus());
		}

		if (respondant.getRespondantStatus() == Respondant.STATUS_SCORED) {
			//DBUtil.beginTransaction();

			try {
				// Stage 1
				List<PredictionResult> predictions = predictionService.runPredictionsStageForAllTargets(respondant);

				// Stage 2
				GradingResult gradingResult = gradingService.gradeRespondantByPredictions(respondant, predictions);

				// Assimilate results, Update respondant lifecycle, and persist state
				respondant.setProfileRecommendation(gradingResult.getRecommendedProfile());
				respondant.setCompositeScore(gradingResult.getCompositeScore());
				respondant.setRespondantStatus(Respondant.STATUS_PREDICTED);

				Respondant savedRespondant = respondantService.save(respondant);

				//respondant.mergeMe();

				//DBUtil.commit();
			} catch(Exception e) {
				log.warn("Transaction Rollback for assessment analysis. Failed to run predictions/grading for respondant " + respondant.getId(), e);
				//DBUtil.rollback();
				throw e;
			}

			log.debug("Assessment analysis for respondant {} complete", respondant.getId());
		}

		return;
	}

}

package com.talytica.integration.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.ScoringScale;
import com.talytica.integration.objects.*;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO: Determine Mechanics of grading
 *
 * @author NShah
 *
 */
@Slf4j
@Service
@Transactional
public class GradingService {


	/**
	 * TODO: Implement grading logic
	 * For now, doing a simple mean score from all prediction scores and looking at a hypothetical static grade curve.
	 *
	 * @param respondant
	 * @param predictions
	 * @return grading result
	 */
	public GradingResult gradeRespondantByPredictions(Respondant respondant, List<PredictionResult> predictions) {
		log.debug("Initiating grading for respondant {} with {} predictions", respondant.getId(), predictions.size());

		GradingResult result = new GradingResult();
		Double compositeScore = null;
		
		if (predictions.size() == 0) {
			Set<RespondantScore> scores = respondant.getRespondantScores();
			Double averageScore = scores.stream()
					.mapToDouble(score -> (null != score.getValue()) ? score.getValue() * 9 : 0.0D)
					.average()
					.orElse(0.0D);
			compositeScore = new BigDecimal(averageScore).setScale(2, RoundingMode.HALF_UP).doubleValue();
		} else {
			// compute grade composite score as average of the percentiles * 100
			Double averagePercentile = predictions.stream()
					.mapToDouble(p -> (null != p.getPercentile()) ? p.getPercentile() * 100 : 0.0D)
					.average()
					.orElse(0.0D);
			compositeScore = new BigDecimal(averagePercentile).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		
		ScoringScale scale = respondant.getPosition().getScoringScale();

		result.setCompositeScore(compositeScore);
		result.setRecommendedProfile(scale.getProfile(compositeScore));

		log.debug("Grade results for respondant {} determined as {}", respondant.getId(), result);
		return result;

	}
}

package com.talytica.integration.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.employmeo.data.model.PositionProfile;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.google.common.collect.Range;
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

	private static final Range<Double> gradeCurveProfileD = Range.closed(0.0D, 40.0D);
	private static final Range<Double> gradeCurveProfileC = Range.closed(40.001D, 65.00D);
	private static final Range<Double> gradeCurveProfileB = Range.closed(65.001D, 90.00D);
	private static final Range<Double> gradeCurveProfileA = Range.closed(90.001D, 100.0D);


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
		
		if(gradeCurveProfileD.contains(compositeScore)) {
			result.setRecommendedProfile(PositionProfile.PROFILE_D);
		} else if (gradeCurveProfileC.contains(compositeScore)) {
			result.setRecommendedProfile(PositionProfile.PROFILE_C);
		} else if (gradeCurveProfileB.contains(compositeScore)) {
			result.setRecommendedProfile(PositionProfile.PROFILE_B);
		} else {
			result.setRecommendedProfile(PositionProfile.PROFILE_A);
		}

		result.setCompositeScore(compositeScore);

		log.debug("Grade results for respondant {} determined as {}", respondant.getId(), result);
		return result;

	}
}

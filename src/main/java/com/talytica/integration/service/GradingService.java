package com.talytica.integration.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.ScoringScale;
import com.employmeo.data.service.CorefactorService;
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

	@Autowired
	CorefactorService corefactorService;

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
		Double compositeScore = 0d;
		
		if (predictions.size() == 0) {
			Set<RespondantScore> scores = respondant.getRespondantScores();
			Double baseline = 0d;
			Double total = 0d;
			for (RespondantScore score : scores) {
				Corefactor cf = corefactorService.findCorefactorById(score.getId().getCorefactorId());
				Double coeff = (null != cf.getDefaultCoefficient()) ? cf.getDefaultCoefficient() : 1d;
				total += coeff * (score.getValue()-cf.getLowValue());
				baseline += coeff * (cf.getHighValue()-cf.getLowValue());
			}
			if (baseline > 0) compositeScore = new BigDecimal(100d*total/baseline).setScale(2, RoundingMode.HALF_UP).doubleValue();
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

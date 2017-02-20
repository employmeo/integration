package com.talytica.integration.analytics;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.service.PredictionModelService;
import com.talytica.integration.objects.CorefactorScore;
import com.talytica.integration.objects.PredictionResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class SimpleLinearRegressionEngine implements PredictionModelEngine {

	@Autowired
	private PredictionModelService predictionModelService;

	private static final Double DEFAULT_EXPONENT = 1.0D;
	private static final Double DEFAULT_COEFFICIENT = 1.0D;
	private static final Double DEFAULT_INTERCEPT = 0.0D;

	private PredictionModel model;
	private List<LinearRegressionConfig> modelConfigs;
	private NormalDistribution normalDistribution;

	public SimpleLinearRegressionEngine() {
		log.info("New linear regression prediction model instantiated");
	}
	
	@Override
	public void initialize(PredictionModel model) {
		log.info("Initializing ..");

		this.model = model;
		log.info("New linear regression model instantiated for {} (ID: {})", model.getName(), model.getModelId());
		modelConfigs = predictionModelService.getLinearRegressionConfiguration(getModelId());

		log.info("Initialization complete.");
	}

	@Override
	public PredictionResult runPredictions(Respondant respondant, PositionPredictionConfiguration posConfig, Location location,
			List<CorefactorScore> corefactorScores) {
		log.debug("Running predictions for {}", respondant.getId());

		PredictionResult prediction = new PredictionResult();
		Double targetOutcomeScore = evaluate(corefactorScores);
		
		normalDistribution = new NormalDistribution(posConfig.getMean(),posConfig.getStDev());
		Double percentile = normalDistribution.cumulativeProbability(targetOutcomeScore);
		
		prediction.setScore(targetOutcomeScore);
		prediction.setPercentile(percentile);

		log.info("Prediction outcome for respondant {} is {}", respondant.getId(), targetOutcomeScore);
		return prediction;
	}

	public Double evaluate(List<CorefactorScore> corefactorScores) {
			Double scoreSigma = 0.0D;
			for(LinearRegressionConfig config : modelConfigs) {
				if(config.getConfigType() == LinearRegressionConfigType.INTERCEPT) {
					scoreSigma +=  getInterceptScore(config);
				} else if(config.getConfigType() == LinearRegressionConfigType.COEFFICIENT) {
					scoreSigma +=  getCorefactorComponentScore(config, corefactorScores);
				}
				log.debug("Revised score sigma {}", scoreSigma);
			}
			return scoreSigma;
	}

	private Double getInterceptScore(LinearRegressionConfig config) {
		log.debug("Fetching intercept score from config {}", config);
		Double intercept = (null == config.getCoefficient()) ? DEFAULT_INTERCEPT : config.getCoefficient();

		log.debug("Intercept score is {}", intercept);
		return intercept;

	}

	private Double getCorefactorComponentScore(LinearRegressionConfig config, List<CorefactorScore> corefactorScores) {
		Double componentScore = 0.0D;

		Optional<CorefactorScore> corefactorScore = findCorefactorScore(config.getCorefactorId(), corefactorScores);
		if(corefactorScore.isPresent()) {
			Double corefactorScoreValue = corefactorScore.get().getScore();
			Double exponent = (null == config.getExponent()) ? DEFAULT_EXPONENT : config.getExponent();
			Double coefficient = (null == config.getCoefficient()) ? DEFAULT_COEFFICIENT : config.getCoefficient();

			log.debug("Evaluating for corefactor {}: {} * {}^{}", corefactorScore.get(), coefficient, corefactorScoreValue, exponent);
			componentScore = (coefficient * (Math.pow(corefactorScoreValue, exponent)));

			log.debug("Corefactor {} component score = {}", corefactorScore.get(), componentScore);
		} else {
			if(config.getRequired()) {
				throw new IllegalStateException("Corefactor scores for corefactorId " + config.getCorefactorId() + " required for this model, but not present");
			} else {
				log.debug("Corefactor score not available, but optional and hence bypassed. CorefactorId = " + config.getCorefactorId());
			}
		}

		return componentScore;
	}
	
	private Optional<CorefactorScore> findCorefactorScore(Long corefactorId, List<CorefactorScore> corefactorScores) {
		return corefactorScores.stream().filter(cfs -> corefactorId.equals(cfs.getCorefactor().getId())).findFirst();
	}

	@Override
	public String getModelName() {
		return this.model.getName();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "::" + getModelName();
	}

	@Override
	public Long getModelId() {
		return this.model.getModelId();
	}

	@Override
	public String getModelType() {
		return this.model.getModelTypeValue();
	}

}

package com.employmeo.util;

import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.employmeo.objects.CorefactorScore;
import com.employmeo.objects.LinearRegressionConfig;
import com.employmeo.objects.LinearRegressionConfig.ConfigType;
import com.employmeo.objects.Location;
import com.employmeo.objects.Position;
import com.employmeo.objects.Respondant;

public class SimpleLinearRegressionEngine implements PredictionModelEngine<LinearRegressionModelConfiguration> {

	private static final Logger log = LoggerFactory.getLogger(SimpleLinearRegressionEngine.class);
	
	private static final Double DEFAULT_EXPONENT = 1.0D;
	private static final Double DEFAULT_COEFFICIENT = 1.0D;
	private static final Double DEFAULT_INTERCEPT = 0.0D;
	
	private String modelName;
	private LinearRegressionModelConfiguration modelConfig;
	NormalDistribution normalDistribution;
	
	public SimpleLinearRegressionEngine(String modelName) {
		this.modelName = modelName;
		
		log.info("New linear regression prediction model instantiated for " + modelName);
	}
	
	public void initialize() {
		log.info("Initializing ..");
		
		modelConfig = ModelUtil.getLinearRegressionConfiguration(getModelName());
		
		if(Double.compare(modelConfig.getMean(), 0.0D) <= 0) {
			throw new IllegalStateException("Mean value of linear regression model not specified, invalid model configuration for model " + getModelName());
		}
		if(Double.compare(modelConfig.getStdDev(), 0.0D) <= 0) {
			throw new IllegalStateException("Std Dev value of linear regression model not specified, invalid model configuration for model " + getModelName());
		}	
		
		RandomGenerator rndGen = null; // we don't need sample results
		normalDistribution = new NormalDistribution(rndGen, modelConfig.getMean(), modelConfig.getStdDev());
		
		log.info("Initialization complete.");
	}

	@Override
	public PredictionResult runPredictions(Respondant respondant, Position position, Location location,
			List<CorefactorScore> corefactorScores) {
		log.debug("Running predictions for {}", respondant.getRespondantId());

		PredictionResult prediction = new PredictionResult();
		Double targetOutcomeScore = evaluate(corefactorScores);
		Double percentile = getPercentile(targetOutcomeScore);
		prediction.setScore(targetOutcomeScore);
		prediction.setPercentile(percentile);
		
		log.info("Prediction outcome for respondant {} is {}", respondant.getRespondantId(), targetOutcomeScore);
		return prediction;
	}
	
	public Double evaluate(List<CorefactorScore> corefactorScores) {
			Double scoreSigma = 0.0D;
			for(LinearRegressionConfig config : modelConfig.getConfigEntries()) {
				if(config.getConfigType() == ConfigType.INTERCEPT) {
					scoreSigma +=  getInterceptScore(config);
				} else if(config.getConfigType() == ConfigType.COEFFICIENT) {
					scoreSigma +=  getCorefactorComponentScore(config, corefactorScores);
				}
				log.debug("Revised score sigma {}", scoreSigma);
			}
			return scoreSigma;
	}
	
	private Double getPercentile(Double score) {	
		Double cumulativeProbability = normalDistribution.cumulativeProbability(score);

		log.debug("Normal distribution cumulative probability with mean {} and stdDev {} for score {}  is {}", normalDistribution.getMean(), normalDistribution.getStandardDeviation(), score, cumulativeProbability);
		return cumulativeProbability;
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
				throw new IllegalStateException("Corefactor scores for corefactorId " + config.getCorefactorId() + " are required for this model, but not available.");
			} else {
				log.debug("Corefactor score not available, but optional and hence bypassed. CorefactorId = " + config.getCorefactorId());
			}
		}
		
		return componentScore;
	}
	
	private Optional<CorefactorScore> findCorefactorScore(Integer corefactorId, List<CorefactorScore> corefactorScores) {
		return corefactorScores.stream().filter(cfs -> corefactorId.equals(cfs.getCorefactor().getCorefactorId())).findFirst();
	}


	@Override
	public String getModelName() {
		return this.modelName;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "::" + getModelName();
	}

	@Override
	public LinearRegressionModelConfiguration getModelConfiguration() {
		return this.modelConfig;
	}
}

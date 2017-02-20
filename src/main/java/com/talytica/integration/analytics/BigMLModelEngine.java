package com.talytica.integration.analytics;

import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.talytica.integration.objects.CorefactorScore;
import com.talytica.integration.objects.PredictionResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class BigMLModelEngine implements PredictionModelEngine<BigMLModelConfiguration> {

	@Value("${com.talytica.apis.bigml.name}")
	private String userName;
	@Value("${com.talytica.apis.bigml.key}")
	private String apiKey;
	@Value("${com.talytica.apis.bigml.devmode:true}")
	private Boolean devMode;
	
	@Autowired
	private ModelService modelService;
	
	private String modelName;
	private BigMLModelConfiguration modelConfig;
	
	private NormalDistribution normalDistribution;

	public BigMLModelEngine() {
		log.info("New Big ML prediction model instantiated");
	}
	
	public BigMLModelEngine(String modelName) {
		this.modelName = modelName;

		log.info("New Big ML prediction model instantiated for " + modelName);
	}

	@Override
	public void initialize(String modelName) {
		log.info("Initializing ..");
		modelConfig = modelService.getBigMLModelConfiguration(getModelName());
		
		this.modelName = modelName;
		log.info("New Big ML prediction model instantiated for " + modelName);

		log.info("Initialization complete.");
	}

	@Override
	public PredictionResult runPredictions(Respondant respondant, Position position, Location location,
			List<CorefactorScore> corefactorScores) {
		log.debug("Running predictions for {}", respondant.getId());
		
		PredictionResult prediction = new PredictionResult();
		try {
			BigMLClient api = BigMLClient.getInstance(userName,apiKey,devMode);
			JSONObject args = null;
			JSONObject model = api.getModel(modelConfig.getModelId());
			JSONObject inputData = new JSONObject();
			for (CorefactorScore cs : corefactorScores) {
				inputData.put(cs.getCorefactor().getName(), cs.getScore());
			}
			JSONObject pred = api.createPrediction((String)model.get("resource"), inputData, true, args, null, null);
			pred = api.getPrediction(pred);
			
		} catch (Exception e) {
			log.error("Prediction failed for {}, with exception {}", respondant.getId(), e);
		}
		
		Double targetOutcomeScore = 0d;
		Double percentile = getPercentile(targetOutcomeScore);
		prediction.setScore(targetOutcomeScore);
		prediction.setPercentile(percentile);

		log.info("Prediction outcome for respondant {} is {}", respondant.getId(), targetOutcomeScore);
		return prediction;
	}


	private Double getPercentile(Double score) {
		Double cumulativeProbability = normalDistribution.cumulativeProbability(score);

		log.debug("Normal distribution cumulative probability with mean {} and stdDev {} for score {}  is {}", normalDistribution.getMean(), normalDistribution.getStandardDeviation(), score, cumulativeProbability);
		return cumulativeProbability;
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
	public BigMLModelConfiguration getModelConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}

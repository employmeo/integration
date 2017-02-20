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
public class BigMLModelEngine implements PredictionModelEngine {

	@Value("${com.talytica.apis.bigml.name}")
	private String userName;
	@Value("${com.talytica.apis.bigml.key}")
	private String apiKey;
	@Value("${com.talytica.apis.bigml.devmode:true}")
	private Boolean devMode;
	
	@Autowired
	private PredictionModel model;
	
	private NormalDistribution normalDistribution;

	public BigMLModelEngine() {
		log.info("New Big ML prediction model instantiated");
	}

	@Override
	public void initialize(PredictionModel model) {
		log.info("Initializing ..");
		
		this.model = model;
		log.info("New Big ML prediction model instantiated for {} (ID: {})", model.getName(), model.getModelId());

		log.info("Initialization complete.");
	}

	@Override
	public PredictionResult runPredictions(Respondant respondant, PositionPredictionConfiguration posConfig, Location location,
			List<CorefactorScore> corefactorScores) {
		log.debug("Running predictions for {}", respondant.getId());
		
		PredictionResult prediction = new PredictionResult();
		try {
			BigMLClient api = BigMLClient.getInstance(userName,apiKey,devMode);
			JSONObject args = null;
			JSONObject model = api.getModel("");
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
		normalDistribution = new NormalDistribution(posConfig.getMean(),posConfig.getStDev());
		Double percentile = normalDistribution.cumulativeProbability(targetOutcomeScore);
		
		prediction.setScore(targetOutcomeScore);
		prediction.setPercentile(percentile);

		log.info("Prediction outcome for respondant {} is {}", respondant.getId(), targetOutcomeScore);
		return prediction;
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

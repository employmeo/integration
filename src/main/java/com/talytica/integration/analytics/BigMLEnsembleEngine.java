package com.talytica.integration.analytics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.LocalEnsemble;
import org.bigml.binding.PredictionMethod;
import org.json.simple.JSONObject;
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
public class BigMLEnsembleEngine implements PredictionModelEngine {

	@Value("${com.talytica.apis.bigml.name}")
	private String userName;
	@Value("${com.talytica.apis.bigml.key}")
	private String apiKey;
	@Value("${com.talytica.apis.bigml.devmode:true}")
	private Boolean devMode;
	
	private static boolean byName = true;
    private static boolean withConfidence = true;
	
	private PredictionModel model;
	
	private NormalDistribution normalDistribution;

	public BigMLEnsembleEngine() {
		log.info("New Big ML ensemble model instantiated");
	}

	@Override
	public void initialize(PredictionModel model) {
		log.info("Initializing ..");
		
		this.model = model;
		log.info("New Big ML ensemble model instantiated for {} (ID: {})", model.getName(), model.getModelId());

		log.info("Initialization complete.");
	}

	@Override
	public PredictionResult runPredictions(Respondant respondant, PositionPredictionConfiguration posConfig, Location location,
			List<CorefactorScore> corefactorScores) {
		log.debug("Running predictions for {}", respondant.getId());
		
		PredictionResult prediction = new PredictionResult();
		Double targetOutcomeScore = 0d;
		try {
			BigMLClient api = BigMLClient.getInstance(userName,apiKey,devMode);
			JSONObject inputData = new JSONObject();
			for (CorefactorScore cs : corefactorScores) {
				inputData.put(cs.getCorefactor().getName(), cs.getScore());
			}	
			          
			JSONObject args = null;
			JSONObject pred  = api.createPrediction(this.model.getForeignId(), inputData, byName, args, null, null);
			JSONObject object = (JSONObject) api.getPrediction(pred).get("object");
			JSONObject result = (JSONObject) object.get("prediction");
			Boolean outcome = new Boolean( result.get(object.get("objective_field")).toString());
            Double confidence = (Double) object.get("confidence");

            targetOutcomeScore = (outcome) ? confidence : 1 - confidence;
            
            log.info("Ensemble Prediction is: {}, with {} confidence",object.get("prediction"),object.get("confidence"));


		} catch (Exception e) {
			log.error("Ensemble Prediction failed for {}, with exception {}", respondant.getId(), e);
		}

		prediction.setScore(targetOutcomeScore);
		try {		
			normalDistribution = new NormalDistribution(posConfig.getMean(),posConfig.getStDev());
			Double percentile = normalDistribution.cumulativeProbability(targetOutcomeScore);
			prediction.setPercentile(percentile);
		} catch (Exception e) {
			log.error("Ensemble Prediction failed for {}, with exception {}", respondant.getId(), e);
		}

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

package com.talytica.integration.analytics;

import java.util.List;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.talytica.integration.objects.NameValuePair;
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
	
	private PredictionModel model;
	private static boolean byName = true;
	
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

	@SuppressWarnings(value = {"unchecked"})
	@Override
	public PredictionResult runPredictions( Respondant respondant, 
											PositionPredictionConfiguration posConfig,
											List<NameValuePair> modelInputs) throws Exception {
		log.debug("Running predictions for {}", respondant.getId());
		
		PredictionResult prediction = new PredictionResult();
		JSONObject args = null;
		JSONObject inputData = new JSONObject();
		for (NameValuePair nvp: modelInputs) {
			inputData.put(nvp.getName(), nvp.getValue());
		}
		
		try {
			BigMLClient api = BigMLClient.getInstance(userName,apiKey,devMode);
			JSONObject pred = api.createPrediction(this.model.getForeignId(), inputData, byName, args, null, null);
			prediction.setForeignId((String)pred.get("resource"));
			JSONObject object = (JSONObject) api.getPrediction(pred).get("object");
			
			JSONObject result = (JSONObject) object.get("prediction");
			Boolean outcome = new Boolean( result.get(object.get("objective_field")).toString());
            Double confidence = (Double) object.get("confidence");
            
            prediction.setOutcome(outcome);
    		prediction.setScore((outcome) ? confidence : 1 - confidence);
		
		} catch (Exception e) {
			log.error("Ensemble Prediction failed for {}, with exception {}", respondant.getId(), e);
		}

		log.info("Prediction outcome for respondant {} is {}", respondant.getId(), prediction.getScore());
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

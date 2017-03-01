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
public class BigMLEnsembleEngine implements PredictionModelEngine {

	@Value("${com.talytica.apis.bigml.name}")
	private String userName;
	@Value("${com.talytica.apis.bigml.key}")
	private String apiKey;
	@Value("${com.talytica.apis.bigml.devmode:true}")
	private Boolean devMode;
	
	private static boolean byName = true;
   // private static boolean withConfidence = true;
	
	private PredictionModel model;

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
	public PredictionResult runPredictions(Respondant respondant, PositionPredictionConfiguration posConfig, List<NameValuePair> modelInputs) {
		log.debug("Running predictions for {}", respondant.getId());
		
		PredictionResult prediction = new PredictionResult();
		JSONObject args = null;
		JSONObject inputData = new JSONObject();
		for (NameValuePair nvp: modelInputs) {
			inputData.put(nvp.getName(), nvp.getValue());
		}
		
		if ((null != model.getPrep()) && (!model.getPrep().isEmpty())) inputData = prepInputData(inputData);
		
		try {
			BigMLClient api = BigMLClient.getInstance(userName,apiKey,devMode);		          
			JSONObject pred  = api.createPrediction(this.model.getForeignId(), inputData, byName, args, null, null);
			prediction.setForeignId((String)pred.get("resource"));
			JSONObject object = (JSONObject) api.getPrediction(pred).get("object");

			JSONObject result = (JSONObject) object.get("prediction");
			Boolean outcome = outcomeFromObject(result.get(object.get("objective_field")));
            Double confidence = (Double) object.get("confidence");
            
            prediction.setOutcome(outcome);
            prediction.setScore((outcome) ? confidence : 1 - confidence);  		
            log.info("Ensemble Prediction is: {}, with {} confidence",object.get("prediction"),object.get("confidence"));

		} catch (Exception e) {
			log.error("Ensemble Prediction failed for {}, with exception {}", respondant.getId(), e);
		}

		log.info("Prediction outcome for respondant {} is {}", respondant.getId(), prediction.getScore());
		return prediction;
	}

	private JSONObject prepInputData(JSONObject inputData) {
		//call topic model!
		return inputData;
	}

	private Boolean outcomeFromObject(Object outcome) {		
		Boolean result = Boolean.FALSE;
		if (String.class.isInstance(outcome)) {
			String value = ((String) outcome).toLowerCase();
			switch (value) {
				case "t":
				case "true":
				case "1":
				case "y":
				case "yes":
					result = true;
					break;
				default:
					result = false;
					break;
			}
		} else if (Integer.class.isInstance(outcome)) {
			result = (1 == (Integer) outcome);
		} else if (Boolean.class.isInstance(outcome)) {
			result = (Boolean) outcome;
		}

		return result;
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

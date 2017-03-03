package com.talytica.integration.analytics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

	private static final Integer ATTEMPT_LIMIT = 5;
	private static final Integer RETRY_WAIT = 3000;
	
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
		
		if ((null != model.getPrep()) && (!model.getPrep().isEmpty())) {
			inputData = prepInputData(inputData);
		} else {
			log.info("No Topic Model: {}", model);
		}
		
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
		log.info("Topic Model: {}", model.getPrepName());	
		try {
			JSONObject createargs = new JSONObject();
			createargs.put("input_data", inputData);
			createargs.put("topicmodel", model.getPrepName());
			//log.info("posting {} to big ml", createargs);
			JSONObject dist = bigMLPost(model.getPrep(), createargs);
			String topicDistId = (String) dist.get("resource");
			Boolean executed = false;
			Integer attempts = 0;
			while (!executed) {
				JSONObject topicDist = (JSONObject) bigMLGet(topicDistId).get("topic_distribution");
				attempts++;
				if (!topicDist.containsKey("result")) {		
					// if failed attempts too high, log issue, executed = true;
					if (attempts >= ATTEMPT_LIMIT) {
						log.warn("Attempted {} topic dists and gave up", attempts);
						executed = Boolean.TRUE;
					} else {
						log.warn("Attempted {} topic dists, now retrying again", attempts);
						Thread.sleep(RETRY_WAIT);
					}
					continue;
				}
				JSONArray values = (JSONArray) topicDist.get("result");
				JSONArray topics = (JSONArray) topicDist.get("topics");
				
				for (int i=0;i<topics.size();i++) {
					String name = (String) ((JSONObject) topics.get(i)).get("name");
					inputData.put(name, values.get(i));
					log.debug("Added Topic {} with value: {}",name,values.get(i));
				}
				executed = Boolean.TRUE;
			}
		} catch (Exception e){
			log.warn("Topic Model Failure with exception {}", e);
		}
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

	public JSONObject bigMLPost(String postTarget, JSONObject params) {
		Client client = ClientBuilder.newClient();
		String dev = "";
		if (devMode) dev = "dev/";
		WebTarget target = client.target("https://bigml.io/" + dev + postTarget + "?username="+userName+";api_key="+apiKey);
		JSONObject json = null;
		javax.ws.rs.core.Response response = null;
		try {
			response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(params.toString(), MediaType.APPLICATION_JSON));
			log.trace("Service {} yielded response : {}", postTarget, response);
			json = (JSONObject) JSONValue.parse(response.readEntity(String.class));
		} catch (Exception e) {
			log.warn("Failed to grab service {}. Exception: {}", postTarget, e);
		}
		return json;
	}
	
	public JSONObject bigMLGet(String getTarget) {
		Client client = ClientBuilder.newClient();
		String dev = "";
		if (devMode) dev = "dev/";
		WebTarget target = client.target("https://bigml.io/" + dev + getTarget + "?username="+userName+";api_key="+apiKey);
		JSONObject json = null;
		javax.ws.rs.core.Response response = null;
		try {
			response = target.request(MediaType.APPLICATION_JSON).get();
			log.trace("Service {} yielded response : {}", getTarget, response);
			json = (JSONObject) JSONValue.parse(response.readEntity(String.class));
		} catch (Exception e) {
			log.warn("Failed to grab service {}. Exception: {}", getTarget, e);
		}
		return json;
	}
	
}

package com.talytica.integration.analytics;

import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.PredictionModel;
import com.employmeo.data.model.PredictionModelType;
import com.employmeo.data.service.PredictionModelService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;


/**
 * For now, a static build-time registry of algorithms and model implementations.
 * Subsequently to be moved to a dynamic registry
 * @author NShah
 *
 */
@Slf4j
@Component
public class PredictionModelRegistry {

	@Autowired
	PredictionModelService predictionModelService;
	@Autowired
	private ApplicationContext applicationContext;

	private Map<PredictionModelType, Class<? extends PredictionModelEngine>> modelRegistry;

	@PostConstruct
	public void initialize() {
		log.info("Initializing prediction model registry");
		modelRegistry =
				Maps.newHashMap(
						new ImmutableMap.Builder<PredictionModelType, Class<? extends PredictionModelEngine>>()
		                   .put(PredictionModelType.LINEAR_REGRESSION, SimpleLinearRegressionEngine.class)
		                   .put(PredictionModelType.BIGML_ENSEMBLE, BigMLEnsembleEngine.class)
		                   .put(PredictionModelType.BIGML_MODEL, BigMLModelEngine.class)
		                   .build()
				);
		log.info("PredictionModelRegistry state: {}", modelRegistry);
	}

	public Optional<PredictionModelEngine> getPredictionModelEngineById(@NotNull Long modelId) {
		Optional<PredictionModelEngine> modelEngine = Optional.empty();
		PredictionModel predictionModel = predictionModelService.getModelById(modelId);
		Optional<Class<? extends PredictionModelEngine>> modelEngineClass = Optional.ofNullable(modelRegistry.get(predictionModel.getModelType()));
		log.debug("Registry consulted for model id: {} with {}", modelId, modelEngineClass);
		
		if(modelEngineClass.isPresent()) {
			try {
				log.debug("Attempting to create a new engine instance for model ID {}", modelId);
				PredictionModelEngine engineInstance = applicationContext.getBean(modelEngineClass.get());
				engineInstance.initialize(predictionModel);
				modelEngine = Optional.of(engineInstance);
				
			} catch ( IllegalArgumentException | SecurityException e) {
				log.warn("Failed to create new instance of prediction model engine for model id {}", modelId, e);
			}
		} else {
			log.debug("Failed to find prediction engine for model id: {}", modelId);
		}

		return modelEngine;
	}


}

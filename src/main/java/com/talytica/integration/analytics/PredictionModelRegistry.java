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
		                   .put(PredictionModelType.BIGML_MODEL, BigMLModelEngine.class)
		                   .build()
				);
		log.info("PredictionModelRegistry state: {}", modelRegistry);
	}

	public Optional<PredictionModelEngine> getPredictionModelEngineById(@NotNull Long modelId) {
		Optional<PredictionModelEngine> modelEngine = Optional.empty();

		log.debug("Registry consulted for model id: {}", modelId);
		PredictionModel predictionModel = predictionModelService.getModelById(modelId);

		log.debug("Checking registry for mappings for modelType {}", predictionModel.getModelType());
		Optional<Class<? extends PredictionModelEngine>> modelEngineClass = Optional.ofNullable(modelRegistry.get(predictionModel.getModelType()));

		log.debug("Registry is configured for {} with {}", modelId, modelEngineClass);
		if(modelEngineClass.isPresent()) {
			try {

				PredictionModelEngine engineInstance = applicationContext.getBean(modelEngineClass.get());
				log.debug("Attempting to create a new engine instance for model ID {}", modelId);
				//Constructor<? extends PredictionModelEngine<?>> engineConstructor = modelEngineClass.get().getConstructor(modelName.getClass());
				//PredictionModelEngine<?> engineInstance = engineConstructor.newInstance(modelName);

				log.debug("PredictionModelEngine {} instantiated. Now initializing..", engineInstance);
				engineInstance.initialize(predictionModel);

				modelEngine = Optional.of(engineInstance);
			} catch ( IllegalArgumentException | SecurityException e) {
				log.warn("Failed to create new instance of prediction model engine for model id {}", modelId, e);
			}
		}
		log.debug("Returning modelEngine for {} as {}", modelId, modelEngine);

		return modelEngine;
	}


}

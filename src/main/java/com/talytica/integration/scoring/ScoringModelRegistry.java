package com.talytica.integration.scoring;

import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;


/**
 * A static build-time registry of scoring model implementations.
 */

@Slf4j
@Component
public class ScoringModelRegistry {

	@Autowired
	private ApplicationContext applicationContext;

	private Map<ScoringModelType, Class<? extends ScoringModelEngine>> modelRegistry;

	@PostConstruct
	public void initialize() {
		log.info("Initializing scoring model registry");
		modelRegistry =
				Maps.newHashMap(
						new ImmutableMap.Builder<ScoringModelType, Class<? extends ScoringModelEngine>>()
		                   .put(ScoringModelType.AUDIO, AudioScoring.class)
		                   .put(ScoringModelType.TRAIT, BlendedTypeScoring.class)
		                   .put(ScoringModelType.HEXACO, BlendedTypeScoring.class)
		                   .put(ScoringModelType.KNOCKOUT, KnockoutScoring.class)
		                   .put(ScoringModelType.DECEPTION, KnockoutScoring.class)
		                   .put(ScoringModelType.MERCER, MercerScoring.class)
		                   .put(ScoringModelType.NONE, NoScoring.class)
		                   .put(ScoringModelType.WORKINGMEM, PercentileAverageScoring.class)
		                   .put(ScoringModelType.REACTION, PercentileAverageScoring.class)
		                   .put(ScoringModelType.SELECTIVE, PercentileAverageScoring.class)
		                   .put(ScoringModelType.RANKER, RankerScoring.class)
		                   .put(ScoringModelType.REFERENCE, ReferenceScoring.class)
		                   .put(ScoringModelType.RIGHTWRONGBLANK, RightWrongBlankScoring.class)
		                   .put(ScoringModelType.AVERAGE, SimpleAverageScoring.class)
		                   .build()
				);
		log.info("ScoringModelRegistry state: {}", modelRegistry);
	}

	public Optional<ScoringModelEngine> getScoringModelEngineByName(@NotNull String modelName) {
		ScoringModelType type = ScoringModelType.getByValue(modelName);
		Optional<ScoringModelEngine> modelEngine = Optional.empty();
		log.debug("Registry consulted for modelName {}", modelName);
		Optional<Class<? extends ScoringModelEngine>> modelEngineClass = Optional.ofNullable(modelRegistry.get(type));

		log.debug("Registry is configured for {} with {}", modelName, modelEngineClass);
		if(modelEngineClass.isPresent()) {
			try {

				ScoringModelEngine engineInstance = applicationContext.getBean(modelEngineClass.get());
				log.debug("Attempting to create a new engine instance for {}", modelName);
				engineInstance.initialize(modelName);
				modelEngine = Optional.of(engineInstance);
			} catch ( IllegalArgumentException | SecurityException e) {
				
				log.warn("Failed to create new instance of scoring model engine for " + modelName, e);
			}
		}
		log.debug("Returning modelEngine for {} as {}", modelName, modelEngine);

		return modelEngine;
	}


}

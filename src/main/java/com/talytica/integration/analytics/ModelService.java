package com.talytica.integration.analytics;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.LinearRegressionConfigRepository;
import com.employmeo.data.repository.PredictionModelRepository;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ModelService {

	@Autowired
	private PredictionModelRepository predictionModelRepository;
	@Autowired
	private LinearRegressionConfigRepository linearRegressionConfigRepository;

	public LinearRegressionModelConfiguration getLinearRegressionConfiguration(@NonNull String modelName) {
		log.debug("Fetching linear regression configurations for modelName {}", modelName);

		LinearRegressionModelConfiguration configuration = null;

		PredictionModel predictionModel = getModelByName(modelName);

		if(PredictionModelType.LINEAR_REGRESSION == predictionModel.getModelType()) {
			List<LinearRegressionConfig> configEntries = linearRegressionConfigRepository.findByModelId(predictionModel.getModelId());

			Optional<LinearRegressionConfig> meanConfig = findEntry(configEntries, LinearRegressionConfigType.MEAN);
			Double mean = meanConfig.isPresent() ? meanConfig.get().getCoefficient() : 0.0D;

			Optional<LinearRegressionConfig> stdDevConfig = findEntry(configEntries, LinearRegressionConfigType.STD_DEV);
			Double stdDev = stdDevConfig.isPresent() ? stdDevConfig.get().getCoefficient() : 0.0D;

			Optional<LinearRegressionConfig> populationConfig = findEntry(configEntries, LinearRegressionConfigType.POPULATION);
			Double population = populationConfig.isPresent() ? populationConfig.get().getCoefficient() : 0.0D;

			configuration = LinearRegressionModelConfiguration.builder()
					.configEntries(configEntries)
					.mean(mean)
					.stdDev(stdDev)
					.population(population)
					.build();
			log.debug("LinearRegressionConfigs for model {} : {}", modelName, configuration);
		} else {
			log.warn("Model {} is not a linear regression type model. Please review configurations.", modelName);
			throw new IllegalStateException("Model " + modelName + " is not a linear regression type model. Please review setup.");
		}

		return configuration;
	}

	private Optional<LinearRegressionConfig> findEntry(List<LinearRegressionConfig> configEntries, LinearRegressionConfigType configType) {
		return configEntries.stream().filter(e -> e.getConfigType() == configType).findFirst();
	}

	public PredictionModel getModelByName(@NonNull String modelName) {
		log.debug("Fetching prediction model by name {}", modelName);

		PredictionModel predictionModel = predictionModelRepository.findByName(modelName);
		log.debug("PredictionModel for modelName {} : {}", modelName, predictionModel);

		return predictionModel;
	}

}

package com.employmeo.util;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.employmeo.objects.LinearRegressionConfig;
import com.employmeo.objects.LinearRegressionConfig.ConfigType;
import com.employmeo.objects.PredictionModel;
import com.employmeo.objects.PredictionModel.PredictionModelType;

import lombok.NonNull;

public class ModelUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ModelUtil.class);

	public static LinearRegressionModelConfiguration getLinearRegressionConfiguration(@NonNull String modelName) {
		log.debug("Fetching linear regression configurations for modelName {}", modelName);

		LinearRegressionModelConfiguration configuration = null;

		PredictionModel predictionModel = getModelByName(modelName);
		
		if(PredictionModelType.LINEAR_REGRESSION == predictionModel.getModelType()) {
			List<LinearRegressionConfig> configEntries = DBUtil.getEntityManager()
								.createNamedQuery("LinearRegressionConfig.findByModelId", LinearRegressionConfig.class)
								.setParameter("modelId", predictionModel.getModelId())
								.getResultList();
			
			Optional<LinearRegressionConfig> meanConfig = findEntry(configEntries, ConfigType.MEAN);
			Double mean = meanConfig.isPresent() ? meanConfig.get().getCoefficient() : 0.0D;
			
			Optional<LinearRegressionConfig> stdDevConfig = findEntry(configEntries, ConfigType.STD_DEV);
			Double stdDev = stdDevConfig.isPresent() ? stdDevConfig.get().getCoefficient() : 0.0D;
			
			Optional<LinearRegressionConfig> populationConfig = findEntry(configEntries, LinearRegressionConfig.ConfigType.POPULATION);
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
	
	private static Optional<LinearRegressionConfig> findEntry(List<LinearRegressionConfig> configEntries, ConfigType configType) {	
		return configEntries.stream().filter(e -> e.getConfigType() == configType).findFirst();
	}

	public static PredictionModel getModelByName(@NonNull String modelName) {
		log.debug("Fetching prediction model by name {}", modelName);
		EntityManager em = DBUtil.getEntityManager();
		
		PredictionModel predictionModel = em.createNamedQuery("PredictionModel.findByName", PredictionModel.class)
											.setParameter("name", modelName)
											.getSingleResult();
		
		log.debug("PredictionModel for modelName {} : {}", modelName, predictionModel);
		return predictionModel;
	}	
	
}

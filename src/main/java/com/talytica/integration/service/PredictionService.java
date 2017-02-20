package com.talytica.integration.service;

import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PredictionRepository;
import com.employmeo.data.service.CorefactorService;
import com.google.common.collect.Lists;
import com.talytica.integration.analytics.PredictionModelEngine;
import com.talytica.integration.analytics.PredictionModelRegistry;
import com.talytica.integration.objects.CorefactorScore;
import com.talytica.integration.objects.PredictionResult;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PredictionService {

	@Autowired
	private CorefactorService corefactorService;
	@Autowired
	private PredictionRepository predictionRepository;
	@Autowired
	private PredictionModelRegistry predictionModelRegistry;

	public List<PredictionResult> runPredictionsStageForAllTargets(@NonNull Respondant respondant) {
		List<PredictionResult> predictions = Lists.newArrayList();
		List<CorefactorScore> corefactorScores = getCorefactorScores(respondant);

		Set<PositionPredictionConfiguration> positionPredictionConfigs = respondant.getPosition().getPositionPredictionConfigurations();
		positionPredictionConfigs.forEach(predictionConfig -> {
				PredictionResult predictionResult = predictForTarget(respondant, corefactorScores, predictionConfig);
				predictions.add(predictionResult);
		});

		return predictions;
	}


	private PredictionResult predictForTarget(Respondant respondant, List<CorefactorScore> corefactorScores,
			PositionPredictionConfiguration predictionConfig) {
		PredictionTarget predictionTarget = predictionConfig.getPredictionTarget();
		PredictionModel predictionModel = predictionConfig.getPredictionModel();
		PredictionModelEngine<?> predictionEngine = getPredictionModelEngine(predictionModel);

		log.debug("Initiating predictions run for respondant {} and target {} with predictionEngine {} for position {} at location {} with corefactorScores as {}",
				respondant.getId(), predictionTarget.getName(), predictionEngine, respondant.getPosition().getPositionName(), respondant.getLocation().getLocationName(), corefactorScores);

		PredictionResult predictionResult = predictionEngine.runPredictions(respondant, respondant.getPosition(), respondant.getLocation(), corefactorScores);
		predictionResult.setModelName(predictionModel.getName());
		predictionResult.setPredictionTarget(predictionTarget);

		log.info("Prediction for respondant {} for position {} and target {} : {}",
				respondant.getId(), respondant.getPosition().getPositionName(), predictionTarget.getName(), predictionResult);

		savePrediction(respondant, predictionConfig, predictionResult);
		return predictionResult;
	}


	private void savePrediction(Respondant respondant,
			PositionPredictionConfiguration predictionConfig,
			PredictionResult predictionResult) {

		Prediction prediction = new Prediction();
		prediction.setRespondant(respondant);
		prediction.setRespondantId(respondant.getId());
		prediction.setPositionPredictionConfig(predictionConfig);
		prediction.setPositionPredictionConfigId(predictionConfig.getPositionPredictionConfigId());
		prediction.setPredictionScore(predictionResult.getScore());
		prediction.setScorePercentile(predictionResult.getPercentile());

		Prediction savedPrediction = predictionRepository.save(prediction);

		log.debug("Prediction persisted: {}", savedPrediction);
	}

	private PredictionModelEngine<?> getPredictionModelEngine(@NonNull PredictionModel predictionModel) {
		Optional<PredictionModelEngine<?>> registeredPredictionEngine = predictionModelRegistry.getPredictionModelEngineByName(predictionModel.getName());

		log.debug("Retrieved {} as prediction engine for {}", registeredPredictionEngine, predictionModel.getName() );
		return registeredPredictionEngine.orElseThrow(() -> new IllegalStateException(
				"No prediction engines registered for prediction target: " + predictionModel.getName()));
	}


	private List<CorefactorScore> getCorefactorScores(Respondant respondant) {
		List<CorefactorScore> corefactorScores = respondant.getRespondantScores().stream()
					.map(rs -> getCorefactorById(rs))
					.collect(Collectors.toList());

		log.debug("CorefactorScores for respondant {} are {}", respondant.getId(), corefactorScores);
		return corefactorScores;
	}

	private CorefactorScore getCorefactorById(RespondantScore rs) {
		Corefactor cf = corefactorService.findCorefactorById(rs.getId().getCorefactorId());
		CorefactorScore cfScore = new CorefactorScore(cf, rs.getValue());
		return cfScore;
	}
}

package com.talytica.integration.service;

import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PredictionRepository;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.RespondantService;
import com.google.common.collect.Lists;
import com.talytica.integration.analytics.PredictionModelEngine;
import com.talytica.integration.analytics.PredictionModelRegistry;
import com.talytica.integration.objects.NameValuePair;
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
	@Autowired
	private RespondantService respondantService;

	public List<PredictionResult> runPostAssessmentPredictions(@NonNull Respondant respondant) {
		List<PredictionResult> predictions = Lists.newArrayList();
		List<NameValuePair> corefactorScores = getModelInputsFromCorefactorScores(respondant);

		Set<PositionPredictionConfiguration> positionPredictionConfigs = respondant.getPosition().getPositionPredictionConfigurations();
		positionPredictionConfigs.forEach(predictionConfig -> {
				if (predictionConfig.getTriggerPoint() == PositionPredictionConfiguration.TRIGGER_POINT_ASSESSMENT) {
					PredictionResult predictionResult = predictForTarget(respondant, corefactorScores, predictionConfig);
					if (null != predictionResult) predictions.add(predictionResult);
				}
		});

		return predictions;
	}

	public List<PredictionResult> runPreAssessmentPredictions(@NonNull Respondant respondant) {
		List<PredictionResult> predictions = Lists.newArrayList();
		List<NameValuePair> modelInputs = getModelInputsFromNvps(respondant);

		Set<PositionPredictionConfiguration> positionPredictionConfigs = respondant.getPosition().getPositionPredictionConfigurations();
		positionPredictionConfigs.forEach(predictionConfig -> {
			if (predictionConfig.getTriggerPoint() == PositionPredictionConfiguration.TRIGGER_POINT_CREATION) {
				PredictionResult predictionResult = predictForTarget(respondant, modelInputs, predictionConfig);
				if (predictionResult != null) predictions.add(predictionResult);
			}
		});

		return predictions;
	}

	private PredictionResult predictForTarget(Respondant respondant, List<NameValuePair> modelInputs,
			PositionPredictionConfiguration predictionConfig) {
		PredictionTarget predictionTarget = predictionConfig.getPredictionTarget();
		PredictionModel predictionModel = predictionConfig.getPredictionModel();
		PredictionModelEngine predictionEngine = getPredictionModelEngine(predictionModel);

		log.debug("Initiating predictions run for respondant {} and target {} with predictionEngine {} for position {} at location {} with {} model inputs}",
				respondant.getId(), predictionTarget.getName(), predictionEngine, respondant.getPosition().getPositionName(), respondant.getLocation().getLocationName(), modelInputs.size());

		PredictionResult predictionResult = predictionEngine.runPredictions(respondant, predictionConfig, modelInputs);
		if (predictionResult == null) return null;
		predictionResult.setModelName(predictionModel.getName());
		predictionResult.setPredictionTarget(predictionTarget);
		NormalDistribution normalDistribution = new NormalDistribution(predictionConfig.getMean(),predictionConfig.getStDev());
		predictionResult.setPercentile(normalDistribution.cumulativeProbability(predictionResult.getScore()));
		
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
		prediction.setTargetId(predictionConfig.getPredictionTargetId());
		prediction.setPredictionScore(predictionResult.getScore());
		prediction.setForeignId(predictionResult.getForeignId());
		prediction.setValue(predictionResult.getOutcome());
		prediction.setScorePercentile(predictionResult.getPercentile());

		Prediction savedPrediction = predictionRepository.save(prediction);

		log.debug("Prediction persisted: {}", savedPrediction);
	}

	private PredictionModelEngine getPredictionModelEngine(@NonNull PredictionModel predictionModel) {
		Optional<PredictionModelEngine> registeredPredictionEngine = predictionModelRegistry.getPredictionModelEngineById(predictionModel.getModelId());

		log.debug("Retrieved {} as prediction engine for {}", registeredPredictionEngine, predictionModel.getName() );
		return registeredPredictionEngine.orElseThrow(() -> new IllegalStateException(
				"No prediction engines registered for prediction target: " + predictionModel.getName()));
	}

	private List<NameValuePair> getModelInputsFromCorefactorScores(Respondant respondant) {
		List<NameValuePair> modelInputs = respondant.getRespondantScores().stream()
					.map(rs ->  getModelInputFromScore(rs))
					.collect(Collectors.toList());
		log.debug("CorefactorScores for respondant {} are {}", respondant.getId(), modelInputs);
		return modelInputs;
	}

	private NameValuePair getModelInputFromScore(RespondantScore rs) {
		Corefactor cf = corefactorService.findCorefactorById(rs.getId().getCorefactorId());
		NameValuePair nvPair = new NameValuePair(cf.getName(), rs.getValue());
		return nvPair;
	}
	
	private List<NameValuePair> getModelInputsFromNvps(Respondant respondant) {
		List<NameValuePair> modelInputs = respondantService.getNVPsForRespondant(respondant.getId()).stream()
					.map(nvp ->  getModelInputFromNvp(nvp)).collect(Collectors.toList());
		log.debug("CorefactorScores for respondant {} are {}", respondant.getId(), modelInputs);
		return modelInputs;
	}

	private NameValuePair getModelInputFromNvp(RespondantNVP nvp) {
		NameValuePair nvPair = new NameValuePair("Var" + nvp.getNameId().toString(), nvp.getValue());
		return nvPair;
	}
}

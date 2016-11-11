package com.talytica.integration.analytics;

import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PredictionRepository;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.RespondantService;
import com.google.common.collect.Lists;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class PredictionService {

	@Autowired
	private CorefactorService corefactorService;
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private PredictionRepository predictionRepository;
	@Autowired
	private PredictionModelRegistry predictionModelRegistry;

	public void predictRespondant(Respondant respondant) {
		log.debug("Predictions requested for respondant {}", respondant.getId());

		if (respondant.getRespondantStatus() <= Respondant.STATUS_SCORED) {
			//respondant = refresh(respondant);
			log.debug("Respondant {} has status = {}, no predictions have been run yet.", respondant.getId(), respondant.getRespondantStatus());
		}

		if (respondant.getRespondantStatus() == Respondant.STATUS_SCORED) {
			//DBUtil.beginTransaction();

			try {
				// Stage 1
				List<PredictionResult> predictions = runPredictionsStageForAllTargets(respondant);

				// Stage 2
				GradingResult gradingResult = GradingUtil.gradeRespondantByPredictions(respondant, predictions);

				// Assimilate results, Update respondant lifecycle, and persist state
				respondant.setProfileRecommendation(gradingResult.getRecommendedProfile());
				respondant.setCompositeScore(gradingResult.getCompositeScore());
				respondant.setRespondantStatus(Respondant.STATUS_PREDICTED);

				Respondant savedRespondant = respondantService.save(respondant);

				//respondant.mergeMe();

				//DBUtil.commit();
			} catch(Exception e) {
				log.warn("Failed to run predictions/grading for respondant " + respondant.getId(), e);

				//DBUtil.rollback();
				log.warn("Rolled back transaction");
			}

			log.debug("Predictions for respondant {} complete", respondant.getId());
		}

		return;
	}

/*
	private Respondant refresh(Respondant respondant) {
		// the application tends to get in a state where a rollback leads to entity manager state being inconsistent
		// given multiple respondants get run for predictions in the same call/thread.

		EntityManager em = DBUtil.getEntityManager();
		if(em.contains(respondant)) {
			respondant.refreshMe();
		} else {
			respondant = em.createNamedQuery("Respondant.findById",Respondant.class)
						.setParameter("respondantId", respondant.getId())
						.getSingleResult();
		}

		return respondant;
	}
*/

	private List<PredictionResult> runPredictionsStageForAllTargets(Respondant respondant) {
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
		prediction.setPositionPredictionConfig(predictionConfig);
		prediction.setPredictionScore(predictionResult.getScore());
		prediction.setScorePercentile(predictionResult.getPercentile());

		Prediction savedPrediction = predictionRepository.save(prediction);
		//DBUtil.getEntityManager().persist(prediction);
		//prediction.persistMe();

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

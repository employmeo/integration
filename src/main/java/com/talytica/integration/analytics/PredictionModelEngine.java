package com.talytica.integration.analytics;

import java.util.List;

import com.employmeo.data.model.*;

public interface PredictionModelEngine<MC> {

	/**
	 * Prediction implementations can do local processing, or make requisite
	 * network api calls to run predictions
	 *
	 * @param respondant
	 * @return
	 */
	public abstract PredictionResult runPredictions(Respondant respondant, Position position, Location location, List<CorefactorScore> corefactorScores);
	public abstract String getModelName();
	public abstract void initialize(String modelName);
	public abstract MC getModelConfiguration();

}

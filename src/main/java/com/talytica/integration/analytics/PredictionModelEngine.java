package com.talytica.integration.analytics;

import java.util.List;

import com.employmeo.data.model.*;
import com.talytica.integration.objects.CorefactorScore;
import com.talytica.integration.objects.PredictionResult;

public interface PredictionModelEngine {

	/**
	 * Prediction implementations can do local processing, or make requisite
	 * network api calls to run predictions
	 *
	 * @param respondant
	 * @return
	 */
	public abstract PredictionResult runPredictions(Respondant respondant, PositionPredictionConfiguration posConfig, Location location, List<CorefactorScore> corefactorScores);
	public abstract String getModelName();
	public abstract Long getModelId();
	public abstract String getModelType();
	public abstract void initialize(PredictionModel model);

}

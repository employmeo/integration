package com.employmeo.util;

import java.util.List;

import com.employmeo.objects.CorefactorScore;
import com.employmeo.objects.Location;
import com.employmeo.objects.Position;
import com.employmeo.objects.Respondant;

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
	public abstract void initialize();
	public abstract MC getModelConfiguration();
	
}

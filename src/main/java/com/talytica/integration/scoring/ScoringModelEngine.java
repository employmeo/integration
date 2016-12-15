package com.talytica.integration.scoring;

import java.util.List;

import com.employmeo.data.model.*;

public interface ScoringModelEngine {

	/**
	 * 
	 * Scoring Model implementations can process a set of responses, and either trigger other events or
	 * return a set of corefactors.
	 * 
	 * Scores responses into corefactors, or returns null value if respondant should move to incomplete status
	 *
	 * @param response array
	 * @return score array
	 */
	public abstract List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses);
	public abstract String getModelName();
	public abstract void initialize(String modelName);

}

package com.talytica.integration.analytics;

import com.employmeo.data.model.PredictionModelType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionModelAlgorithm {
	private String modelName;
	private PredictionModelType modelType;
	private String predictionTarget;
	private Integer modelVersion;
}

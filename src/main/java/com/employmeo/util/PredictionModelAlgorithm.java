package com.employmeo.util;

import com.employmeo.objects.PredictionModel.PredictionModelType;

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

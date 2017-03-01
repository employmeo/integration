package com.talytica.integration.objects;

import com.employmeo.data.model.PredictionTarget;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PredictionResult {

	private String modelName;
	private PredictionTarget predictionTarget;
	private Double score = 0.0D;
	private Double percentile = 0.0D;
	private String foreignId;
	private Boolean outcome;

}

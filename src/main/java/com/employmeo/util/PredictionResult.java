package com.employmeo.util;

import com.employmeo.objects.PredictionTarget;

import lombok.Data;

@Data
public class PredictionResult {
	
	private String modelName;
	private PredictionTarget predictionTarget;
	private Double score = 0.0D;
	private Double percentile = 0.0D;

}

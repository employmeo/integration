package com.talytica.integration.analytics;

import com.employmeo.data.model.PredictionModel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BigMLModelConfiguration {

	private PredictionModel predictionModel;
	
	public BigMLModelConfiguration(PredictionModel predictionModel) {
		this.predictionModel = predictionModel;
	}

	public String getModelId() {
		return "ID";
	}

}

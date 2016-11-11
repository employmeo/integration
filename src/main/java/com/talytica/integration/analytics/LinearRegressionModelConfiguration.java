package com.talytica.integration.analytics;

import java.util.List;

import com.employmeo.data.model.LinearRegressionConfig;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinearRegressionModelConfiguration {

	private List<LinearRegressionConfig> configEntries;
	private Double mean = 0.0D;
	private Double stdDev = 0.0D;
	private Double population = 0.0D;

}

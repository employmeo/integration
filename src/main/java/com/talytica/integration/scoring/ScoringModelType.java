package com.talytica.integration.scoring;

import lombok.*;

public enum ScoringModelType {
	WORKINGMEM("workingmem"),
	SELECTIVE("selective"),
	REACTION("reaction"),	
	MERCER("mercer"),
	AUDIO("audio"),
	REFERENCE("reference"),
	RANKER("ranker"),
	KNOCKOUT("knockout"),
	DECEPTION("deception"),
	HEXACO("hexaco"),
	TRAIT("trait"),
	AVERAGE("average"),
	RIGHTWRONGBLANK("rightwrongblank"),
	NONE("none");
	
	@Getter
	private String value;

	private ScoringModelType(String value) {
		this.value = value;
	}

	public static ScoringModelType getByValue(@NonNull String value) {
        for (ScoringModelType modelType : ScoringModelType.values()) {
            if (value.equalsIgnoreCase(modelType.getValue())) {
                return modelType;
            }
        }
        throw new IllegalArgumentException("No such ModelType configured: " + value);
	}
}

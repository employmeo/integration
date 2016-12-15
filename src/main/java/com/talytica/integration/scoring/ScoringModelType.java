package com.talytica.integration.scoring;

import lombok.*;

public enum ScoringModelType {
	MERCER("mercer"),
	AUDIO("audio"),
	REFERENCE("reference"),
	RANKER("ranker"),
	LIKERTFIVE("likertfive"),
	SLIDERSIXTY("slidersixty"),
	KNOCKOUT("knockout"),
	AVERAGE("average"),
	NONE("none");
	
	@Getter
	private String value;

	private ScoringModelType(String value) {
		this.value = value;
	}

	public static ScoringModelType getByValue(@NonNull String value) {
        for (ScoringModelType modelType : ScoringModelType.values()) {
            if (value.equals(modelType.getValue())) {
                return modelType;
            }
        }
        throw new IllegalArgumentException("No such ModelType configured: " + value);
	}
}

package com.talytica.integration.analytics;

import com.employmeo.data.model.Corefactor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CorefactorScore {
	private Corefactor corefactor;
	private Double score;

	@Override
	public String toString() {
		return "CorefactorScore[cfId=" + corefactor.getId() + ", cfName=" + corefactor.getName() + ", score=" + score + "]";
	}


}

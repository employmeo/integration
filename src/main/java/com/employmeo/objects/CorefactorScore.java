package com.employmeo.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CorefactorScore {
	private Corefactor corefactor;
	private Double score;
	
	@Override
	public String toString() {
		return "[cfId=" + corefactor.getCorefactorId() + ", cfName=" + corefactor.getCorefactorName() + ", score=" + score + "]";
	}
	
	
}

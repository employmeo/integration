package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the respondant_scores database table.
 * 
 */
@Embeddable
public class PredictiveModelPK implements Serializable {
	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name = "pm_corefactor_id", insertable = false, updatable = false)
	private Integer pmCorefactorId;

	@Column(name = "pm_position_id", insertable = false, updatable = false)
	private Long pmPositionId;

	public PredictiveModelPK() {
	}

	public Integer getPmCorefactorId() {
		return this.pmCorefactorId;
	}

	public void setPmCorefactorId(Integer cfId) {
		this.pmCorefactorId = cfId;
	}

	public Long getPmPositionId() {
		return this.pmPositionId;
	}

	public void setPmPositionId(Long posId) {
		this.pmPositionId = posId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PredictiveModelPK)) {
			return false;
		}
		PredictiveModelPK castOther = (PredictiveModelPK) other;
		return this.pmCorefactorId.equals(castOther.pmCorefactorId) && this.pmPositionId.equals(castOther.pmPositionId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.pmCorefactorId.hashCode();
		hash = hash * prime + this.pmPositionId.hashCode();

		return hash;
	}
}
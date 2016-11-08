package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import lombok.ToString;

/**
 * The primary key class for the respondant_scores database table.
 * 
 */
@Embeddable
@ToString
public class RespondantScorePK implements Serializable {
	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name = "rs_cf_id", insertable = false, updatable = false)
	private Integer rsCfId;

	@Column(name = "rs_respondant_id", insertable = false, updatable = false)
	private Long rsRespondantId;

	public RespondantScorePK() {
	}

	public Integer getRsCfId() {
		return this.rsCfId;
	}

	public void setRsCfId(Integer rsCfId) {
		this.rsCfId = rsCfId;
	}

	public Long getRsRespondantId() {
		return this.rsRespondantId;
	}

	public void setRsRespondantId(Long rsRespondantId) {
		this.rsRespondantId = rsRespondantId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RespondantScorePK)) {
			return false;
		}
		RespondantScorePK castOther = (RespondantScorePK) other;
		return this.rsCfId.equals(castOther.rsCfId) && this.rsRespondantId.equals(castOther.rsRespondantId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.rsCfId.hashCode();
		hash = hash * prime + this.rsRespondantId.hashCode();

		return hash;
	}
}
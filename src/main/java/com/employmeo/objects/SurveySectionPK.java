package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the survey_sections database table.
 * 
 */
@Embeddable
public class SurveySectionPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="ss_survey_id")
	private Long ssSurveyId;

	@Column(name="ss_survey_section")
	private Integer ssSurveySection;

	public SurveySectionPK() {
	}
	public Long getSsSurveyId() {
		return this.ssSurveyId;
	}
	public void setSsSurveyId(Long ssSurveyId) {
		this.ssSurveyId = ssSurveyId;
	}
	public Integer getSsSurveySection() {
		return this.ssSurveySection;
	}
	public void setSsSurveySection(Integer ssSurveySection) {
		this.ssSurveySection = ssSurveySection;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SurveySectionPK)) {
			return false;
		}
		SurveySectionPK castOther = (SurveySectionPK)other;
		return 
			this.ssSurveyId.equals(castOther.ssSurveyId)
			&& this.ssSurveySection.equals(castOther.ssSurveySection);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.ssSurveyId.hashCode();
		hash = hash * prime + this.ssSurveySection.hashCode();
		
		return hash;
	}
	
	@Override
	public String toString() {
		return "SurveySectionPK [ssSurveyId=" + ssSurveyId + ", ssSurveySection=" + ssSurveySection + "]";
	}
	
	
}
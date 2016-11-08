package com.employmeo.objects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.json.JSONObject;

/**
 * The persistent class for the predictive_model database table.
 * 
 */
@Entity
@Table(name = "predictive_model")
@NamedQuery(name = "PredictiveModel.findAll", query = "SELECT p FROM PredictiveModel p")
public class PredictiveModel extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PredictiveModelPK id;

	@Column(name = "pm_coefficient")
	private double pmCoefficient;

	@Column(name = "pm_corefactor_id", insertable = true, updatable = false)
	private Integer pmCorefactorId;

	@ManyToOne
	@JoinColumn(name = "pm_corefactor_id", insertable = false, updatable = false)
	private Corefactor corefactor;

	@Column(name = "pm_position_id", insertable = true, updatable = false)
	private Long pmPositionId;

	@ManyToOne
	@JoinColumn(name = "pm_position_id", insertable = false, updatable = false)
	private Position position;

	@Column(name = "pm_profile_a_score")
	private double pmProfileAScore;

	@Column(name = "pm_profile_b_score")
	private double pmProfileBScore;

	@Column(name = "pm_profile_c_score")
	private double pmProfileCScore;

	@Column(name = "pm_profile_d_score")
	private double pmProfileDScore;

	@Column(name = "pm_significance")
	private double pmSignificance;

	public PredictiveModel() {
	}

	public double getPmCoefficient() {
		return this.pmCoefficient;
	}

	public void setPmCoefficient(double pmCoefficient) {
		this.pmCoefficient = pmCoefficient;
	}

	public Integer getPmCorefactorId() {
		return this.pmCorefactorId;
	}

	public void setPmCorefactorId(Integer pmCorefactorId) {
		this.pmCorefactorId = pmCorefactorId;
		this.corefactor = Corefactor.getCorefactorById(this.pmCorefactorId);
	}

	public Corefactor getCorefactor() {
		if (this.corefactor == null)
			this.corefactor = Corefactor.getCorefactorById(this.pmCorefactorId);
		return this.corefactor;
	}

	public Long getPmPositionId() {
		return this.pmPositionId;
	}

	public void setPmPositionId(Long pmPositionId) {
		this.pmPositionId = pmPositionId;
		this.position = Position.getPositionById(pmPositionId);
	}

	public Position getPosition() {
		if (this.position == null)
			this.position = Position.getPositionById(this.pmPositionId);
		return this.position;
	}

	public double getPmProfileScore(String profileName) {
		double score;
		switch (profileName) {
		case PositionProfile.PROFILE_A:
			score = getPmProfileAScore();
			break;
		case PositionProfile.PROFILE_B:
			score = getPmProfileBScore();
			break;
		case PositionProfile.PROFILE_C:
			score = getPmProfileCScore();
			break;
		case PositionProfile.PROFILE_D:
			score = getPmProfileDScore();
			break;
		default:
			score = 0;
			break;
		}
		return score;
	}

	public double getPmProfileAScore() {
		return this.pmProfileAScore;
	}

	public void setPmProfileAScore(double pmProfileAScore) {
		this.pmProfileAScore = pmProfileAScore;
	}

	public double getPmProfileBScore() {
		return this.pmProfileBScore;
	}

	public void setPmProfileBScore(double pmProfileBScore) {
		this.pmProfileBScore = pmProfileBScore;
	}

	public double getPmProfileCScore() {
		return this.pmProfileCScore;
	}

	public void setPmProfileCScore(double pmProfileCScore) {
		this.pmProfileCScore = pmProfileCScore;
	}

	public double getPmProfileDScore() {
		return this.pmProfileDScore;
	}

	public void setPmProfileDScore(Integer pmProfileDScore) {
		this.pmProfileDScore = pmProfileDScore;
	}

	public double getPmSignificance() {
		return this.pmSignificance;
	}

	public void setPmSignificance(double pmSignificance) {
		this.pmSignificance = pmSignificance;
	}

	@Override
	public JSONObject getJSON() {
		JSONObject factor = this.getCorefactor().getJSON();
		factor.put("pm_significance", this.pmSignificance);
		factor.put("pm_coefficient", this.pmCoefficient);
		factor.put("pm_score_a", this.pmProfileAScore);
		factor.put("pm_score_b", this.pmProfileBScore);
		factor.put("pm_score_c", this.pmProfileCScore);
		factor.put("pm_score_d", this.pmProfileDScore);
		return factor;
	}
}
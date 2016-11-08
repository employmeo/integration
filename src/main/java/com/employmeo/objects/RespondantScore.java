package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import lombok.ToString;

/**
 * The persistent class for the respondant_scores database table.
 * 
 */
@Entity
@Table(name = "respondant_scores")
@NamedQuery(name = "RespondantScore.findAll", query = "SELECT r FROM RespondantScore r")
@ToString(exclude = "respondant")
public class RespondantScore extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private RespondantScorePK id;

	@Column(name = "rs_cf_id", insertable = true, updatable = false)
	private Integer rsCfId;

	@Column(name = "rs_respondant_id", insertable = true, updatable = false)
	private Long rsRespondantId;

	@ManyToOne
	@JoinColumn(name = "rs_respondant_id", insertable = false, updatable = false)
	private Respondant respondant;

	@Column(name = "rs_question_count")
	private Integer rsQuestionCount;

	@Column(name = "rs_value")
	private double rsValue;

	public RespondantScore() {
	}

	public RespondantScorePK getId() {
		return this.id;
	}

	public void setId(RespondantScorePK id) {
		this.id = id;
		this.rsCfId = id.getRsCfId();
		this.rsRespondantId = id.getRsRespondantId();
		this.respondant = Respondant.getRespondantById(this.rsRespondantId);
	}

	public void setPK(int cfid, long respondantId) {
		RespondantScorePK pk = new RespondantScorePK();
		pk.setRsCfId(cfid);
		pk.setRsRespondantId(respondantId);
		setId(pk);
	}

	public Integer getRsCfId() {
		return this.rsCfId;
	}

	public Long getRsRespondantId() {
		return this.rsRespondantId;
	}

	public Respondant getRespondant() {
		return this.respondant;
	}

	public Integer getRsQuestionCount() {
		return this.rsQuestionCount;
	}

	public void setRsQuestionCount(Integer rsQuestionCount) {
		this.rsQuestionCount = rsQuestionCount;
	}

	public double getRsValue() {
		return this.rsValue;
	}

	public void setRsValue(double rsValue) {
		this.rsValue = rsValue;
	}

	public String getDescription() {
		return Corefactor.getCorefactorById(this.rsCfId).getDescriptionForScore(this.rsValue);
	}
	
	@Override
	public JSONObject getJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
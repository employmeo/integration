package com.employmeo.objects;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import lombok.Data;

/**
 * The persistent class for the predictive_model database table.
 * 
 */
@Entity
@Table(name = "predictions")
@Data
public class Prediction extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prediction_id")
	private Integer predictionId;

	@ManyToOne
	@JoinColumn(name = "respondant_id")
	private Respondant respondant;	
	
	@ManyToOne
	@JoinColumn(name = "position_prediction_config_id")
	private PositionPredictionConfiguration positionPredictionConfig;	

	@Column(name = "prediction_score")
	private Double predictionScore;
	
	@Column(name = "score_percentile")
	private Double scorePercentile;
	
	
	@Column(name = "active")
	private Boolean active = Boolean.TRUE;

	@Column(name = "created_date", insertable = false, updatable = false)
	private Date createdDate;

	public Prediction() {
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("prediction_id", this.predictionId);
		json.put("prediction_score", this.predictionScore);
		json.put("prediction_percentile", this.scorePercentile);
		json.put("label", positionPredictionConfig.getPredictionTarget().getLabel());
		json.put("model_id", positionPredictionConfig.getPredictionModel().getModelId());
		return json;
	}
}
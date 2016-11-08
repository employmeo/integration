package com.employmeo.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.json.JSONObject;

import lombok.Data;

/**
 * The persistent class for the predictive_model database table.
 * 
 */
@Entity
@Table(name = "prediction_targets")
@Data
public class PredictionTarget extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prediction_target_id")
	private Integer predictionTargetId;

	@Column(name = "name")
	private String name;

	@Column(name = "label")
	private String label;

	@Column(name = "description")
	private String description;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "created_date", insertable = false, updatable = false)
	private Date createdDate;

	@OneToMany(mappedBy = "predictionTarget", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private List<PositionPredictionConfiguration> positionTargets;
	
	@OneToMany(mappedBy = "predictionTarget", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private List<PredictionModel> predictionModels;	

	public PredictionTarget() {
	}

	@Override
	public JSONObject getJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
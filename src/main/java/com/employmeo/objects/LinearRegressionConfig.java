package com.employmeo.objects;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.json.JSONObject;

import lombok.Data;

/**
 * The persistent class for the predictive_model database table.
 * 
 */
@Entity
@Table(name = "linear_regression_config")
@NamedQuery(name = "LinearRegressionConfig.findByModelId", query = "SELECT lrc FROM LinearRegressionConfig lrc WHERE lrc.modelId = :modelId")
@Data
public class LinearRegressionConfig extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "config_id")
	private Long configId;
	
	@Column(name = "model_id")
	private Long modelId;	

	@Column(name = "corefactor_id")
	private Integer corefactorId;

	@Column(name = "coefficient")
	private Double coefficient;
	
	@Column(name = "significance")
	private Double significance;
	
	@Column(name = "exponent")
	private Double exponent;	
	
	@Column(name = "config_type")
	private Integer configTypeId;	

	@Column(name = "required")
	private Boolean required = Boolean.TRUE;
	
	@Column(name = "active")
	private Boolean active = Boolean.TRUE;

	@Column(name = "created_date")
	private Date createdDate;
	
	public LinearRegressionConfig() {
	}

    public ConfigType getConfigType() {
        return ConfigType.getConfigType(this.configTypeId);
    }
 
    public void setConfigType(ConfigType configType) {
        if (configType == null) {
            this.configTypeId = null;
        } else {
            this.configTypeId = configType.getTypeId();
        }
    }
    
	@Override
	public JSONObject getJSON() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static enum ConfigType {
		INTERCEPT(1),
		COEFFICIENT(2),
		MEAN(3),
		STD_DEV(4),
		POPULATION(5);
		
		private Integer typeId;
		
		private ConfigType(Integer typeId) {
			this.typeId = typeId;
		}
		
		public static ConfigType getConfigType(Integer typeId) {
		       
	        if (typeId == null) {
	            return null;
	        }
	 
	        for (ConfigType configType : ConfigType.values()) {
	            if (typeId.equals(configType.getTypeId())) {
	                return configType;
	            }
	        }
	        throw new IllegalArgumentException("No such ConfigType configured for id " + typeId);
	    }
	 
	    public int getTypeId() {
	        return typeId;
	    }		
	}

}
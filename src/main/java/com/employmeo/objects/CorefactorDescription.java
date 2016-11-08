package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import lombok.Data;


/**
 * The persistent class for the corefactor_descriptions database table.
 * 
 */
@Entity
@Table(name="corefactor_descriptions")
@NamedQuery(name="CorefactorDescription.findAll", query="SELECT c FROM CorefactorDescription c")
@Data
public class CorefactorDescription extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional=false)
	@Column(name="cfdesc_id")
	private Long cfdescId;

	@Column(name="cf_description")
	private String cfDescription;

	@Column(name="cf_high_end")
	private double cfHighEnd;

	// bi-directional many-to-one association to Corefactor
	@ManyToOne
	@JoinColumn(name = "cf_id",insertable=false,updatable=false)
	private Corefactor corefactor;
	
	@Column(name="cf_id")
	private Long cfId;

	@Column(name="cf_low_end")
	private double cfLowEnd;



	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		
		json.put("corefactor_description_id", this.cfdescId);
		json.put("corefactor_id", this.cfId);
		json.put("corefactor_description", this.cfDescription);
		json.put("corefactor_high_end", this.cfHighEnd);
		json.put("corefactor_low_end", this.cfLowEnd);
		
		return json;
	}
	
	public static CorefactorDescription fromJSON(JSONObject json) {
		CorefactorDescription corefactorDescription = new CorefactorDescription();
		
		corefactorDescription.setCfdescId(json.getLong("corefactor_description_id"));
		corefactorDescription.setCfId(json.getLong("corefactor_id"));
		corefactorDescription.setCfDescription(json.optString("corefactor_description",null));
		corefactorDescription.setCfHighEnd(json.optDouble("corefactor_high_end"));
		corefactorDescription.setCfLowEnd(json.optDouble("corefactor_low_end"));
		
		return corefactorDescription;
	}	

}
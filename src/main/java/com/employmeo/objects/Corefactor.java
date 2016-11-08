package com.employmeo.objects;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;

import lombok.Data;

/**
 * The persistent class for the corefactors database table.
 * 
 */
@Entity
@Table(name = "corefactors")
@NamedQueries({
	@NamedQuery(name = "Corefactor.findAll", query = "SELECT c FROM Corefactor c"),
	@NamedQuery(name = "Corefactor.findById", query = "SELECT c FROM Corefactor c WHERE c.corefactorId = :cfId")
})
@Data
public class Corefactor extends PersistantObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static List<Corefactor> corefactors = null;

	@Id
	@Basic(optional=false)
	@Column(name = "corefactor_id")
	private Integer corefactorId;

	@Column(name = "cf_high")
	private double cfHigh;

	@Column(name = "cf_high_description")
	private String cfHighDescription;

	@Column(name = "cf_low")
	private double cfLow;

	@Column(name = "cf_low_description")
	private String cfLowDescription;

	@Column(name = "cf_mean_score")
	private double cfMeanScore;

	@Column(name = "cf_measurements")
	private Long cfMeasurements;

	@Column(name = "cf_score_deviation")
	private double cfScoreDeviation;

	@Column(name = "cf_source")
	private String cfSource;

	@Column(name = "corefactor_description")
	private String corefactorDescription;

	@Column(name = "corefactor_name")
	private String corefactorName;

	@Column(name = "corefactor_foreign_id")
	private String corefactorForeignId;

	@Column(name = "cf_display_group")
	private String cfDisplayGroup;
	
	// bi-directional many-to-one association to Account
	@OneToMany(mappedBy = "corefactor", fetch = FetchType.EAGER)
	private List<CorefactorDescription> corefactorDescriptions;

	public String getDescriptionForScore (double score) {
		for (CorefactorDescription cfd : corefactorDescriptions) {
			if ((score >= cfd.getCfLowEnd()) && (score <= cfd.getCfHighEnd()))
				return cfd.getCfDescription();
		}
		return null;
	}
	
	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("corefactor_name", this.corefactorName);
		json.put("corefactor_display_group", this.cfDisplayGroup);
		json.put("corefactor_id", this.corefactorId);
		json.put("corefactor_description", this.corefactorDescription);
		json.put("corefactor_high", this.cfHigh);
		json.put("corefactor_low", this.cfLow);
		json.put("corefactor_high_desc", this.cfHighDescription);
		json.put("corefactor_low_desc", this.cfLowDescription);
		json.put("corefactor_mean_score", this.cfMeanScore);
		json.put("corefactor_score_deviation", this.cfScoreDeviation);
		json.put("corefactor_measurements", this.cfMeasurements);
		json.put("corefactor_source", this.cfSource);
		json.put("corefactor_foreign_id", this.corefactorForeignId);
		return json;
	}

	public static Corefactor fromJSON(JSONObject json) {
		Corefactor corefactor = new Corefactor();
		
		corefactor.setCorefactorId(json.getInt("corefactor_id"));
		corefactor.setCorefactorName(json.getString("corefactor_name"));
		corefactor.setCfDisplayGroup(json.optString("corefactor_display_group", null)); 
		corefactor.setCorefactorDescription(json.getString("corefactor_description"));
		corefactor.setCfHigh(json.optDouble("corefactor_high"));
		corefactor.setCfLow(json.optDouble("corefactor_low"));
		corefactor.setCfHighDescription(json.optString("corefactor_high_desc",null));
		corefactor.setCfLowDescription(json.optString("corefactor_low_desc",null));
		corefactor.setCfMeanScore(json.optDouble("corefactor_mean_score"));
		corefactor.setCfScoreDeviation(json.optDouble("corefactor_score_deviation"));
		corefactor.setCfMeasurements(json.optLong("corefactor_measurements"));
		corefactor.setCfSource(json.optString("corefactor_source",null));
		corefactor.setCorefactorForeignId(json.optString("corefactor_foreign_id",null));
		
		return corefactor;
	}
	
	public static List<Corefactor> getAllCorefactors() {
		if (corefactors == null) {
			EntityManager em = DBUtil.getEntityManager();
			TypedQuery<Corefactor> q = em.createQuery("SELECT c FROM Corefactor c", Corefactor.class);
			try {
				corefactors = q.getResultList();
			} catch (NoResultException nre) {
			}
		}
		return corefactors;
	}

	public static Corefactor getCorefactorById(int lookupId) {

		EntityManager em = DBUtil.getEntityManager();
		return em.find(Corefactor.class, lookupId);
	}

	public static Corefactor getCorefactorByForeignId(String id) {
		Corefactor corefactor = null;
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Corefactor> q = em.createQuery("SELECT c FROM Corefactor c WHERE c.corefactorForeignId = :id", Corefactor.class);
		q.setParameter("id", id);
		corefactor =  q.getSingleResult();
		return corefactor;
	}



}
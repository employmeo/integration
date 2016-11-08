package com.employmeo.objects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;

/**
 * The persistent class for the positions database table.
 * 
 */
@Entity
@Table(name = "positions")
@NamedQuery(name = "Position.findAll", query = "SELECT p FROM Position p")
public class Position extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "position_id")
	private Long positionId;

	@Column(name = "position_name")
	private String positionName;

	@Column(name = "position_description")
	private String positionDescription;

	@Column(name = "position_target_hireratio")
	private BigDecimal positionTargetHireratio;

	@Column(name = "position_target_tenure")
	private BigDecimal positionTargetTenure;

	// bi-directional many-to-one association to Account
	@ManyToOne
	@JoinColumn(name = "position_account")
	private Account account;

	// bi-directional many-to-one association to Account
	@OneToMany(mappedBy = "position", fetch = FetchType.EAGER)
	private List<PredictiveModel> pmFactors;

	@OneToMany(mappedBy = "position", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<PositionPredictionConfiguration> positionPredictionConfigs;

	public Position() {
	}

	public Long getPositionId() {
		return this.positionId;
	}

	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}

	public String getPositionName() {
		return this.positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getPositionDescription() {
		return this.positionDescription;
	}

	public void setPositionDescription(String positionDescription) {
		this.positionDescription = positionDescription;
	}

	public BigDecimal getPositionTargetHireratio() {
		return this.positionTargetHireratio;
	}

	public void setPositionTargetHireratio(BigDecimal positionTargetHireratio) {
		this.positionTargetHireratio = positionTargetHireratio;
	}

	public BigDecimal getPositionTargetTenure() {
		return this.positionTargetTenure;
	}

	public void setPositionTargetTenure(BigDecimal positionTargetTenure) {
		this.positionTargetTenure = positionTargetTenure;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public static Position getPositionById(String lookupId) {

		return getPositionById(new Long(lookupId));

	}

	public static Position getPositionById(Long lookupId) {
		EntityManager em = DBUtil.getEntityManager();
		return em.find(Position.class, lookupId);
	}

	public List<PredictiveModel> getPmFactors() {
		return this.pmFactors;
	}

	public List<PositionPredictionConfiguration> getPositionPredictionConfigs() {
		return positionPredictionConfigs;
	}

	public void setPositionPredictionConfigs(List<PositionPredictionConfiguration> positionPredictionConfigs) {
		this.positionPredictionConfigs = positionPredictionConfigs;
	}

	public List<Corefactor> getCorefactors() {
		List<Corefactor> corefactors = new ArrayList<Corefactor>();
		for (int i = 0; i < this.pmFactors.size(); i++) {
			corefactors.add(pmFactors.get(i).getCorefactor());
		}
		return corefactors;
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("position_id", this.positionId);
		json.put("position_name", this.positionName);
		json.put("position_description", this.positionDescription);
		json.put("position_target_hireratio", this.positionTargetHireratio);
		json.put("position_target_tenure", this.positionTargetTenure);

		if (this.account != null)
			json.put("position_account", this.account.getJSON());

		if (!pmFactors.isEmpty()) {
			for (int i = 0; i < this.pmFactors.size(); i++) {
				json.accumulate("position_corefactors", pmFactors.get(i).getJSON());
			}
			json.accumulate("position_profiles", PositionProfile.getProfileA(this));
			json.accumulate("position_profiles", PositionProfile.getProfileB(this));
			json.accumulate("position_profiles", PositionProfile.getProfileC(this));
			json.accumulate("position_profiles", PositionProfile.getProfileD(this));
		}

		return json;
	}

}
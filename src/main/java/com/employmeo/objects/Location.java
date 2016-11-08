package com.employmeo.objects;

import java.io.Serializable;

import javax.persistence.*;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;

/**
 * The persistent class for the locations database table.
 * 
 */
@Entity
@Table(name = "locations")
@NamedQuery(name = "Location.findAll", query = "SELECT l FROM Location l")
public class Location extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "location_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long locationId;

	@Column(name = "location_name")
	private String locationName;

	// bi-directional many-to-one association to Account
	@ManyToOne
	@JoinColumn(name = "location_account_id", insertable = false, updatable = false)
	private Account account;

	@Column(name = "location_account_id", insertable = true, updatable = false)
	private Long locationAccountId;

	@Column(name = "location_city")
	private String locationCity;

	@Column(name = "location_fein")
	private String locationFein;

	@Column(name = "location_lat")
	private double locationLat;

	@Column(name = "location_long")
	private double locationLong;

	@Column(name = "location_state")
	private String locationState;

	@Column(name = "location_street1")
	private String locationStreet1;

	@Column(name = "location_street2")
	private String locationStreet2;

	@Column(name = "location_zip")
	private String locationZip;

	@Column(name = "location_ats_id")
	private String locationAtsId;

	@Column(name = "location_payroll_id")
	private String locationPayrollId;

	public Location() {
	}

	public Long getLocationId() {
		return this.locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public Long getLocationAccountId() {
		return this.locationAccountId;
	}

	public void setLocationAccountId(Long locationAccountId) {
		this.locationAccountId = locationAccountId;
		this.account = Account.getAccountById(locationAccountId);
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationCity() {
		return this.locationCity;
	}

	public void setLocationCity(String locationCity) {
		this.locationCity = locationCity;
	}

	public String getLocationFein() {
		return this.locationFein;
	}

	public void setLocationFein(String locationFein) {
		this.locationFein = locationFein;
	}

	public double getLocationLat() {
		return this.locationLat;
	}

	public void setLocationLat(double locationLat) {
		this.locationLat = locationLat;
	}

	public double getLocationLong() {
		return this.locationLong;
	}

	public void setLocationLong(double locationLong) {
		this.locationLong = locationLong;
	}

	public String getLocationState() {
		return this.locationState;
	}

	public void setLocationState(String locationState) {
		this.locationState = locationState;
	}

	public String getLocationStreet1() {
		return this.locationStreet1;
	}

	public void setLocationStreet1(String locationStreet1) {
		this.locationStreet1 = locationStreet1;
	}

	public String getLocationStreet2() {
		return this.locationStreet2;
	}

	public void setLocationStreet2(String locationStreet2) {
		this.locationStreet2 = locationStreet2;
	}

	public String getLocationZip() {
		return this.locationZip;
	}

	public void setLocationZip(String locationZip) {
		this.locationZip = locationZip;
	}

	public String getLocationAtsId() {
		return this.locationAtsId;
	}

	public void setLocationAtsId(String atsId) {
		this.locationAtsId = atsId;
	}

	public String getLocationPayrollId() {
		return this.locationPayrollId;
	}

	public void setLocationPayrollId(String payrollId) {
		this.locationPayrollId = payrollId;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.locationAccountId = account.getAccountId();
		this.account = account;
	}

	public static Location getLocationById(Long lookupId) {
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Location> q = em.createQuery("SELECT l FROM Location l WHERE l.locationId = :locationId",
				Location.class);
		q.setParameter("locationId", lookupId);
		Location location = null;
		try {
			location = q.getSingleResult();
		} catch (NoResultException nre) {
		}

		return location;
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("location_name", this.locationName);
		json.put("location_id", this.locationId);
		json.put("location_ats_id", this.locationAtsId);
		json.put("location_payroll_id", this.locationPayrollId);
		json.put("location_account_id", this.locationAccountId);
		json.put("location_street1", this.locationStreet1);
		json.put("location_street2", this.locationStreet2);
		json.put("location_city", this.locationCity);
		json.put("location_state", this.locationState);
		json.put("location_zip", this.locationZip);
		json.put("location_lat", this.locationLat);
		json.put("location_long", this.locationLong);
		return json;
	}

}
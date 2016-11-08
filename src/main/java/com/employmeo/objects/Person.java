package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import java.util.List;

/**
 * The persistent class for the persons database table.
 * 
 */
@Entity
@Table(name = "persons")
@NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p")
public class Person extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "person_id")
	private Integer personId;

	@Column(name = "person_email")
	private String personEmail;

	@Column(name = "person_fname")
	private String personFname;

	@Column(name = "person_lname")
	private String personLname;

	@Column(name = "person_ssn")
	private String personSsn;

	@Column(name = "person_address")
	private String personAddress;

	@Column(name = "person_lat")
	private double personLat;

	@Column(name = "person_long")
	private double personLong;

	@Column(name = "person_ats_id")
	private String personAtsId;

	// bi-directional many-to-one association to Respondant
	@OneToMany(mappedBy = "person")
	private List<Respondant> respondants;

	public Person() {
	}

	public Integer getPersonId() {
		return this.personId;
	}

	public void setPersonId(Integer personId) {
		this.personId = personId;
	}

	public String getPersonAddress() {
		return this.personAddress;
	}

	public void setPersonAddress(String address) {
		this.personAddress = address;
	}

	public String getPersonEmail() {
		return this.personEmail;
	}

	public void setPersonEmail(String personEmail) {
		this.personEmail = personEmail;
	}

	public String getPersonFname() {
		return this.personFname;
	}

	public void setPersonFname(String personFname) {
		this.personFname = personFname;
	}

	public String getPersonLname() {
		return this.personLname;
	}

	public void setPersonLname(String personLname) {
		this.personLname = personLname;
	}

	public String getPersonFullName() {
		return this.personFname + " " + this.personLname;
	}
	public String getPersonSsn() {
		return this.personSsn;
	}

	public void setPersonSsn(String personSsn) {
		this.personSsn = personSsn;
	}

	public String getPersonAtsId() {
		return this.personAtsId;
	}

	public void setPersonAtsId(String personAtsId) {
		this.personAtsId = personAtsId;
	}
	public double getPersonLat() {
		return this.personLat;
	}

	public void setPersonLat(double personLat) {
		this.personLat = personLat;
	}

	public double getPersonLong() {
		return this.personLong;
	}

	public void setPersonLong(double personLong) {
		this.personLong = personLong;
	}

	public List<Respondant> getRespondants() {
		return this.respondants;
	}

	public void setRespondants(List<Respondant> respondants) {
		this.respondants = respondants;
	}

	public Respondant addRespondant(Respondant respondant) {
		getRespondants().add(respondant);
		respondant.setPerson(this);

		return respondant;
	}

	public Respondant removeRespondant(Respondant respondant) {
		getRespondants().remove(respondant);
		respondant.setPerson(null);

		return respondant;
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("person_id", this.personId);
		json.put("person_email", this.personEmail);
		json.put("person_fname", this.personFname);
		json.put("person_lname", this.personLname);
		json.put("person_address", this.personAddress);
		json.put("person_lat", this.personLat);
		json.put("person_long", this.personLong);

		return json;
	}

}
package com.employmeo.objects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.employmeo.util.DBUtil;
import com.employmeo.util.PredictionUtil;
import com.employmeo.util.ScoringUtil;

import lombok.ToString;

/**
 * The persistent class for the respondants database table.
 * 
 */
@Entity
@Table(name = "respondants")
@NamedQueries({
	@NamedQuery(name = "Respondant.findAll", query = "SELECT r FROM Respondant r"),
	@NamedQuery(name = "Respondant.findById", query = "SELECT r FROM Respondant r WHERE r.respondantId = :respondantId")
})
@ToString(of = {"respondantId", "respondantUuid", "respondantAccountId", "respondantAsid", "respondantStatus", "respondantPartnerId","person","respondantScores"})
public class Respondant extends PersistantObject implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(Respondant.class);
	private static final long serialVersionUID = 1L;

	public static final int STATUS_INVITED = 1;
	public static final int STATUS_STARTED = 5;
	public static final int STATUS_COMPLETED = 10;
	public static final int STATUS_SCORED = 13;
	public static final int STATUS_PREDICTED = 15;
	public static final int STATUS_REJECTED = 16;
	public static final int STATUS_OFFERED = 17;
	public static final int STATUS_DECLINED = 18;
	public static final int STATUS_HIRED = 20;
	public static final int STATUS_QUIT = 30;
	public static final int STATUS_TERMINATED = 40;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "respondant_id")
	private Long respondantId;

	@Column(name = "respondant_uuid", insertable = false, updatable = false, columnDefinition = "UUID")
	@Convert(converter = com.employmeo.util.UuidConverter.class)
	private UUID respondantUuid;

	// bi-directional many-to-one association to Account
	@ManyToOne
	@JoinColumn(name = "respondant_account_id", insertable = false, updatable = false)
	private Account account;

	@Column(name = "respondant_account_id", insertable = true, updatable = false)
	private Long respondantAccountId;

	@Column(name = "respondant_created_date", insertable = false, updatable = false)
	private Date respondantCreatedDate;

	@Column(name = "respondant_status")
	private int respondantStatus = Respondant.STATUS_INVITED;

	@ManyToOne
	@JoinColumn(name = "respondant_asid", insertable = false, updatable = false)
	private AccountSurvey accountSurvey;

	@Column(name = "respondant_asid", insertable = true, updatable = false)
	private Long respondantAsid;

	@ManyToOne
	@JoinColumn(name = "respondant_partner_id", insertable = false, updatable = false)
	private Partner partner;

	@Column(name = "respondant_partner_id", insertable = true, updatable = true)
	private Integer respondantPartnerId;

	@ManyToOne
	@JoinColumn(name = "respondant_position_id", insertable = false, updatable = false)
	private Position position;

	@Column(name = "respondant_position_id", insertable = true, updatable = true)
	private Long respondantPositionId;

	@ManyToOne
	@JoinColumn(name = "respondant_location_id", insertable = false, updatable = false)
	private Location location;

	@Column(name = "respondant_location_id", insertable = true, updatable = true)
	private Long respondantLocationId;

	// bi-directional many-to-one association to Person
	@ManyToOne
	@JoinColumn(name = "respondant_person_id")
	private Person person;

	// bi-directional many-to-one association to Responses
	@OneToMany(mappedBy = "respondant")
	private List<Response> responses;

	// bi-directional many-to-one association to Responses
	@OneToMany(mappedBy = "respondant", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<RespondantScore> respondantScores;
	
	@OneToMany(mappedBy = "respondant", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<Prediction> predictions;	

	// Scoring info
	@Column(name = "respondant_profile")
	private String respondantProfile;

	@Column(name = "respondant_composite_score")
	private Double compositeScore;

	@Column(name = "respondant_profile_a")
	private Double profileA;

	@Column(name = "respondant_profile_b")
	private Double profileB;

	@Column(name = "respondant_profile_c")
	private Double profileC;

	@Column(name = "respondant_profile_d")
	private Double profileD;

	@Column(name = "respondant_ats_id")
	private String respondantAtsId;

	@Column(name = "respondant_payroll_id")
	private String respondantPayrollId;

	@Column(name = "respondant_redirect_page")
	private String respondantRedirectUrl;

	@Column(name = "respondant_email_recipient")
	private String respondantEmailRecipient;

	@Column(name = "respondant_score_postmethod")
	private String respondantScorePostMethod;

	// Scoring info
	@Column(name = "respondant_user_agent")
	private String respondantUserAgent;

	@Column(name = "respondant_start_time")
	private Timestamp respondantStartTime;

	@Column(name = "respondant_finish_time")
	private Timestamp respondantFinishTime;

	@Column(name = "respondant_hire_date")
	private Date respondantHireDate;

	public Respondant() {
	}

	public Long getRespondantId() {
		return this.respondantId;
	}

	public void setRespondantId(Long respondantId) {
		this.respondantId = respondantId;
	}

	public UUID getRespondantUuid() {
		return this.respondantUuid;
	}

	public Long getRespondantAccountId() {
		return this.respondantAccountId;
	}

	public Account getRespondantAccount() {
		if (this.account == null)
			this.account = Account.getAccountById(this.respondantAccountId);
		return this.account;
	}

	public void setRespondantAccountId(Long accountId) {
		this.respondantAccountId = accountId;
		this.account = Account.getAccountById(accountId);
	}

	public void setAccount(Account account) {
		this.account = account;
		this.respondantAccountId = account.getAccountId();
	}

	public Long getRespondantLocationId() {
		return this.respondantLocationId;
	}

	public Location getLocation() {
		if (this.location == null)
			this.location = Location.getLocationById(this.respondantLocationId);
		return this.location;
	}

	public void setRespondantLocationId(Long locationId) {
		this.respondantLocationId = locationId;
		this.location = Location.getLocationById(this.respondantLocationId);
	}

	public Long getRespondantPositionId() {
		if (this.respondantPositionId == null)
			this.respondantPositionId = this.position.getPositionId();
		return this.respondantPositionId;
	}

	public Position getPosition() {
		if (this.position == null)
			this.position = Position.getPositionById(this.respondantPositionId);
		return this.position;
	}

	public void setRespondantPositionId(Long positionId) {
		this.respondantPositionId = positionId;
		this.position = Position.getPositionById(positionId);
	}

	public void setPosition(Position position) {
		this.respondantPositionId = position.getPositionId();
		this.position = position;
	}

	public Integer getRespondantPartnerId() {
		if ((this.respondantPartnerId == null) && (this.partner != null))
			this.respondantPartnerId = this.partner.getPartnerId();
		return this.respondantPartnerId;
	}

	public Partner getPartner() {
		if ((this.partner == null) && (this.respondantPartnerId != null))
			this.partner = Partner.getPartnerById(this.respondantPartnerId);
		return this.partner;
	}

	public void setRespondantPartnerId(Integer partnerId) {
		this.respondantPartnerId = partnerId;
		this.partner = Partner.getPartnerById(partnerId);
	}

	public void setPartner(Partner partner) {
		this.respondantPartnerId = partner.getPartnerId();
		this.partner = partner;
	}

	public Date getRespondantCreatedDate() {
		return this.respondantCreatedDate;
	}

	public void setRespondantCreatedDate(Date respondantCreatedDate) {
		this.respondantCreatedDate = respondantCreatedDate;
	}

	public Date getRespondantHireDate() {
		return this.respondantHireDate;
	}

	public void setRespondantHireDate(Date respondantHireDate) {
		this.respondantHireDate = respondantHireDate;
	}

	public int getRespondantStatus() {
		return this.respondantStatus;
	}

	public void setRespondantStatus(int respondantStatus) {
		this.respondantStatus = respondantStatus;
	}

	public String getRespondantStatusText() {
		String text = null;

		switch (this.respondantStatus) {
		case STATUS_INVITED:
			text = "Invited";
			break;
		case STATUS_STARTED:
			text = "Started";
			break;
		case STATUS_COMPLETED:
			text = "Completed";
			break;
		case STATUS_SCORED:
		case STATUS_PREDICTED:
			text = "Scored";
			break;
		case STATUS_REJECTED:
			text = "Rejected";
			break;
		case STATUS_OFFERED:
			text = "Offered";
			break;
		case STATUS_DECLINED:
			text = "Declined Offer";
			break;
		case STATUS_HIRED:
			text = "Hired";
			break;
		case STATUS_QUIT:
			text = "Quit";
			break;
		case STATUS_TERMINATED:
			text = "Terminated";
			break;
		default:
			break;

		}

		return text;
	}

	public String getRespondantProfile() {
		return this.respondantProfile;
	}

	public void setRespondantProfile(String profile) {
		this.respondantProfile = profile;
	}

	public String getProfileLabel() {
		if(getRespondantProfile()!= null) return PositionProfile.getProfileDefaults(getRespondantProfile()).getString("profile_name");
		return null;
	}

	public Double getCompositeScore() {
		return this.compositeScore;
	}

	public void setCompositeScore(Double score) {
		this.compositeScore = score;
	}

	public Double getProfileA() {
		return this.profileA;
	}

	public void setProfileA(Double probability) {
		this.profileA = probability;
	}

	public Double getProfileB() {
		return this.profileB;
	}

	public void setProfileB(Double probability) {
		this.profileB = probability;
	}

	public Double getProfileC() {
		return this.profileC;
	}

	public void setProfileC(Double probability) {
		this.profileC = probability;
	}

	public Double getProfileD() {
		return this.profileD;
	}

	public void setProfileD(Double probability) {
		this.profileD = probability;
	}

	public Long getRespondantSurveyId() {
		return getSurvey().getSurveyId();
	}

	public Survey getSurvey() {
		return getAccountSurvey().getSurvey();
	}

	public AccountSurvey getAccountSurvey() {
		if (this.accountSurvey == null)
			this.accountSurvey = AccountSurvey.getAccountSurveyByASID(this.respondantAsid);
		return this.accountSurvey;
	}

	public void setAccountSurvey(AccountSurvey accountSurvey) {
		this.accountSurvey = accountSurvey;
		this.respondantAsid = accountSurvey.getAsId();
	}

	public void setRespondantAsid(Long asid) {
		this.respondantAsid = asid;
		this.accountSurvey = AccountSurvey.getAccountSurveyByASID(this.respondantAsid);
	}

	public Person getPerson() {
		return this.person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Response> getResponses() {
		return this.responses;
	}

	public void addResponse(Response response) {
		this.responses.add(response);
	}

	public List<RespondantScore> getRespondantScores() {
		return this.respondantScores;
	}

	public void addRespondantScore(RespondantScore score) {
		this.respondantScores.add(score);
	}

	public void setRespondantAtsId(String atsId) {
		this.respondantAtsId = atsId;
	}

	public String getRespondantAtsId() {
		return this.respondantAtsId;
	}

	public void setRespondantPayrollId(String payrollId) {
		this.respondantAtsId = payrollId;
	}

	public String getRespondantPayrollId() {
		return this.respondantAtsId;
	}

	public String getRespondantRedirectUrl() {
		if (this.respondantRedirectUrl != null)
			return this.respondantRedirectUrl;
		return this.accountSurvey.getAsRedirectPage();
	}

	public void setRespondantRedirectUrl(String respondantRedirectUrl) {
		this.respondantRedirectUrl = respondantRedirectUrl;
	}

	public String getRespondantScorePostMethod() {
		return this.respondantScorePostMethod;
	}

	public void setRespondantScorePostMethod(String respondantScorePostMethod) {
		this.respondantScorePostMethod = respondantScorePostMethod;
	}

	public String getRespondantEmailRecipient() {
		return this.respondantEmailRecipient;
	}

	public void setRespondantEmailRecipient(String respondantEmailRecipient) {
		this.respondantEmailRecipient = respondantEmailRecipient;
	}

	public String getRespondantUserAgent() {
		return this.respondantUserAgent;
	}

	public void setRespondantUserAgent(String userAgent) {
		this.respondantUserAgent = userAgent;
	}

	public void setRespondantStartTime(Timestamp start) {
		this.respondantStartTime = start;
	}

	public Timestamp getRespondantStartTime() {
		return this.respondantStartTime;
	}

	public void setRespondantFinishTime(Timestamp finish) {
		this.respondantFinishTime = finish;
	}

	public Timestamp getRespondantFinishTime() {
		return this.respondantFinishTime;
	}
	
	public List<Prediction> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<Prediction> predictions) {
		this.predictions = predictions;
	}	

	public static Respondant getRespondantById(String lookupId) {
		return getRespondantById(new Long(lookupId));
	}

	public static Respondant getRespondantById(Long lookupId) {
		EntityManager em = DBUtil.getEntityManager();
		return em.find(Respondant.class, lookupId);
	}

	public static Respondant getRespondantByUuid(UUID uuid) {
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Respondant> q = em.createQuery("SELECT r FROM Respondant r WHERE r.respondantUuid = :uuId",
				Respondant.class);
		q.setParameter("uuId", uuid);
		Respondant respondant = null;
		try {
			respondant = q.getSingleResult();
		} catch (Exception e) {
			// Return null.
		}
		return respondant;
	}

	public JSONObject getJSON() {
		//log.trace("Getting JSON for respondant: {}", this.respondantId);
		JSONObject json = new JSONObject();
		json.put("respondant_id", this.respondantId);
		json.put("respondant_uuid", this.respondantUuid.toString());
		json.put("respondant_created_date", this.respondantCreatedDate);
		json.put("respondant_status", this.respondantStatus);
		json.put("respondant_status_text", this.getRespondantStatusText());
		json.put("respondant_profile", this.respondantProfile);
		json.put("respondant_composite_score", this.compositeScore);
		json.put("respondant_profile_label", this.getProfileLabel());
		json.put("respondant_profile_a", this.profileA);
		json.put("respondant_profile_b", this.profileB);
		json.put("respondant_profile_c", this.profileC);
		json.put("respondant_profile_d", this.profileD);
		json.put("respondant_redirect_url", getRespondantRedirectUrl());

		if (this.account != null)
			json.put("respondant_account_id", this.account.getAccountId());
		if (getAccountSurvey() != null) {
			json.put("respondant_asid", this.respondantAsid);
			json.put("respondant_survey_name", this.getAccountSurvey().getSurveyName());
		}
		if (this.getRespondantLocationId() != null)
			json.put("respondant_location_id", this.getRespondantLocationId());
		if (this.getLocation() != null)
			json.put("respondant_location_name", this.location.getLocationName());
		if (this.getRespondantPositionId() != null)
			json.put("respondant_position_id", this.getRespondantPositionId());
		if (this.getPosition() != null)
			json.put("respondant_position_name", this.position.getPositionName());

		if (this.person != null) {
			json.put("respondant_person_fname", this.person.getPersonFname());
			json.put("respondant_person_lname", this.person.getPersonLname());
			json.put("respondant_person_email", this.person.getPersonEmail());
			json.put("respondant_person_address", this.person.getPersonAddress());
		}

		if (this.respondantProfile != null) {
			PositionProfile profile = PositionProfile.getProfileDefaults(this.getRespondantProfile());
			json.put("respondant_profile_icon", profile.get("profile_icon"));
			json.put("respondant_profile_class", profile.get("profile_class"));
		}

		if (!this.predictions.isEmpty()) {
			JSONArray jPredictions = new JSONArray();
			for (Prediction prediction : this.predictions) {
				jPredictions.put(prediction.getJSON());
			}
			json.put("predictions", jPredictions);		
		}
		return json;
	}

	/***
	 * Special section for unique functionality for the Respondant Object
	 */

	public JSONObject getAssessmentScore() {
		//log.trace("Getting assessment score for respondant {}", this);
		if (getRespondantStatus() <= Respondant.STATUS_COMPLETED)
			this.refreshMe();

		JSONObject scores = new JSONObject();
		if (this.getRespondantStatus() < Respondant.STATUS_COMPLETED) {
			return scores; // return no scores when survey incomplete
		} else if (this.getRespondantStatus() == Respondant.STATUS_COMPLETED) {
			ScoringUtil.scoreAssessment(this);
		}

		if (this.getRespondantStatus() == Respondant.STATUS_SCORED)
			PredictionUtil.predictRespondant(this);

		if (this.getRespondantStatus() >= Respondant.STATUS_SCORED) {
			for (int i = 0; i < getRespondantScores().size(); i++) {
				Corefactor corefactor = Corefactor.getCorefactorById(getRespondantScores().get(i).getRsCfId());
				scores.put(corefactor.getCorefactorName(), getRespondantScores().get(i).getRsValue());
			}
		}

		return scores;
	}

	public JSONArray getAssessmentDetailedScore() {
		//log.trace("Getting assessment detailed score for respondant {}", this);
		if (getRespondantStatus() <= Respondant.STATUS_COMPLETED)
			this.refreshMe();

		JSONArray scores = new JSONArray();
		if (this.getRespondantStatus() < Respondant.STATUS_COMPLETED) {
			return scores; // return no scores when survey incomplete
		} else if (this.getRespondantStatus() == Respondant.STATUS_COMPLETED) {
			ScoringUtil.scoreAssessment(this);
		}

		if (this.getRespondantStatus() == Respondant.STATUS_SCORED)
			PredictionUtil.predictRespondant(this);

		if (this.getRespondantStatus() >= Respondant.STATUS_SCORED) {
			for (int i = 0; i < getRespondantScores().size(); i++) {
				Corefactor corefactor = Corefactor.getCorefactorById(getRespondantScores().get(i).getRsCfId());
				JSONObject score = corefactor.getJSON();
				score.put("cf_description",
						corefactor.getDescriptionForScore(getRespondantScores().get(i).getRsValue()));
				score.put("cf_score", getRespondantScores().get(i).getRsValue());
				scores.put(score);
			}
		}

		return scores;
	}



}
package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the accounts database table.
 * 
 */
@Entity
@Table(name = "accounts")
@NamedQuery(name = "Account.findAll", query = "SELECT a FROM Account a")
public class Account extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id")
	private Long accountId;

	@Column(name = "account_ats_partner")
	private Long accountAtsPartner;

	@Column(name = "account_default_email")
	private String accountDefaultEmail;

	@Column(name = "account_default_redirect")
	private String accountDefaultRedirect;

	@Column(name = "account_feature_scoring")
	private Boolean accountFeatureScoring;

	@Column(name = "account_sentby_text")
	private String accountSentbyText;

	@Column(name = "account_name")
	private String accountName;

	@Column(name = "account_status")
	private int accountStatus;

	@Column(name = "account_type")
	private int accountType;

	@Column(name = "account_ats_id")
	private String accountAtsId;

	@Column(name = "account_default_location_id")
	private long accountDefaultLocationId;

	@Column(name = "account_default_position_id")
	private long accountDefaultPositionId;

	@Column(name = "account_default_asid")
	private long accountDefaultAsId;

	// bi-directional many-to-one association to Survey
	@OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<AccountSurvey> accountSurveys;

	// bi-directional many-to-one association to Position
	@OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<Position> positions;

	// bi-directional many-to-one association to Position
	@OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<Location> locations;

	// bi-directional many-to-one association to User
	@OneToMany(mappedBy = "account")
	private List<User> users;

	// bi-directional many-to-one association to Position
	@OneToMany(mappedBy = "account")
	// @OrderBy("respondant.respondantCreatedDate DESC")
	private List<Respondant> respondants;

	// bi-directional many-to-one association to BillingItem
	@OneToMany(mappedBy = "account")
	private List<BillingItem> billingItems;

	public Account() {
	}

	public Long getAccountId() {
		return this.accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return this.accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public int getAccountStatus() {
		return this.accountStatus;
	}

	public void setAccountStatus(int accountStatus) {
		this.accountStatus = accountStatus;
	}

	public int getAccountType() {
		return this.accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public List<AccountSurvey> getAccountSurveys() {
		return this.accountSurveys;
	}

	public List<Survey> getSurveys() {
		List<Survey> surveyset = new ArrayList<Survey>();
		for (int i = 0; i < this.accountSurveys.size(); i++)
			surveyset.add(accountSurveys.get(i).getSurvey());
		return surveyset;
	}

	public Survey getDefaultSurvey() {
		return AccountSurvey.getSurveyByASID(this.accountDefaultAsId);
	}

	public AccountSurvey getDefaultAccountSurvey() {
		return AccountSurvey.getAccountSurveyByASID(this.accountDefaultAsId);
	}

	/*
	 * At some point we'll need to create logic for setting one of the existing
	 * account surveys as default, and logic to put a new survey into the
	 * account
	 * 
	 * public void setDefaultSurveyId(long surveyId) { AccountSurvey as = new
	 * AccountSurvey(); AccountSurvey.getSurveyByASID(this.accountDefaultAsId);
	 * }
	 */

	public void setDefaultLocation(long locationId) {
		this.accountDefaultLocationId = locationId;
	}

	public Location getDefaultLocation() {
		return Location.getLocationById(this.accountDefaultLocationId);
	}

	public void setDefaultPosition(long positionId) {
		this.accountDefaultPositionId = positionId;
	}

	public Position getDefaultPosition() {
		return Position.getPositionById(this.accountDefaultPositionId);
	}

	public String getAccountAtsId() {
		return this.accountAtsId;
	}

	public void setAccountAtsId(String atsId) {
		this.accountAtsId = atsId;
	}

	public Long getAccountAtsPartner() {
		return this.accountAtsPartner;
	}

	public void setAccountAtsPartner(Long accountAtsPartner) {
		this.accountAtsPartner = accountAtsPartner;
	}

	public String getAccountDefaultEmail() {
		return this.accountDefaultEmail;
	}

	public void setAccountDefaultEmail(String accountDefaultEmail) {
		this.accountDefaultEmail = accountDefaultEmail;
	}

	public String getAccountDefaultRedirect() {
		return this.accountDefaultRedirect;
	}

	public void setAccountDefaultRedirect(String accountDefaultRedirect) {
		this.accountDefaultRedirect = accountDefaultRedirect;
	}

	public Boolean getAccountFeatureScoring() {
		return this.accountFeatureScoring;
	}

	public void setAccountFeatureScoring(Boolean accountFeatureScoring) {
		this.accountFeatureScoring = accountFeatureScoring;
	}

	public String getAccountSentbyText() {
		return this.accountSentbyText;
	}

	public void setAccountSentbyText(String accountSentbyText) {
		this.accountSentbyText = accountSentbyText;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public User addUser(User user) {
		getUsers().add(user);
		user.setAccount(this);

		return user;
	}

	public User removeUser(User user) {
		getUsers().remove(user);
		user.setAccount(null);

		return user;
	}

	public List<Position> getPositions() {
		return this.positions;
	}

	public void setPositions(List<Position> positions) {
		this.positions = positions;
	}

	public Position addPosition(Position position) {
		getPositions().add(position);
		position.setAccount(this);

		return position;
	}

	public Position removePosition(Position position) {
		getPositions().remove(position);
		position.setAccount(null);

		return position;
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public Location addLocation(Location location) {
		getLocations().add(location);
		location.setAccount(this);

		return location;
	}

	public Location removeLocation(Location location) {
		getLocations().remove(location);
		location.setAccount(null);

		return location;
	}

	public List<BillingItem> getBillingItems() {
		return this.billingItems;
	}

	public void setBillingItems(List<BillingItem> billingItems) {
		this.billingItems = billingItems;
	}

	public BillingItem addBillingItem(BillingItem billingItem) {
		getBillingItems().add(billingItem);
		billingItem.setAccount(this);

		return billingItem;
	}

	public BillingItem removeBillingItem(BillingItem billingItem) {
		getBillingItems().remove(billingItem);
		billingItem.setAccount(null);

		return billingItem;
	}

	public static Account getAccountById(String lookupId) {
		return getAccountById(new Long(lookupId));
	}

	public static Account getAccountById(Long lookupId) {
		EntityManager em = DBUtil.getEntityManager();
		return em.find(Account.class, lookupId);
	}

	public Respondant getRespondantByPayrollId(String payrollId) {
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Respondant> q = em.createQuery("SELECT r FROM Respondant r " + 
				"WHERE r.respondantPayrollId = :payrollId AND r.respondantAccountId = :accountId",
				Respondant.class);
		q.setParameter("payrollId", payrollId);
		q.setParameter("accountId", this.accountId);
		Respondant respondant = null;
		try {
			respondant = q.getSingleResult();
		} catch (Exception e) {
			// Return null.
		}
		return respondant;
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("account_id", this.accountId);
		json.put("account_name", this.accountName);
		json.put("account_status", this.accountStatus);
		json.put("account_type", this.accountType);
		json.put("account_ats_id", this.accountAtsId);
		json.put("account_default_email",this.accountDefaultEmail);
		json.put("account_default_location_id",this.accountDefaultLocationId);
		json.put("account_default_position_id",this.accountDefaultPositionId);
		json.put("account_feature_scoring",this.accountFeatureScoring);
		json.put("account_default_redirect",this.accountDefaultRedirect);
		json.put("account_sentby_text",this.accountSentbyText);
		json.put("account_default_asid",this.accountDefaultAsId);
		
		return json;
	}

}
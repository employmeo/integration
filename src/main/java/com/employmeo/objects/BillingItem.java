package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import com.employmeo.objects.Account;

import java.sql.Timestamp;

/**
 * The persistent class for the billing_item database table.
 * 
 */
@Entity
@Table(name = "billing_item")
@NamedQuery(name = "BillingItem.findAll", query = "SELECT b FROM BillingItem b")
public class BillingItem extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "billing_item_id")
	private Long billingItemId;

	@Column(name = "billing_item_amount")
	private double billingItemAmount;

	@Column(name = "billing_item_date")
	private Timestamp billingItemDate;

	@Column(name = "billing_item_description")
	private String billingItemDescription;

	@Column(name = "billing_item_reference")
	private Long billingItemReference;

	@Column(name = "billing_item_status")
	private Integer billingItemStatus;

	// bi-directional many-to-one association to Account
	@ManyToOne
	@JoinColumn(name = "billing_item_account_id")
	private Account account;

	public BillingItem() {
	}

	public Long getBillingItemId() {
		return this.billingItemId;
	}

	public void setBillingItemId(Long billingItemId) {
		this.billingItemId = billingItemId;
	}

	public double getBillingItemAmount() {
		return this.billingItemAmount;
	}

	public void setBillingItemAmount(double billingItemAmount) {
		this.billingItemAmount = billingItemAmount;
	}

	public Timestamp getBillingItemDate() {
		return this.billingItemDate;
	}

	public void setBillingItemDate(Timestamp billingItemDate) {
		this.billingItemDate = billingItemDate;
	}

	public String getBillingItemDescription() {
		return this.billingItemDescription;
	}

	public void setBillingItemDescription(String billingItemDescription) {
		this.billingItemDescription = billingItemDescription;
	}

	public Long getBillingItemReference() {
		return this.billingItemReference;
	}

	public void setBillingItemReference(Long billingItemReference) {
		this.billingItemReference = billingItemReference;
	}

	public Integer getBillingItemStatus() {
		return this.billingItemStatus;
	}

	public void setBillingItemStatus(Integer billingItemStatus) {
		this.billingItemStatus = billingItemStatus;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public JSONObject getJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
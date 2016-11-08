package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The persistent class for the PAYROLL_ENTRY database table.
 * 
 */
@Entity
@Table(name = "PAYROLL_ENTRY")
@NamedQuery(name = "PayrollEntry.findAll", query = "SELECT p FROM PayrollEntry p")
public class PayrollEntry extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PAYROLL_ENTRY_ID")
	private Long payrollEntryId;

	@Column(name = "PAYROLL_ENTRY_BONUS_PAY")
	private BigDecimal payrollEntryBonusPay;

	@Temporal(TemporalType.DATE)
	@Column(name = "PAYROLL_ENTRY_PERIOD_END")
	private Date payrollEntryPeriodEnd;

	@Temporal(TemporalType.DATE)
	@Column(name = "PAYROLL_ENTRY_PERIOD_START")
	private Date payrollEntryPeriodStart;

	@Column(name = "PAYROLL_ENTRY_PERSON_ID")
	private Long payrollEntryPersonId;

	@Column(name = "PAYROLL_ENTRY_TOTAL_HOURS")
	private BigDecimal payrollEntryTotalHours;

	@Column(name = "PAYROLL_ENTRY_TOTAL_WAGES")
	private BigDecimal payrollEntryTotalWages;

	public PayrollEntry() {
	}

	public Long getPayrollEntryId() {
		return this.payrollEntryId;
	}

	public void setPayrollEntryId(Long payrollEntryId) {
		this.payrollEntryId = payrollEntryId;
	}

	public BigDecimal getPayrollEntryBonusPay() {
		return this.payrollEntryBonusPay;
	}

	public void setPayrollEntryBonusPay(BigDecimal payrollEntryBonusPay) {
		this.payrollEntryBonusPay = payrollEntryBonusPay;
	}

	public Date getPayrollEntryPeriodEnd() {
		return this.payrollEntryPeriodEnd;
	}

	public void setPayrollEntryPeriodEnd(Date payrollEntryPeriodEnd) {
		this.payrollEntryPeriodEnd = payrollEntryPeriodEnd;
	}

	public Date getPayrollEntryPeriodStart() {
		return this.payrollEntryPeriodStart;
	}

	public void setPayrollEntryPeriodStart(Date payrollEntryPeriodStart) {
		this.payrollEntryPeriodStart = payrollEntryPeriodStart;
	}

	public Long getPayrollEntryPersonId() {
		return this.payrollEntryPersonId;
	}

	public void setPayrollEntryPersonId(Long payrollEntryPersonId) {
		this.payrollEntryPersonId = payrollEntryPersonId;
	}

	public BigDecimal getPayrollEntryTotalHours() {
		return this.payrollEntryTotalHours;
	}

	public void setPayrollEntryTotalHours(BigDecimal payrollEntryTotalHours) {
		this.payrollEntryTotalHours = payrollEntryTotalHours;
	}

	public BigDecimal getPayrollEntryTotalWages() {
		return this.payrollEntryTotalWages;
	}

	public void setPayrollEntryTotalWages(BigDecimal payrollEntryTotalWages) {
		this.payrollEntryTotalWages = payrollEntryTotalWages;
	}

	@Override
	public JSONObject getJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The persistent class for the payroll_summary database table.
 * 
 */
@Entity
@Table(name = "payroll_summary")
@NamedQuery(name = "PayrollSummary.findAll", query = "SELECT p FROM PayrollSummary p")
public class PayrollSummary extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "payroll_person_id")
	private Long payrollPersonId;

	@Column(name = "payroll_bonus_wages")
	private BigDecimal payrollBonusWages;

	@Temporal(TemporalType.DATE)
	@Column(name = "payroll_end_date")
	private Date payrollEndDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "payroll_hire_date")
	private Date payrollHireDate;

	@Column(name = "payroll_latest_wage_rate")
	private BigDecimal payrollLatestWageRate;

	@Temporal(TemporalType.DATE)
	@Column(name = "payroll_start_date")
	private Date payrollStartDate;

	@Column(name = "payroll_start_wage_rate")
	private BigDecimal payrollStartWageRate;

	@Column(name = "payroll_still_employed")
	private byte payrollStillEmployed;

	@Column(name = "payroll_total_hours")
	private BigDecimal payrollTotalHours;

	@Column(name = "payroll_total_wages")
	private BigDecimal payrollTotalWages;

	@Column(name = "payroll_weekly_hours")
	private BigDecimal payrollWeeklyHours;

	public PayrollSummary() {
	}

	public Long getPayrollPersonId() {
		return this.payrollPersonId;
	}

	public void setPayrollPersonId(Long payrollPersonId) {
		this.payrollPersonId = payrollPersonId;
	}

	public BigDecimal getPayrollBonusWages() {
		return this.payrollBonusWages;
	}

	public void setPayrollBonusWages(BigDecimal payrollBonusWages) {
		this.payrollBonusWages = payrollBonusWages;
	}

	public Date getPayrollEndDate() {
		return this.payrollEndDate;
	}

	public void setPayrollEndDate(Date payrollEndDate) {
		this.payrollEndDate = payrollEndDate;
	}

	public Date getPayrollHireDate() {
		return this.payrollHireDate;
	}

	public void setPayrollHireDate(Date payrollHireDate) {
		this.payrollHireDate = payrollHireDate;
	}

	public BigDecimal getPayrollLatestWageRate() {
		return this.payrollLatestWageRate;
	}

	public void setPayrollLatestWageRate(BigDecimal payrollLatestWageRate) {
		this.payrollLatestWageRate = payrollLatestWageRate;
	}

	public Date getPayrollStartDate() {
		return this.payrollStartDate;
	}

	public void setPayrollStartDate(Date payrollStartDate) {
		this.payrollStartDate = payrollStartDate;
	}

	public BigDecimal getPayrollStartWageRate() {
		return this.payrollStartWageRate;
	}

	public void setPayrollStartWageRate(BigDecimal payrollStartWageRate) {
		this.payrollStartWageRate = payrollStartWageRate;
	}

	public byte getPayrollStillEmployed() {
		return this.payrollStillEmployed;
	}

	public void setPayrollStillEmployed(byte payrollStillEmployed) {
		this.payrollStillEmployed = payrollStillEmployed;
	}

	public BigDecimal getPayrollTotalHours() {
		return this.payrollTotalHours;
	}

	public void setPayrollTotalHours(BigDecimal payrollTotalHours) {
		this.payrollTotalHours = payrollTotalHours;
	}

	public BigDecimal getPayrollTotalWages() {
		return this.payrollTotalWages;
	}

	public void setPayrollTotalWages(BigDecimal payrollTotalWages) {
		this.payrollTotalWages = payrollTotalWages;
	}

	public BigDecimal getPayrollWeeklyHours() {
		return this.payrollWeeklyHours;
	}

	public void setPayrollWeeklyHours(BigDecimal payrollWeeklyHours) {
		this.payrollWeeklyHours = payrollWeeklyHours;
	}

	@Override
	public JSONObject getJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
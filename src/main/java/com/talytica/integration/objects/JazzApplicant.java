package com.talytica.integration.objects;

import java.util.Set;

import com.employmeo.data.model.RespondantNVP;

import lombok.ToString;

@ToString
public class JazzApplicant {
	
	public String email;
	public String firstName;
	public String lastName;
	public String address;
	public String phone;
	public String id;
	public String appId;
	public String workflow;
	public Integer workflowStep;
	
	public Set<RespondantNVP> nvps;

}

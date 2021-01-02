package com.talytica.integration.partners.fountain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FountainApplicant {
	
	private String email;
    private String name;
    private String first_name;
    private String last_name;
    private String phone_number;
    private Date created_at;
	private Boolean receive_automated_emails;
	//private List<String> labels;
	private String id;

}

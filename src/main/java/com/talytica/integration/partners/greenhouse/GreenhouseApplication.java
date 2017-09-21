package com.talytica.integration.partners.greenhouse;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GreenhouseApplication {
	private Long id;
	private Long candidate_id;
	private Boolean prospect;
	private Date applied_at;
	private Date rejected_at;
	private Date last_activity_at;
	private GHSource source;
	private GHUser credited_to;
	private GHReason rejection_reason;
	private String rejection_details;
	private List<GHJob> jobs;
	private String status;
	private GHStage current_stage;
	private List<GHAnswer> answers;
	private JSONObject custom_fields;
	
	@Data
	@ToString
	public class GHSource {
		private Long id;
		private String public_name;
	}
	@Data
	@ToString
	public class GHUser{
		private Long id;
		private String name;
		private String employee_id;
	}
	@Data
	@ToString
	public class GHReason {
		private Long id;
		private String name;
	}
	@Data
	@ToString
	public class GHJob {
		private Long id;
		private String name;
	}
	@Data
	@ToString
	public class GHStage {
		private Long id;
		private String name;
	}
	@Data
	@ToString
	public class GHAnswer {
		private String question;
		private String answer;
	}

}

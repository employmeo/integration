package com.talytica.integration.partners.greenhouse;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GreenhouseWebHook {
	private String action;
	private GHPayload payload;

	@Data
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GHPayload {
		private GHOffer offer;
		private GreenhouseApplication application;
	}

	@Data
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GHOffer {
		private Long id;
		private Long application_id;
		private Long offering_user_id;
		private String offer_status;
		private Date sent_on;
		private Long job_id;
	}
}

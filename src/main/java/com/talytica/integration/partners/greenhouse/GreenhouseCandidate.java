package com.talytica.integration.partners.greenhouse;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GreenhouseCandidate {
	private Long id;
	private String first_name;
	private String last_name;
	private String title;
	private String company;
	private Date created_at;
	private String external_id;
	private String photo_url;
	private String url;
	private List<GHContact> phone_numbers;
	private List<GHContact> email_addresses;
	private List<GHContact> addresses;
	private List<GHContact> website_addresses;
	private List<GHContact> social_media_addresses;
	private List<GHAttachment> attachments;
	
	@Data
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GHContact {
		private String value;
		private String type;
	}

	@Data
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GHAttachment {
		private String filename;
		private String url;
		private String type;
	}
}
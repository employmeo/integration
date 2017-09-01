package com.talytica.integration.partners.smartrecruiters;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SmartRecruitersOffer {

	private String catalogId; // assessment asid
	private String name;
	private String description;
	private OfferTerms terms;
	
	@Data
	@ToString
	private class OfferTerms {
		String type;
		OfferPrice price = new OfferPrice();
	}

	@Data
	@ToString
	private class OfferPrice {
		String amount = "0.0";
		String currencyCode = "USD";
	}
	
}

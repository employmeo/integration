package com.talytica.integration.objects;

import com.employmeo.data.model.Location;

public class ATSAddress {

	public String street;
	public String city;
	public String state;
	public String zip;
	public Double lat;
	public Double lng;
	
	public ATSAddress (Location location) {
		this.street = location.getStreet1();
		this.city = location.getCity();
		this.state = location.getState();
		this.zip = location.getZip();
		this.lat = location.getLatitude();
		this.lng = location.getLongitude();
	}
	
}

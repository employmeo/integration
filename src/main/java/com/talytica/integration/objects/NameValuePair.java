package com.talytica.integration.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NameValuePair {
	private String name;
	private Object value;

	@Override
	public String toString() {
		return "NVP[Name=" + name + ", value=" + value + "]";
	}

}

package com.talytica.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JSONReaderUtil<T> implements MessageBodyReader<T> {

	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return true;
	}

	@Override
	public T readFrom(Class<T> type, Type generic, Annotation[] as, MediaType mt, MultivaluedMap<String, String> map,
			InputStream is) throws IOException, WebApplicationException {

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String read;
		while ((read = br.readLine()) != null) {
			sb.append(read);
		}
		br.close();

		if (String.class == generic)
			return type.cast(sb.toString());

		if (JSONObject.class == generic) {
			JSONObject json = null;
			try {
				json = new JSONObject(sb.toString());
			} catch (Exception e) {
				throw new WebApplicationException(Response.Status.BAD_REQUEST);
			}
			return type.cast(json);
		}

		// If not converting to String or JSON, return null.
		return null;
	}
}

package com.talytica.integration.resources;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/echo")
@Api( value="/echo", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class EchoResource {

	@Context
	private UriInfo uriInfo;

	@POST
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Echos your JSON Post", response = String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Echo Processed"),
	   })	
	public String doPost(@ApiParam(value="body") @RequestBody String body) throws JSONException {
		JSONObject json = new JSONObject(body);
		log.debug("Echo Called with: {}" , json.toString());
		return json.toString();
	}

}
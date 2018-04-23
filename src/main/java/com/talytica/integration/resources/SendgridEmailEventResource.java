package com.talytica.integration.resources;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.SendGridEmailEvent;
import com.employmeo.data.repository.SendGridEventRepository;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/emailevent")
@Api( value="/emailevent", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
@PermitAll
public class SendgridEmailEventResource {

	@Autowired
	SendGridEventRepository sendGridEventRepository;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost(List<SendGridEmailEvent> events) throws JSONException {
		log.debug("processing with {} " + events);

		sendGridEventRepository.saveAll(events);
		return Response.status(Response.Status.ACCEPTED).entity("{ message: 'Status Change Accepted' }").build();
	}
}
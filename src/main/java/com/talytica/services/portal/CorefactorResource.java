package com.talytica.services.portal;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.service.CorefactorService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/corefactor")
@Api( value="/1/corefactor", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class CorefactorResource {
	private static final Logger log = LoggerFactory.getLogger(CorefactorResource.class);

	@Autowired
	private CorefactorService corefactorService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all Corefactors", response = Corefactor.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Corefactors found"),
	     @ApiResponse(code = 404, message = "Corefactors not found")
	   })	
	public Iterable<Corefactor> getAllCorefactors() {
		return corefactorService.getAllCorefactors();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the corefactor by provided Id", response = Corefactor.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Corefactor found"),
	     @ApiResponse(code = 404, message = "No such Corefactor found")
	   })	
	public Response getCorefactor(@ApiParam(value = "corefactor id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested corefactor by id {}", id);
		
		Corefactor corefactor = corefactorService.findCorefactorById(id);
		log.debug("Returning corefactor by id {} as {}", id, corefactor);
		
		if(null != corefactor) {
			return Response.status(Status.OK).entity(corefactor).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided corefactor", response = Corefactor.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Corefactor saved"),
	   })	
	public Response saveCorefactor(Corefactor corefactor) {
		log.debug("Requested corefactor save: {}", corefactor);
		
		Corefactor savedCorefactor = corefactorService.save(corefactor);
		log.debug("Saved corefactor {}", savedCorefactor);
		
		return Response.status(Status.CREATED).entity(savedCorefactor).build();
	}		
}

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

import com.employmeo.data.model.Partner;
import com.employmeo.data.service.PartnerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/partner")
@Api( value="/1/partner", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class PartnerResource {
	private static final Logger log = LoggerFactory.getLogger(PartnerResource.class);

	@Autowired
	private PartnerService partnerService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all Partners", response = Partner.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Partners found"),
	     @ApiResponse(code = 404, message = "Partners not found")
	   })	
	public Iterable<Partner> getAllPartners() {
		return partnerService.getAllPartners();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the partner by provided Id", response = Partner.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Partner found"),
	     @ApiResponse(code = 404, message = "No such Partner found")
	   })	
	public Response getPartner(@ApiParam(value = "partner id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested partner by id {}", id);
		
		Partner partner = partnerService.getPartnerById(id);
		log.debug("Returning partner by id {} as {}", id, partner);
		
		if(null != partner) {
			return Response.status(Status.OK).entity(partner).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided partner", response = Partner.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Partner saved"),
	   })	
	public Response savePartner(Partner partner) {
		log.debug("Requested partner save: {}", partner);
		
		Partner savedPartner = partnerService.save(partner);
		log.debug("Saved partner {}", savedPartner);
		
		return Response.status(Status.CREATED).entity(savedPartner).build();
	}		
}

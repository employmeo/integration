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

import com.employmeo.data.model.Account;
import com.employmeo.data.service.AccountService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/account")
@Api( value="/1/account", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class AccountResource {
	private static final Logger log = LoggerFactory.getLogger(AccountResource.class);

	@Autowired
	private AccountService accountService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all Accounts", response = Account.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Accounts found"),
	     @ApiResponse(code = 404, message = "Accounts not found")
	   })	
	public Iterable<Account> getAllAccounts() {
		return accountService.getAllAccounts();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the account by provided Id", response = Account.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Account found"),
	     @ApiResponse(code = 404, message = "No such Account found")
	   })	
	public Response getAccount(@ApiParam(value = "account id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested account by id {}", id);
		
		Account account = accountService.getAccountById(id);
		log.debug("Returning account by id {} as {}", id, account);
		
		if(null != account) {
			return Response.status(Status.OK).entity(account).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided account", response = Account.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Account saved"),
	   })	
	public Response saveAccount(Account account) {
		log.debug("Requested account save: {}", account);
		
		Account savedAccount = accountService.save(account);
		log.debug("Saved account {}", savedAccount);
		
		return Response.status(Status.CREATED).entity(savedAccount).build();
	}
	
	@GET
	@Path("/{id}/locations")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the account by provided Id", response = Account.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Account found"),
	     @ApiResponse(code = 404, message = "No such Account found")
	   })	
	public Response getLocations(@ApiParam(value = "account id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested locations for account id {}", id);
		
		Account account = accountService.getAccountById(id);
		log.debug("Returning locations for account id {}", id);
		
		if(null != account) {
			return Response.status(Status.OK).entity(account.getLocations()).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	

	@GET
	@Path("/{id}/positions")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the account by provided Id", response = Account.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Account found"),
	     @ApiResponse(code = 404, message = "No such Account found")
	   })	
	public Response getPositions(@ApiParam(value = "account id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested positions for account id {}", id);
		
		Account account = accountService.getAccountById(id);
		log.debug("Returning positions for account id {}", id);
		
		if(null != account) {
			return Response.status(Status.OK).entity(account.getPositions()).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	

	@GET
	@Path("/{id}/assessments")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the account by provided Id", response = Account.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Account found"),
	     @ApiResponse(code = 404, message = "No such Account found")
	   })	
	public Response getAssessments(@ApiParam(value = "account id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested assessments for account id {}", id);
		
		Account account = accountService.getAccountById(id);
		log.debug("Returning assessments for account id {}", id);
		
		if(null != account) {
			return Response.status(Status.OK).entity(account.getAccountSurveys()).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	

}

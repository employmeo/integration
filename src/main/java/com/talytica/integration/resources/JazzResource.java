package com.talytica.integration.resources;

import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.employmeo.data.repository.RespondantNVPRepository;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.PersonService;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.objects.JazzApplicant;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/jazz")
@Api( value="/1/jazz", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class JazzResource {
	
	private static final Response ACCOUNT_NOT_FOUND = Response.status(Response.Status.NOT_FOUND).entity("Account Not Found").build();

	@Context
	private SecurityContext sc;
	@Autowired
	private PartnerRepository partnerRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;
	@Autowired
	private PersonService personService;
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private RespondantNVPRepository respondantNVPRepository;

	

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Saves respondant to database", response = Person.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}")
	public Response saveJazzPerson (
			@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId,
			@ApiParam (value = "Person") JazzApplicant candidate) {
		log.debug("Save Person called with: {} for {}" , atsId, candidate);
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = accountService.getAccountByAtsId(pu.addPrefix(atsId));

		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}
		
		Person savedPerson = personService.getPersonByAtsId(pu.addPrefix(candidate.id));
		if (null == savedPerson) {
			log.debug("no person found with id {}", pu.addPrefix(candidate.id));
			Person person = new Person();
			person.setAtsId(pu.addPrefix(candidate.id));
			person.setAddress(candidate.address);
			person.setEmail(candidate.email);
			person.setFirstName(candidate.firstName);
			person.setLastName(candidate.lastName);	
			savedPerson = personService.save(person);
		}
		
		Respondant respondant = new Respondant();
		respondant.setAccount(account);
		respondant.setAccountId(account.getId());
		respondant.setAtsId(pu.addPrefix(candidate.appId));
		respondant.setPerson(savedPerson);
		respondant.setPersonId(savedPerson.getId());
		respondant.setPositionId(account.getDefaultPositionId());
		respondant.setLocationId(account.getDefaultLocationId());
		respondant.setAccountSurveyId(account.getDefaultAsId());
		if ((candidate.workflowStep == 11) || (candidate.workflowStep == 12)) {
			respondant.setRespondantStatus(Respondant.STATUS_HIRED);
		}
		
		Respondant savedRespondant = respondantService.save(respondant);
		
		for (RespondantNVP nvp : candidate.nvps) {
			nvp.setRespondantId(savedRespondant.getId());
			if (nvp.getValue().length() <= 4096) respondantNVPRepository.save(nvp);
		}
		return Response.status(Response.Status.CREATED).build();
	}


}

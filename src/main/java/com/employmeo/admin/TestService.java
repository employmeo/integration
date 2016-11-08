package com.employmeo.admin;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.employmeo.objects.Position;
import com.employmeo.objects.User;

@Path("test")
@PermitAll
public class TestService {

	private static final Logger log = LoggerFactory.getLogger(TestService.class);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String doMethod(@Context final HttpServletRequest reqt, String stringdata) {

User user = (User) reqt.getSession().getAttribute("User");

		List<Position> acctpositions = user.getAccount().getPositions();
		
		for (Position p : acctpositions) {
			System.out.println(Position.getPositionById(p.getPositionId()).getJSONString());			
		}
		
		return stringdata;
	}


}	
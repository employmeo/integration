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

import com.employmeo.data.model.Question;
import com.employmeo.data.service.QuestionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/question")
@Api( value="/1/question", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class QuestionResource {
	private static final Logger log = LoggerFactory.getLogger(QuestionResource.class);

	@Autowired
	private QuestionService questionService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all Questions", response = Question.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Questions found"),
	     @ApiResponse(code = 404, message = "Questions not found")
	   })	
	public Iterable<Question> getAllQuestions() {
		return questionService.getAllQuestions();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the question by provided Id", response = Question.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Question found"),
	     @ApiResponse(code = 404, message = "No such Question found")
	   })	
	public Response getQuestion(@ApiParam(value = "question id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested question by id {}", id);
		
		Question question = questionService.getQuestionById(id);
		log.debug("Returning question by id {} as {}", id, question);
		
		if(null != question) {
			return Response.status(Status.OK).entity(question).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided question", response = Question.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Question saved"),
	   })	
	public Response saveQuestion(Question question) {
		log.debug("Requested question save: {}", question);
		
		Question savedQuestion = questionService.save(question);
		log.debug("Saved question {}", savedQuestion);
		
		return Response.status(Status.CREATED).entity(savedQuestion).build();
	}		
}

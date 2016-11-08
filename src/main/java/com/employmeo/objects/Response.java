package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

/**
 * The persistent class for the responses database table.
 * 
 */
@Entity
@Table(name = "responses")
@NamedQuery(name = "Response.findAll", query = "SELECT r FROM Response r")
public class Response extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "response_id")
	private Long responseId;

	// bi-directional many-to-one association to Respondant
	@ManyToOne
	@JoinColumn(name = "response_respondant_id", insertable = false, updatable = false)
	private Respondant respondant;

	@Column(name = "response_respondant_id", insertable = true, updatable = false)
	private Long responseRespondantId;

	@Column(name = "response_text")
	private String responseText;

	@Column(name = "response_value")
	private int responseValue;

	// bi-directional many-to-one association to Question
	@ManyToOne
	@JoinColumn(name = "response_question_id", insertable = false, updatable = false)
	private Question question;

	@Column(name = "response_question_id", insertable = true, updatable = false)
	private Long responseQuestionId;

	public Response() {
	}

	public Long getResponseId() {
		return this.responseId;
	}

	public void setResponseId(Long responseId) {
		this.responseId = responseId;
	}

	public Respondant getRespondant() {
		return this.respondant;
	}

	public void setRespondant(Respondant respondant) {
		this.respondant = respondant;
		this.responseRespondantId = respondant.getRespondantId();
	}

	public Long getResponseRespondantId() {
		return responseRespondantId;
	}

	public void setResponseRespondantId(String respondantId) {
		this.setResponseRespondantId(new Long(respondantId));
	}

	public void setResponseRespondantId(Long respondantId) {
		this.responseRespondantId = respondantId;
		this.respondant = Respondant.getRespondantById(respondantId);
	}

	public Long getResponseQuestionId() {
		return responseQuestionId;
	}

	public void setResponseQuestionId(String quesId) {
		this.responseRespondantId = new Long(quesId);
	}

	public void setResponseQuestionId(Long quesId) {
		this.responseQuestionId = quesId;
	}

	public String getResponseText() {
		return this.responseText;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	public int getResponseValue() {
		return this.responseValue;
	}

	public void setResponseValue(int responseValue) {
		this.responseValue = responseValue;
	}

	public Question getQuestion() {
		return this.question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("response_id", this.responseId);
		json.put("response_respondant_id", this.responseRespondantId);
		json.put("response_question_id", this.responseQuestionId);
		json.put("response_value", this.responseValue);
		json.put("response_text", this.getResponseText());

		return json;
	}
}
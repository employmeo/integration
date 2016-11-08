package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;

/**
 * The persistent class for the answers database table.
 * 
 */
@Entity
@Table(name = "answers")
@NamedQuery(name = "Answer.findAll", query = "SELECT a FROM Answer a")
public class Answer extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "ANSWER_ID")
	private String answerId;

	@Column(name = "ANSWER_DESCRIPTION")
	private String answerDescription;

	@Column(name = "ANSWER_DISPLAY_ID")
	private Long answerDisplayId;

	@Column(name = "ANSWER_TEXT")
	private String answerText;

	@Column(name = "ANSWER_VALUE")
	private int answerValue;

	// bi-directional many-to-one association to Question
	@ManyToOne
	@JoinColumn(name = "ANSWER_QUESTION_ID")
	private Question question;

	public Answer() {
	}

	public String getAnswerId() {
		return this.answerId;
	}

	public void setAnswerId(String answerId) {
		this.answerId = answerId;
	}

	public String getAnswerDescription() {
		return this.answerDescription;
	}

	public void setAnswerDescription(String answerDescription) {
		this.answerDescription = answerDescription;
	}

	public Long getAnswerDisplayId() {
		return this.answerDisplayId;
	}

	public void setAnswerDisplayId(Long answerDisplayId) {
		this.answerDisplayId = answerDisplayId;
	}

	public String getAnswerText() {
		return this.answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	public int getAnswerValue() {
		return this.answerValue;
	}

	public void setAnswerValue(int answerValue) {
		this.answerValue = answerValue;
	}

	public Question getQuestion() {
		return this.question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("answer_id", this.answerId);
		json.put("answer_description", this.answerDescription);
		json.put("answer_text", this.answerText);
		json.put("answer_value", this.getAnswerValue());
		json.put("answer_display_id", this.answerDisplayId);

		return json;
	}

	public static Answer fromJSON(JSONObject json) {
		Answer answer = new Answer();

		answer.setAnswerId(json.getString("answer_id"));
		answer.setAnswerDescription(json.getString("answer_description"));
		answer.setAnswerText(json.getString("answer_text"));
		answer.setAnswerValue(json.getInt("answer_value"));
		answer.setAnswerDisplayId(json.getLong("answer_display_id"));
		
		return answer;
	}
	
	public static Answer findById(String lookupId) {
		EntityManager em = DBUtil.getEntityManager();
		return em.find(Answer.class, lookupId);
	}	
}
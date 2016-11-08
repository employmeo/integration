package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the surveys database table.
 * 
 */
@Entity
@Table(name = "surveys")
@NamedQuery(name = "Survey.findAll", query = "SELECT s FROM Survey s")
public class Survey extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int TYPE_INTERNAL = 1;
	public static final int TYPE_MERCER = 2;
	public static final int TYPE_MIXED = 3;

	@Id
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "survey_id")
	private Long surveyId;

	@Column(name = "survey_name")
	private String surveyName;

	@Column(name = "survey_description")
	private String surveyDescription;

	@Column(name = "survey_status")
	private int surveyStatus;

	@Column(name = "survey_type")
	private int surveyType;

	@Column(name = "survey_list_price")
	private Double surveyListPrice;

	@Column(name = "survey_completion_time")
	private Long surveyCompletionTime;

	@Column(name = "survey_completion_pct")
	private Double surveyCompletionPct;

	@Column(name = "survey_foreign_id")
	private String surveyForeignId;

	// bi-directional many-to-one association to SurveyQuestion
	@OneToMany(mappedBy = "survey", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<SurveyQuestion> surveyQuestions = new ArrayList<SurveyQuestion>();

	// bi-directional many-to-one association to SurveyQuestion
	@OneToMany(mappedBy = "survey", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<SurveySection> surveySections = new ArrayList<SurveySection>();

	public Survey() {
	}

	public Long getSurveyId() {
		return this.surveyId;
	}

	public void setSurveyId(Long surveyId) {
		this.surveyId = surveyId;
	}

	public void setSurveyId(String surveyId) {
		this.surveyId = new Long(surveyId);
	}

	public String getSurveyName() {
		return this.surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public String getSurveyDescription() {
		return this.surveyDescription;
	}

	public void setSurveyDescription(String surveyDesc) {
		this.surveyDescription = surveyDesc;
	}

	public int getSurveyStatus() {
		return this.surveyStatus;
	}

	public void setSurveyStatus(int surveyStatus) {
		this.surveyStatus = surveyStatus;
	}

	public int getSurveyType() {
		return this.surveyType;
	}

	public void setSurveyType(int surveyType) {
		this.surveyType = surveyType;
	}

	public Double getSurveyListPrice() {
		return this.surveyListPrice;
	}

	public void setSurveyListPrice(Double price) {
		this.surveyListPrice = price;
	}

	public Double getSurveyCompletionPct() {
		return this.surveyCompletionPct;
	}

	public void setSurveyCompletionPct(Double pct) {
		this.surveyCompletionPct = pct;
	}

	public Long getSurveyCompletionTime() {
		return this.surveyCompletionTime;
	}

	public void setSurveyCompletionTime(Long milliseconds) {
		this.surveyCompletionTime = milliseconds;
	}

	public List<SurveyQuestion> getSurveyQuestions() {
		return this.surveyQuestions;
	}

	public void setSurveyQuestions(List<SurveyQuestion> surveyQuestions) {
		this.surveyQuestions = surveyQuestions;
	}

	public SurveyQuestion addSurveyQuestion(SurveyQuestion surveyQuestion) {
		getSurveyQuestions().add(surveyQuestion);
		surveyQuestion.setSurvey(this);
		return surveyQuestion;
	}

	public SurveyQuestion removeSurveyQuestion(SurveyQuestion surveyQuestion) {
		getSurveyQuestions().remove(surveyQuestion);
		surveyQuestion.setSurvey(null);
		return surveyQuestion;
	}

	public List<SurveySection> getSurveySections() {
		return this.surveySections;
	}

	public void setSurveySections(List<SurveySection> surveySections) {
		this.surveySections = surveySections;
	}
	
	public static Survey getSurveyById(String lookupId) {
		return getSurveyById(new Long(lookupId));
	}

	public static Survey getSurveyById(Long lookupId) {
		EntityManager em = DBUtil.getEntityManager();
		return em.find(Survey.class, lookupId);
	}

	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("survey_id", this.surveyId);
		json.put("survey_name", this.surveyName);
		json.put("survey_description", this.surveyDescription);
		json.put("survey_status", this.surveyStatus);
		json.put("survey_type", this.surveyType);
		json.put("survey_list_price", this.surveyListPrice);
		json.put("survey_completion_pct", this.surveyCompletionPct);
		json.put("survey_completion_time", this.surveyCompletionTime);

		JSONArray questions = new JSONArray();
		for (int i = 0; i < this.surveyQuestions.size(); i++) {
			questions.put(this.surveyQuestions.get(i).getJSON());
		}
		json.put("questions", questions);

		JSONArray sections = new JSONArray();
		for (int i = 0; i < this.surveySections.size(); i++) {
			sections.put(this.surveySections.get(i).getJSON());
		}
		json.put("sections", sections);

		return json;
	}
	
	public static Survey fromJSON(JSONObject json) {
		Survey survey = new Survey();
		survey.setSurveyId(json.getLong("survey_id"));
		survey.setSurveyName(json.getString("survey_name"));
		survey.setSurveyDescription(json.getString("survey_description"));
		survey.setSurveyStatus(json.getInt("survey_status"));
		survey.setSurveyType(json.getInt("survey_type"));
		survey.setSurveyListPrice(json.getDouble("survey_list_price"));
		survey.setSurveyCompletionPct(json.getDouble("survey_completion_pct"));
		survey.setSurveyCompletionTime(json.getLong("survey_completion_time"));
		
		List<SurveyQuestion> surveyQuestions = new ArrayList<SurveyQuestion>();
		JSONArray jsonQuestions = json.getJSONArray("questions");
		for(int i=0; i < jsonQuestions.length(); i++) {		
			SurveyQuestion surveyQuestion = SurveyQuestion.fromJSON(jsonQuestions.getJSONObject(i));
			surveyQuestion.setSurvey(survey);
			surveyQuestions.add(surveyQuestion);
		}
		survey.setSurveyQuestions(surveyQuestions);
		
		List<SurveySection> surveySections = new ArrayList<SurveySection>();
		JSONArray jsonSections = json.getJSONArray("sections");
		for(int i=0; i < jsonSections.length(); i++) {		
			SurveySection surveySection = SurveySection.fromJSON(jsonSections.getJSONObject(i));
			surveySections.add(surveySection);
		}
		survey.setSurveySections(surveySections);
		
		return survey;
	}

}
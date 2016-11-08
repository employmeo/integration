package com.employmeo.objects;

import java.io.Serializable;
import javax.persistence.*;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;


/**
 * The persistent class for the survey_sections database table.
 * 
 */
@Entity
@Table(name="survey_sections")
@NamedQuery(name="SurveySection.findAll", query="SELECT s FROM SurveySection s")
public class SurveySection extends PersistantObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private SurveySectionPK id;

	@Column(name="ss_all_required")
	private Boolean ssAllRequired;

	@Column(name="ss_instructions")
	private String ssInstructions;

	@Column(name="ss_questions_per_page")
	private Integer ssQuestionsPerPage;

	@Column(name="ss_time_seconds")
	private Integer ssTimeSeconds;

	// bi-directional many-to-one association to Survey
	@ManyToOne
	@JoinColumn(name = "ss_survey_id", insertable=false, updatable=false)
	private Survey survey;

	public SurveySection() {
	}

	public SurveySectionPK getId() {
		return this.id;
	}

	public void setId(SurveySectionPK id) {
		this.id = id;
	}

	public Boolean getSsAllRequired() {
		return this.ssAllRequired;
	}

	public void setSsAllRequired(Boolean ssAllRequired) {
		this.ssAllRequired = ssAllRequired;
	}

	public String getSsInstructions() {
		return this.ssInstructions;
	}

	public void setSsInstructions(String ssInstructions) {
		this.ssInstructions = ssInstructions;
	}

	public Integer getSsQuestionsPerPage() {
		return this.ssQuestionsPerPage;
	}

	public void setSsQuestionsPerPage(Integer ssQuestionsPerPage) {
		this.ssQuestionsPerPage = ssQuestionsPerPage;
	}

	public Boolean getSsTimed() {
		return (this.ssTimeSeconds > 0);
	}

	public Integer getSsTimeSeconds() {
		return this.ssTimeSeconds;
	}

	public void setSsTimeSeconds(Integer time) {
		this.ssTimeSeconds = time;
	}
	
	@Override
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		json.put("section_survey_id", this.getId().getSsSurveyId());
		json.put("section_number", this.getId().getSsSurveySection());
		json.put("section_instructions",this.getSsInstructions());
		json.put("section_questions_per_page",this.getSsQuestionsPerPage());
		json.put("section_all_required",this.getSsAllRequired());
		json.put("section_timed",this.getSsTimed());
		json.put("section_time_seconds",this.getSsTimeSeconds());
		
		return json;
	}
	
	public static SurveySection fromJSON(JSONObject json) {
		SurveySection surveySection = new SurveySection();
		
		SurveySectionPK surveySectionPK = new SurveySectionPK();
		surveySectionPK.setSsSurveyId(json.getLong("section_survey_id"));
		surveySectionPK.setSsSurveySection(json.getInt("section_number"));
		surveySection.setId(surveySectionPK);
		
		surveySection.setSsInstructions(json.getString("section_instructions"));
		surveySection.setSsQuestionsPerPage(json.getInt("section_questions_per_page"));
		surveySection.setSsAllRequired(json.getBoolean("section_all_required"));
		surveySection.setSsTimeSeconds(json.getInt("section_time_seconds"));
		
		return surveySection;
		
	}
	
	public static SurveySection findById(Long surveyId, Integer surveySectionId) {
		SurveySectionPK surveySectionPK = new SurveySectionPK();
		surveySectionPK.setSsSurveyId(surveyId);
		surveySectionPK.setSsSurveySection(surveySectionId);
		
		EntityManager em = DBUtil.getEntityManager();
		return em.find(SurveySection.class, surveySectionPK);
	}

}
package com.talytica.integration.scoring;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Grader;
import com.employmeo.data.model.GraderConfig;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.RespondantScorePK;
import com.employmeo.data.model.Response;
import com.employmeo.data.model.User;
import com.employmeo.data.service.AccountSurveyService;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.GraderService;
import com.employmeo.data.service.UserService;
import com.google.common.collect.Lists;
import com.talytica.common.service.EmailService;
import com.talytica.common.service.SpeechToTextService;
import com.talytica.common.service.TextAnalyticsService;
import com.talytica.common.service.BeyondVerbalService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AudioScoring implements ScoringModelEngine {

	private static final Long RANKER_CFID = 99l;
	private static final Long WATSON_SENTIMENT = 1000l;
	
	@Autowired
	private UserService userService;
	@Autowired
	private GraderService graderService;
	@Autowired
	private AccountSurveyService accountSurveyService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private BeyondVerbalService beyondVerbalService;
	@Autowired
	private SpeechToTextService speechToTextService;
	@Autowired
	private TextAnalyticsService textAnalyticsService;
	@Autowired
	private CorefactorService corefactorService;
		
		
	private String modelName;
	private String contentType;
	private Boolean audioAnalytics;
	private Boolean internalGrading;
	private Boolean sentiment;
	private Boolean speechToText;
	
	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = Lists.newArrayList();

		if (internalGrading) {
			Double graderSaved = 0d;
			Set<GraderConfig> configs = accountSurveyService.getGraderConfigsForSurvey(respondant.getAccountSurveyId());
	
			if (configs.isEmpty()) {
				Set<User> users = userService.getUsersForAccount(respondant.getAccountId());// creating a grader for every user?
				users.forEach(user -> {
					GraderConfig config = new GraderConfig();
					config.setUser(user);
					config.setUserId(user.getId());
					configs.add(config);
					});
				log.info("No Configs Found. Addeded {} users to grade Respondant {}",configs.size(),respondant.getId());
			} 
			
			for (GraderConfig config : configs) {
				Grader savedGrader = null;
				for (Response response : responses) {	
					Grader grader = new Grader();
					grader.setAccountId(respondant.getAccountId());
					grader.setAccount(respondant.getAccount());
					grader.setRelationship("Assigned");
					grader.setStatus(Grader.STATUS_NEW);
					grader.setUser(config.getUser());
					grader.setUserId(config.getUserId());
					grader.setRespondant(respondant);
					grader.setRespondantId(respondant.getId());
					grader.setResponse(response);
					grader.setResponseId(response.getId());
					grader.setQuestionId(response.getQuestionId());			
					grader.setType(Grader.TYPE_USER);;
					if (config.getSummarize()) grader.setType(Grader.TYPE_SUMMARY_USER);;
					savedGrader = graderService.save(grader);
					graderSaved++;
					if (config.getSummarize()) break;
				}
				log.debug("Grader {} created with {} responses to grade, notify = {}", savedGrader, responses.size(),config.getNotify());
				if ((savedGrader != null) && (config.getNotify())) emailService.sendGraderRequest(savedGrader);
			}
			if (graderSaved>0) {
				RespondantScore ranker = new RespondantScore();
				ranker.setId(new RespondantScorePK(RANKER_CFID,respondant.getId()));
				ranker.setValue(graderSaved);
				scores.add(ranker); // Indicates that there are outstanding "graders"
			}
			log.debug("{} grader requests created", graderSaved);
		}	
		
		if (audioAnalytics) {
			scores.addAll(beyondVerbalService.analyzeResponses(responses));
		}
		
		if (speechToText) {
			Set<RespondantNVP> nvps = speechToTextService.convertToTextFeatures(responses, this.contentType);
				if (sentiment) {
				StringBuffer text = new StringBuffer();
				for (RespondantNVP nvp : nvps) text.append(nvp.getValue());		
				String allText = text.toString();
				if (!allText.isEmpty()) {
					Double score = textAnalyticsService.analyzeSentiment(allText);
					log.debug("Respondant {} has sentiment score of: {}", respondant.getId(), score);
					Corefactor cf = corefactorService.findCorefactorById(WATSON_SENTIMENT);
					RespondantScorePK pk = new RespondantScorePK(WATSON_SENTIMENT,respondant.getId());
					RespondantScore rs = new RespondantScore();
					rs.setId(pk);
					rs.setValue(((cf.getHighValue()-cf.getLowValue())*score)+cf.getLowValue());
					rs.setQuestionCount(3);	
					scores.add(rs);
				}
			}
		}
		
		return scores;
	}

	@Override
	public String getModelName() {
		return this.modelName;
	}

	@Override
	public void initialize(String modelName) {
		this.modelName = modelName;
		this.contentType = SpeechToTextService.AUDIO_WAV; // default is twilio audio
		this.audioAnalytics = Boolean.TRUE; // default is to use beyond verbal to analyze moods
		this.speechToText = Boolean.TRUE; // default is to convert media to text & save as text feature
		this.sentiment = Boolean.TRUE; // default is to get sentiment score from converted text
		this.internalGrading = Boolean.TRUE;

		switch (modelName) {
		case "video":
		case "audio":
			this.audioAnalytics = Boolean.FALSE;
			this.speechToText = Boolean.FALSE;
			this.sentiment = Boolean.FALSE;
			break;
			case "video-":
				this.contentType = SpeechToTextService.VIDEO_WEBM;
				this.audioAnalytics = Boolean.FALSE;
			case "audio-":
				this.internalGrading = Boolean.FALSE;
				break;

			case "video+":
				this.contentType = SpeechToTextService.VIDEO_WEBM;
				this.audioAnalytics = Boolean.FALSE;
				break;
			case "audio+":
			default:
				break;
		}
	}
}

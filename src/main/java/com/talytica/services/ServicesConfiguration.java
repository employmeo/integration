package com.talytica.services;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.talytica.services.integration.ATSOrder;
import com.talytica.services.integration.Echo;
import com.talytica.services.integration.GetAssessments;
import com.talytica.services.integration.GetLocations;
import com.talytica.services.integration.GetPositions;
import com.talytica.services.integration.GetScore;
import com.talytica.services.integration.HireNotice;
import com.talytica.services.integration.ICIMSApplicationComplete;
import com.talytica.services.integration.ICIMSStatusUpdate;
import com.talytica.services.portal.AccountResource;
import com.talytica.services.portal.ChangePassword;
import com.talytica.services.portal.CorefactorResource;
import com.talytica.services.portal.Dashboard;
import com.talytica.services.portal.ForgotPassword;
import com.talytica.services.portal.GetLastTenRespondants;
import com.talytica.services.portal.InviteApplicant;
import com.talytica.services.portal.Login;
import com.talytica.services.portal.Logout;
import com.talytica.services.portal.PartnerResource;
import com.talytica.services.portal.PersonResource;
import com.talytica.services.portal.PredictionConfigurationResource;
import com.talytica.services.portal.QuestionResource;
import com.talytica.services.portal.RespondantResource;
import com.talytica.services.portal.SurveyResource;
import com.talytica.services.portal.UserResource;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;


@Component
@ApplicationPath("${spring.jersey.application-path:/api}")
public class ServicesConfiguration extends ResourceConfig {
	private static final Logger log = LoggerFactory.getLogger(ServicesConfiguration.class);

	 @Value("${spring.jersey.application-path:/api}")
	 private String apiPath;
	 
	public ServicesConfiguration() {
        registerEndpoints();
		//register(IntegrationAuthProvider.class);     
        register(RolesAllowedDynamicFeature.class);
	}
	
	@PostConstruct
    public void init() {
		log.info("Initializing swagger");
        configureSwagger();
        log.info("Swagger initialized");
    }	

	private void registerEndpoints() {

		packages("com.talytica.services.admin");
		packages("com.talytica.services.integration");
		packages("com.talytica.services.portal");
		packages("com.talytica.services.survey");
		
		register(WadlResource.class);
	}

	
	private void configureSwagger() {
	     this.register(ApiListingResource.class);
	     this.register(SwaggerSerializers.class);

	     BeanConfig config = new BeanConfig();
	     config.setConfigId("com.talytica.services");
	     config.setTitle("Talytica Service APIs");
	     config.setVersion("v1");
	     config.setContact("info@talytica.com");
	     config.setSchemes(new String[] { "http", "https" });
	     config.setBasePath(apiPath);
	     config.setResourcePackage("com.talytica.services.admin,com.talytica.services.integration,com.talytica.services.portal,com.talytica.services.survey");
	     config.setPrettyPrint(true);
	     config.setScan(true);
	   }
}

package com.talytica.integration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
@Component
public class IntegrationClientFactory {
	private static final String defaultUserName = "test";
	private static final String defaultUserPassword = "password";
	
	@Getter
	@Value("${com.talytica.urls.integration}")
	private String server;
	
	
	  public Client newInstance() {
		  return newInstance(defaultUserName, defaultUserPassword);
	}
	  
	  public Client newInstance(String providedUserName, String providedPassword) {
		    Client client = null;
		    ClientConfig cc = new ClientConfig();
		    cc.property("jersey.config.client.followRedirects", Boolean.valueOf(false));
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[]{new X509TrustManager() {
			        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
			    }}, new java.security.SecureRandom());

				client = ClientBuilder.newBuilder().sslContext(sslContext).withConfig(cc).build();
				
			} catch (Exception e) {
				log.warn("Integration client configured WITHOUT ssl context");
				client = ClientBuilder.newBuilder().withConfig(cc).build();			
			}
		    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(providedUserName, providedPassword);
		    client.register(feature);
		    
		    log.debug("New integration client instantiated for server: {}", server);
		    return client;
	  }
}

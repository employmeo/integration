package com.talytica.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.talytica.integration.service.IntegrationPartnerDetailsService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Order(2)
public class IntegrationSecurityConfig extends WebSecurityConfigurerAdapter {

		@Value("${com.talytica.integration.openpages:/health}")
		String[] openPages;
		
	    @Autowired
	    private IntegrationPartnerDetailsService partnerCredentialService;
	    
	    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	        auth
	          .userDetailsService(partnerCredentialService)
	          .passwordEncoder(passwordEncoder());
	        }
	    
	    @Override
	    protected void configure(HttpSecurity http) throws Exception {
			http
	    		.authorizeRequests()
	    		  .antMatchers(openPages).permitAll()
	    		  .anyRequest().authenticated()
		    	.and()
	    		  .sessionManagement()
	    		  .sessionCreationPolicy(SessionCreationPolicy.NEVER)
		    	.and()
		    	  .httpBasic()
	    		.and()
	    		  .csrf().disable()
	    		  .addFilterBefore(new BearerLoginFilter(authenticationManager()),UsernamePasswordAuthenticationFilter.class);
	    }
	    
		@Bean
		public PasswordEncoder passwordEncoder(){
			PasswordEncoder encoder = new IntegrationPasswordEncoder();
			return encoder;
		}			
			    
}
package com.talytica.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;


@Configuration
@EnableWebSecurity
public class ServicesSecurityConfig extends WebSecurityConfigurerAdapter {

	    @Autowired
	    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
	    }
	    
	    @Override
	    protected void configure(HttpSecurity http) throws Exception {
	    	http.csrf().disable();
	    }
}
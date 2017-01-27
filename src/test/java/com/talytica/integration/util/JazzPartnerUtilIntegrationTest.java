package com.talytica.integration.util;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import com.employmeo.data.config.PersistenceConfiguration;
import com.talytica.integration.IntegrationConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PersistenceConfiguration.class, IntegrationConfiguration.class}, loader=AnnotationConfigContextLoader.class)
@ComponentScan({"com.employmeo.data", "com.talytica.integration", "com.talytica.common"})
@Transactional
@Ignore
public class JazzPartnerUtilIntegrationTest {
	
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	//@Autowired
	//private JazzPartnerUtil jazzPartnerUtil;
	

	@Test
	//@Sql(scripts = "/sql/CorefactorRepositoryIntegrationTest.sql", config = @SqlConfig(commentPrefix = "--"))
	public void findById() {
		log.info("integration test findById invoked");
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}

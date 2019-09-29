package com.talytica.integration;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IntegrationSecurityLog { 
	
    @EventListener(condition = "#event.auditEvent.type == 'AUTHENTICATION_FAILURE'")
    public void onAuthFailure(AuditApplicationEvent event) {
        AuditEvent auditEvent = event.getAuditEvent();
        log.info("Auth failure by: {}", auditEvent.getPrincipal());
    } 
}
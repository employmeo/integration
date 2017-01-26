package com.talytica.integration.util;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.PartnerApplicant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class DefaultPartnerUtil extends BasePartnerUtil {@Override
	public List<PartnerApplicant> fetchPartnerApplicants(String[] statuses, Optional<Range<Date>> period) {
		throw new UnsupportedOperationException("Need a specific ATS partner to fetch partner applicants.");
	}

}

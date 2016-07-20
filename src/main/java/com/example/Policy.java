package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.ipacc.enterprise.elm.EventMetadata;

@Component
@ConfigurationProperties(prefix="policy")
public class Policy {
	private String policyNumber;
	
	private String policyHolder;

	@EventMetadata
	public String getPolicyNumber() {
		return policyNumber;
	}

	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}

	@EventMetadata
	public String getPolicyHolder() {
		return policyHolder;
	}

	public void setPolicyHolder(String policyHolder) {
		this.policyHolder = policyHolder;
	}
	
	
}

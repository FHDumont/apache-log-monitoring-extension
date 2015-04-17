package com.appdynamics.extensions.logmonitor.apache.config;

import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Florencio Sarmiento
 *
 */
public class IndividualMetricsToDisplay {

	private Set<String> includeVisitors;

	private Set<String> includeSpiders;

	private Set<String> includePages;

	private Set<String> includeBrowsers;

	private Set<String> includeOs;
	
	private Set<Integer> includeResponseCodes;

	public Set<String> getIncludeVisitors() {
		return includeVisitors;
	}

	public void setIncludeVisitors(Set<String> includeVisitors) {
		this.includeVisitors = includeVisitors;
	}

	public Set<String> getIncludeSpiders() {
		return includeSpiders;
	}

	public void setIncludeSpiders(Set<String> includeSpiders) {
		this.includeSpiders = includeSpiders;
	}

	public Set<String> getIncludePages() {
		return includePages;
	}

	public void setIncludePages(Set<String> includePages) {
		this.includePages = includePages;
	}

	public Set<String> getIncludeBrowsers() {
		return includeBrowsers;
	}

	public void setIncludeBrowsers(Set<String> includeBrowsers) {
		this.includeBrowsers = includeBrowsers;
	}

	public Set<String> getIncludeOs() {
		return includeOs;
	}

	public void setIncludeOs(Set<String> includeOs) {
		this.includeOs = includeOs;
	}

	public Set<Integer> getIncludeResponseCodes() {
		return includeResponseCodes;
	}

	public void setIncludeResponseCodes(Set<Integer> includeResponseCodes) {
		this.includeResponseCodes = includeResponseCodes;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

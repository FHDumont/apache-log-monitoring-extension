package com.appdynamics.extensions.logmonitor.apache.config;

import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Florencio Sarmiento
 *
 */
public class MetricsFilterForCalculation {

	private Set<String> excludeVisitors;

	private Set<String> excludeSpiders;

	private Set<String> excludeUrls;

	private Set<String> excludeBrowsers;

	private Set<String> excludeOs;

	public Set<String> getExcludeVisitors() {
		return excludeVisitors;
	}

	public void setExcludeVisitors(Set<String> excludeVisitors) {
		this.excludeVisitors = excludeVisitors;
	}

	public Set<String> getExcludeSpiders() {
		return excludeSpiders;
	}

	public void setExcludeSpiders(Set<String> excludeSpiders) {
		this.excludeSpiders = excludeSpiders;
	}

	public Set<String> getExcludeBrowsers() {
		return excludeBrowsers;
	}

	public void setExcludeBrowsers(Set<String> excludeBrowsers) {
		this.excludeBrowsers = excludeBrowsers;
	}

	public Set<String> getExcludeOs() {
		return excludeOs;
	}

	public void setExcludeOs(Set<String> excludeOs) {
		this.excludeOs = excludeOs;
	}

	public Set<String> getExcludeUrls() {
		return excludeUrls;
	}

	public void setExcludeUrls(Set<String> excludeUrls) {
		this.excludeUrls = excludeUrls;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

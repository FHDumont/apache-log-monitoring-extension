package com.appdynamics.extensions.logmonitor.apache.config;

import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Florencio Sarmiento
 *
 */
public class Configuration {

	private String metricPrefix;

	private List<ApacheLog> apacheLogs;

	private int noOfThreads;

	public String getMetricPrefix() {
		return metricPrefix;
	}

	public void setMetricPrefix(String metricPrefix) {
		this.metricPrefix = metricPrefix;
	}

	public List<ApacheLog> getApacheLogs() {
		return apacheLogs;
	}

	public void setApacheLogs(List<ApacheLog> apacheLogs) {
		this.apacheLogs = apacheLogs;
	}

	public int getNoOfThreads() {
		return noOfThreads;
	}

	public void setNoOfThreads(int noOfThreads) {
		this.noOfThreads = noOfThreads;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

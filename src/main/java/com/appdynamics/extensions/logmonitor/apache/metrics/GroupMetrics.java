/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Florencio Sarmiento
 *
 */
public class GroupMetrics extends Metrics {
	
	private Map<String, Metrics> members = new ConcurrentHashMap<String, Metrics>();
	
	public void incrementGroupAndMemberMetrics(String memberKey, Integer bandwidth, boolean isPageView) {
		incrementGroupMetrics(bandwidth, isPageView);
		incrementMemberMetrics(memberKey, bandwidth, isPageView);
	}
	
	public void incrementGroupMetrics(Integer bandwidth, boolean isPageView) {
		incrementMetricsCounter(this, bandwidth, isPageView);
	}
	
	public void incrementMemberMetrics(String memberKey, Integer bandwidth, boolean isPageView) {
		if (StringUtils.isNotBlank(memberKey)) {
			Metrics metricsCounter = this.members.get(memberKey);
			
			if (metricsCounter == null) {
				metricsCounter = new Metrics();
			}
			
			incrementMetricsCounter(metricsCounter, bandwidth, isPageView);
			this.members.put(memberKey, metricsCounter);
		}
	}
	
	private void incrementMetricsCounter(Metrics counter, Integer bandwidth, boolean isPageView) {
		if (isPageView) {
			counter.incrementPageViewCount();

		}
		
		if (bandwidth != null) {
			counter.addBandwidth(bandwidth);
		}
		
		counter.incrementHitCount();
	}

	public void incrementGroupAndMemberMetrics(String memberKey, Integer bandwidth, boolean isPageView, Integer response, Integer responseTimeMicro, Integer responseTimeMicro200, Integer responseTimeMili, Integer responseTimeMili200) {
		incrementGroupMetrics(bandwidth, isPageView);

		incrementMemberNewMetrics(memberKey, bandwidth, isPageView, response, responseTimeMicro, responseTimeMicro200, responseTimeMili, responseTimeMili200);
	}
	
	public void incrementGroupMetrics(Integer bandwidth, boolean isPageView, Integer response, Integer responseTimeMicro, Integer responseTimeMicro200, Integer responseTimeMili, Integer responseTimeMili200) {
		incrementMetricsCounter(this, bandwidth, isPageView, response, responseTimeMicro, responseTimeMicro200, responseTimeMili, responseTimeMili200);
	}
	
	public void incrementMemberMetrics(String memberKey, Integer bandwidth, boolean isPageView, Integer response, Integer responseTimeMicro, Integer responseTimeMicro200, Integer responseTimeMili, Integer responseTimeMili200 ) {
		if (StringUtils.isNotBlank(memberKey)) {
			Metrics metricsCounter = this.members.get(memberKey);
			
			if (metricsCounter == null) {
				metricsCounter = new Metrics();
			}
			
			incrementMetricsCounter(metricsCounter, bandwidth, isPageView, response, responseTimeMicro, responseTimeMicro200, responseTimeMili, responseTimeMili200);
			this.members.put(memberKey, metricsCounter);
		}
	}

	public void incrementMemberNewMetrics(String memberKey, Integer bandwidth, boolean isPageView, Integer response, Integer responseTimeMicro, Integer responseTimeMicro200, Integer responseTimeMili, Integer responseTimeMili200 ) {
		if (StringUtils.isNotBlank(memberKey)) {
			Metrics metricsCounter = this.members.get(memberKey);
			
			if (metricsCounter == null) {
				metricsCounter = new Metrics();
			}
			
			incrementMetricsCounter(metricsCounter, bandwidth, isPageView, response, responseTimeMicro, responseTimeMicro200, responseTimeMili, responseTimeMili200);
			this.members.put(memberKey, metricsCounter);
		}
	}
	
	private void incrementMetricsCounter(Metrics counter, Integer bandwidth, boolean isPageView, Integer response, Integer responseTimeMicro, Integer responseTimeMicro200, Integer responseTimeMili, Integer responseTimeMili200) {
		if (isPageView) {
			counter.incrementPageViewCount();

			if ((response >= 200) && (response < 300)) {
				counter.incrementHit200Count();
			}
			else if(response >= 300) {
				counter.incrementHitNon200Count();
			}

			if (responseTimeMicro != null) {
				counter.addResponseTimeMicro(responseTimeMicro);
			}
	
			if (responseTimeMicro200 != null) {
				counter.addResponseTimeMicro200(responseTimeMicro200);
			}
	
			if (responseTimeMili != null) {
				counter.addResponseTimeMili(responseTimeMili);
			}
	
			if (responseTimeMili200 != null) {
				counter.addResponseTimeMili200(responseTimeMili200);
			}
	
		}
		
		if (bandwidth != null) {
			counter.addBandwidth(bandwidth);
		}
		
		counter.incrementHitCount();

	}
	
	public Map<String, Metrics> getMembers() {
		return this.members;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}

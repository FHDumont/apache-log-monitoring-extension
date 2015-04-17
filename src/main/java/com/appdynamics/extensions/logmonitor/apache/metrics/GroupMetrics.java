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
	
	public Map<String, Metrics> getMembers() {
		return this.members;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}

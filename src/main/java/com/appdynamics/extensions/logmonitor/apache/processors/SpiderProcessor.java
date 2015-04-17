package com.appdynamics.extensions.logmonitor.apache.processors;

import static com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil.*;
import static com.appdynamics.extensions.logmonitor.apache.Constants.*;

import java.util.Set;
import java.util.regex.Pattern;

import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;

/**
 * @author Florencio Sarmiento
 *
 */
public class SpiderProcessor {
	
	private Pattern metricExcludesPattern;
	
	private Pattern displayIncludesPattern;

	public SpiderProcessor(Set<String> metricExcludes,
			Set<String> displayIncludes) {
		
		this.metricExcludesPattern = createPattern(metricExcludes);
		this.displayIncludesPattern = createPattern(displayIncludes);
	}
	
	public boolean isSpider(String deviceName, String request) {
		return SPIDER.equalsIgnoreCase(deviceName) ||
				SPIDER_REQUEST.equals(request);
	}
	
	public boolean isToMonitor(String agentName) {
		return isNotMatch(agentName, metricExcludesPattern);
	}
	
	public void processMetrics(String agentName, Integer bandwidth, 
			boolean isPageView, ApacheLogMetrics apacheLogMetrics) {
		
		if (isMatch(agentName, displayIncludesPattern)) {
			apacheLogMetrics.getSpiderMetrics()
				.incrementGroupAndMemberMetrics(agentName, bandwidth, isPageView);
			
		} else {
			apacheLogMetrics.getSpiderMetrics()
				.incrementGroupMetrics(bandwidth, isPageView);
		}
	}
}

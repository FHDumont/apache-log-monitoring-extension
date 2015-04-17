package com.appdynamics.extensions.logmonitor.apache.processors;

import static com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil.*;

import java.util.Set;
import java.util.regex.Pattern;

import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;

/**
 * @author Florencio Sarmiento
 *
 */
public class BrowserProcessor {
	
	private Pattern metricExcludesPattern;
	
	private Pattern displayIncludesPattern;

	public BrowserProcessor(Set<String> metricExcludes,
			Set<String> displayIncludes) {
		
		this.metricExcludesPattern = createPattern(metricExcludes);
		this.displayIncludesPattern = createPattern(displayIncludes);
	}
	
	public boolean isToMonitor(String browser) {
		return isNotMatch(browser, metricExcludesPattern);
	}
	
	public void processMetrics(String browser, Integer bandwidth, 
			boolean isPageView, ApacheLogMetrics apacheLogMetrics) {
		
		if (isMatch(browser, displayIncludesPattern)) {
			apacheLogMetrics.getBrowserMetrics()
				.incrementGroupAndMemberMetrics(browser, bandwidth, isPageView);
			
		} else {
			apacheLogMetrics.getBrowserMetrics()
				.incrementGroupMetrics(bandwidth, isPageView);
		}
	}
}

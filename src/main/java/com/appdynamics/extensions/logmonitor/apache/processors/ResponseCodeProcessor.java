package com.appdynamics.extensions.logmonitor.apache.processors;

import static com.appdynamics.extensions.logmonitor.apache.config.DefaultValues.getDefaultHitResponseCodes;

import java.util.Set;

import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;

/**
 * @author Florencio Sarmiento
 *
 */
public class ResponseCodeProcessor {
	
	private Set<Integer> hitResponseCodes;
	
	private Set<Integer> displayIncludes;
	
	public ResponseCodeProcessor(Set<Integer> hitResponseCodes, 
			Set<Integer> displayIncludes) {
		if (hitResponseCodes == null || hitResponseCodes.isEmpty()) {
			this.hitResponseCodes = getDefaultHitResponseCodes();
			
		} else {
			this.hitResponseCodes = hitResponseCodes;
		}
		
		this.displayIncludes = displayIncludes;
	}
	
	public boolean isSuccessfulHit(Integer response) {
		return isMatch(response, hitResponseCodes);
	}	
	
	public void processMetrics(Integer response, Integer bandwidth, 
			boolean isPageView, ApacheLogMetrics apacheLogMetrics) {
		
		if (isToDisplay(response)) {
			apacheLogMetrics.getResponseCodeMetrics()
				.incrementMemberMetrics(response.toString(), bandwidth, isPageView);
		}
	}
	
	private boolean isToDisplay(Integer response) {
		return isMatch(response, displayIncludes);
	}
	
	private boolean isMatch(Integer response, Set<Integer> responseCodes) {
		boolean isMatch = false;

		if (response != null && responseCodes != null) {
			for (Integer hitResponseCode : responseCodes) {
				if (hitResponseCode.equals(response)) {
					isMatch = true;
					break;
				}
			}
		}

		return isMatch;
	}	
}

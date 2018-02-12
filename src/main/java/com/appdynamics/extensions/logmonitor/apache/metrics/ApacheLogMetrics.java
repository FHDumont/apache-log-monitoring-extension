/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache.metrics;

import java.math.BigInteger;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Florencio Sarmiento
 *
 */
public class ApacheLogMetrics {
	
	private String apacheLogName;

	private GroupMetrics visitorMetrics = new GroupMetrics();

	private GroupMetrics spiderMetrics = new GroupMetrics();

	private GroupMetrics browserMetrics = new GroupMetrics();

	private GroupMetrics osMetrics = new GroupMetrics();
	
	private GroupMetrics pageMetrics = new GroupMetrics();
	
	private GroupMetrics responseCodeMetrics = new GroupMetrics();
	
	public String getApacheLogName() {
		return apacheLogName;
	}

	public void setApacheLogName(String apacheLogName) {
		this.apacheLogName = apacheLogName;
	}

	public GroupMetrics getVisitorMetrics() {
		return visitorMetrics;
	}

	public GroupMetrics getSpiderMetrics() {
		return spiderMetrics;
	}

	public GroupMetrics getBrowserMetrics() {
		return browserMetrics;
	}

	public GroupMetrics getOsMetrics() {
		return osMetrics;
	}

	public GroupMetrics getPageMetrics() {
		return pageMetrics;
	}

	public GroupMetrics getResponseCodeMetrics() {
		return responseCodeMetrics;
	}
	
	public BigInteger getTotalHitCount() {
		return getVisitorMetrics().getHitCount()
				.add(getSpiderMetrics().getHitCount());
	}
	
	public BigInteger getTotalPageViewCount() {
		return getVisitorMetrics().getPageViewCount()
				.add(getSpiderMetrics().getPageViewCount());
	}
	
	public BigInteger getTotalBandwidth() {
		return getVisitorMetrics().getBandwidth()
				.add(getSpiderMetrics().getBandwidth());
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

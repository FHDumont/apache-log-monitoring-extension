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
 * @author Diego Pereira
 *
 */
public class Metrics {

	private BigInteger hitCount = BigInteger.ZERO;
	
	private BigInteger pageViewCount = BigInteger.ZERO;
	
	private BigInteger bandwidth = BigInteger.ZERO;

	//  New Features - Diego Pereira

	private BigInteger hit200Count = BigInteger.ZERO;

	private BigInteger hitNon200Count = BigInteger.ZERO;

	private BigInteger responseTimeMicro = BigInteger.ZERO;

	private BigInteger responseTimeMili = BigInteger.ZERO;

	private BigInteger responseTimeMicro200 = BigInteger.ZERO;

	private BigInteger responseTimeMili200 = BigInteger.ZERO;

	// End New Features - Diego Pereira


	
	public void incrementHitCount() {
		this.hitCount = this.hitCount.add(BigInteger.ONE);
	}
	
	public void incrementPageViewCount() {
		this.pageViewCount = this.pageViewCount.add(BigInteger.ONE);
	}
	
	public void addBandwidth(Integer bandwidth) {
		this.bandwidth = this.bandwidth.add(BigInteger.valueOf(bandwidth));
	}

	//  End New Features - Diego Pereira
	
	public void addResponseTimeMicro(Integer responseTime) {

		if (this.responseTimeMicro != BigInteger.ZERO) {
			this.responseTimeMicro = this.responseTimeMicro.add(BigInteger.valueOf(responseTime)).divide(BigInteger.valueOf(2));
		} else {
			this.responseTimeMicro = this.responseTimeMicro.add(BigInteger.valueOf(responseTime));
		}
		
		
	}

	public void addResponseTimeMili(Integer responseTime) {
		
		if (this.responseTimeMili != BigInteger.ZERO) {
			this.responseTimeMili = this.responseTimeMili.add(BigInteger.valueOf(responseTime)).divide(BigInteger.valueOf(2));
		} else {
			this.responseTimeMili = this.responseTimeMili.add(BigInteger.valueOf(responseTime));
		}
	}

	public void addResponseTimeMicro200(Integer responseTime) {

		if (this.responseTimeMicro200 != BigInteger.ZERO) {
			this.responseTimeMicro200 = this.responseTimeMicro200.add(BigInteger.valueOf(responseTime)).divide(BigInteger.valueOf(2));
		} else {
			this.responseTimeMicro200 = this.responseTimeMicro200.add(BigInteger.valueOf(responseTime));
		}
	}

	public void addResponseTimeMili200(Integer responseTime) {
		
		if (this.responseTimeMili200 != BigInteger.ZERO) {
			this.responseTimeMili200 = this.responseTimeMili200.add(BigInteger.valueOf(responseTime)).divide(BigInteger.valueOf(2));
		} else {
			this.responseTimeMili200 = this.responseTimeMili200.add(BigInteger.valueOf(responseTime));
		}
	}

	public void incrementHit200Count() {
		this.hit200Count = this.hit200Count.add(BigInteger.ONE);
	}

	public void incrementHitNon200Count() {
		this.hitNon200Count = this.hitNon200Count.add(BigInteger.ONE);
	}

	//  End New Features - Diego Pereira


	public BigInteger getHitCount() {
		return hitCount;
	}

	public BigInteger getPageViewCount() {
		return pageViewCount;
	}

	public BigInteger getBandwidth() {
		return bandwidth;
	}

	//  New Features - Diego Pereira

	public BigInteger getResponseTimeMicro() {
		BigInteger avgResponseTime;
		avgResponseTime = responseTimeMicro;
		return avgResponseTime;
	}

	public BigInteger getResponseTimeMili() {
		BigInteger avgResponseTime;
		avgResponseTime = responseTimeMili;
		return avgResponseTime;
	}

	public BigInteger getResponseTimeMicro200() {
		BigInteger avgResponseTime;
		avgResponseTime = responseTimeMicro200;
		return avgResponseTime;
	}

	public BigInteger getResponseTimeMili200() {
		BigInteger avgResponseTime;
		avgResponseTime = responseTimeMili200;
		return avgResponseTime;
	}

	public BigInteger getHit200Count() {
		return hit200Count;
	}

	public BigInteger getHitNon200Count() {
		return hitNon200Count;
	}

	// End New Features
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

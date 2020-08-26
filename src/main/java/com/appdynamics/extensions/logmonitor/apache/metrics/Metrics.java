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

	private BigInteger hit200CountRT = BigInteger.ZERO;

	private BigInteger hitCountRT = BigInteger.ZERO;

	private BigInteger totalHitCount = BigInteger.ZERO;

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
		this.hitCountRT = this.hitCountRT.add(BigInteger.ONE);
		this.responseTimeMicro = this.responseTimeMicro.add(BigInteger.valueOf(responseTime));
	}

	public void addResponseTimeMili(Integer responseTime) {
		
		this.responseTimeMili = this.responseTimeMili.add(BigInteger.valueOf(responseTime));
	}

	public void addResponseTimeMicro200(Integer responseTime) {
		this.hit200CountRT = this.hit200CountRT.add(BigInteger.ONE);
		this.responseTimeMicro200 = this.responseTimeMicro200.add(BigInteger.valueOf(responseTime));
		
	}

	public void addResponseTimeMili200(Integer responseTime) {
		
		this.responseTimeMili200 = this.responseTimeMili200.add(BigInteger.valueOf(responseTime));
	}

	public void incrementHit200Count() {
		this.hit200Count = this.hit200Count.add(BigInteger.ONE);
		this.totalHitCount = this.totalHitCount.add(BigInteger.ONE);
	}

	public void incrementHitNon200Count() {
		this.hitNon200Count = this.hitNon200Count.add(BigInteger.ONE);
		this.totalHitCount = this.totalHitCount.add(BigInteger.ONE);
	}

	//  End New Features - Diego Pereira


	public BigInteger getHitCount() {
		return hitCount;
	}
	public BigInteger getTotalHitCount() {
		return totalHitCount;
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
		if (this.hitCountRT != BigInteger.ZERO) {
			avgResponseTime = responseTimeMicro.divide(this.hitCountRT);
		} else {
			avgResponseTime = responseTimeMicro;
		}
		
		return avgResponseTime;
	}

	public BigInteger getResponseTimeMili() {
		BigInteger avgResponseTime;
		if (this.hitCountRT != BigInteger.ZERO) {
			avgResponseTime = responseTimeMili.divide(this.hitCountRT);
		} else {
			avgResponseTime = responseTimeMili;
		}
		
		return avgResponseTime;
	}

	public BigInteger getResponseTimeMicro200() {
		BigInteger avgResponseTime;
		if (this.hit200CountRT != BigInteger.ZERO) {
			avgResponseTime = responseTimeMicro200.divide(this.hit200CountRT);
		} else {
			avgResponseTime = responseTimeMicro200;
		}
		
		return avgResponseTime;
	}

	public BigInteger getResponseTimeMili200() {
		BigInteger avgResponseTime;
		if (this.hit200CountRT != BigInteger.ZERO) {
			avgResponseTime = responseTimeMili200.divide(this.hit200CountRT);
		} else {
			avgResponseTime = responseTimeMili200;
		}
		
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

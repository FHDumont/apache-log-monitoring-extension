package com.appdynamics.extensions.logmonitor.apache.metrics;

import java.math.BigInteger;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Florencio Sarmiento
 *
 */
public class Metrics {

	private BigInteger hitCount = BigInteger.ZERO;
	
	private BigInteger pageViewCount = BigInteger.ZERO;
	
	private BigInteger bandwidth = BigInteger.ZERO;
	
	public void incrementHitCount() {
		this.hitCount = this.hitCount.add(BigInteger.ONE);
	}
	
	public void incrementPageViewCount() {
		this.pageViewCount = this.pageViewCount.add(BigInteger.ONE);
	}
	
	public void addBandwidth(Integer bandwidth) {
		this.bandwidth = this.bandwidth.add(BigInteger.valueOf(bandwidth));
	}

	public BigInteger getHitCount() {
		return hitCount;
	}

	public BigInteger getPageViewCount() {
		return pageViewCount;
	}

	public BigInteger getBandwidth() {
		return bandwidth;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

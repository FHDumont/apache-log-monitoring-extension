/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache.processors;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;
import com.appdynamics.extensions.logmonitor.apache.metrics.Metrics;

public class VisitorProcessorTest {
	
	private VisitorProcessor classUnderTest;
	
	@Test
	public void testIsToMonitorReturnsTrue() {
		classUnderTest = new VisitorProcessor(new HashSet<String>(), 
				new HashSet<String>());
		
		for (String host: getTestHosts()) {
			assertTrue(classUnderTest.isToMonitor(host));
		}
	}
	
	@Test
	public void testIsToMonitorReturnsFalse() {
		Set<String> metricExcludes = new HashSet<String>();
		metricExcludes.add("localhost");
		metricExcludes.add("10\\.10\\.10\\.1");
		
		classUnderTest = new VisitorProcessor(metricExcludes, 
				new HashSet<String>());
		
		List<String> testHosts = Arrays.asList("10.10.10.1",
				"localhost");
		
		for (String host: testHosts) {
			assertFalse(classUnderTest.isToMonitor(host));
		}
	}
	
	@Test
	public void testProcessMetricsNoMemberToDisplay() {
		classUnderTest = new VisitorProcessor(new HashSet<String>(), 
				new HashSet<String>());
		
		ApacheLogMetrics testMetrics = new ApacheLogMetrics();
		Integer testBandwidth = 15;
		Integer responseTimeMicro = 1000;
		Integer responseTimeMicro200 = 1000;
		Integer responseTimeMili = 1;
		Integer responseTimeMili200 = 1;
		Integer response = 200;

		for (String host: getTestHosts()) {
			classUnderTest.processMetrics(host, testBandwidth, true, testMetrics, response, responseTimeMicro, responseTimeMicro200, responseTimeMili, responseTimeMili200);
		}
		
		assertEquals(BigInteger.valueOf(5), testMetrics.getVisitorMetrics().getHitCount());
		assertEquals(BigInteger.valueOf(5), testMetrics.getVisitorMetrics().getPageViewCount());
		assertEquals(BigInteger.valueOf(testBandwidth * getTestHosts().size()), 
				testMetrics.getVisitorMetrics().getBandwidth());
		assertTrue(testMetrics.getVisitorMetrics().getMembers().isEmpty());
	}
	
	@Test
	public void testProcessMetricsWithMembersToDisplay() {
		Set<String> displayIncludes = new HashSet<String>();
		displayIncludes.add("localhost");
		displayIncludes.add("10\\.10\\.10\\.1");
		
		classUnderTest = new VisitorProcessor(new HashSet<String>(), 
				displayIncludes);
		
		ApacheLogMetrics testMetrics = new ApacheLogMetrics();
		Integer testBandwidth = 15;
		Integer responseTimeMicro = 1000;
		Integer responseTimeMicro200 = 1000;
		Integer responseTimeMili = 1;
		Integer responseTimeMili200 = 1;
		Integer response = 200;
		
		for (String host: getTestHosts()) {
			classUnderTest.processMetrics(host, testBandwidth, true, testMetrics, response, responseTimeMicro, responseTimeMicro200, responseTimeMili, responseTimeMili200);
		}
		
		assertEquals(BigInteger.valueOf(5), testMetrics.getVisitorMetrics().getHitCount());
		assertEquals(BigInteger.valueOf(5), testMetrics.getVisitorMetrics().getPageViewCount());
		assertEquals(BigInteger.valueOf(testBandwidth * getTestHosts().size()), 
				testMetrics.getVisitorMetrics().getBandwidth());
		assertEquals(2, testMetrics.getVisitorMetrics().getMembers().size());
		
		Metrics localhostMetrics = testMetrics.getVisitorMetrics().getMembers().get("localhost");
		assertEquals(BigInteger.valueOf(2), localhostMetrics.getHitCount());
		assertEquals(BigInteger.valueOf(2), localhostMetrics.getPageViewCount());
		assertEquals(BigInteger.valueOf(testBandwidth * 2), localhostMetrics.getBandwidth());
		
		Metrics ipMetrics = testMetrics.getVisitorMetrics().getMembers().get("10.10.10.1");
		assertEquals(BigInteger.ONE, ipMetrics.getHitCount());
		assertEquals(BigInteger.ONE, ipMetrics.getPageViewCount());
		assertEquals(BigInteger.valueOf(testBandwidth), ipMetrics.getBandwidth());
	}
	
	private List<String> getTestHosts() {
		return Arrays.asList("1.1.1.1", 
				"10.10.10.1",
				"this.is.a.test",
				"localhost",
				"localhost");
		
	}
}

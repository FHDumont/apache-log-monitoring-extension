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

public class ResponseCodeProcessorTest {
	
	private ResponseCodeProcessor classUnderTest;
	
	@Test
	public void testIsSuccessfulHitUsingDefaultResponseCodeReturnsTrue() {
		classUnderTest = new ResponseCodeProcessor(new HashSet<Integer>(),
				new HashSet<Integer>());
		
		assertTrue(classUnderTest.isSuccessfulHit(200));
		assertTrue(classUnderTest.isSuccessfulHit(304));
	}
	
	@Test
	public void testIsSuccessfulHitUsingDefaultResponseCodeReturnsFalse() {
		classUnderTest = new ResponseCodeProcessor(new HashSet<Integer>(),
				new HashSet<Integer>());
		
		assertFalse(classUnderTest.isSuccessfulHit(220));
		assertFalse(classUnderTest.isSuccessfulHit(305));
		assertFalse(classUnderTest.isSuccessfulHit(354));
	}
	
	@Test
	public void testIsSuccessfulHitReturnsTrue() {
		Set<Integer> testHitResponseCodes = new HashSet<Integer>();
		testHitResponseCodes.add(214);
		testHitResponseCodes.add(220);
		testHitResponseCodes.add(221);
		
		classUnderTest = new ResponseCodeProcessor(testHitResponseCodes,
				new HashSet<Integer>());
		
		assertTrue(classUnderTest.isSuccessfulHit(214));
		assertTrue(classUnderTest.isSuccessfulHit(220));
		assertTrue(classUnderTest.isSuccessfulHit(221));
	}
	
	@Test
	public void testIsSuccessfulHitReturnsFalse() {
		Set<Integer> testHitResponseCodes = new HashSet<Integer>();
		testHitResponseCodes.add(214);
		testHitResponseCodes.add(220);
		testHitResponseCodes.add(221);
		
		classUnderTest = new ResponseCodeProcessor(testHitResponseCodes,
				new HashSet<Integer>());
		
		assertFalse(classUnderTest.isSuccessfulHit(200));
		assertFalse(classUnderTest.isSuccessfulHit(304));
	}
	
	@Test
	public void testProcessMetricsNothingToDisplay() {
		classUnderTest = new ResponseCodeProcessor(new HashSet<Integer>(),
				new HashSet<Integer>());
		
		List<Integer> testResponseCodes = Arrays.asList(200, 211, 214, 305, 354, 354);
		ApacheLogMetrics testMetrics = new ApacheLogMetrics();
		Integer testBandwidth = 15;

		Integer responseTimeMicro = 1000;
		Integer responseTimeMicro200 = 1000;
		Integer responseTimeMili = 1;
		Integer responseTimeMili200 = 1;
		
		for (Integer responseCode : testResponseCodes) {
			classUnderTest.processMetrics(responseCode, testBandwidth, true, testMetrics);
		}
		
		// we're not expecting any group metrics
		assertEquals(BigInteger.ZERO, testMetrics.getResponseCodeMetrics().getHitCount());
		assertEquals(BigInteger.ZERO, testMetrics.getResponseCodeMetrics().getPageViewCount());
		assertEquals(BigInteger.ZERO, testMetrics.getResponseCodeMetrics().getBandwidth());
		assertTrue(testMetrics.getResponseCodeMetrics().getMembers().isEmpty());
	}
	
	@Test
	public void testProcessMetricsSelectedMembersToDisplay() {
		Set<Integer> displayIncludes = new HashSet<Integer>();
		displayIncludes.add(200);
		displayIncludes.add(354);
		
		classUnderTest = new ResponseCodeProcessor(new HashSet<Integer>(),
				displayIncludes);
		
		List<Integer> testResponseCodes = Arrays.asList(200, 211, 214, 305, 354, 354);
		ApacheLogMetrics testMetrics = new ApacheLogMetrics();
		Integer testBandwidth = 15;
		
		Integer responseTimeMicro = 1000;
		Integer responseTimeMicro200 = 1000;
		Integer responseTimeMili = 1;
		Integer responseTimeMili200 = 1;
		
		for (Integer responseCode : testResponseCodes) {
			classUnderTest.processMetrics(responseCode, testBandwidth, true, testMetrics);
		}
		
		// we're not expecting any group metrics, only member metrics
		assertEquals(BigInteger.ZERO, testMetrics.getResponseCodeMetrics().getHitCount());
		assertEquals(BigInteger.ZERO, testMetrics.getResponseCodeMetrics().getPageViewCount());
		assertEquals(BigInteger.ZERO, testMetrics.getResponseCodeMetrics().getBandwidth());
		assertEquals(2, testMetrics.getResponseCodeMetrics().getMembers().size());
		
		Metrics resp200Metrics = testMetrics.getResponseCodeMetrics().getMembers().get("200");
		assertEquals(BigInteger.ONE, resp200Metrics.getHitCount());
		assertEquals(BigInteger.ONE, resp200Metrics.getPageViewCount());
		assertEquals(BigInteger.valueOf(testBandwidth), resp200Metrics.getBandwidth());
		
		Metrics resp354Metrics = testMetrics.getResponseCodeMetrics().getMembers().get("354");
		assertEquals(BigInteger.valueOf(2), resp354Metrics.getHitCount());
		assertEquals(BigInteger.valueOf(2), resp354Metrics.getPageViewCount());
		assertEquals(BigInteger.valueOf(2 * testBandwidth), resp354Metrics.getBandwidth());
	}

}

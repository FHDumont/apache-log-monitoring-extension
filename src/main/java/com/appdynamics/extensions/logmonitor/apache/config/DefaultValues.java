/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache.config;

import static com.appdynamics.extensions.logmonitor.apache.Constants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Florencio Sarmiento
 *
 */
public final class DefaultValues {
	
	private static final Set<Integer> hitResponseCodes = new HashSet<Integer>();
	
	private static final Set<String> nonPageExtensions = new HashSet<String>();
	
	public static final String DEFAULT_METRIC_PATH = String.format("%s%s%s%s", "Custom Metrics", 
			METRIC_PATH_SEPARATOR, "Apache Log Monitor", METRIC_PATH_SEPARATOR);
	
	public static final String DEFAULT_HACK_PREFIX = "false";

	public static final int DEFAULT_NO_OF_THREADS = 3;
	
	public static final int THREAD_TIMEOUT = 60;
	
	static {
		hitResponseCodes.add(OK_RESPONSE);
		hitResponseCodes.add(NOT_MODIFIED_RESPONSE);
		
		nonPageExtensions.add("ico");
		nonPageExtensions.add("css");
		nonPageExtensions.add("js");
		nonPageExtensions.add("class");
		nonPageExtensions.add("gif");
		nonPageExtensions.add("jpg");
		nonPageExtensions.add("jpeg");
		nonPageExtensions.add("png");
		nonPageExtensions.add("bmp");
		nonPageExtensions.add("rss");
		nonPageExtensions.add("xml");
		nonPageExtensions.add("swf");
	}

	public static Set<Integer> getDefaultHitResponseCodes() {
		return Collections.unmodifiableSet(hitResponseCodes);
	}

	public static Set<String> getDefaultNonPageExtensions() {
		return Collections.unmodifiableSet(nonPageExtensions);
	}
}

/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache;

import java.io.File;

/**
 * @author Florencio Sarmiento
 *
 */
public final class Constants {
	
	public static final String CONFIG_DIR = "config-dir";
	
	public static final String CONFIG_FILE = "config.yaml";
	
	public static final String PATTERNS = "patterns";
	
	public static final String GROK_FILE = String.format("%s%s%s", 
			PATTERNS, File.separator, "grok-patterns.grok");
	
	public static final String USER_AGENT_FILE = String.format("%s%s%s", 
			PATTERNS, File.separator, "user-agent-regexes.yaml");
	
	public static final String FILEPOINTER_FILENAME = "filepointer.json";
	
	public static final String METRIC_PATH_SEPARATOR = "|";
	
	public static final String RESPONSE = "response";
	
	public static final String BYTES = "bytes";
	
	public static final String REQUEST = "request";
	
	public static final String AGENT = "agent";
	
	public static final String HOST = "clientip";

	// New Features - Diego Pereira
	
	public static final String REQUEST_PORT = "portnumber";
	
	public static final String REQUEST_VERB = "verb";

	public static final String REQUEST_MILI = "miliseg";

	public static final String REQUEST_SEG = "seg";

	public static final String EXTRA_HOSTPORT = "hostport";
	
	public static final String LOG_DATETIME = "timestamp";

	// End New Features
	
	public static final String SPIDER = "Spider";
	
	public static final String SPIDER_REQUEST = "/robots.txt";
	
	public static final int OK_RESPONSE = 200;
	
	public static final int NOT_MODIFIED_RESPONSE = 304;
	
	public static final String TOTAL_HITS = "Total Hits";
	
	public static final String TOTAL_PAGES = "Total Pages";
	
	public static final String TOTAL_BANDWIDTH = "Total Bandwidth (bytes)";

	// New Features - Diego Pereira

	public static final String TOTAL_PAGES_CALLS = "Total Pages View";
	
	public static final String TOTAL_200_HITS = "Total Success Pages View";


	public static final String TOTAL_NON_200_HITS = "Total Non Success Pages View";

	public static final String HITS_200 = "Success Pages View";
	public static final String HITS_NON_200 = "Non Success Pages View";


	public static final String AVG_RESPONSETIME_MICRO = "Avg Response Time (micro)";
	public static final String AVG_RESPONSETIME_MICRO200 = "Avg Normal Response Time (micro)";
	public static final String AVG_RESPONSETIME_MILI = "Avg Response Time (mili)";
	public static final String AVG_RESPONSETIME_MILI200 = "Avg Normal Response Time (mili)";

	// End New Features - Diego Pereira
	
	public static final String HITS = "Hits";
	
	public static final String PAGES = "Pages";
	
	public static final String BANDWIDTH = "Bandwidth (bytes)";
	
	public static final String VISITOR = "Visitor";
	
	public static final String BROWSER = "Browser";
	
	public static final String OS = "OS";
	
	public static final String PAGE = "Page";
	
	public static final String RESPONSE_CODE = "Response Code";
}

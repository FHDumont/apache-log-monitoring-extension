/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache;

import static com.appdynamics.extensions.logmonitor.apache.Constants.*;
import static com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

//import org.apache.log4j.Logger;
//import org.slf4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;


import org.codehaus.jackson.map.ObjectMapper;

import ua_parser.Client;
import ua_parser.Parser;

import com.appdynamics.extensions.logmonitor.apache.config.ApacheLog;
import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;
import com.appdynamics.extensions.logmonitor.apache.processors.BrowserProcessor;
import com.appdynamics.extensions.logmonitor.apache.processors.OsProcessor;
import com.appdynamics.extensions.logmonitor.apache.processors.RequestProcessor;
import com.appdynamics.extensions.logmonitor.apache.processors.ResponseCodeProcessor;
import com.appdynamics.extensions.logmonitor.apache.processors.SpiderProcessor;
import com.appdynamics.extensions.logmonitor.apache.processors.VisitorProcessor;

/**
 * @author Florencio Sarmiento
 *
 */
public class MetricsExtractor {
	
	private static final Logger LOGGER = LogManager.getLogger("com.appdynamics.extensions.logmonitor.apache.MetricsExtractor");
	
	private ApacheLog apacheLogConfig;
	
	private Grok grok;
	private ObjectMapper mapper;
	private Parser userAgentParser;
	
	private VisitorProcessor visitorProcessor;
	private SpiderProcessor spiderProcessor;
	private BrowserProcessor browserProcessor;
	private OsProcessor osProcessor;
	private RequestProcessor requestProcessor;
	private ResponseCodeProcessor responseCodeProcessor;
	
	public MetricsExtractor(String grokPatternFilePath,
			String userAgentPatternFilePath,
			ApacheLog apacheLogConfig) throws GrokException, FileNotFoundException {
		
		this.apacheLogConfig = apacheLogConfig;
		initialiseLogParsers(grokPatternFilePath, userAgentPatternFilePath);
		initialiseProcessors();
	}

	@SuppressWarnings("unchecked")
	public void extractMetrics(String data, ApacheLogMetrics apacheLogMetrics) {
		try {
			Match gm = grok.match(data);
			gm.captures();
			LOGGER.info(String.format(
								"Data JSON [%s]",
								gm.toJson()));
			
			Map<String, Object> rawData = mapper.readValue(gm.toJson(), Map.class);

			
			LOGGER.info(String.format(
							"Debug Test [%s]",
							data));
							
			if (rawData != null && !rawData.isEmpty()) {
								
								
				LOGGER.info(String.format(
								"Data Matched [%s]",
								data));

				String host = (String) rawData.get(HOST);
				Integer response = (Integer) rawData.get(RESPONSE);
				Integer bandwidth = (Integer) rawData.get(BYTES);
				String request = requestProcessor.removeParam(((String) rawData.get(REQUEST)));
				String userAgent = (String) rawData.get(AGENT);

				boolean isPageView = requestProcessor.isPage(request);
				Client agentInfo = parseUserAgent(userAgent);

				// New Features - Diego Pereira

				Integer port = (Integer) rawData.get(REQUEST_PORT);
				String verb = (String) rawData.get(REQUEST_VERB);
				
				Double rmicrodouble;
				Double rmilidouble;


				// Parse Response Time
				try {
					Integer rmicroint = (Integer) rawData.get(REQUEST_MILI);
					rmicrodouble = Double.valueOf(rmicroint);
					
				} catch (Exception e) {
					LOGGER.info(e.getMessage());
					LOGGER.info("Trying get from String");
					rmicrodouble = Double.parseDouble((String) rawData.get(REQUEST_MILI));
				}
				
				try {
					Integer rmiliint = (Integer) rawData.get(REQUEST_SEG);
					rmilidouble = Double.valueOf(rmiliint);
					
				} catch (Exception e) {
					LOGGER.info(e.getMessage());
					LOGGER.info("Trying get from String");
					rmilidouble = Double.parseDouble((String) rawData.get(REQUEST_SEG));
				}

				Integer rmicro = (int) (Math.round(rmicrodouble * 1000.0)) ;
				Integer rmili = (int) (Math.round(rmilidouble * 1000.0));


				Integer rmicro200;
				Integer rmili200;


				if (response >= 200 && response < 300) {
					rmicro200 = rmicro;
					rmili200 = rmili;

				} else {
					rmicro200 = null;
					rmili200 = null;
				}

				//String rehostport = (String) rawData.get(EXTRA_HOSTPORT);
				String ldatetime = (String) rawData.get(LOG_DATETIME);

				Integer current_minute = LocalDateTime.now().getMinute();

				LOGGER.info(String.format(
							"Got Current Minute [%s], DateTime [%s], Host [%s], Port [%s], Verb [%s], Request [%s], User-Agent [%s], OS [%s], Status [%s], Bytes [%s], Micro [%s], Mili [%s]",
							current_minute, ldatetime, host, port, verb, request, agentInfo.userAgent.family, agentInfo.os.family, response, bandwidth, rmicro, rmili));

				// End New Features

				
				if (!isToMonitorMetrics(host, request, agentInfo)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String.format(
								"Excluded Host [%s], Request [%s], User-Agent [%s], OS [%s]",
								host, request, agentInfo.userAgent.family, agentInfo.os.family));
					}
					
					return;
				}
				
				if (!responseCodeProcessor.isSuccessfulHit(response)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String.format(
								"Ignoring request [%s] from [%s] as not a successful hit. Response [%s] metric may still be counted.",
								request, host, response));
					}
					
					responseCodeProcessor.processMetrics(response, bandwidth, 
							isPageView, apacheLogMetrics);
					
					return;
				}
				
				if (spiderProcessor.isSpider(agentInfo.device.family, request)) {
					spiderProcessor.processMetrics(agentInfo.userAgent.family, bandwidth, 
							isPageView, apacheLogMetrics, response, rmicro, rmicro200, rmili, rmili200);

					
				} else {
					visitorProcessor.processMetrics(host, bandwidth, 
							isPageView, apacheLogMetrics, response, rmicro, rmicro200, rmili, rmili200);
					
					browserProcessor.processMetrics(agentInfo.userAgent.family, 
							bandwidth, isPageView, apacheLogMetrics);
				}
				
				osProcessor.processMetrics(agentInfo.os.family, 
						bandwidth, isPageView, apacheLogMetrics);
				
				requestProcessor.processMetrics(request, bandwidth, 
						isPageView, apacheLogMetrics, response, rmicro, rmicro200, rmili, rmili200);
				
				responseCodeProcessor.processMetrics(response, bandwidth, 
						isPageView, apacheLogMetrics);
				
			} else if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(String.format("[%s] did not match grok pattern", data));
			}
			
		} catch (Exception ex) {
			LOGGER.error(String.format("Unable to extract metrics for line [%s] due to error.",
					data), ex);
		} 
	}
	
	private void initialiseLogParsers(String grokPatternFilePath, 
			String userAgentPatternFilePath) throws GrokException, FileNotFoundException {
		this.grok = Grok.create(resolvePath(grokPatternFilePath), 
				apacheLogConfig.getLogPattern());
		LOGGER.info(String.format("Grok Pattern: [%s] .", apacheLogConfig.getLogPattern()) );
		this.mapper = new ObjectMapper();
		this.userAgentParser = new Parser(
				new FileInputStream(resolvePath(userAgentPatternFilePath)));
	}
	
	private void initialiseProcessors() {
		this.visitorProcessor = new VisitorProcessor(
				apacheLogConfig.getMetricsFilterForCalculation().getExcludeVisitors(), 
				apacheLogConfig.getIndividualMetricsToDisplay().getIncludeVisitors());
		
		this.spiderProcessor = new SpiderProcessor(
				apacheLogConfig.getMetricsFilterForCalculation().getExcludeSpiders(), 
				apacheLogConfig.getIndividualMetricsToDisplay().getIncludeSpiders());
		
		this.browserProcessor = new BrowserProcessor(
				apacheLogConfig.getMetricsFilterForCalculation().getExcludeBrowsers(), 
				apacheLogConfig.getIndividualMetricsToDisplay().getIncludeBrowsers());
		
		this.osProcessor = new OsProcessor(
				apacheLogConfig.getMetricsFilterForCalculation().getExcludeOs(), 
				apacheLogConfig.getIndividualMetricsToDisplay().getIncludeOs());
		
		this.requestProcessor = new RequestProcessor(
				apacheLogConfig.getMetricsFilterForCalculation().getExcludeUrls(), 
				apacheLogConfig.getIndividualMetricsToDisplay().getIncludePages(), 
				apacheLogConfig.getNonPageExtensions());
		
		this.responseCodeProcessor = new ResponseCodeProcessor(
				apacheLogConfig.getHitResponseCodes(),
				apacheLogConfig.getIndividualMetricsToDisplay().getIncludeResponseCodes());
	}
	
	private boolean isToMonitorMetrics(String host, String url, Client agentInfo) {
		if (spiderProcessor.isSpider(agentInfo.device.family, url)) {
			return spiderProcessor.isToMonitor(agentInfo.userAgent.family) &&
					osProcessor.isToMonitor(agentInfo.os.family) &&
					requestProcessor.isToMonitor(url);
		} 
		
		return visitorProcessor.isToMonitor(host) &&
				browserProcessor.isToMonitor(agentInfo.userAgent.family) &&
				osProcessor.isToMonitor(agentInfo.os.family) &&
				requestProcessor.isToMonitor(url);
	}
	
	private Client parseUserAgent(String agent) {
		agent = (agent == null ? "" : agent);
		return this.userAgentParser.parse(agent);
	}
	
}

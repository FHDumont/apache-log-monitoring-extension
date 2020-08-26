/*
 * Copyright 2015. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.logmonitor.apache;

import static com.appdynamics.extensions.logmonitor.apache.Constants.*;
import static com.appdynamics.extensions.logmonitor.apache.config.DefaultValues.*;
import static com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil.*;
import static com.appdynamics.extensions.yml.YmlReader.readFromFile;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.slf4j.Logger;

import com.appdynamics.extensions.logmonitor.apache.config.ApacheLog;
import com.appdynamics.extensions.logmonitor.apache.config.Configuration;
import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;
import com.appdynamics.extensions.logmonitor.apache.metrics.GroupMetrics;
import com.appdynamics.extensions.logmonitor.apache.metrics.Metrics;
import com.appdynamics.extensions.logmonitor.apache.processors.FilePointerProcessor;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

/**
 * @author Diego Pereira
 *
 */
public class ApacheLogMonitor extends AManagedMonitor {
	
	public static final Logger LOGGER = LogManager.getLogger("com.appdynamics.extensions.logmonitor.apache.ApacheLogMonitor");
	
	private volatile FilePointerProcessor filePointerProcessor;
	
	public ApacheLogMonitor() {
		LOGGER.info(String.format("Using Apache Log Monitor Version [%s]", 
				getImplementationVersion()));
		filePointerProcessor = new FilePointerProcessor();
	}

	public TaskOutput execute(Map<String, String> args,
			TaskExecutionContext arg1) throws TaskExecutionException {
		
		LOGGER.info("Starting Apache Log Monitoring task");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Args received were: %s", args));
		}
		
		if (args != null) {
			ExecutorService threadPool = null;
			
			String confDir = resolveConfigDir(args.get(CONFIG_DIR));
			String configFilename = confDir + CONFIG_FILE;
			String grokFilePath = confDir + GROK_FILE;
			String userAgentFilePath = confDir + USER_AGENT_FILE;
			
			try {
				Configuration config = readFromFile(configFilename, Configuration.class);
				
				int noOfThreads = config.getNoOfThreads() > 0 ? config.getNoOfThreads() : DEFAULT_NO_OF_THREADS;
				threadPool = Executors.newFixedThreadPool(noOfThreads);
				
				CompletionService<ApacheLogMetrics> apacheLogMonitorTasks =
						createConcurrentTasks(threadPool, config, grokFilePath, userAgentFilePath);
				
				List<ApacheLogMetrics> apacheLogMetricsList = collectMetrics(apacheLogMonitorTasks,
						config.getApacheLogs().size());
				
				uploadMetrics(config, apacheLogMetricsList);
				
				filePointerProcessor.updateFilePointerFile();
				
				return new TaskOutput("Apache Log Monitoring task successfully completed");
				
			} catch (Exception ex) {
				LOGGER.error("Unfortunately an issue has occurred: ", ex);
				
			} finally {
				if (threadPool != null && !threadPool.isShutdown()) {
					threadPool.shutdown();
				}
			}
		}
		
		throw new TaskExecutionException("Apache Log Monitoring task completed with failures.");
	}
	
	private CompletionService<ApacheLogMetrics> createConcurrentTasks(ExecutorService threadPool,
			Configuration config, 
			String grokFilePath, 
			String userAgentFilePath) {
		CompletionService<ApacheLogMetrics> apacheLogMonitorTasks = 
				new ExecutorCompletionService<ApacheLogMetrics>(threadPool);
		
		for (ApacheLog apacheLog : config.getApacheLogs()) {
			ApacheLogMonitorTask task = new ApacheLogMonitorTask(
					filePointerProcessor, 
					grokFilePath, 
					userAgentFilePath, 
					apacheLog);
			apacheLogMonitorTasks.submit(task);
		}
		
		return apacheLogMonitorTasks;
	}
	
	private List<ApacheLogMetrics> collectMetrics(CompletionService<ApacheLogMetrics> parallelTasks,
			int noOfApacheLogMonitorTasks) {
		
		List<ApacheLogMetrics> apacheLogMetricsList = new ArrayList<ApacheLogMetrics>();
		
		for (int i=0; i<noOfApacheLogMonitorTasks; i++) {
			
			try {
				ApacheLogMetrics collectedMetrics = 
						parallelTasks.take().get(THREAD_TIMEOUT, TimeUnit.SECONDS);
				apacheLogMetricsList.add(collectedMetrics);
				
			} catch (InterruptedException e) {
				LOGGER.error("Task interrupted. ", e);
				
			} catch (ExecutionException e) {
				LOGGER.error("Task execution failed. ", e);
				
			} catch (TimeoutException e) {
				LOGGER.error("Task timed out. ", e);
			}
		}
		
		return apacheLogMetricsList;
	}
    
    private void uploadMetrics(Configuration config, List<ApacheLogMetrics> apacheLogMetricsList) {
    	for (ApacheLogMetrics apacheLogMetrics : apacheLogMetricsList) {
			String hackPrefix = getHackPrefix(config);
			LOGGER.debug("HackPrefix: " + hackPrefix );
				
			String apacheLogPrefix;
			if (hackPrefix.equals("true")) {
				LOGGER.debug(String.format("HackPrefix true: %s", getMetricPrefix(config)));
				apacheLogPrefix = String.format("%s", getMetricPrefix(config));
			} else {
				LOGGER.debug(String.format("HackPrefix false: %s", getMetricPrefix(config)));
				apacheLogPrefix = String.format("%s%s%s", getMetricPrefix(config),
    				apacheLogMetrics.getApacheLogName(), METRIC_PATH_SEPARATOR);
			}
			
    		if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("HackPrefix: %s %s", hackPrefix, apacheLogPrefix));
			}

    		uploadSummaryMetrics(apacheLogPrefix, apacheLogMetrics);
    		uploadAllMetrics(apacheLogPrefix, BROWSER, apacheLogMetrics.getBrowserMetrics());
    		uploadAllMetrics(apacheLogPrefix, OS, apacheLogMetrics.getOsMetrics());
    		uploadAllMetrics(apacheLogPrefix, SPIDER, apacheLogMetrics.getSpiderMetrics());
    		uploadAllMetrics(apacheLogPrefix, VISITOR, apacheLogMetrics.getVisitorMetrics());
			
			if (hackPrefix.equals("true")) {
				uploadPageHackMetrics(apacheLogPrefix, apacheLogMetrics.getPageMetrics());
			} else {
				uploadPageMetrics(apacheLogPrefix, apacheLogMetrics.getPageMetrics());
			}
			
    		
    		uploadResponseCodeMetrics(apacheLogPrefix, apacheLogMetrics.getResponseCodeMetrics());
    	}
    }
    
    private void uploadSummaryMetrics(String apacheLogPrefix, ApacheLogMetrics apacheLogMetrics) {
    	printCollectiveObservedSum(apacheLogPrefix + TOTAL_HITS, 
    			apacheLogMetrics.getTotalHitCount());
    	printCollectiveObservedCurrent(apacheLogPrefix + TOTAL_BANDWIDTH, 
    			apacheLogMetrics.getTotalBandwidth());
		printCollectiveObservedSum(apacheLogPrefix + TOTAL_PAGES, 
				apacheLogMetrics.getTotalPageViewCount());
		printCollectiveSumSum(apacheLogPrefix + TOTAL_PAGES_CALLS, apacheLogMetrics.getTotalCallsCount());
		printCollectiveSumSum(apacheLogPrefix + TOTAL_200_HITS, apacheLogMetrics.getTotalHit200Count());
		printCollectiveSumSum(apacheLogPrefix + TOTAL_NON_200_HITS, apacheLogMetrics.getTotalHitNon200Count());
		printCollectiveAvgAvg(apacheLogPrefix + AVG_RESPONSETIME_MICRO, apacheLogMetrics.getResponseTimeMicro());
		printCollectiveAvgAvg(apacheLogPrefix + AVG_RESPONSETIME_MICRO200, apacheLogMetrics.getResponseTimeMicro200());
		printCollectiveAvgAvg(apacheLogPrefix + AVG_RESPONSETIME_MILI, apacheLogMetrics.getResponseTimeMili());
		printCollectiveAvgAvg(apacheLogPrefix + AVG_RESPONSETIME_MILI200, apacheLogMetrics.getResponseTimeMili200());
    }
    
    private void uploadAllMetrics(String apacheLogPrefix, String groupName, 
    		GroupMetrics groupMetrics) {
    	String groupPrefix = createGroupPrefix(apacheLogPrefix, groupName);	
    	uploadGroupMetrics(groupPrefix, groupMetrics, true);
    	uploadMemberMetrics(groupPrefix, groupMetrics, true);
    }
    
    private void uploadPageMetrics(String apacheLogPrefix, GroupMetrics groupMetrics) {
    	String groupPrefix = createGroupPrefix(apacheLogPrefix, PAGE);
    	uploadGroupMetrics(groupPrefix, groupMetrics, false);
    	uploadMemberNewMetrics(groupPrefix, groupMetrics, false);
	}
	
    private void uploadPageHackMetrics(String apacheLogPrefix, GroupMetrics groupMetrics) {
    	String groupPrefix = createGroupPrefix(apacheLogPrefix, PAGE);
    	uploadGroupMetrics(groupPrefix, groupMetrics, false);
    	uploadMemberNewMetrics(apacheLogPrefix, groupMetrics, false);
    }
    
    private void uploadResponseCodeMetrics(String apacheLogPrefix, GroupMetrics groupMetrics) {
    	String groupPrefix = createGroupPrefix(apacheLogPrefix, RESPONSE_CODE);
    	uploadMemberMetrics(groupPrefix, groupMetrics, true);
    }
    
    private void uploadGroupMetrics(String groupPrefix, GroupMetrics groupMetrics, 
    		boolean includePageMetrics) {
		printCollectiveObservedSum(groupPrefix + TOTAL_HITS, groupMetrics.getHitCount());

		if (groupPrefix.contains(PAGE)) {
			printCollectiveSumSum(groupPrefix + TOTAL_PAGES_CALLS, groupMetrics.getTotalHitCount());
			printCollectiveSumSum(groupPrefix + TOTAL_200_HITS, groupMetrics.getHit200Count());
			printCollectiveSumSum(groupPrefix + TOTAL_NON_200_HITS, groupMetrics.getHitNon200Count());
			printCollectiveAvgAvg(groupPrefix + AVG_RESPONSETIME_MICRO, groupMetrics.getResponseTimeMicro());
			printCollectiveAvgAvg(groupPrefix + AVG_RESPONSETIME_MICRO200, groupMetrics.getResponseTimeMicro200());
			printCollectiveAvgAvg(groupPrefix + AVG_RESPONSETIME_MILI, groupMetrics.getResponseTimeMili());
			printCollectiveAvgAvg(groupPrefix + AVG_RESPONSETIME_MILI200, groupMetrics.getResponseTimeMili200());
			printCollectiveSumSum(groupPrefix + TOTAL_BANDWIDTH, groupMetrics.getBandwidth());
		}
    	
    	if (includePageMetrics) {
    		printCollectiveObservedSum(groupPrefix + TOTAL_PAGES, groupMetrics.getPageViewCount());
    	}
    }
    
    private void uploadMemberMetrics(String groupPrefix, GroupMetrics groupMetrics, 
    		boolean includePageMetrics) {
    	for (Map.Entry<String, Metrics> member : groupMetrics.getMembers().entrySet()) {
    		String memberPrefix = String.format("%s%s%s", 
    				groupPrefix, member.getKey(), METRIC_PATH_SEPARATOR);
    		
    		Metrics metrics = member.getValue();
    		printCollectiveObservedSum(memberPrefix + HITS, metrics.getHitCount());
			printCollectiveSumSum(memberPrefix + BANDWIDTH, metrics.getBandwidth());
			if (memberPrefix.contains(PAGE)) {
				printCollectiveSumSum(memberPrefix + TOTAL_PAGES_CALLS, metrics.getTotalHitCount());
				printCollectiveSumSum(memberPrefix + TOTAL_200_HITS, metrics.getHit200Count());
				printCollectiveSumSum(memberPrefix + TOTAL_NON_200_HITS, metrics.getHitNon200Count());
				printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MICRO, metrics.getResponseTimeMicro());
				printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MICRO200, metrics.getResponseTimeMicro200());
				printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MILI, metrics.getResponseTimeMili());
				printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MILI200, metrics.getResponseTimeMili200());
			}

        	if (includePageMetrics) {
        		printCollectiveObservedSum(memberPrefix + PAGES, metrics.getPageViewCount());
        	}
    	}
	}
	
    private void uploadMemberNewMetrics(String groupPrefix, GroupMetrics groupMetrics, 
    		boolean includePageMetrics) {
    	for (Map.Entry<String, Metrics> member : groupMetrics.getMembers().entrySet()) {
    		String memberPrefix = String.format("%s%s%s", 
    				groupPrefix, member.getKey(), METRIC_PATH_SEPARATOR);
    		
    		Metrics metrics = member.getValue();
    		printCollectiveObservedSum(memberPrefix + HITS, metrics.getHitCount());
			printCollectiveSumSum(memberPrefix + BANDWIDTH, metrics.getBandwidth());
			printCollectiveSumSum(memberPrefix + TOTAL_PAGES_CALLS, metrics.getTotalHitCount());
			printCollectiveSumSum(memberPrefix + TOTAL_200_HITS, metrics.getHit200Count());
			printCollectiveSumSum(memberPrefix + TOTAL_NON_200_HITS, metrics.getHitNon200Count());
			printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MICRO, metrics.getResponseTimeMicro());
			printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MICRO200, metrics.getResponseTimeMicro200());
			printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MILI, metrics.getResponseTimeMili());
			printCollectiveAvgAvg(memberPrefix + AVG_RESPONSETIME_MILI200, metrics.getResponseTimeMili200());


        	if (includePageMetrics) {
        		printCollectiveObservedSum(memberPrefix + PAGES, metrics.getPageViewCount());
        	}
    	}
    }
    
    private void printCollectiveObservedCurrent(String metricName, BigInteger metricValue) {
        printMetric(metricName, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
	}
	
	private void printCollectiveObservedSum(String metricName, BigInteger metricValue) {
        printMetric(metricName, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }
	
	private void printCollectiveAvgAvg(String metricName, BigInteger metricValue) {
        printMetric(metricName, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }
	private void printCollectiveSumSum(String metricName, BigInteger metricValue) {
        printMetric(metricName, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }
    
    private void printMetric(String metricName, BigInteger metricValue, String aggregation, 
    		String timeRollup, String cluster) {
    	
		MetricWriter metricWriter = getMetricWriter(metricName, aggregation,
				timeRollup, cluster);
        
        BigInteger valueToReport = convertValueToZeroIfNullOrNegative(metricValue);
        
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug(String.format("Sending [%s/%s/%s] metric = %s = %s => %s",
            		aggregation, timeRollup, cluster,
                    metricName, metricValue, valueToReport));
        }
        
        metricWriter.printMetric(valueToReport.toString());
    }
    
	private String getMetricPrefix(Configuration config) {
		String metricPrefix = config.getMetricPrefix();
		
		if (StringUtils.isBlank(metricPrefix)) {
			metricPrefix = DEFAULT_METRIC_PATH;
			
		} else {
			metricPrefix = metricPrefix.trim();
			
			if (!metricPrefix.endsWith(METRIC_PATH_SEPARATOR)) {
				metricPrefix = metricPrefix + METRIC_PATH_SEPARATOR;
			}
		}
		
		return metricPrefix;
	}

	private String getHackPrefix(Configuration config) {
		String prefixHack = config.getPrefixHack();
		
		if (StringUtils.isBlank(prefixHack)) {
			prefixHack = DEFAULT_HACK_PREFIX;
			
		} else {
			prefixHack = prefixHack;
		}
		
		return prefixHack;
	}
    
    private String createGroupPrefix(String apacheLogPrefix, String groupName) {
    	return String.format("%s%s%s", apacheLogPrefix, 
    			groupName, METRIC_PATH_SEPARATOR);
    }
	
	private String resolveConfigDir(String confDirPath) {
		String resolvedPath = resolvePath(confDirPath);
		
		if (!resolvedPath.endsWith(File.separator)) {
			resolvedPath = resolvedPath + File.separator;
		}
		
		return resolvedPath;
	}
	
	private static String getImplementationVersion() {
		return ApacheLogMonitor.class.getPackage().getImplementationTitle();
	}
}

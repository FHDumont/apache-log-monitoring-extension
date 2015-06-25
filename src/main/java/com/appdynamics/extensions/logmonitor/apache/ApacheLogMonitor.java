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
import org.apache.log4j.Logger;

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
 * @author Florencio Sarmiento
 *
 */
public class ApacheLogMonitor extends AManagedMonitor {
	
	public static final Logger LOGGER = Logger.getLogger("com.singularity.extensions.logmonitor.apache.ApacheLogMonitor");
	
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
    		String apacheLogPrefix = String.format("%s%s%s", getMetricPrefix(config),
    				apacheLogMetrics.getApacheLogName(), METRIC_PATH_SEPARATOR);
    		
    		uploadSummaryMetrics(apacheLogPrefix, apacheLogMetrics);
    		uploadAllMetrics(apacheLogPrefix, BROWSER, apacheLogMetrics.getBrowserMetrics());
    		uploadAllMetrics(apacheLogPrefix, OS, apacheLogMetrics.getOsMetrics());
    		uploadAllMetrics(apacheLogPrefix, SPIDER, apacheLogMetrics.getSpiderMetrics());
    		uploadAllMetrics(apacheLogPrefix, VISITOR, apacheLogMetrics.getVisitorMetrics());
    		uploadPageMetrics(apacheLogPrefix, apacheLogMetrics.getPageMetrics());
    		uploadResponseCodeMetrics(apacheLogPrefix, apacheLogMetrics.getResponseCodeMetrics());
    	}
    }
    
    private void uploadSummaryMetrics(String apacheLogPrefix, ApacheLogMetrics apacheLogMetrics) {
    	printCollectiveObservedCurrent(apacheLogPrefix + TOTAL_HITS, 
    			apacheLogMetrics.getTotalHitCount());
    	printCollectiveObservedCurrent(apacheLogPrefix + TOTAL_BANDWIDTH, 
    			apacheLogMetrics.getTotalBandwidth());
    	printCollectiveObservedCurrent(apacheLogPrefix + TOTAL_PAGES, 
    			apacheLogMetrics.getTotalPageViewCount());
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
    	uploadMemberMetrics(groupPrefix, groupMetrics, false);
    }
    
    private void uploadResponseCodeMetrics(String apacheLogPrefix, GroupMetrics groupMetrics) {
    	String groupPrefix = createGroupPrefix(apacheLogPrefix, RESPONSE_CODE);
    	uploadMemberMetrics(groupPrefix, groupMetrics, true);
    }
    
    private void uploadGroupMetrics(String groupPrefix, GroupMetrics groupMetrics, 
    		boolean includePageMetrics) {
    	printCollectiveObservedCurrent(groupPrefix + TOTAL_HITS, groupMetrics.getHitCount());
    	printCollectiveObservedCurrent(groupPrefix + TOTAL_BANDWIDTH, groupMetrics.getBandwidth());
    	
    	if (includePageMetrics) {
    		printCollectiveObservedCurrent(groupPrefix + TOTAL_PAGES, groupMetrics.getPageViewCount());
    	}
    }
    
    private void uploadMemberMetrics(String groupPrefix, GroupMetrics groupMetrics, 
    		boolean includePageMetrics) {
    	for (Map.Entry<String, Metrics> member : groupMetrics.getMembers().entrySet()) {
    		String memberPrefix = String.format("%s%s%s", 
    				groupPrefix, member.getKey(), METRIC_PATH_SEPARATOR);
    		
    		Metrics metrics = member.getValue();
    		printCollectiveObservedCurrent(memberPrefix + HITS, metrics.getHitCount());
        	printCollectiveObservedCurrent(memberPrefix + BANDWIDTH, metrics.getBandwidth());
        	
        	if (includePageMetrics) {
        		printCollectiveObservedCurrent(memberPrefix + PAGES, metrics.getPageViewCount());
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

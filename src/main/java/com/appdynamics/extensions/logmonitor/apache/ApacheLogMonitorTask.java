package com.appdynamics.extensions.logmonitor.apache;

import static com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitbucket.kienerj.OptimizedRandomAccessFile;

import com.appdynamics.extensions.logmonitor.apache.config.ApacheLog;
import com.appdynamics.extensions.logmonitor.apache.exceptions.FileNotReadableException;
import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;

/**
 * @author Florencio Sarmiento
 *
 */
public class ApacheLogMonitorTask implements Callable<ApacheLogMetrics> {
	
	private static final Logger LOGGER = 
			Logger.getLogger("com.singularity.extensions.logmonitor.apache.ApacheLogMonitorTask");
	
	private AtomicLong filePointer;
	
	private ApacheLog apacheLogConfig;
	
	private String grokPatternFilePath;
	
	private String userAgentPatternFilePath;
	
	public ApacheLogMonitorTask(AtomicLong filePointer,
			String grokPatternFilePath,
			String userAgentPatternFilePath,
			ApacheLog apacheLogConfig) {
		
		this.filePointer = filePointer;
		this.grokPatternFilePath = grokPatternFilePath;
		this.userAgentPatternFilePath = userAgentPatternFilePath;
		this.apacheLogConfig = apacheLogConfig;
	}

	public ApacheLogMetrics call() throws Exception {
		ApacheLogMetrics logMetrics = new ApacheLogMetrics();
		logMetrics.setApacheLogName(getApacheLogName());
		
		OptimizedRandomAccessFile randomAccessFile = null;
		
		LOGGER.info(String.format("Processing log file: name [%s] path [%s]", 
				apacheLogConfig.getName(), apacheLogConfig.getLogPath()));
		
		long curFilePointer = 0;
		
		try {
			MetricsExtractor metricsExtractor = new MetricsExtractor(
					grokPatternFilePath,
					userAgentPatternFilePath,
					apacheLogConfig);
			
			File file = getFile(resolvePath(apacheLogConfig.getLogPath()));
			randomAccessFile = new OptimizedRandomAccessFile(file, "r");
			long fileSize = randomAccessFile.length();
			curFilePointer = getCurrentFilePointer(fileSize);
			
			LOGGER.info(String.format("Starting from position [%s]", 
					curFilePointer));
			
			randomAccessFile.seek(curFilePointer);
			
			String currentLine = null;
			
			while((currentLine = randomAccessFile.readLine()) != null) {
				metricsExtractor.extractMetrics(currentLine, logMetrics);
				curFilePointer = randomAccessFile.getFilePointer();
			}
			
			
		} finally {
			closeRandomAccessFile(randomAccessFile);
		}
		
		filePointer.set(curFilePointer);
		
		LOGGER.info(String.format("Sucessfully processed log file: name [%s] path [%s]", 
				apacheLogConfig.getName(), apacheLogConfig.getLogPath()));
		
		return logMetrics;
	}
	
	private File getFile(String path) throws FileNotFoundException {
		File file = new File(path);
		
		if (!file.exists()) {
			throw new FileNotFoundException(
					String.format("Unable to find file [%s]", path));
			
		} else if (!file.canRead()) {
			throw new FileNotReadableException(
					String.format("Unable to read file [%s]", path));
		}
		
		return file;
	}
    
    private long getCurrentFilePointer(long fileSize) {
    	long curFilePointer = filePointer.get();
    	
    	if (isLogRotated(fileSize, curFilePointer)) {
    		curFilePointer = 0;
    	}
    	
    	return curFilePointer;
    }
	
	private boolean isLogRotated(long fileSize, long startPosition) {
		return fileSize < startPosition;
	}
	
	private String getApacheLogName() {
		String logName = apacheLogConfig.getName();
		if (StringUtils.isBlank(logName)) {
			logName = apacheLogConfig.getLogPath();
		}
		
		return logName;
	}
	
}

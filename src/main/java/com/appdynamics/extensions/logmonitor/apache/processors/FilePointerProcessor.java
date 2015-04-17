package com.appdynamics.extensions.logmonitor.apache.processors;

import static com.appdynamics.extensions.logmonitor.apache.Constants.FILEPOINTER_FILENAME;
import static com.appdynamics.extensions.logmonitor.apache.Constants.METRIC_PATH_SEPARATOR;
import static com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil.closeRandomAccessFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitbucket.kienerj.OptimizedRandomAccessFile;

import com.appdynamics.extensions.logmonitor.apache.ApacheLogMonitor;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * @author Florencio Sarmiento
 *
 */
public class FilePointerProcessor {
	
	public static final Logger LOGGER = Logger.getLogger("com.singularity.extensions.logmonitor.apache.FilePointerProcessor");
	
	private Map<String, AtomicLong> filePointers = new ConcurrentHashMap<String, AtomicLong>();
	
	public FilePointerProcessor() {
		initialiseFilePointers();
	}

	public AtomicLong getFilePointer(String logPath) {
		if (filePointers.containsKey(logPath)) {
			return filePointers.get(logPath);
		}
		
		AtomicLong filePointer = new AtomicLong(0);
		filePointers.put(logPath, filePointer);
		return filePointer;
	}
    
    public void updateFilePointerFile() {
    	String filePointerPath = getFilePointerPath();
    	
    	File file = new File(filePointerPath);
    	FileWriter fileWriter = null;
    	
    	try {
    		fileWriter = new FileWriter(file, false);
    		StringBuilder output = new StringBuilder();
    		
    		for (Map.Entry<String, AtomicLong> filePointer : filePointers.entrySet()) {
    			if (StringUtils.isNotBlank(filePointer.getKey())) {
    				output.append(filePointer.getKey())
    				.append(METRIC_PATH_SEPARATOR)
    				.append(filePointer.getValue())
    				.append(System.getProperty("line.separator"));
    			}
    		}
    		
    		if (output.length() > 0) {
    	    	if (LOGGER.isDebugEnabled()) {
    	    		LOGGER.debug(String.format(
    	    				"Updating [%s] with [%s]", filePointerPath, output.toString()));
    	    	}
    	    	
    			fileWriter.write(output.toString());
    		}
    		
    	} catch (IOException ex) {
    		LOGGER.error(String.format(
					"Unfortunately an error occurred while reading the file %s", file.getPath()),
					ex);
    		
    	} finally {
    		if (fileWriter != null) {
    			try {
    				fileWriter.close();
    			} catch (IOException e) {}
    		}
    	}
    }
	
    private void initialiseFilePointers() {
    	LOGGER.info("Initialising filepointers...");
    	
    	File file = new File(getFilePointerPath());
    	
    	OptimizedRandomAccessFile randomAccessFile = null;
		
		try {
			randomAccessFile = new OptimizedRandomAccessFile(file, "rws");
			
			String currentLine = null;
			
			while((currentLine = randomAccessFile.readLine()) != null) {
				List<String> stringList = Lists.newArrayList(Splitter
						.on(METRIC_PATH_SEPARATOR)
						.trimResults()
						.omitEmptyStrings()
						.split(currentLine));
				
				String filepath = null;
				String stringFilePointer = null;
				int index = 0;
				
				for (String value : stringList) {
					if (index == 0) {
						filepath = value;
					} else {
						stringFilePointer = value;
						break;
					}
					
					index++;
				}
				
				if (StringUtils.isNotBlank(filepath)) {
					Long filePointer = convertFilePointerToLong(stringFilePointer);
					filePointers.put(filepath, new AtomicLong(filePointer));
				}
			}
			
		} catch (Exception e) {
			LOGGER.error(String.format(
					"Unfortunately an error occurred while reading the file %s", file.getPath()),
					e);
			return;
			
		} finally {
			closeRandomAccessFile(randomAccessFile);
		}
		
		LOGGER.info("Filepointers initialised with: " + filePointers);
    }
	
    private String getFilePointerPath() {
    	String path = null;
    	
    	try {
    		URL classUrl = ApacheLogMonitor.class.getResource(
    				ApacheLogMonitor.class.getSimpleName() + ".class");
    		String jarPath = classUrl.toURI().toString();
    		
    		// workaround for jar file
    		jarPath = jarPath.replace("jar:", "").replace("file:", "");
    		
    		if (jarPath.contains("!")) {
    			jarPath = jarPath.substring(0, jarPath.indexOf("!"));
    		}
    		
    		File file = new File(jarPath);
    		String jarDir = file.getParentFile().toURI().getPath();
    		
    		if (jarDir.endsWith(File.separator)) {
    			path = jarDir + FILEPOINTER_FILENAME;
    					
    		} else {
    			path = String.format("%s%s%s", jarDir , 
            			File.separator, FILEPOINTER_FILENAME);
    		}
    		
    	} catch (Exception ex) {
    		LOGGER.warn("Unable to resolve installation dir, finding an alternative.");
    	}
    	
    	if (StringUtils.isBlank(path)) {
    		path = String.format("%s%s%s", new File(".").getAbsolutePath(), 
        			File.separator, FILEPOINTER_FILENAME);
    	}
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("Filepointer path: " + path);
    	}
    	
    	return path;
    }
    
    private Long convertFilePointerToLong(String stringFilePointer) {
    	long filePointer;
    	
    	try {
    		filePointer = Long.valueOf(stringFilePointer);
    		
    	} catch (NumberFormatException ex) {
    		if (LOGGER.isDebugEnabled()) {
    			LOGGER.debug(String.format("Unable to convert [%s] to long, defaulting to 0", 
    					stringFilePointer));
    		}
    		
    		filePointer = 0;
    	}
    	
    	return filePointer;
    }
}

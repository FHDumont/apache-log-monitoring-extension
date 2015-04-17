package com.appdynamics.extensions.logmonitor.apache.util;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.kienerj.OptimizedRandomAccessFile;

import com.appdynamics.extensions.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;

/**
 * @author Florencio Sarmiento
 *
 */
public class ApacheLogMonitorUtil {
	
    public static String resolvePath(String filename) {
        if(StringUtils.isBlank(filename)){
            return "";
        }
        
        //for absolute paths
        if(new File(filename).exists()){
            return filename;
        }
        
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = String.format("%s%s%s", jarPath, File.separator, filename);
        return configFileName;
    }
    
	public static Pattern createPattern(Set<String> rawPatterns) {
		Pattern pattern = null;
		
		if (rawPatterns != null && !rawPatterns.isEmpty()) {
			StringBuilder rawPatternsStringBuilder = new StringBuilder();
			int index = 0;
			
			for (String rawPattern : rawPatterns) {
				if (index > 0) {
					rawPatternsStringBuilder.append("|");
				}
				
				rawPatternsStringBuilder.append(rawPattern);
				index++;
			}
			
			pattern = Pattern.compile(rawPatternsStringBuilder.toString());
		}
		
		return pattern;
	}
	
	public static final boolean isNotMatch(String name, Pattern pattern) {
		return !isMatch(name, pattern);
	}
	
	public static final boolean isMatch(String name, Pattern pattern) {
		if (name != null && pattern != null) {
			Matcher matcher = pattern.matcher(name);
			
			if (matcher.matches()) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void closeRandomAccessFile(OptimizedRandomAccessFile randomAccessFile) {
		if (randomAccessFile != null) {
			try {
				randomAccessFile.close();
			} catch (IOException e) {}
		}
	}
	
    public static BigInteger convertValueToZeroIfNullOrNegative(BigInteger value) {
    	if (value == null || value.compareTo(BigInteger.ZERO) < 0) {
    		return BigInteger.ZERO;
    	}
    	
    	return value;
    }

}

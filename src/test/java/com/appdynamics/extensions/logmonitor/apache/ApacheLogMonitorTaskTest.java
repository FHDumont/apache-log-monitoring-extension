package com.appdynamics.extensions.logmonitor.apache;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import oi.thekraken.grok.api.exception.GrokException;

import org.bitbucket.kienerj.OptimizedRandomAccessFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.appdynamics.extensions.logmonitor.apache.ApacheLogMonitorTask;
import com.appdynamics.extensions.logmonitor.apache.config.ApacheLog;
import com.appdynamics.extensions.logmonitor.apache.metrics.ApacheLogMetrics;
import com.appdynamics.extensions.logmonitor.apache.util.ApacheLogMonitorUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApacheLogMonitorTask.class, ApacheLogMonitorUtil.class})
@PowerMockIgnore({"org.apache.*, javax.xml.*"})
public class ApacheLogMonitorTaskTest {
	
	@Mock
	private ApacheLog mockApacheLog;
	
	@Mock
	private File mockFile;
	
	@Mock
	private OptimizedRandomAccessFile mockRandomAccessFile;
	
	@Mock
	private MetricsExtractor mockMetricsExtractor;
	
	private ApacheLogMonitorTask classUnderTest;
	
	@Before
	public void setUp() throws Exception {
		mockStatic(ApacheLogMonitorUtil.class);
		PowerMockito.when(ApacheLogMonitorUtil.resolvePath(anyString())).thenReturn("./target");
		
		whenNew(OptimizedRandomAccessFile.class).withArguments(mockFile, "r")
			.thenReturn(mockRandomAccessFile);
		
		whenNew(File.class).withArguments(anyString()).thenReturn(mockFile);
		when(mockFile.exists()).thenReturn(true);
		when(mockFile.canRead()).thenReturn(true);
	}
	
	@Test
	public void testProcessLogIsSuccessful() throws Exception {
		String testLine1 = "10.10.1.2 - - [14/Apr/2015:04:54:21 -0400] \"GET /test.html?param1=value HTTP/1.1\" 200 5678 \"-\" "
				+ "\"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)\"";
		
		String testLine2 = "10.10.1.2 - - [14/Apr/2015:04:54:21 -0400] \"GET /test.html?param1=value HTTP/1.1\" 200 5678 \"-\" "
				+ "\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.25 Safari/534.3\"";
		
		whenNew(MetricsExtractor.class)
			.withArguments(anyString(), anyString(), any(ApacheLog.class))
			.thenReturn(mockMetricsExtractor);
		
		when(mockRandomAccessFile.readLine()).thenReturn(testLine1, testLine2, null);
		when(mockRandomAccessFile.getFilePointer()).thenReturn(10L, 20L);
		
		AtomicLong testFilePointer = new AtomicLong(0);
		classUnderTest = new ApacheLogMonitorTask(testFilePointer, "anyGrokPath", "anyUserAgentPath", mockApacheLog);
		
		ApacheLogMetrics result = classUnderTest.call();
		
		assertNotNull(result);
		assertEquals(20L, testFilePointer.get());
		verify(mockMetricsExtractor, times(2)).extractMetrics(anyString(), any(ApacheLogMetrics.class));
	}
	
	@Test
	public void testProcessLogThrowsExeption() throws Exception {
		whenNew(MetricsExtractor.class)
			.withArguments(anyString(), anyString(), any(ApacheLog.class))
			.thenThrow(new GrokException());
		
		AtomicLong testFilePointer = new AtomicLong(1);
		classUnderTest = new ApacheLogMonitorTask(testFilePointer, "anyGrokPath", 
				"anyUserAgentPath", mockApacheLog);
		
		ApacheLogMetrics result = null;
		
		try {
			result = classUnderTest.call();
			fail("shouldn't have reached here");
		} catch (GrokException ex) {}
		
		assertNull(result);
		// ensure filepointer didn't change
		assertEquals(1L, testFilePointer.get());
		verify(mockMetricsExtractor, never()).extractMetrics(anyString(), any(ApacheLogMetrics.class));
	}
}

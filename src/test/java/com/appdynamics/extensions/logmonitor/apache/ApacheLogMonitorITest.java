package com.appdynamics.extensions.logmonitor.apache;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.appdynamics.extensions.logmonitor.apache.ApacheLogMonitor;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.TaskOutput;

public class ApacheLogMonitorITest {
	
	private ApacheLogMonitor classUnderTest = new ApacheLogMonitor();
	
	@Test
	public void testMetricsCollection() throws Exception {
		Map<String, String> args = Maps.newHashMap();
		args.put("config-dir","src/test/resources/conf");
		
		TaskOutput result = classUnderTest.execute(args, null);
		assertTrue(result.getStatusMessage().contains("successfully completed"));
	}

}

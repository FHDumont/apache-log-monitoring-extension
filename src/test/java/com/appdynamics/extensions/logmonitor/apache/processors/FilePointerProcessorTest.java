package com.appdynamics.extensions.logmonitor.apache.processors;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class FilePointerProcessorTest {
	
	private FilePointerProcessor classUnderTest;
	
	@Test
	public void testUpdateFilePointerFileIsPersisted() {
		classUnderTest = new FilePointerProcessor();
		String logPath = "src/test/resources/test.log";
		
		AtomicLong origFilePointer = classUnderTest.getFilePointer(logPath);
		assertEquals(0, origFilePointer.get());
		
		// lets update the filePointer
		long newFilePointer = 1234;
		origFilePointer.set(newFilePointer);
		classUnderTest.updateFilePointerFile();
		
		// re-initialise the filepointer 
		// it should pick up from the file
		classUnderTest = new FilePointerProcessor();
		AtomicLong result = classUnderTest.getFilePointer(logPath);
		assertEquals(newFilePointer, result.get());
	}

}

package com.logicmonitor.logprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.logicmonitor.logprocessor.dto.FileBean;
import com.logicmonitor.logprocessor.service.LogProcessorService;
import com.logicmonitor.logprocessor.tasks.LineNumberUpdaterTask;

public class LogProcessorTest {

	@Test
	public void testNullPath() {
		assertFalse(LogProcessorService.LOGPROCESSOR.LogProcessor(null, 1));
	}

	@Test
	public void testEmptyPath() {
		assertFalse(LogProcessorService.LOGPROCESSOR.LogProcessor("", 1));
	}

	@Test
	public void testThreadCountZero() {
		assertFalse(LogProcessorService.LOGPROCESSOR.LogProcessor(
				"/Users/kaush/Coding/Workspace_LEARN/LogProcessor/SampleLogs",
				0));
	}

	@Test
	public void testLineNumberUpdateTask() throws InterruptedException {
		// Mock
		FileBean fb = new FileBean();
		fb.setFileName("logtest.2014-06-06");
		fb.setStartLineNumber(BigInteger.ZERO);
		fb.setContents(Arrays.asList("", "", ""));

		BlockingQueue<FileBean> filesToProcess = new ArrayBlockingQueue<>(1000);
		filesToProcess.put(fb);
		BlockingQueue<FileBean> filesToWrite = new ArrayBlockingQueue<>(1000);

		ExecutorService readService = Executors.newFixedThreadPool(10);
		ExecutorService processService = Executors.newSingleThreadExecutor();
		processService.submit(new LineNumberUpdaterTask(filesToProcess,
				filesToWrite, readService));

		readService.shutdown();
		try {
			readService.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}

		processService.shutdownNow();
	}

}

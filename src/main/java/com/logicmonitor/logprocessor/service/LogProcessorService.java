package com.logicmonitor.logprocessor.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.logicmonitor.logprocessor.dto.FileBean;
import com.logicmonitor.logprocessor.tasks.FileReadTask;
import com.logicmonitor.logprocessor.tasks.FileWriteTask;
import com.logicmonitor.logprocessor.tasks.LineNumberUpdaterTask;
import com.logicmonitor.logprocessor.tasks.MemoryMonitor;
import com.logicmonitor.logprocessor.util.ThreadLock;

/**
 * Singleton Class which creates multiple threads for read and write. But a
 * single thread processes the contents to add line numbers
 * 
 * @author kaush
 *
 */
public enum LogProcessorService {

	LOGPROCESSOR;

	/**
	 * @param path
	 *            File Path
	 * @param threadCount
	 *            No of Threads
	 * @return the state of log processing (True / False)
	 */
	public boolean LogProcessor(String path, int threadCount) {

		if (path == null || path.length() == 0)
			return false;
		if (threadCount <= 0)
			return false;

		List<String> allFilesList = new ArrayList<>();
		Map<String, BigInteger> allFilesMap = new TreeMap<>();
		final ThreadLock lock = new ThreadLock();

		File dir = new File(path);

		for (File file : dir.listFiles((File directory, String name) -> name
				.matches("logtest.\\d{4}-\\d{2}-\\d{2}.log"))) {
			if (file.isFile())
				allFilesList.add(Paths.get(path, file.getName()).toString());
		}

		/* Saves the end line number in a map */
		BigInteger startLineNumber = BigInteger.ZERO;
		for (String file : allFilesList) {
			try (Stream<String> stream = Files.lines(Paths.get(file),
					Charset.defaultCharset())) {
				allFilesMap.put(file, startLineNumber);
				startLineNumber = startLineNumber.add(BigInteger.valueOf(stream
						.count()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*
		 * Blocking Queue is thread safe and explicit implementation of wait and
		 * notify is not required
		 */
		BlockingQueue<FileBean> filesToProcess = new ArrayBlockingQueue<>(1000);
		BlockingQueue<FileBean> filesToWrite = new ArrayBlockingQueue<>(1000);

		/* Creating various thread Services for different tasks */
		ExecutorService readService = Executors.newFixedThreadPool(threadCount,
				new ThreadFactory() {
					private final static String th_name_prefix = "readService_";
					private int th_name_suffix = 0;

					@Override
					public Thread newThread(Runnable r) {
						Thread out = new Thread(r);
						out.setName(th_name_prefix + th_name_suffix++);
						return out;
					}
				});

		ExecutorService processService = Executors
				.newSingleThreadExecutor(new ThreadFactory() {
					private final static String TH_NAME = "processService";

					@Override
					public Thread newThread(Runnable r) {
						Thread out = new Thread(r);
						out.setName(TH_NAME);
						return out;
					}
				});

		ExecutorService writeService = Executors.newFixedThreadPool(
				threadCount, new ThreadFactory() {
					private final static String th_name_prefix = "writeService_";
					private int th_name_suffix = 0;

					@Override
					public Thread newThread(Runnable r) {
						Thread out = new Thread(r);
						out.setName(th_name_prefix + th_name_suffix++);
						return out;
					}
				});

		/* Monitor Service to check the memory */
		ExecutorService monitorService = Executors
				.newSingleThreadExecutor(new ThreadFactory() {
					private final static String TH_NAME = "monitorService";

					@Override
					public Thread newThread(Runnable r) {
						Thread out = new Thread(r);
						out.setName(TH_NAME);
						return out;
					}
				});
		monitorService.execute(new MemoryMonitor(readService, processService,
				writeService, lock));

		for (int i = 0; i < threadCount; i++)
			readService.submit(new FileReadTask(filesToProcess, allFilesList,
					allFilesMap, lock));

		processService.submit(new LineNumberUpdaterTask(filesToProcess,
				filesToWrite, readService));

		for (int i = 0; i < threadCount; i++)
			writeService.submit(new FileWriteTask(filesToWrite));

		readService.shutdown();
		try {
			readService.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}

		processService.shutdownNow();
		writeService.shutdownNow();
		monitorService.shutdownNow();

		return true;
	}

}

package com.logicmonitor.logprocessor.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.logicmonitor.logprocessor.dto.FileBean;
import com.logicmonitor.logprocessor.util.ThreadLock;

/**
 * Reads the file contents, wraps it into FileBean and adds it to a
 * BlockingQueue 'filesToProcess' Blocking Queue is thread safe, hence explicit
 * Implementation of wait and notify is not necessary
 * 
 * @author kaush
 *
 */
public class FileReadTask implements Runnable {

	private final BlockingQueue<FileBean> filesToProcess;
	List<String> allFilesList;
	Map<String, BigInteger> allFilesMap;
	FileBean fileBean;
	final ThreadLock lock;
	private static volatile int readPointer = 0;

	public FileReadTask(BlockingQueue<FileBean> filesToProcess,
			List<String> allFilesList, Map<String, BigInteger> allFilesMap,
			ThreadLock lock) {
		this.filesToProcess = filesToProcess;
		this.allFilesList = allFilesList;
		this.allFilesMap = allFilesMap;
		this.lock = lock;
	}

	@Override
	public void run() {

		try {
			synchronized (lock) {
				if (lock.isStatus())
					lock.wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (readPointer < allFilesList.size()) {
			String currentFile = allFilesList.get(readPointer++);
			fileBean = new FileBean();
			fileBean.setFileName(currentFile);

			BufferedReader reader = null;
			try {
				reader = Files.newBufferedReader(Paths.get(currentFile));
				fileBean.setStartLineNumber(allFilesMap.get(currentFile));
				List<String> contents = new ArrayList<String>();
				String line = "";
				while ((line = reader.readLine()) != null)
					contents.add(line);

				fileBean.setContents(contents);
				filesToProcess.put(fileBean);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

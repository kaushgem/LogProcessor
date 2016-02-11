package com.logicmonitor.logprocessor.tasks;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import com.logicmonitor.logprocessor.dto.FileBean;

/**
 * Reads the contents from 'filesToProcess' Blocking Queue, adds line number to
 * it and adds them to 'filesToWrite' Blocking Queue.
 * 
 * @author kaush
 *
 */
public class LineNumberUpdaterTask implements Runnable {
	
	private final BlockingQueue<FileBean> filesToProcess;
	private final BlockingQueue<FileBean> filesToWrite;
	FileBean fileBean;
	ExecutorService readService;

	public LineNumberUpdaterTask(BlockingQueue<FileBean> filesToProcess,
			BlockingQueue<FileBean> filesToWrite, ExecutorService readService) {
		this.filesToProcess = filesToProcess;
		this.filesToWrite = filesToWrite;
		this.readService = readService;
	}

	@Override
	public void run() {
		FileBean fileBean;
		while (true) {
			try {
				fileBean = filesToProcess.take();
				updateContents(fileBean);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		while ((fileBean = filesToProcess.poll()) != null) {
			updateContents(fileBean);
		}
	}

	private void updateContents(FileBean fileBean) {
		StringBuffer updatedContents = new StringBuffer();

		BigInteger lineNo = fileBean.getStartLineNumber();

		for (String line : fileBean.getContents()) {
			lineNo = lineNo.add(BigInteger.ONE);
			updatedContents.append(lineNo).append(". ").append(line)
					.append("\n");
		}
		fileBean.setContentsWithLineNumber(updatedContents.toString());
		try {
			filesToWrite.put(fileBean);
		} catch (InterruptedException e) {
		}
	}
}

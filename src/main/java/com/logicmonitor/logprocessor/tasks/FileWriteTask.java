package com.logicmonitor.logprocessor.tasks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import com.logicmonitor.logprocessor.dto.FileBean;

/**
 * Persists the contents of all the FileBeans from the BlockingQueue
 * 'filesToWrite'. Blocking Queue is thread safe, hence explicit Implementation
 * of wait and notify is not necessary
 * 
 * @author kaush
 *
 */
public class FileWriteTask implements Runnable {

	private final BlockingQueue<FileBean> filesToWrite;
	FileBean fileBean;

	public FileWriteTask(BlockingQueue<FileBean> filesToWrite) {
		this.filesToWrite = filesToWrite;
	}

	@Override
	public void run() {
		while (filesToWrite.size() > 0) {
			BufferedWriter writer = null;
			try {
				fileBean = filesToWrite.take();
				writer = Files.newBufferedWriter(Paths.get(fileBean.getFileName()));
				writer.write(fileBean.getContentsWithLineNumber());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}

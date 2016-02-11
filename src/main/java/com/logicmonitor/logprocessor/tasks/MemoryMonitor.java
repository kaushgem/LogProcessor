package com.logicmonitor.logprocessor.tasks;

import java.util.concurrent.ExecutorService;

import com.logicmonitor.logprocessor.util.ThreadLock;

/**
 * Monitors the memory of JVM and triggers GC if necessary
 * 
 * @author kaush
 *
 */
public class MemoryMonitor implements Runnable {

	ExecutorService readService;
	ExecutorService processService;
	ExecutorService writeService;
	final ThreadLock lock;

	public MemoryMonitor(ExecutorService readService,
			ExecutorService processService, ExecutorService writeService,
			ThreadLock lock) {
		this.readService = readService;
		this.processService = processService;
		this.writeService = writeService;
		this.lock = lock;
	}

	@Override
	public void run() {
		double totalMem, freeMem, usedMem;

		while (!readService.isTerminated() || !processService.isTerminated()
				|| !writeService.isTerminated()) {
			totalMem = Double.valueOf(Runtime.getRuntime().totalMemory());
			freeMem = Double.valueOf(Runtime.getRuntime().freeMemory());
			usedMem = totalMem - freeMem;

			if (usedMem > totalMem * 0.6) {
				synchronized (lock) {
					lock.setStatus(true);
					System.gc();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} else {
				synchronized (lock) {
					if (lock.isStatus()) {
						lock.setStatus(false);
						lock.notifyAll();
					}
				}
			}

		}
	}

}
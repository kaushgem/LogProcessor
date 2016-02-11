package com.logicmonitor.logprocessor.util;

/**
 * Bean Object used by MemoryMonitor to pause other threads during Garbage
 * collection
 * 
 * @author kaush
 *
 */
public class ThreadLock {
	boolean status;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

}

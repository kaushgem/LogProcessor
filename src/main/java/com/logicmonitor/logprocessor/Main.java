package com.logicmonitor.logprocessor;

import com.logicmonitor.logprocessor.service.LogProcessorService;

/**
 * Main method args[0] - Path of log file directory args[1] - number of threads
 * 
 * @author kaush
 *
 */
public class Main {
	public static void main(String[] args) {
		LogProcessorService.LOGPROCESSOR.LogProcessor(args[0],
				Integer.parseInt(args[1]));
	}
}

package com.logicmonitor.logprocessor;

import com.logicmonitor.logprocessor.service.LogProcessorService;

public class Main {
	public static void main(String[] args) {
		LogProcessorService.LOGPROCESSOR.LogProcessor(args[0], Integer.parseInt(args[1]));
	}
}

package com.logicmonitor.logprocessor.dto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for file contents and its meta data
 * 
 * @author kaush
 *
 */
public class FileBean {

	private String fileName;
	private List<String> contents = new ArrayList<>();
	private BigInteger startLineNumber;
	private String contentsWithLineNumber;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<String> getContents() {
		return contents;
	}

	public void setContents(List<String> contents) {
		this.contents = contents;
	}

	public BigInteger getStartLineNumber() {
		return startLineNumber;
	}

	public void setStartLineNumber(BigInteger startLineNumber) {
		this.startLineNumber = startLineNumber;
	}

	public String getContentsWithLineNumber() {
		return contentsWithLineNumber;
	}

	public void setContentsWithLineNumber(String contentsWithLineNumber) {
		this.contentsWithLineNumber = contentsWithLineNumber;
	}

}

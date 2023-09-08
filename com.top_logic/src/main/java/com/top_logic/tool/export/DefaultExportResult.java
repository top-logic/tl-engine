/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.OutputStream;

import com.top_logic.basic.util.ResKey;

/**
 * The DefaultExportResult is a basic implementation of ExportResult.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class DefaultExportResult implements ExportResult {

	private final OutputStream out;

	private ResKey i18nFailure;

	private ResKey filename;
	private String fileext;
	
	public DefaultExportResult(OutputStream anOutput) {
		this.out = anOutput;
	}
	
	@Override
	public OutputStream getOutputStream() {
		return this.out;
	}

	@Override
	public void setFailureKey(ResKey aResKey) {
		this.i18nFailure = aResKey;
	}

	@Override
	public ResKey getFailureKey() {
		return this.i18nFailure;
	}
	
	@Override
	public String getFileExtension() {
		return this.fileext;
	}
	
	@Override
	public ResKey getFileDisplaynameKey() {
		return this.filename;
	}
	
	@Override
	public void setFileExtension(String aFileextension) {
		this.fileext = aFileextension;		
	}
	
	@Override
	public void setFileDisplaynameKey(ResKey aResKey) {
		this.filename = aResKey;
	}
	
}

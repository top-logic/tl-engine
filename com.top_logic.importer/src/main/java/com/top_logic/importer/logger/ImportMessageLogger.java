/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.importer.excel.transformer.TransformException;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;
import com.top_logic.util.Resources;

/**
 * Converts all log messages into {@link ImportMessage}s.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ImportMessageLogger extends AbstractLogger {

	private final List<ImportMessage> messages;

	public ImportMessageLogger(List<ImportMessage> someMessages) {
		super();

		messages = someMessages;
	}

	@Override
	protected void log(String stack) {
	}

	@Override
    protected void log(String type, Object caller, ResKey key, Object... params) {
		if(INFO.equals(type)){
			messages.add(new ImportMessage(ImportMessage.INFO, key, params));
			Logger.info(Resources.getInstance().getMessage(key, params), caller);
        }
		else if(WARN.equals(type)){
			messages.add(new ImportMessage(ImportMessage.ERROR, key, params));
			Logger.warn(Resources.getInstance().getMessage(key, params), caller);
		}
		else if(ERROR.equals(type)){
			messages.add(new ImportMessage(ImportMessage.ERROR, key, params));
			Logger.error(Resources.getInstance().getMessage(key, params), caller);
		}
	}
	
	public void logTransformException(Object handler, TransformException e) {
		error(handler, e.getKey(), e.getAllParams());
	}

	public void logTransformExceptionAsWarn(Object handler, TransformException e) {
		info(handler, e.getKey(), e.getAllParams());
	}

	public List<ImportMessage> getImportMessages() {
		return messages;
	}
	
	
}

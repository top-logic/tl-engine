/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.io.BasicFileLog;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} that searches all system log files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogFilesBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link LogFilesBuilder} instance.
	 */
	public static final LogFilesBuilder INSTANCE = new LogFilesBuilder();

	private LogFilesBuilder() {
		// Singleton constructor.
	}
	
	private static final FileFilter PLAIN_FILES = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
	};

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		return anObject instanceof File;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		BasicFileLog logger = BasicFileLog.getInstance();
		File logDir = logger.getBasicPath();
		if (logDir == null) {
            return Collections.EMPTY_LIST;
        }
		
		File[] logFiles = logDir.listFiles(PLAIN_FILES);
        if (logFiles == null) {
        	return Collections.EMPTY_LIST;
        }
        
        return new ArrayList<>(Arrays.asList(logFiles));
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

}

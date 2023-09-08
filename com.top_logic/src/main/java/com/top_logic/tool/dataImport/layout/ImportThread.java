/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import com.top_logic.basic.thread.InContext;
import com.top_logic.tool.dataImport.AbstractDataImporter;
import com.top_logic.util.TLContext;

/**
 * {@link Thread} that actually performs the asynchronous import.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class ImportThread extends Thread implements InContext {
	protected final AbstractDataImporter _importer;

	public ImportThread(String name, AbstractDataImporter importer) {
		super(name);

		_importer = importer;
	}

	@Override
	public void run() {
		TLContext.inSystemContext(_importer.getClass(), this);
	}

	@Override
	public void inContext() {
		TLContext.pushSuperUser();
		try {
			doImport();
		} finally {
			TLContext.popSuperUser();
		}
	}

	protected abstract void doImport();
}
/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.FileManager;

/**
 * {@link TestSetup} that installs a custom {@link FileManager} for a test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileManagerTestSetup extends TestSetup {

	/**
	 * Creates a {@link TestSetup} caching the current {@link FileManager}.
	 */
	public FileManagerTestSetup(Test test) {
		super(test);
	}

	private FileManager _fileManager;

	@Override
	protected void setUp() throws Exception {
		_fileManager = FileManager.getInstance();
	}
	
	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_fileManager);
	}

}
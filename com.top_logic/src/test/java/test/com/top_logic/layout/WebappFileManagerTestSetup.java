/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;

/**
 * {@link TestSetup} setting up a {@link FileManager} with a single path of
 * {@link ModuleLayoutConstants#WEBAPP_DIR}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebappFileManagerTestSetup extends TestSetup {

	/**
	 * Creates a {@link WebappFileManagerTestSetup}.
	 */
	public static WebappFileManagerTestSetup setup(Class<?> testClass) {
		return setup(new TestSuite(testClass));
	}

	/**
	 * Creates a {@link WebappFileManagerTestSetup}.
	 */
	public static WebappFileManagerTestSetup setup(Test test) {
		return new WebappFileManagerTestSetup(test);
	}

	private FileManager _before;

	/**
	 * Creates a {@link WebappFileManagerTestSetup}.
	 */
	private WebappFileManagerTestSetup(Test test) {
		super(test);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_before = FileManager.setInstance(new DefaultFileManager(ModuleLayoutConstants.WEBAPP_DIR));
	}

	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_before);

		super.tearDown();
	}

}

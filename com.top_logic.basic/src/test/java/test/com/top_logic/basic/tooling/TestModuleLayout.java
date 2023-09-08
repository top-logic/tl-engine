/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.tooling;

import java.io.File;

import junit.framework.TestCase;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.tooling.ModuleLayout;

/**
 * Test case for {@link ModuleLayout}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TestModuleLayout extends TestCase {

	private static final String BASIC_DIR = "com.top_logic.basic";

	/**
	 * Tests whether the workspace directory has a direct child folder named
	 * <code>com.top_logic.basic</code>.
	 */
	public void testGetWorkspaceDir() {
		ModuleLayout moduleLayout = moduleLayout();
		File workspaceDir = moduleLayout.getWorkspaceDir();
		File basicFolder = new File(workspaceDir, BASIC_DIR);

		assertTrue(basicFolder.exists());
	}

	private static ModuleLayout moduleLayout() {
		return new ModuleLayout(new BufferingProtocol(), new File("."));
	}
}

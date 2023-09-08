/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.knowledge.service.DBMain;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Test case for {@link DBMain} helper.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDBMain extends AbstractXMainTest {
	
	private static final String KNOWLEDGE_BASE_SECTION = PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME;

	public void testCreateTables() throws Exception {
		String[] args = {
			"dropTables",
			"createTables"
		};
		
		runDBMain(args);
	}

	private void runDBMain(String[] args) throws Exception {
		DBMain main = new DBMain();
		main.setProtocol(new AssertProtocol());
		main.runMainCommandLine(prependMetaConf(args));
	}
	
	public static Test suite() {
		return suite(TestDBMain.class);
	}

}

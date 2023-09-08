/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * {@link TestSetup} that simulates a second cluster node.
 * 
 * @see #kbNode2() Access to the {@link KnowledgeBase} on the simulated second
 *      node.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBKnowledgeBaseClusterTestSetup extends AbstractMultiKBTestSetup {

	public DBKnowledgeBaseClusterTestSetup(Test test) {
		super(test);
	}

	@Override
	protected DBKnowledgeBase setupSecondKB(KnowledgeBaseConfiguration config) throws Exception {
		SchemaSetup schemaSetup = SetupKBHelper.newSchemaSetup(config, getAdditionalProvider());
		DBKnowledgeBase kb = new DBKnowledgeBaseAccess(schemaSetup);

		Protocol log = new AssertProtocol();
		kb.initialize(log, config);
		
		kb.startup(new AssertProtocol());
		return kb;
	}

	@Override
	protected String getTestDBNode2() {
		return getTestDB();
	}

	@Override
	protected void teardownSecondKB(DBKnowledgeBase kb) throws Exception {
		kb.shutDown();
	}

}

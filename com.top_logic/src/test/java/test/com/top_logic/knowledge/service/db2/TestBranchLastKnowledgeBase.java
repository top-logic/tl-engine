/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.db.schema.BranchLastSetup;

import com.top_logic.basic.config.Decision;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;

/**
 * Knowledge base tests run with {@link SchemaConfiguration#isBranchLast()} set.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranchLastKnowledgeBase {

	/**
	 * Suite of knowledge base tests with the branch column placed last in the indexes.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestBranchLastKnowledgeBase.class.getName());
		suite.addTest(TestBranching.suite());
		suite.addTest(TestDBKnowledgeBase.suite());
		suite.addTest(TestFlexAttributes.suite());
		suite.addTest(TestDeletedObjectPurge.suite());
		return new BranchLastSetup(suite, Decision.TRUE);
	}

}

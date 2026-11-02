/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.db.schema.MultipleBranchSetup;

import com.top_logic.basic.config.Decision;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;

/**
 * Knowledge base tests run with a single branch ({@link SchemaConfiguration#hasMultipleBranches()}
 * set to <code>false</code>).
 *
 * <p>
 * The knowledge base tests run their single-branch variant only together with the tests on other
 * databases. This suite runs it on the default database.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSingleBranchKnowledgeBase {

	/**
	 * Suite of knowledge base tests with a single branch.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestSingleBranchKnowledgeBase.class.getName());
		suite.addTest(TestDBKnowledgeBase.suite());
		suite.addTest(TestFlexAttributes.suite());
		return new MultipleBranchSetup(suite, Decision.FALSE);
	}

}

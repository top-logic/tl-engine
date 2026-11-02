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
import com.top_logic.basic.sql.H2Helper;

/**
 * Knowledge base tests run with the branch column leading the indexes
 * ({@link SchemaConfiguration#getBranchLast()} pinned to <code>false</code>).
 *
 * <p>
 * The knowledge base tests run on H2 by default, where the {@link H2Helper dialect} places the
 * branch column last. This suite covers the other layout, which all other databases use.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranchFirstKnowledgeBase {

	/**
	 * Suite of knowledge base tests with the branch column leading the indexes.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestBranchFirstKnowledgeBase.class.getName());
		suite.addTest(TestBranching.suite());
		suite.addTest(TestDBKnowledgeBase.suite());
		suite.addTest(TestFlexAttributes.suite());
		suite.addTest(TestDeletedObjectPurge.suite());
		return new BranchLastSetup(suite, Decision.FALSE);
	}

}

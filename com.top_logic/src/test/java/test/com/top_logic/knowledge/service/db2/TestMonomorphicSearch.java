/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for {@link com.top_logic.knowledge.service.db2.MonomorphicSearch}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMonomorphicSearch extends AbstractDBKnowledgeBaseClusterTest {

	public void testLargeMultiBranchIdChunk() throws KnowledgeBaseException, RefetchTimeout {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = kb().beginTransaction();
		int objCnt = 500;
		for (int n = 0; n < objCnt; n++) {
			BObj.newBObj("b" + n);
		}
		tx.commit();

		int branchCnt = 9;
		for (int n = 0; n < branchCnt; n++) {
			kb().createBranch(kb().getTrunk(), tx.getCommitRevision(), null);
		}

		kbNode2().refetch();

		int cnt = 0;
		try (CloseableIterator<BObj> result =
			kbNode2().searchStream(queryResolved(allOf(B_NAME), BObj.class).setBranchParam(BranchParam.all))) {
			while (result.hasNext()) {
				BObj obj = result.next();
				assertNotNull(obj);
				cnt++;
			}
		}
		assertEquals(objCnt * (1 + branchCnt), cnt);
	}

	public static Test suite() {
		return suite(TestMonomorphicSearch.class);
	}
}

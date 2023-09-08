/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.exec;

import static com.top_logic.basic.col.LongRangeSet.*;
import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import test.com.top_logic.knowledge.service.db2.expr.visit.AbstractHistoryQueryTest;

import com.top_logic.basic.col.LongRange;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodOracle;

/**
 * The class {@link TestLifePeriodOracle} tests {@link LifePeriodOracle}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLifePeriodOracle extends AbstractHistoryQueryTest {

	public void testReferenceHistoricAttributeQuery() throws DataObjectException {
		String attr1 = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		String attr2 = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);

		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject d1 = newD("d1");
		e1.setAttributeValue(attr1, d1);
		KnowledgeObject d2 = newD("d2");
		e1.setAttributeValue(attr2, d2);
		commit(tx1);
		
		// d1Match start
		Transaction tx2 = begin();
		d1.setAttributeValue(A2_NAME, "match");
		commit(tx2);

		// e1Match start
		Transaction tx3 = begin();
		e1.setAttributeValue(A2_NAME, "match");
		commit(tx3);

		// d1Match stop
		Transaction tx4 = begin();
		d1.setAttributeValue(A2_NAME, "unmatch");
		commit(tx4);

		// d2Match start
		Transaction tx5 = begin();
		d2.setAttributeValue(A2_NAME, "match");
		commit(tx5);

		// e1Match stop
		Transaction tx6 = begin();
		e1.setAttributeValue(A2_NAME, "unmatch");
		commit(tx6);
		
		Expression e1Match = eqBinary(attribute(D_NAME, A2_NAME), literal("match"));
		Expression d1Match = eqBinary(attribute(reference(E_NAME, attr1), D_NAME, A2_NAME), literal("match"));
		Expression d2Match = eqBinary(attribute(reference(E_NAME, attr2), D_NAME, A2_NAME), literal("match"));
		Map<?, List<LongRange>> searchResult =
			kb().search(historyQuery(filter(allOf(E_NAME), and(e1Match, or(d1Match, d2Match)))));
		assertEquals(1, searchResult.size());
		List<LongRange> lifePeriod = searchResult.get(getObjectID(e1));
		List<LongRange> e1d1Match = range(tx3, tx3);
		List<LongRange> e1d2Match = range(tx5, tx5);
		assertEquals(union(e1d1Match, e1d2Match), lifePeriod);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestLifePeriodOracle}.
	 */
	public static Test suite() {
		return suite(TestLifePeriodOracle.class);
	}

}

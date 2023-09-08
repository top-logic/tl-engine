/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.basic.col.LongRangeSet.*;
import static com.top_logic.basic.col.LongRangeSet.union;
import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.HistoryQueryArguments;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Super class of tests of {@link HistoryQuery}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractHistoryQueryTest extends AbstractDBKnowledgeBaseClusterTest {

	/**
	 * Creates a singleton list of {@link LongRange} from the <code>startRevision</code> to the
	 * <code>stopRevision</code>.
	 */
	protected static List<LongRange> range(Revision startRevision, Revision stopRevision) {
		long start = startRevision.getCommitNumber();
		long stop = stopRevision.getCommitNumber();
		return LongRangeSet.range(start, stop);
	}

	/**
	 * Creates a singleton list of {@link LongRange} from the revision of
	 * <code>startTransaction</code> to the revision of <code>stopTransaction</code>.
	 */
	protected static List<LongRange> range(Transaction startTransaction, Transaction stopTransaction) {
		assertCommitted(startTransaction);
		assertCommitted(stopTransaction);
		return range(startTransaction.getCommitRevision(), stopTransaction.getCommitRevision());
	}

	/**
	 * Creates a singleton list of {@link LongRange} from the revision of
	 * <code>startTransaction</code> to the current revision.
	 */
	protected static List<LongRange> endSection(Transaction startTransaction) {
		return range(startTransaction.getCommitRevision(), Revision.CURRENT);
	}

	/**
	 * Returns the given {@link KnowledgeItem} in the commit revision of the given transaction.
	 */
	protected KnowledgeItem inRev(Transaction tx, KnowledgeItem item) throws DataObjectException {
		assertCommitted(tx);
		return HistoryUtils.getKnowledgeItem(tx.getCommitRevision(), item);
	}

	private static void assertCommitted(Transaction tx) {
		assertSame("Only possible for committed transactions: " + tx, Transaction.STATE_COMMITTED, tx.getState());
	}

	/**
	 * Checks that the result of the {@link HistoryQuery} is consistent with the result of the
	 * corresponding {@link RevisionQuery}, i.e. for each time the returned life period of any
	 * object, the object must be found by the corresponding {@link RevisionQuery}.
	 */
	public static void assertHistorySearch(KnowledgeBase kb, HistoryQuery historyQuery,
			HistoryQueryArguments historyArgs) {
		Map<?, List<LongRange>> historyResult = kb.search(historyQuery, historyArgs);
		assertNotEquals("Test did not produce any history.", 0, historyResult.size());
		
		RevisionQuery<KnowledgeObject> revisionQuery =
			queryUnresolved(historyQuery.getBranchParam(), RangeParam.complete, historyQuery.getSearchParams(),
				historyQuery.getSearch(), null);
		RevisionQueryArguments revisionArgs = revisionArgs();
		revisionArgs.setRequestedBranches(historyArgs.getRequestedBranches());
		revisionArgs.setArguments(historyArgs.getArguments());
		
		long startRev = historyArgs.getStartRevision();
		long stopRev = historyArgs.getStopRevision();
		if (stopRev == Revision.CURRENT_REV) {
			stopRev = kb.getHistoryManager().getLastRevision();
		}
		
		CompiledQuery<KnowledgeObject> compiledQuery = kb.compileQuery(revisionQuery);
		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			for (long commitNumber = startRev; commitNumber < stopRev; commitNumber++) {
				revisionArgs.setRequestedRevision(commitNumber);
				
				HashSet<Object> allIdsInHistory = new HashSet<>(historyResult.keySet());
				
				try (CloseableIterator<KnowledgeObject> revisionResult =
					compiledQuery.searchStream(connection, revisionArgs)) {
					while (revisionResult.hasNext()) {
						KnowledgeItem item = revisionResult.next();

						ObjectBranchId id = getObjectID(item);

						List<LongRange> lifePeriod = historyResult.get(id);
						assertNotNull(lifePeriod);
						assertTrue(LongRangeSet.contains(lifePeriod, commitNumber));

						allIdsInHistory.remove(id);
					}
				}
				
				for (Object unmatchedId : allIdsInHistory) {
					List<LongRange> lifePeriod = historyResult.get(unmatchedId);
					assertNotNull(lifePeriod);
					assertFalse(LongRangeSet.contains(lifePeriod, commitNumber));
				}
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	
		List<LongRange> before = LongRangeSet.range(1, startRev - 1);
		List<LongRange> after;
		if (historyArgs.getStopRevision() == Revision.CURRENT_REV) {
			after = EMPTY_SET;
		} else {
			after = LongRangeSet.range(historyArgs.getStopRevision() + 1, Revision.CURRENT_REV);
		}
		List<LongRange> noResultRange = union(before, after);
		for (Entry<?, List<LongRange>> entry : historyResult.entrySet()) {
			assertTrue(intersect(entry.getValue(), noResultRange).isEmpty());
		}
	}

}


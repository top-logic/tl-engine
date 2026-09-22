/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.KnowledgeReferenceStorageImpl;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.BranchSupport;
import com.top_logic.knowledge.service.db2.HistoryCompaction;
import com.top_logic.knowledge.service.db2.ItemTables;
import com.top_logic.knowledge.service.db2.HistoryCompaction.Report;
import com.top_logic.knowledge.service.db2.I18NConstants;
import com.top_logic.knowledge.service.db2.RevisionType;
import com.top_logic.knowledge.service.db2.RevisionXref;

/**
 * Test of {@link HistoryCompaction}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestHistoryCompaction extends AbstractDBKnowledgeBaseTest {

	private static final String FLEX_ATTR = "flexAttr";

	private static final boolean MONOMORPHIC = true;

	private static final boolean BRANCH_GLOBAL = true;

	private Log _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_log = new AssertProtocol(getClass().getName());
	}

	private HistoryCompaction newCompaction() throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		return new HistoryCompaction(pool.getSQLDialect(), pool, kb().getMORepository());
	}

	/**
	 * Objects with a history, a deleted object, pinned references and a branch, all created before
	 * the compaction revision.
	 */
	private static final class Scenario {

		KnowledgeObject _target;

		KnowledgeObject _doomed;

		KnowledgeObject _targetSubtype;

		KnowledgeObject _doomedSubtype;

		KnowledgeObject _doomedGlobal;

		KnowledgeObject _holder;

		KnowledgeObject _flexible;

		Branch _branch;

		long _branchBaseRevision;

		long _branchCreateRevision;

		long _compactionRevision;
	}

	private Scenario setupScenario() throws Exception {
		Scenario scenario = new Scenario();

		Transaction tx = begin();
		scenario._target = newD("target-v1");
		scenario._doomed = newD("doomed");
		scenario._targetSubtype = newE("target-subtype");
		scenario._doomedSubtype = newE("doomed-subtype");
		scenario._doomedGlobal = newD("doomed-global");
		scenario._holder = newD("holder");
		scenario._flexible = newB("flexible");
		commit(tx);
		Revision r1 = tx.getCommitRevision();
		KnowledgeItem targetAtR1 = HistoryUtils.getKnowledgeItem(r1, scenario._target);
		KnowledgeItem doomedAtR1 = HistoryUtils.getKnowledgeItem(r1, scenario._doomed);
		KnowledgeItem targetSubtypeAtR1 = HistoryUtils.getKnowledgeItem(r1, scenario._targetSubtype);
		KnowledgeItem doomedSubtypeAtR1 = HistoryUtils.getKnowledgeItem(r1, scenario._doomedSubtype);
		KnowledgeItem doomedGlobalAtR1 = HistoryUtils.getKnowledgeItem(r1, scenario._doomedGlobal);

		tx = begin();
		setA1(scenario._target, "target-v2");
		commit(tx);
		Revision r2 = tx.getCommitRevision();

		scenario._branch = HistoryUtils.createBranch(trunk(), r2, types(B_NAME));
		scenario._branchBaseRevision = scenario._branch.getBaseRevision().getCommitNumber();
		scenario._branchCreateRevision = scenario._branch.getCreateRevision().getCommitNumber();

		tx = begin();
		setA1(scenario._target, "target-v3");
		scenario._flexible.setAttributeValue(FLEX_ATTR, "flex-v3");
		commit(tx);

		tx = begin();
		scenario._doomed.delete();
		scenario._doomedSubtype.delete();
		scenario._doomedGlobal.delete();
		commit(tx);

		tx = begin();
		setReference(scenario._holder, targetAtR1, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		setReference(scenario._holder, doomedAtR1, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		setReference(scenario._holder, doomedSubtypeAtR1, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		setReference(scenario._holder, doomedGlobalAtR1, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		setReference(scenario._holder, targetSubtypeAtR1, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		commit(tx);

		/* Supersedes the row the references were set in. That row then lies completely inside the
		 * compacted range and is deleted, while its copy carries the same pins on. */
		tx = begin();
		setA1(scenario._holder, "holder-at-cut");
		setA1(scenario._target, "target-at-cut");
		scenario._flexible.setAttributeValue(FLEX_ATTR, "flex-at-cut");
		commit(tx);
		scenario._compactionRevision = tx.getCommitRevision().getCommitNumber();

		tx = begin();
		setA1(scenario._target, "target-current");
		scenario._flexible.setAttributeValue(FLEX_ATTR, "flex-current");
		commit(tx);

		return scenario;
	}

	public void testCompactHistory() throws Exception {
		Scenario scenario = setupScenario();
		long cut = scenario._compactionRevision;
		assertTrue("The branch is based on a revision below the cut.", scenario._branchBaseRevision < cut);
		assertTrue("The branch was created below the cut.", scenario._branchCreateRevision < cut);

		ObjectKey targetKey = scenario._target.tId();
		ObjectKey holderKey = scenario._holder.tId();
		ObjectKey flexibleKey = scenario._flexible.tId();
		TLID doomedId = scenario._doomed.getObjectName();

		HistoryCompaction compaction = newCompaction();
		Report analysis = compaction.analyzeRevisions(0, cut, _log);
		Report result = compaction.compactRevisions(0, cut, _log);
		assertFalse("Something must have been compacted.", result.isEmpty());
		assertEquals("Dry run must predict the deletions.", analysis.getDeletedRows(), result.getDeletedRows());
		assertEquals("Dry run must predict the rewrites.", analysis.getRewrittenRows(), result.getRewrittenRows());
		assertEquals("Dry run must predict the re-pinned references.", analysis.getRewrittenPins(),
			result.getRewrittenPins());
		assertEquals("Only the pins of the surviving rows are cleared.", 3, result.getClearedPins());
		assertEquals("Dry run must predict the cleared references.", result.getClearedPins(),
			analysis.getClearedPins());
		assertEquals(cut, result.getCompactionRevision());

		KBTestUtils.clearCache(kb());

		// The current state is untouched.
		KnowledgeItem target = kb().resolveObjectKey(targetKey);
		KnowledgeItem flexible = kb().resolveObjectKey(flexibleKey);
		KnowledgeItem holder = kb().resolveObjectKey(holderKey);
		assertEquals("target-current", target.getAttributeValue(A1_NAME));
		assertEquals("flex-current", flexible.getAttributeValue(FLEX_ATTR));

		// The state at the compaction revision is preserved.
		Revision cutRevision = kb().getRevision(cut);
		assertEquals("target-at-cut", inRevision(cutRevision, target).getAttributeValue(A1_NAME));
		assertEquals("flex-at-cut", inRevision(cutRevision, flexible).getAttributeValue(FLEX_ATTR));

		// The compaction revision is marked as a system commit.
		assertEquals(HistoryCompaction.COMPACTION_AUTHOR, cutRevision.getAuthor());
		assertEquals(ResKey.encode(I18NConstants.HISTORY_COMPACTED), ResKey.encode(cutRevision.getLog()));

		// Older revisions are gone.
		assertEquals("Revisions below the cut are deleted.", 0, countRevisionsBelow(cut));
		assertEquals("No rows are valid before the cut.", 0, countRowsEndingBefore(type(D_NAME), cut));
		assertEquals("No dynamic values are valid before the cut.", 0,
			countRowsEndingBefore(kb().lookupType(AbstractFlexDataManager.FLEX_DATA), cut));
		assertEquals("An object deleted before the cut leaves no rows.", 0,
			countRowsOf((DBTableMetaObject) type(D_NAME), doomedId));

		// The cross reference is rebuilt for the compaction revision only.
		assertEquals("Cross references below the cut are deleted.", 0, countXrefBelow(cut));
		assertTrue("Objects of the changed type are registered in the compaction revision.",
			countXrefAt(cut, D_NAME) > 0);
		/* The only change of a B object in the compaction revision was a dynamic attribute value.
		 * Such a change touches the object, so the item table alone carries the information the
		 * cross reference is rebuilt from. */
		assertTrue("A type changed only in its dynamic values is registered, too.",
			countXrefAt(cut, B_NAME) > 0);

		// The objects created before the cut appear to be created in the compaction revision.
		assertEquals(cut, kb().getCreateRevision(target));

		// A pin to a surviving object was moved to the compaction revision.
		String survivingPin = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		assertEquals(cut, readPinColumn(holderKey, survivingPin, ReferencePart.revision));
		KnowledgeItem pinnedTarget = getReference(holder, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		assertNotNull("The pinned reference still resolves.", pinnedTarget);
		assertEquals("The pin resolves to the state at the compaction revision.", "target-at-cut",
			pinnedTarget.getAttributeValue(A1_NAME));

		/* A pin to an object deleted within the compacted range would point at a revision in which
		 * its target does not exist, so it is cleared. The three variants cover a target of the
		 * declared target type, a target of a subtype (the type column names the concrete table)
		 * and a branch global reference. */
		assertClearedPin(holder, holderKey, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		assertClearedPin(holder, holderKey, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		assertClearedPin(holder, holderKey, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);

		// A polymorphic pin to a surviving object of a subtype is kept.
		String subtypePin = getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		assertEquals(cut, readPinColumn(holderKey, subtypePin, ReferencePart.revision));
		KnowledgeItem pinnedSubtype = getReference(holder, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		assertNotNull("A pin to a surviving object of a subtype is kept.", pinnedSubtype);
		assertEquals("target-subtype", pinnedSubtype.getAttributeValue(A1_NAME));

		assertEquals("No row is left pinned to a discarded revision.", 0,
			countPinsBelow(type(D_NAME), cut));

		// The branch was moved out of the discarded range.
		assertEquals(cut, readBranchRevision(scenario._branch, BranchSupport.getBaseRevAttr(branchType())));
		assertEquals(cut, readBranchRevision(scenario._branch, BranchSupport.getCreateRevAttr(branchType())));

		// A repeated run has nothing left to do.
		Report second = newCompaction().compactRevisions(0, cut, _log);
		assertTrue("A second run must not change anything: " + second, second.isEmpty());
	}

	/**
	 * The delete advances in windows; a tiny window must produce the same result as a single one.
	 */
	public void testSmallDeleteWindow() throws Exception {
		Scenario scenario = setupScenario();
		long cut = scenario._compactionRevision;
		assertTrue("The scenario must span more than two revisions.", cut > 4);

		ObjectKey targetKey = scenario._target.tId();

		HistoryCompaction compaction = newCompaction();
		compaction.setDeleteWindowSize(2);
		Report result = compaction.compactRevisions(0, cut, _log);
		assertFalse(result.isEmpty());

		KBTestUtils.clearCache(kb());
		KnowledgeItem target = kb().resolveObjectKey(targetKey);
		assertEquals("target-current", target.getAttributeValue(A1_NAME));
		assertEquals("target-at-cut", inRevision(kb().getRevision(cut), target).getAttributeValue(A1_NAME));
		assertEquals("No rows are valid before the cut.", 0, countRowsEndingBefore(type(D_NAME), cut));
	}

	/**
	 * Compacting an inner range must preserve the state at both of its bounds.
	 */
	public void testCompactInnerRange() throws Exception {
		Transaction tx = begin();
		KnowledgeObject changing = newB("changing-v1");
		KnowledgeObject vanishing = newB("vanishing");
		commit(tx);

		tx = begin();
		setA1(changing, "changing-at-lower");
		commit(tx);
		long lower = tx.getCommitRevision().getCommitNumber();

		tx = begin();
		setA1(changing, "changing-inside");
		commit(tx);

		tx = begin();
		vanishing.delete();
		commit(tx);

		tx = begin();
		setA1(changing, "changing-at-upper");
		commit(tx);
		long upper = tx.getCommitRevision().getCommitNumber();

		tx = begin();
		setA1(changing, "changing-current");
		commit(tx);

		ObjectKey changingKey = changing.tId();
		TLID vanishingId = vanishing.getObjectName();

		Report result = newCompaction().compactRevisions(lower, upper, _log);
		assertFalse(result.isEmpty());
		assertEquals(lower, result.getLowerRevision());

		KBTestUtils.clearCache(kb());
		KnowledgeItem reloaded = kb().resolveObjectKey(changingKey);
		assertEquals("changing-current", reloaded.getAttributeValue(A1_NAME));
		assertEquals("changing-at-lower",
			inRevision(kb().getRevision(lower), reloaded).getAttributeValue(A1_NAME));
		assertEquals("changing-at-upper",
			inRevision(kb().getRevision(upper), reloaded).getAttributeValue(A1_NAME));

		assertEquals("Revisions between the bounds are deleted.", 0,
			countRevisionsBetween(lower, upper));
		assertEquals("No row lives completely inside the compacted range.", 0,
			countRowsInsideRange(type(B_NAME), lower, upper));
		assertEquals("The object deleted inside the range survives up to the revision before the cut.",
			upper - 1, readRevMax((DBTableMetaObject) type(B_NAME), vanishingId));

		assertNotNull("The deleted object is visible at the lower bound.",
			HistoryUtils.getKnowledgeItem(trunk(), kb().getRevision(lower), type(B_NAME), vanishingId));
		assertNull("The deleted object is gone at the compaction revision.",
			HistoryUtils.getKnowledgeItem(trunk(), kb().getRevision(upper), type(B_NAME), vanishingId));
	}

	public void testNoopRanges() throws Exception {
		Transaction tx = begin();
		newB("b1");
		commit(tx);
		long last = tx.getCommitRevision().getCommitNumber();

		HistoryCompaction compaction = newCompaction();
		assertTrue("An empty range changes nothing.", compaction.compactRevisions(last, last, _log).isEmpty());
		assertTrue("Adjacent revisions change nothing.",
			compaction.compactRevisions(last - 1, last, _log).isEmpty());
		try {
			compaction.compactRevisions(-1, last, _log);
			fail("A negative lower bound must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		try {
			compaction.compactRevisions(0, last + 1000, _log);
			fail("A non existing compaction revision must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/**
	 * The tables that implement versioning itself must not be processed as item tables.
	 */
	public void testItemTableSelection() throws Exception {
		Set<String> itemTables = new HashSet<>();
		for (ItemTables.Table table : newCompaction().getTables().getItemTables()) {
			itemTables.add(table.getType().getName());
		}
		assertTrue("Versioned application tables are processed.", itemTables.contains(D_NAME));
		assertTrue("Unversioned application tables are processed, too.", itemTables.contains(H_NAME));
		assertFalse(itemTables.contains(BasicTypes.REVISION_TYPE_NAME));
		assertFalse(itemTables.contains(BasicTypes.BRANCH_TYPE_NAME));
		assertFalse(itemTables.contains(BranchSupport.BRANCH_SWITCH_TYPE_NAME));
		assertFalse(itemTables.contains(RevisionXref.REVISION_XREF_TYPE_NAME));
		assertFalse(itemTables.contains(AbstractFlexDataManager.FLEX_DATA));
		assertFalse("Abstract types have no table of their own.", itemTables.contains(BasicTypes.ITEM_TYPE_NAME));

		ItemTables.Table flexData = newCompaction().getTables().getFlexData();
		assertNotNull("The dynamic values table is reported separately.", flexData);
		assertEquals(AbstractFlexDataManager.FLEX_DATA, flexData.getType().getName());
		assertNull("The dynamic values table has no create revision.", flexData.getRevCreate());
	}

	public void testResolveCompactionRevision() throws Exception {
		Transaction tx = begin();
		newB("b1");
		commit(tx);
		long middle = tx.getCommitRevision().getCommitNumber();

		tx = begin();
		newB("b2");
		commit(tx);
		long last = tx.getCommitRevision().getCommitNumber();

		HistoryCompaction compaction = newCompaction();
		assertEquals("No revision was committed before the epoch.", 0, compaction.resolveCompactionRevision(0));
		assertEquals("All revisions were committed before the end of time.", last,
			compaction.resolveCompactionRevision(Long.MAX_VALUE));

		long middleDate = kb().getRevision(middle).getDate();
		long resolved = compaction.resolveCompactionRevision(middleDate);
		assertTrue("The resolved revision is not older than the requested one.", resolved >= middle);
		assertTrue("The resolved revision was committed in time.",
			kb().getRevision(resolved).getDate() <= middleDate);
		if (resolved < last) {
			assertTrue("The next revision was committed later.",
				kb().getRevision(resolved + 1).getDate() > middleDate);
		}
	}

	public void testCompactHistoryByDate() throws Exception {
		Scenario scenario = setupScenario();
		long cut = scenario._compactionRevision;
		long cutDate = kb().getRevision(cut).getDate();

		HistoryCompaction compaction = newCompaction();
		long resolved = compaction.resolveCompactionRevision(cutDate);
		Report result = compaction.compactHistory(cutDate, _log);
		assertEquals(resolved, result.getCompactionRevision());
		assertEquals("Revisions below the compaction revision are deleted.", 0, countRevisionsBelow(resolved));
	}

	private MOClass branchType() {
		return BasicTypes.getBranchType(kb());
	}

	/**
	 * Checks that the given reference was reset to the value of a reference without a target.
	 */
	private void assertClearedPin(KnowledgeItem holder, ObjectKey holderKey, boolean monomorphic,
			HistoryType historyType, boolean branchGlobal) throws Exception {
		String attribute = getReferenceAttr(monomorphic, historyType, branchGlobal);
		assertNull("Reference '" + attribute + "' must answer nothing.",
			getReference(holder, monomorphic, historyType, branchGlobal));
		assertEquals("Pin of '" + attribute + "' must be reset.",
			KnowledgeReferenceStorageImpl.NULL_REPLACEMENT.longValue(),
			readPinColumn(holderKey, attribute, ReferencePart.revision));
		assertNull("Target of '" + attribute + "' must be the identifier reserved for no value.",
			readPinId(holderKey, attribute));
	}

	/**
	 * The number of pin values in the given table that name a revision that no longer exists.
	 */
	private long countPinsBelow(MOClass type, long revision) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		long result = 0;
		for (MOAttribute attribute : type.getAttributes()) {
			if (!(attribute instanceof MOReference)) {
				continue;
			}
			DBAttribute pin = ((MOReference) attribute).getColumn(ReferencePart.revision);
			if (pin == null) {
				continue;
			}
			String column = sqlDialect.columnRef(pin.getDBName());
			String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName())
				+ " WHERE " + column + " > " + KnowledgeReferenceStorageImpl.NULL_REPLACEMENT
				+ " AND " + column + " < " + revision;
			result += querySingleLong(sql, null);
		}
		return result;
	}

	/**
	 * The raw value of a column of the given reference in the current row of the given object.
	 */
	private long readPinColumn(ObjectKey holderKey, String attribute, ReferencePart part) throws SQLException {
		return querySingleLong(selectPinColumn(holderKey, attribute, part), holderKey.getObjectName());
	}

	/**
	 * The raw identifier the given reference points to in the current row of the given object.
	 */
	private TLID readPinId(ObjectKey holderKey, String attribute) throws SQLException {
		String sql = selectPinColumn(holderKey, attribute, ReferencePart.name);
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			IdentifierUtil.setId(statement, 1, holderKey.getObjectName());
			try (ResultSet result = statement.executeQuery()) {
				assertTrue("Query without result: " + sql, result.next());
				return IdentifierUtil.getId(result, 1);
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private String selectPinColumn(ObjectKey holderKey, String attribute, ReferencePart part) throws SQLException {
		MOClass type = (MOClass) holderKey.getObjectType();
		MOAttribute reference = type.getAttributeOrNull(attribute);
		String column = ((MOReference) reference).getColumn(part).getDBName();
		DBHelper sqlDialect = sqlDialect();
		return "SELECT " + sqlDialect.columnRef(column)
			+ " FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " = ?"
			+ " AND " + sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME) + " = " + Revision.CURRENT_REV;
	}

	private long readRevMax(DBTableMetaObject table, TLID id) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		String sql = "SELECT " + sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME)
			+ " FROM " + sqlDialect.tableRef(table.getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " = ?";
		return querySingleLong(sql, id);
	}

	private long readBranchRevision(Branch branch, com.top_logic.dob.sql.DBAttribute column) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		String sql = "SELECT " + sqlDialect.columnRef(column.getDBName())
			+ " FROM " + sqlDialect.tableRef(((DBTableMetaObject) branchType()).getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.BRANCH_DB_NAME) + " = " + branch.getBranchId();
		return querySingleLong(sql, null);
	}

	private long countRevisionsBelow(long revision) throws SQLException {
		return countRevisionsBetween(0, revision);
	}

	private long countRevisionsBetween(long lower, long upper) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		MOClass revisionType = BasicTypes.getRevisionType(kb());
		String revColumn = sqlDialect.columnRef(RevisionType.getRevisionAttribute(revisionType).getDBName());
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) revisionType).getDBName())
			+ " WHERE " + revColumn + " > " + lower + " AND " + revColumn + " < " + upper;
		return querySingleLong(sql, null);
	}

	private long countXrefBelow(long revision) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		DBTableMetaObject xref = kb().lookupType(RevisionXref.REVISION_XREF_TYPE_NAME);
		String revColumn = sqlDialect.columnRef(
			xref.getAttributeOrNull(RevisionXref.XREF_REV_ATTRIBUTE).getDbMapping()[0].getDBName());
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(xref.getDBName())
			+ " WHERE " + revColumn + " > 0 AND " + revColumn + " < " + revision;
		return querySingleLong(sql, null);
	}

	private long countXrefAt(long revision, String typeName) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		DBTableMetaObject xref = kb().lookupType(RevisionXref.REVISION_XREF_TYPE_NAME);
		String revColumn = sqlDialect.columnRef(
			xref.getAttributeOrNull(RevisionXref.XREF_REV_ATTRIBUTE).getDbMapping()[0].getDBName());
		String typeColumn = sqlDialect.columnRef(
			xref.getAttributeOrNull(RevisionXref.XREF_TYPE_ATTRIBUTE).getDbMapping()[0].getDBName());
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(xref.getDBName())
			+ " WHERE " + revColumn + " = " + revision + " AND " + typeColumn + " = '" + typeName + "'";
		return querySingleLong(sql, null);
	}

	private long countRowsEndingBefore(MOClass type, long revision) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME) + " < " + revision;
		return querySingleLong(sql, null);
	}

	private long countRowsInsideRange(MOClass type, long lower, long upper) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.REV_MIN_DB_NAME) + " > " + lower
			+ " AND " + sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME) + " < " + upper;
		return querySingleLong(sql, null);
	}

	private long countRowsOf(DBTableMetaObject table, TLID id) throws SQLException {
		DBHelper sqlDialect = sqlDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(table.getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " = ?";
		return querySingleLong(sql, id);
	}

	private DBHelper sqlDialect() throws SQLException {
		return kb().getConnectionPool().getSQLDialect();
	}

	/**
	 * Executes the given query, optionally binding a single identifier parameter.
	 */
	private long querySingleLong(String sql, TLID id) throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			if (id != null) {
				IdentifierUtil.setId(statement, 1, id);
			}
			try (ResultSet result = statement.executeQuery()) {
				assertTrue("Query without result: " + sql, result.next());
				return result.getLong(1);
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	public static Test suite() {
		return suiteNeedsBranches(TestHistoryCompaction.class);
	}

}

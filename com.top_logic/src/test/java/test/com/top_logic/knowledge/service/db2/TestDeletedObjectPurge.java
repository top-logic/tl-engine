/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Blocker;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Origin;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Report;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.TableReport;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemUtil;
import com.top_logic.knowledge.service.db2.StaticKnowledgeObjectFactory;

/**
 * Test of {@link DeletedObjectPurge}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDeletedObjectPurge extends AbstractDBKnowledgeBaseTest {

	/**
	 * Type with a mandatory reference, which the standard test types have only in the canonical
	 * references of an association.
	 */
	private static final String OWNER_NAME = "PurgeOwner";

	/**
	 * Mandatory reference of {@link #OWNER_NAME} to an object of
	 * {@link KnowledgeBaseTestScenarioConstants#REFERENCE_TYPE_NAME}.
	 */
	private static final String OWNED_NAME = "owned";

	/**
	 * Optional reference of {@link #OWNER_NAME} to an object of
	 * {@link KnowledgeBaseTestScenarioConstants#REFERENCE_TYPE_NAME}.
	 */
	private static final String LINKED_NAME = "linked";

	private static final String FLEX_ATTR = "flexAttr";

	private Log _log;

	/**
	 * Creates the {@link #OWNER_NAME} type.
	 *
	 * <p>
	 * Built when the test setup asks for its types, because the configuration of the
	 * implementation factory needs the running type index.
	 * </p>
	 */
	private static MOKnowledgeItemImpl createOwnerType() {
		MOKnowledgeItemImpl result = new MOKnowledgeItemImpl(OWNER_NAME);
		result.setAbstract(false);
		result.setSuperclass(new DeferredMetaObject(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME));
		MOKnowledgeItemUtil.setImplementationFactory(result, StaticKnowledgeObjectFactory.INSTANCE);
		MOReference owned = KnowledgeBaseTestScenarioImpl.newReferenceById(OWNED_NAME,
			new DeferredMetaObject(REFERENCE_TYPE_NAME), !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		owned.setMandatory(true);
		owned.setDeletionPolicy(DeletionPolicy.DELETE_REFERER);
		MOReference linked = KnowledgeBaseTestScenarioImpl.newReferenceById(LINKED_NAME,
			new DeferredMetaObject(REFERENCE_TYPE_NAME), !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		try {
			result.addAttribute(owned);
			result.addAttribute(linked);
			result.addAttribute(new MOAttributeImpl(A1_NAME, MOPrimitive.STRING));
		} catch (DuplicateAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
		return result;
	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseTestSetup(self, TestDeletedObjectPurge::testTypes);
	}

	private static List<TypeProvider> testTypes() {
		List<TypeProvider> result = new ArrayList<>(KnowledgeBaseTestScenarioImpl.INSTANCE.getTestTypes());
		result.add(KnowledgeBaseTestScenarioImpl.newCopyProvider(createOwnerType()));
		return result;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_log = new AssertProtocol(getClass().getName());
	}

	private DeletedObjectPurge newPurge() throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		return new DeletedObjectPurge(pool.getSQLDialect(), pool, kb().getMORepository());
	}

	private KnowledgeObject newOwner(String name, KnowledgeItem owned) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(OWNER_NAME);
		result.setAttributeValue(A1_NAME, name);
		result.setAttributeValue(OWNED_NAME, owned);
		return result;
	}

	private Report analyze(ObjectKey... seeds) throws SQLException {
		return newPurge().analyze(Arrays.asList(seeds), _log);
	}

	private Report purge(ObjectKey... seeds) throws SQLException {
		return newPurge().purge(Arrays.asList(seeds), _log);
	}

	private static Set<String> tableNames(Report report) {
		Set<String> result = new HashSet<>();
		for (TableReport table : report.getTables()) {
			result.add(table.getTableName());
		}
		return result;
	}

	/**
	 * A link is reference storage and is removed together with the object it points to, while the
	 * object at its other end stays. An optional reference to the removed object is cleared.
	 */
	public void testLinksJoinTheHull() throws Exception {
		Transaction tx = begin();
		KnowledgeObject source = newD("source");
		KnowledgeObject doomed = newD("doomed");
		KnowledgeObject holder = newD("holder");
		commit(tx);

		tx = begin();
		KnowledgeAssociation link = newAB(source, doomed);
		setReference(holder, doomed, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		commit(tx);

		ObjectKey doomedKey = doomed.tId();
		ObjectBranchId doomedId = getObjectID(doomed);
		ObjectBranchId linkId = getObjectID(link);
		ObjectBranchId sourceId = getObjectID(source);
		ObjectBranchId holderId = getObjectID(holder);

		tx = begin();
		doomed.delete();
		commit(tx);

		Report report = analyze(doomedKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertTrue("The seed is in the hull.", report.getHull().contains(doomedId));
		assertTrue("The link joins the hull: " + report, report.getHull().contains(linkId));
		assertEquals(Origin.Kind.REFERENCE, report.getOrigin(linkId).getKind());
		assertEquals(doomedId, report.getOrigin(linkId).getCause());
		assertFalse("The object at the other end of the link stays.", report.getHull().contains(sourceId));
		assertFalse("A holder of an optional reference stays.", report.getHull().contains(holderId));

		TableReport dTable = report.getTable(dbName(D_NAME));
		String optionalReference = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		assertEquals("The historic row of the holder still points to the removed object.",
			Long.valueOf(1), dTable.getClearedReferences().get(optionalReference));
	}

	/**
	 * An object with a mandatory reference to the removed object joins the hull, as long as it is
	 * deleted itself.
	 */
	public void testMandatoryRefererJoinsTheHull() throws Exception {
		Transaction tx = begin();
		KnowledgeObject target = newD("target");
		KnowledgeObject owner = newOwner("owner", target);
		commit(tx);

		ObjectKey targetKey = target.tId();
		ObjectBranchId targetId = getObjectID(target);
		ObjectBranchId ownerId = getObjectID(owner);

		tx = begin();
		// The deletion policy of the mandatory reference deletes the owner along with its target.
		target.delete();
		commit(tx);

		Report report = analyze(targetKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertTrue("The referring object joins the hull: " + report, report.getHull().contains(ownerId));
		Origin origin = report.getOrigin(ownerId);
		assertEquals(Origin.Kind.REFERENCE, origin.getKind());
		assertEquals(targetId, origin.getCause());
		assertTrue("The pulling reference is named: " + origin, origin.getReference().endsWith("." + OWNED_NAME));
	}

	/**
	 * A living object whose mandatory reference pointed to the removed object in an older revision
	 * blocks the purge.
	 */
	public void testLivingRefererBlocks() throws Exception {
		Transaction tx = begin();
		KnowledgeObject target = newD("target");
		KnowledgeObject other = newD("other");
		KnowledgeObject owner = newOwner("owner", target);
		commit(tx);

		tx = begin();
		owner.setAttributeValue(OWNED_NAME, other);
		commit(tx);

		ObjectKey targetKey = target.tId();
		ObjectBranchId targetId = getObjectID(target);
		ObjectBranchId ownerId = getObjectID(owner);

		tx = begin();
		target.delete();
		commit(tx);

		Report report = analyze(targetKey);
		assertTrue("The living referer blocks the purge: " + report, report.isBlocked());
		Blocker blocker = blocker(report, ownerId);
		assertNotNull("The living referer is reported: " + report, blocker);
		assertEquals(Origin.Kind.REFERENCE, blocker.getOrigin().getKind());
		assertEquals(targetId, blocker.getOrigin().getCause());
		assertTrue("The pulling reference is named: " + blocker,
			blocker.getOrigin().getReference().endsWith("." + OWNED_NAME));
	}

	/**
	 * A content deleted together with its container joins the hull, one that left the container
	 * earlier does not.
	 */
	public void testContentsOfTheContainer() throws Exception {
		Transaction tx = begin();
		KnowledgeObject container = newE("container");
		KnowledgeObject content = newD("content");
		KnowledgeObject removed = newD("removed");
		container.setAttributeValue(REFERENCE_DELETE_POLICY_CONTAINER_NAME, content);
		container.setAttributeValue(REFERENCE_CLEAR_POLICY_CONTAINER_NAME, removed);
		commit(tx);

		tx = begin();
		container.setAttributeValue(REFERENCE_CLEAR_POLICY_CONTAINER_NAME, null);
		commit(tx);

		tx = begin();
		removed.delete();
		commit(tx);

		ObjectKey containerKey = container.tId();
		ObjectBranchId containerId = getObjectID(container);
		ObjectBranchId contentId = getObjectID(content);
		ObjectBranchId removedId = getObjectID(removed);

		tx = begin();
		// The container reference deletes the content along with its container.
		container.delete();
		commit(tx);

		Report report = analyze(containerKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertTrue("The content joins the hull: " + report, report.getHull().contains(contentId));
		Origin origin = report.getOrigin(contentId);
		assertEquals(Origin.Kind.CONTENT, origin.getKind());
		assertEquals(containerId, origin.getCause());
		assertTrue("The container reference is named: " + origin,
			origin.getReference().endsWith("." + REFERENCE_DELETE_POLICY_CONTAINER_NAME));
		assertFalse("An object that left the container before it was deleted stays: " + report,
			report.getHull().contains(removedId));
	}

	/**
	 * All rows of an object, over all its revisions, and its dynamic values are counted.
	 */
	public void testRowsOfAllRevisions() throws Exception {
		Transaction tx = begin();
		KnowledgeObject doomed = newB("v1");
		commit(tx);

		tx = begin();
		setA1(doomed, "v2");
		doomed.setAttributeValue(FLEX_ATTR, "flex-v2");
		commit(tx);

		tx = begin();
		setA1(doomed, "v3");
		doomed.setAttributeValue(FLEX_ATTR, "flex-v3");
		commit(tx);

		ObjectKey doomedKey = doomed.tId();

		tx = begin();
		doomed.delete();
		commit(tx);

		Report report = analyze(doomedKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertEquals("One row per revision the object was changed in.", 3,
			report.getTable(dbName(B_NAME)).getErasedRows());
		assertEquals("One row per revision the dynamic value was changed in.", 2,
			report.getTable(AbstractFlexDataManager.FLEX_DATA_DB_NAME).getErasedRows());
	}

	/**
	 * A reference from a row of a branch to an object that lives on the trunk is found, although
	 * the branch does not hold the data of the type of that object.
	 */
	public void testReferenceFromABranch() throws Exception {
		Transaction tx = begin();
		KnowledgeObject target = newD("target");
		commit(tx);
		Revision branchBase = tx.getCommitRevision();

		Branch branch = HistoryUtils.createBranch(trunk(), branchBase, types(OWNER_NAME));

		ObjectBranchId ownerId;
		Branch former = HistoryUtils.setContextBranch(branch);
		try {
			tx = begin();
			KnowledgeObject owner = newOwner("branch-owner", target);
			commit(tx);
			ownerId = getObjectID(owner);

			tx = begin();
			owner.delete();
			commit(tx);
		} finally {
			HistoryUtils.setContextBranch(former);
		}
		assertEquals("The owner lives on the branch.", branch.getBranchId(), ownerId.getBranchId());
		assertEquals("The target lives on the trunk.", trunk().getBranchId(), target.getBranchContext());

		ObjectKey targetKey = target.tId();
		tx = begin();
		target.delete();
		commit(tx);

		Report report = analyze(targetKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertTrue("The reference from the branch row is found: " + report, report.getHull().contains(ownerId));
	}

	/**
	 * Seeding an object that is not deleted blocks the purge.
	 */
	public void testLivingSeedBlocks() throws Exception {
		Transaction tx = begin();
		KnowledgeObject alive = newD("alive");
		commit(tx);

		Report report = analyze(alive.tId());
		assertTrue("A living seed blocks the purge: " + report, report.isBlocked());
		assertEquals(1, report.getBlockers().size());
		assertEquals(getObjectID(alive), report.getBlockers().get(0).getIdentity());
		assertEquals(Origin.Kind.SEED, report.getBlockers().get(0).getOrigin().getKind());
	}

	/**
	 * An object that is not stored in an item table cannot be purged.
	 */
	public void testSeedWithoutItemTable() throws Exception {
		ObjectKey key = KBUtils.createObjectKey(trunk().getBranchId(), Revision.CURRENT_REV,
			BasicTypes.getBranchType(kb()), LongID.valueOf(1));
		try {
			analyze(key);
			fail("A type without an item table must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/**
	 * The analysis only reads: the database is the same afterwards and a second run reports the
	 * same.
	 */
	public void testAnalysisChangesNothing() throws Exception {
		Transaction tx = begin();
		KnowledgeObject source = newD("source");
		KnowledgeObject doomed = newD("doomed");
		KnowledgeObject holder = newD("holder");
		commit(tx);

		tx = begin();
		newAB(source, doomed);
		setReference(holder, doomed, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		commit(tx);

		ObjectKey doomedKey = doomed.tId();

		tx = begin();
		doomed.delete();
		commit(tx);

		long rowsBefore = countRows(type(D_NAME)) + countRows(type(AB_NAME));
		Report first = analyze(doomedKey);
		Report second = analyze(doomedKey);
		assertEquals("The analysis does not change the database.", rowsBefore,
			countRows(type(D_NAME)) + countRows(type(AB_NAME)));
		assertEquals("The hull is the same.", new HashSet<>(first.getHull()), new HashSet<>(second.getHull()));
		assertEquals("The counts are the same.", first.getErasedRows(), second.getErasedRows());
		assertEquals("The counts are the same.", first.getClearedValues(), second.getClearedValues());
		assertFalse("Something must be removed: " + first, first.isEmpty());
	}

	/**
	 * A complete run on the link scenario: the removed object, its link and its dynamic values are
	 * gone, everything else is untouched and the reference that pointed to the removed object reads
	 * nothing.
	 */
	public void testPurgeLinkScenario() throws Exception {
		Transaction tx = begin();
		KnowledgeObject source = newD("source");
		KnowledgeObject doomed = newD("doomed");
		KnowledgeObject holder = newD("holder");
		KnowledgeObject unrelated = newB("unrelated-v1");
		commit(tx);
		Revision created = tx.getCommitRevision();

		tx = begin();
		KnowledgeAssociation link = newAB(source, doomed);
		setReference(holder, doomed, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		doomed.setAttributeValue(FLEX_ATTR, "doomed-flex");
		setA1(unrelated, "unrelated-v2");
		commit(tx);
		Revision referred = tx.getCommitRevision();

		tx = begin();
		setA1(unrelated, "unrelated-v3");
		commit(tx);

		ObjectKey doomedKey = doomed.tId();
		ObjectKey holderKey = holder.tId();
		ObjectKey sourceKey = source.tId();
		ObjectKey unrelatedKey = unrelated.tId();
		TLID doomedId = doomed.getObjectName();
		TLID linkId = link.getObjectName();
		TLID holderId = holder.getObjectName();
		TLID sourceId = source.getObjectName();
		TLID unrelatedId = unrelated.getObjectName();

		tx = begin();
		doomed.delete();
		commit(tx);

		long holderRows = countRowsOf(type(D_NAME), holderId);
		long sourceRows = countRowsOf(type(D_NAME), sourceId);
		long unrelatedRows = countRowsOf(type(B_NAME), unrelatedId);
		String optionalReference = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		assertEquals("The historic row of the holder points to the removed object.", 1,
			countReferencesTo(type(D_NAME), optionalReference, doomedId));

		Report report = purge(doomedKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertFalse(report.isDryRun());
		assertEquals("The reference of the holder was cleared.", Long.valueOf(1),
			report.getTable(dbName(D_NAME)).getClearedReferences().get(optionalReference));
		assertEquals("No row points to the removed object any more.", 0,
			countReferencesTo(type(D_NAME), optionalReference, doomedId));

		assertEquals("No row of the removed object is left.", 0, countRowsOf(type(D_NAME), doomedId));
		assertEquals("No row of its link is left.", 0, countRowsOf(type(AB_NAME), linkId));
		assertEquals("No dynamic value of the removed object is left.", 0, countFlexRowsOf(doomedId));
		assertEquals("The holder keeps its rows.", holderRows, countRowsOf(type(D_NAME), holderId));
		assertEquals("The other end of the link keeps its rows.", sourceRows, countRowsOf(type(D_NAME), sourceId));
		assertEquals("An unrelated object keeps its rows.", unrelatedRows, countRowsOf(type(B_NAME), unrelatedId));

		KBTestUtils.clearCache(kb());

		KnowledgeItem reloadedHolder = kb().resolveObjectKey(holderKey);
		assertEquals("The current state of the holder is unchanged.", "holder",
			reloadedHolder.getAttributeValue(A1_NAME));
		assertNull("The reference to the removed object answers nothing.",
			getReference(inRevision(referred, reloadedHolder), MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL));

		KnowledgeItem reloadedSource = kb().resolveObjectKey(sourceKey);
		assertEquals("source", reloadedSource.getAttributeValue(A1_NAME));
		assertEquals("source", inRevision(referred, reloadedSource).getAttributeValue(A1_NAME));

		KnowledgeItem reloadedUnrelated = kb().resolveObjectKey(unrelatedKey);
		assertEquals("unrelated-v3", reloadedUnrelated.getAttributeValue(A1_NAME));
		assertEquals("unrelated-v2", inRevision(referred, reloadedUnrelated).getAttributeValue(A1_NAME));
		assertEquals("unrelated-v1", inRevision(created, reloadedUnrelated).getAttributeValue(A1_NAME));
	}

	/**
	 * The dry run reports exactly what the run does.
	 */
	public void testDryRunPredictsThePurge() throws Exception {
		Transaction tx = begin();
		KnowledgeObject source = newD("source");
		KnowledgeObject doomed = newD("doomed");
		KnowledgeObject holder = newD("holder");
		commit(tx);

		tx = begin();
		newAB(source, doomed);
		setReference(holder, doomed, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		doomed.setAttributeValue(FLEX_ATTR, "doomed-flex");
		commit(tx);

		ObjectKey doomedKey = doomed.tId();

		tx = begin();
		doomed.delete();
		commit(tx);

		Report analysis = analyze(doomedKey);
		Report purged = purge(doomedKey);
		assertTrue(analysis.isDryRun());
		assertFalse("Something must be removed: " + purged, purged.isEmpty());
		assertEquals("The dry run predicts the erased rows.", analysis.getErasedRows(), purged.getErasedRows());
		assertEquals("The dry run predicts the cleared references.", analysis.getClearedValues(),
			purged.getClearedValues());
		assertEquals("The dry run predicts the tables.", tableNames(analysis), tableNames(purged));
		for (TableReport expected : analysis.getTables()) {
			TableReport actual = purged.getTable(expected.getTableName());
			assertEquals("Rows of '" + expected.getTableName() + "'.", expected.getErasedRows(),
				actual.getErasedRows());
			assertEquals("References of '" + expected.getTableName() + "'.", expected.getClearedReferences(),
				actual.getClearedReferences());
		}
	}

	/**
	 * A repeated purge of the same objects finds nothing left to do.
	 */
	public void testRepeatedPurgeIsEmpty() throws Exception {
		Transaction tx = begin();
		KnowledgeObject source = newD("source");
		KnowledgeObject doomed = newD("doomed");
		commit(tx);

		tx = begin();
		newAB(source, doomed);
		commit(tx);

		ObjectKey doomedKey = doomed.tId();

		tx = begin();
		doomed.delete();
		commit(tx);

		assertFalse(purge(doomedKey).isEmpty());
		Report second = purge(doomedKey);
		assertFalse("An object without rows blocks nothing: " + second, second.isBlocked());
		assertTrue("A second run must not change anything: " + second, second.isEmpty());
	}

	/**
	 * A living object with a mandatory reference to the seed blocks the run, which then changes
	 * nothing.
	 */
	public void testBlockedPurgeChangesNothing() throws Exception {
		Transaction tx = begin();
		KnowledgeObject target = newD("target");
		KnowledgeObject other = newD("other");
		KnowledgeObject owner = newOwner("owner", target);
		commit(tx);

		tx = begin();
		owner.setAttributeValue(OWNED_NAME, other);
		commit(tx);

		ObjectKey targetKey = target.tId();
		ObjectBranchId ownerId = getObjectID(owner);

		tx = begin();
		target.delete();
		commit(tx);

		long dRows = countRows(type(D_NAME));
		long ownerRows = countRows(type(OWNER_NAME));

		Report report = purge(targetKey);
		assertTrue("The living referer blocks the purge: " + report, report.isBlocked());
		assertNotNull(blocker(report, ownerId));
		assertTrue("A blocked run changes nothing: " + report, report.isEmpty());
		assertEquals("A blocked run changes nothing.", dRows, countRows(type(D_NAME)));
		assertEquals("A blocked run changes nothing.", ownerRows, countRows(type(OWNER_NAME)));
	}

	/**
	 * Purging an object that is not deleted changes nothing.
	 */
	public void testPurgeOfLivingSeedChangesNothing() throws Exception {
		Transaction tx = begin();
		KnowledgeObject alive = newD("alive");
		commit(tx);

		long dRows = countRows(type(D_NAME));

		Report report = purge(alive.tId());
		assertTrue("A living seed blocks the purge: " + report, report.isBlocked());
		assertTrue("A blocked run changes nothing: " + report, report.isEmpty());
		assertEquals("A blocked run changes nothing.", dRows, countRows(type(D_NAME)));
		assertEquals("alive", alive.getAttributeValue(A1_NAME));
	}

	/**
	 * Purging a container removes the contents deleted with it and keeps what left it earlier.
	 */
	public void testPurgeContents() throws Exception {
		Transaction tx = begin();
		KnowledgeObject container = newE("container");
		KnowledgeObject content = newD("content");
		KnowledgeObject removed = newD("removed");
		container.setAttributeValue(REFERENCE_DELETE_POLICY_CONTAINER_NAME, content);
		container.setAttributeValue(REFERENCE_CLEAR_POLICY_CONTAINER_NAME, removed);
		commit(tx);

		tx = begin();
		container.setAttributeValue(REFERENCE_CLEAR_POLICY_CONTAINER_NAME, null);
		commit(tx);

		tx = begin();
		removed.delete();
		commit(tx);

		ObjectKey containerKey = container.tId();
		TLID containerId = container.getObjectName();
		TLID contentId = content.getObjectName();
		TLID removedId = removed.getObjectName();
		long removedRows = countRowsOf(type(D_NAME), removedId);

		tx = begin();
		container.delete();
		commit(tx);

		Report report = purge(containerKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertEquals("No row of the container is left.", 0, countRowsOf(type(E_NAME), containerId));
		assertEquals("No row of its content is left.", 0, countRowsOf(type(D_NAME), contentId));
		assertEquals("An object that left the container before keeps its rows.", removedRows,
			countRowsOf(type(D_NAME), removedId));
		assertTrue("The object that left the container was deleted, too.", removedRows > 0);
	}

	/**
	 * Purging an object of the trunk removes the objects of a branch that were deleted with it and
	 * clears the optional references of the branch that pointed to it.
	 */
	public void testPurgeOnABranch() throws Exception {
		Transaction tx = begin();
		KnowledgeObject target = newD("target");
		KnowledgeObject keeper = newD("keeper");
		commit(tx);
		Revision branchBase = tx.getCommitRevision();

		Branch branch = HistoryUtils.createBranch(trunk(), branchBase, types(OWNER_NAME));

		TLID ownerId;
		ObjectKey observerKey;
		Branch former = HistoryUtils.setContextBranch(branch);
		try {
			tx = begin();
			KnowledgeObject owner = newOwner("branch-owner", target);
			KnowledgeObject observer = newOwner("branch-observer", keeper);
			observer.setAttributeValue(LINKED_NAME, target);
			commit(tx);
			ownerId = owner.getObjectName();
			observerKey = observer.tId();

			tx = begin();
			owner.delete();
			commit(tx);
		} finally {
			HistoryUtils.setContextBranch(former);
		}

		ObjectKey targetKey = target.tId();
		TLID targetId = target.getObjectName();

		tx = begin();
		target.delete();
		commit(tx);

		Report report = purge(targetKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertEquals("No row of the removed object is left.", 0, countRowsOf(type(D_NAME), targetId));
		assertEquals("No row of the referring object of the branch is left.", 0,
			countRowsOf(type(OWNER_NAME), ownerId));
		TableReport ownerTable = report.getTable(dbName(OWNER_NAME));
		assertEquals("The optional reference of the branch row is cleared.", Long.valueOf(1),
			ownerTable.getClearedReferences().get(LINKED_NAME));

		KBTestUtils.clearCache(kb());
		KnowledgeItem reloadedObserver = kb().resolveObjectKey(observerKey);
		assertEquals("The observer lives on the branch.", branch.getBranchId(),
			reloadedObserver.getBranchContext());
		assertNull("The reference of the branch to the removed object answers nothing.",
			reloadedObserver.getAttributeValue(LINKED_NAME));
	}

	/**
	 * Purging an object removes the deleted objects that hold a mandatory reference to it.
	 */
	public void testPurgeMandatoryReferer() throws Exception {
		Transaction tx = begin();
		KnowledgeObject target = newD("target");
		KnowledgeObject owner = newOwner("owner", target);
		commit(tx);

		ObjectKey targetKey = target.tId();
		TLID targetId = target.getObjectName();
		TLID ownerId = owner.getObjectName();

		tx = begin();
		target.delete();
		commit(tx);

		Report report = purge(targetKey);
		assertFalse("Everything in the hull is deleted: " + report, report.isBlocked());
		assertEquals("No row of the removed object is left.", 0, countRowsOf(type(D_NAME), targetId));
		assertEquals("No row of the referring object is left.", 0, countRowsOf(type(OWNER_NAME), ownerId));
	}

	private static Blocker blocker(Report report, ObjectBranchId identity) {
		for (Blocker blocker : report.getBlockers()) {
			if (blocker.getIdentity().equals(identity)) {
				return blocker;
			}
		}
		return null;
	}

	private String dbName(String typeName) {
		return ((DBTableMetaObject) type(typeName)).getDBName();
	}

	/**
	 * The number of rows the object with the given identifier has in the given table.
	 */
	private long countRowsOf(MOClass type, TLID id) throws SQLException {
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName())
			+ " WHERE " + sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " = ?";
		return querySingleLong(sql, id);
	}

	/**
	 * The number of dynamic values the object with the given identifier has.
	 */
	private long countFlexRowsOf(TLID id) throws SQLException {
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(AbstractFlexDataManager.FLEX_DATA_DB_NAME)
			+ " WHERE " + sqlDialect.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME) + " = ?";
		return querySingleLong(sql, id);
	}

	/**
	 * The number of rows of the given table whose given reference points to the given object.
	 */
	private long countReferencesTo(MOClass type, String attribute, TLID targetId) throws SQLException {
		DBAttribute column = ((MOReference) type.getAttributeOrNull(attribute)).getColumn(ReferencePart.name);
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName())
			+ " WHERE " + sqlDialect.columnRef(column.getDBName()) + " = ?";
		return querySingleLong(sql, targetId);
	}

	private long querySingleLong(String sql, TLID id) throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			IdentifierUtil.setId(statement, 1, id);
			try (ResultSet result = statement.executeQuery()) {
				assertTrue("Query without result: " + sql, result.next());
				return result.getLong(1);
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private long countRows(MOClass type) throws SQLException {
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		String sql = "SELECT count(*) FROM " + sqlDialect.tableRef(((DBTableMetaObject) type).getDBName());
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			try (ResultSet result = statement.executeQuery()) {
				assertTrue("Query without result: " + sql, result.next());
				return result.getLong(1);
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	public static Test suite() {
		return suiteNeedsBranches(TestDeletedObjectPurge.class);
	}

}

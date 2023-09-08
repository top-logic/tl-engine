/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj.*;
import static test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.event.TestChangeSet;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SA;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SB;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventKind;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.KnowledgeEventVisitor;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.AttributeRenaming;
import com.top_logic.knowledge.event.convert.AttributeValueConversion;
import com.top_logic.knowledge.event.convert.KnowledgeEventConverter;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DBTypeRepository;
import com.top_logic.knowledge.service.db2.OrderedItemEventReader;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.TLContext;

/**
 * Test case for {@link KnowledgeEvent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestKnowledgeEvent extends AbstractDBKnowledgeBaseMigrationTest implements
		KnowledgeBaseMigrationTestScenario {

	private static final class ParseIntMapping implements Mapping<Object, Integer> {
		
		/**
		 * Singleton {@link ParseIntMapping} instance.
		 */
		public static final ParseIntMapping INSTANCE = new ParseIntMapping();

		private ParseIntMapping() {
			// Singleton constructor.
		}
		
		@Override
		public Integer map(Object value) {
			return Integer.parseInt((String) value);
		}
	}

	/**
	 * Regression test for Ticket #5921.
	 * 
	 * <p>
	 * Test with types that have different sort order, when compared in binary or natural order.
	 * </p>
	 */
	public void testTypeOrder() {
		// Must be smaller than com.top_logic.knowledge.service.db2.RevisionImpl.TypeXref._treshold.
		int revisionCnt = 2;
		for (int n = 0; n < revisionCnt; n++) {
			Transaction tx = kb().beginTransaction();
			{
				// Note: This test depends on the sort order of the type names "C" and "bc", which
				// is ["C", "bc"] in binary order and ["bc", "C"] in natuaral order.
				BObj b1 = BObj.newBObj("b1-" + n);
				b1.setF1("flex-value-of-b1-" + n);
				CObj c1 = CObj.newCObj("c1-" + n);
				c1.setF1("flex-value-of-c1-" + n);
				newBC(b1.tHandle(), c1.tHandle());
				tx.commit();
			}
		}
		doMigration();
	}
	
	/**
	 * Tests that the migration of a stabilized current reference works, i.e. it tests that a
	 * historic reference which was filled with a current object and stabilized by this operation is
	 * migrated correctly.
	 */
	public void testMigrationStabilizedCurrentReference() throws DataObjectException {
		final KnowledgeObject referencingObject;
		final KnowledgeObject referencedObject;
		{
			final Transaction tx = begin();
			referencingObject = newE("e1");
			referencedObject = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
			referencedObject.setAttributeValue(A1_NAME, "reference");
			tx.commit();
		}
		{
			final Transaction tx = begin();
			setReference(referencingObject, referencedObject, KnowledgeBaseTestScenarioConstants.MONOMORPHIC,
				HistoryType.HISTORIC, KnowledgeBaseTestScenarioConstants.BRANCH_GLOBAL);
			tx.commit();
		}

		doMigration();

		final ObjectKey referencingKey = referencingObject.tId();
		final ObjectKey referencedKey = referencedObject.tId();

		final HistoryManager node2HistoryManager = kbNode2().getHistoryManager();
		ReaderConfig configuration =
			ReaderConfigBuilder.createConfig(HistoryUtils.getInitialRevision(node2HistoryManager),
				HistoryUtils.getLastRevision(node2HistoryManager));
		long updateEventRevision = -1L;
		try (final ChangeSetReader node2Reader = kbNode2().getChangeSetReader(configuration)) {
			boolean updateFound = false;
			while (true) {
				final ChangeSet cs = node2Reader.read();
				if (cs == null) {
					break;
				}
				List<ItemUpdate> updates = cs.getUpdates();
				if (updates.isEmpty()) {
					continue;
				}
				updateEventRevision = updates.get(0).getRevision();
				if (updateFound) {
					fail("Unexpected evt: " + " expected only one event setting reference");
				}
				updateFound = true;
				break;
			}
			if (!updateFound) {
				fail("No update found which sets the reference");
			}
		}
		
		final KnowledgeItem referencingNode2 = kbNode2().resolveObjectKey(referencingKey);
		final KnowledgeItem reference = getReference(referencingNode2, KnowledgeBaseTestScenarioConstants.MONOMORPHIC,
				HistoryType.HISTORIC, KnowledgeBaseTestScenarioConstants.BRANCH_GLOBAL);
		assertNotNull("Migrated KO has no reference", reference);
		final ObjectKey referencedNode2Key = reference.tId();

		assertEquals(referencedKey.getObjectName(), referencedNode2Key.getObjectName());
		assertEquals(referencedKey.getObjectType().getName(), referencedNode2Key.getObjectType().getName());
		assertEquals(referencedKey.getBranchContext(), referencedNode2Key.getBranchContext());

		assertSame(updateEventRevision, referencedNode2Key.getHistoryContext());
	}

	public void testModifyInitialRevision() throws DataObjectException {
		try (EventWriter writer = KBUtils.getReplayWriter(kbNode2())) {
			write(writer, new ChangeSet(1).setCommit(new CommitEvent(1, "author", System.currentTimeMillis(), "migration")));
			
			Revision r1 = kbNode2().getRevision(1L);
			
			assertEquals("author", r1.getAuthor());
			assertEquals("migration", r1.getLog());
		}
	}

	public void testCheckTargetObjectAlive() {
		try (EventWriter replay = KBUtils.getReplayWriter(kbNode2())) {
			final TLID a1 = kbNode2().createID();
			write(replay,
				new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)),
				new ChangeSet(3).add(updateRow(3, a1, "r1")).setCommit(commit(3)),
				new ChangeSet(4).add(delete(4, a1)).setCommit(commit(4)));
			
			try {
				write(replay,
					new ChangeSet(5).add(updateFlex(5, a1, "f1")));
				fail("Must not be able to update deleted object.");
			} catch (RuntimeException ex) {
				// Expected.
			}
			/* The replay of an commit event at this place is actually a hack: when replaying the
			 * update of the deleted item a transaction has begun. This must be completed, otherwise
			 * the closing of the replay writer fails due to beginning of a transaction without
			 * final commit. */
			write(replay, new ChangeSet(5).setCommit(commit(5)));
		}
	}

	private CommitEvent commit(long rev) {
		return newCommit(rev, TestKnowledgeEvent.class);
	}

	/**
	 * Creates a new {@link CommitEvent} for the given revision and the given test class.
	 * 
	 * @param rev
	 *        Value of {@link CommitEvent#getRevision()}.
	 * @param testClass
	 *        Uses to synthesise {@link CommitEvent#getAuthor()} and {@link CommitEvent#getLog()}.
	 */
	public static CommitEvent newCommit(long rev, Class<?> testClass) {
		String log = "Commit created for test class " + testClass.getName();
		return new CommitEvent(rev, testClass.getSimpleName(), System.currentTimeMillis(), log);
	}

	public void testOutOfOrderEvent() {
		try (EventWriter replay = KBUtils.getReplayWriter(kbNode2())) {
			final TLID a1 = kbNode2().createID();
			write(replay,
				new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)),
				new ChangeSet(3).add(updateRow(3, a1, "r1")).setCommit(commit(3)),
				new ChangeSet(4).add(updateRow(4, a1, "r2")).setCommit(commit(4)));
			
			try {
				write(replay,
					new ChangeSet(4).add(updateFlex(4, a1, "f1")));
				fail("Must not accept out of order events.");
			} catch (RuntimeException ex) {
				// Expected.
			}
		}
	}

	private void write(EventWriter replay, ChangeSet... sets) {
		for (ChangeSet cs : sets) {
			replay.write(cs);
		}
	}

	private ItemDeletion delete(int rev, TLID id) {
		return new ItemDeletion(rev, new ObjectBranchId(1, type(B_NAME), id));
	}

	private ItemUpdate updateFlex(int rev, TLID id, String value) {
		final ItemUpdate result = new ItemUpdate(rev, new ObjectBranchId(1, type(B_NAME), id), false);
		result.setValue(BObj.F3_NAME, null, value);
		return result;
	}

	private ItemUpdate updateRow(int rev, TLID id, String value) {
		final ItemUpdate result = new ItemUpdate(rev, new ObjectBranchId(1, type(B_NAME), id), false);
		result.setValue(A1_NAME, null, value);
		return result;
	}

	private ObjectCreation create(int rev, TLID id) {
		final ObjectCreation result =
			new ObjectCreation(rev, new ObjectBranchId(1, type(B_NAME), id));
		result.setValue(A1_NAME, null, "a1-initial");
		return result;
	}

	public void testBranchCreation() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		try (EventWriter replay = KBUtils.getReplayWriter(kbNode2())) {
			final TLID a1 = kbNode2().createID();
			write(replay,
				new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)));

			try {
				final Revision baseRev = HistoryUtils.getLastRevision(kbNode2().getHistoryManager());
				final Branch baseBranch = kbNode2().getTrunk();
				kbNode2().createBranch(baseBranch, baseRev, null);
			} catch (KnowledgeBaseRuntimeException ex) {
				/* Exptected due to the known bug in ticket #5503: Duplicate revision entry when
				 * creating branch. */
				return;
			}
		}
		fail("Test should fail due to the known bug in ticket #5503: Duplicate revision entry when creating branch.");
	}

	public void testComplexBranchFilter() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Revision r1;
		{
			Transaction tx = begin();
			BObj.newBObj("b1");
			CObj.newCObj("c1");
			newD("d1");
			commit(tx);

			r1 = tx.getCommitRevision();
		}
		final Branch branch1 = HistoryUtils.createBranch(trunk(), r1);
		final Branch branch2 = HistoryUtils.createBranch(trunk(), r1);
		final Revision r2 = lastRevision();
		final Branch branch11 = HistoryUtils.createBranch(branch1, r2);
		final Branch branch12 = HistoryUtils.createBranch(branch1, r2);

		final BObj b_branch1;
		final CObj c_branch2;
		final CObj c_branch11;
		final BObj b_branch12;
		{
			Transaction tx = begin();
			b_branch1 = newBObj(kb(), branch1, "b_branch1", null);
			c_branch2 = newCObj(kb(), branch2, "b_branch2", null); //no reported
			c_branch11 = newCObj(kb(), branch11, "b_branch11", null);
			b_branch12 = newBObj(kb(), branch12, "b_branch12", null); //no reported
			commit(tx);
		}

		String flexAttributeName = "flexAttribute1";
		{
			Transaction tx = begin();
			b_branch1.setA2("a2");
			c_branch2.setA2("a2"); // no reported
			c_branch11.setValue(flexAttributeName, "f3");
			b_branch12.setValue(flexAttributeName, "f3"); // no reported
			commit(tx);
		}

		{
			Transaction tx = begin();
			b_branch1.setValue(flexAttributeName, "f3_2");
			c_branch2.setValue(flexAttributeName, "f3_2"); // no reported
			c_branch11.setA2("a2_2");
			b_branch12.setA2("a2_2"); // no reported
			commit(tx);
		}

		try (final ChangeSetReader reader =
			kb().getChangeSetReader(
				ReaderConfigBuilder.createComplexConfig(kb().getRevision(Revision.FIRST_REV), Revision.CURRENT, null,
					set(branch11, branch1), false, false, /* order */ null))) {
		{
			ChangeSet cs = reader.read();
			assertEquals("Creations on branch branch2 and branch12 are not reported", 2, cs.getCreations().size());
			ObjectCreation creationBranch1 = TestChangeSet.index(cs.getCreations()).get(getObjectID(b_branch1));
			assertNotNull(creationBranch1);
			assertNull(creationBranch1.getValues().get(A2_NAME));
			assertNull(creationBranch1.getValues().get(flexAttributeName));
			assertEquals("b_branch1", creationBranch1.getValues().get(A1_NAME));
			ObjectCreation creationBranch11 = TestChangeSet.index(cs.getCreations()).get(getObjectID(c_branch11));
			assertNotNull(creationBranch11);
			assertNull(creationBranch11.getValues().get(A2_NAME));
			assertNull(creationBranch11.getValues().get(flexAttributeName));
			assertEquals("b_branch11", creationBranch11.getValues().get(A1_NAME));
		}
		{
			ChangeSet cs = reader.read();
			assertEquals("Updates on branch branch2 and branch12 are not reported", 2, cs.getUpdates().size());
			ItemUpdate updateBranch1 = TestChangeSet.index(cs.getUpdates()).get(getObjectID(b_branch1));
			assertNotNull(updateBranch1);
			assertNull(updateBranch1.getValues().get(flexAttributeName));
			assertEquals("a2", updateBranch1.getValues().get(A2_NAME));
			ItemUpdate updateBranch11 = TestChangeSet.index(cs.getUpdates()).get(getObjectID(c_branch11));
			assertNotNull(updateBranch11);
			assertNull(updateBranch11.getValues().get(A2_NAME));
			assertEquals("f3", updateBranch11.getValues().get(flexAttributeName));
		}
		{
			ChangeSet cs = reader.read();
			assertEquals("Updates on branch branch2 and branch12 are not reported", 2, cs.getUpdates().size());
			ItemUpdate updateBranch1 = TestChangeSet.index(cs.getUpdates()).get(getObjectID(b_branch1));
			assertNotNull(updateBranch1);
			assertNull(updateBranch1.getValues().get(A2_NAME));
			assertEquals("f3_2", updateBranch1.getValues().get(flexAttributeName));
			ItemUpdate updateBranch11 = TestChangeSet.index(cs.getUpdates()).get(getObjectID(c_branch11));
			assertNotNull(updateBranch11);
			assertNull(updateBranch11.getValues().get(flexAttributeName));
			assertEquals("a2_2", updateBranch11.getValues().get(A2_NAME));
		}
		assertNull(reader.read());
		}
	}

	/**
	 * Tests type filtering in
	 * {@link KnowledgeBase#getChangeSetReader(com.top_logic.knowledge.service.ReaderConfig)}.
	 */
	public void testComplexTypeFilter() {
		final Revision r1;
		BObj b1;
		CObj c1;
		KnowledgeObject d;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1"); // not reported
			c1 = CObj.newCObj("c1");
			d = newD("d1");
			commit(tx);

			r1 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			setA2(d, "d1_a2");
			b1.setF3("b1_f3"); // not reported
			c1.setA2("c1a2"); c1.setF3("c1_f3"); 
			commit(tx);
		}

		try (final ChangeSetReader reader =
			kb().getChangeSetReader(
				ReaderConfigBuilder.createComplexConfig(kb().getRevision(Revision.FIRST_REV), Revision.CURRENT,
					set(C_NAME, D_NAME),
					null, false, false, /* order */ null))) {
		{
			ChangeSet cs = reader.read();
			assertEquals("Changes on type " + B_NAME + " are not reported", 2, cs.getCreations().size());
			ObjectCreation c1Creation = TestChangeSet.index(cs.getCreations()).get(getObjectID(c1));
			assertNotNull(c1Creation);
			assertEquals("c1", c1Creation.getValues().get(A1_NAME));
			ObjectCreation dCreation = TestChangeSet.index(cs.getCreations()).get(getObjectID(d));
			assertNotNull(dCreation);
			assertEquals("d1", dCreation.getValues().get(A1_NAME));
		}
		{
			ChangeSet cs = reader.read();
			assertEquals("Changes on type " + B_NAME + " are not reported", 2, cs.getUpdates().size());
			ItemUpdate c1Update = TestChangeSet.index(cs.getUpdates()).get(getObjectID(c1));
			assertNotNull(c1Update);
			assertEquals("c1a2", c1Update.getValues().get(A2_NAME));
			assertEquals("c1_f3", c1Update.getValues().get(F3_NAME));
			ItemUpdate dUpdate = TestChangeSet.index(cs.getUpdates()).get(getObjectID(d));
			assertNotNull(dUpdate);
			assertEquals("d1_a2", dUpdate.getValues().get(A2_NAME));
		}
		assertNull(reader.read());
		}
	}

	public void testSmallChunkSize() {
		try {
			inThread(new Execution() {

				@Override
				public void run() throws Exception {
					int chunkSize = kb().getChunkSize();
					kb().setChunkSize(3);
					try {
						long startRev = kb().getLastRevision();

						// Construct 3 B's
						Transaction tx = kb().beginTransaction();
						newB("b1");
						tx.commit();

						tx = kb().beginTransaction();
						newB("b2");
						tx.commit();

						tx = kb().beginTransaction();
						newB("b3");
						tx.commit();

						// Construct 2 C's
						tx = kb().beginTransaction();
						KnowledgeObject newC1 = newC("c1");
						tx.commit();

						tx = kb().beginTransaction();
						KnowledgeObject newC2 = newC("c2");
						tx.commit();

						// Get all C's
						ReaderConfig config =
							ReaderConfigBuilder.createConfig(kb().getRevision(startRev + 1), Revision.CURRENT, C_NAME,
								null,
								false, false);
						try (ChangeSetReader reader = kb().getChangeSetReader(config)) {
							assertEquals(getObjectID(newC1), reader.read().getCreations().get(0).getObjectId());
							assertEquals(getObjectID(newC2), reader.read().getCreations().get(0).getObjectId());
							assertNull(reader.read());
						}
					} finally {
						kb().setChunkSize(chunkSize);
					}
				}
			}, 60000);
		} catch (AssertionError ex) {
			fail("Ticket #2922: infinite loop in " + OrderedItemEventReader.class.getSimpleName(), ex);
		}
	}
	
	/**
	 * Tests branch and type event filtering in
	 * {@link KnowledgeBase#getChangeSetReader(com.top_logic.knowledge.service.ReaderConfig)}.
	 */
	public void testBranchAndTypeEventFilter() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final FilterSetup setup = new FilterSetup();

		final ReaderConfig config =
			ReaderConfigBuilder.createConfig(kb().getRevision(Revision.FIRST_REV), Revision.CURRENT, C_NAME,
				setup.branch2, false, false);
		try (final ChangeSetReader reader = kb().getChangeSetReader(config)) {
			ChangeSet cs;
			long branch2 = setup.branch2.getBranchId();
			while ((cs = reader.read()) != null) {
				for (ObjectCreation creation : cs.getCreations()) {
					assertEquals("Ticket #4739: Event occured on wrong branch", branch2, creation.getOwnerBranch());
					assertEquals("Ticket #4739: Event of wrong type", C_NAME, creation.getObjectType().getName());
				}
				for (ItemUpdate update : cs.getUpdates()) {
					assertEquals("Ticket #4739: Event occured on wrong branch", branch2, update.getOwnerBranch());
					assertEquals("Ticket #4739: Event of wrong type", C_NAME, update.getObjectType().getName());
				}
				for (ItemDeletion deletion : cs.getDeletions()) {
					assertEquals("Ticket #4739: Event occured on wrong branch", branch2, deletion.getOwnerBranch());
					assertEquals("Ticket #4739: Event of wrong type", C_NAME, deletion.getObjectType().getName());
				}
			}
		}
	}

	/**
	 * Tests type filtering in
	 * {@link KnowledgeBase#getChangeSetReader(com.top_logic.knowledge.service.ReaderConfig)}.
	 */
	public void testTypeFilter() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		new FilterSetup();
		
		try (final ChangeSetReader reader =
			kb().getChangeSetReader(
				ReaderConfigBuilder.createConfig(kb().getRevision(Revision.FIRST_REV), Revision.CURRENT, B_NAME, null,
					true, true))) {
			EventCounter<?, ?> tester = new EventCounter<>(reader, null) {
				@Override
				public Object visitCreateObject(ObjectCreation event, Object arg) {
					if (C_NAME.equals(((ItemCreation) event).getObjectType().getName())) {
						fail("Ticket #2910: Unexpected event: " + event);
					}
					return super.visitCreateObject(event, arg);
				}
			};

			assertTrue("No create events at all.", tester.creations > 0);
			assertTrue("No delete events at all.", tester.deletions > 0);
			assertTrue("No update events at all.", tester.updates > 0);
			assertTrue("No branch events at all.", tester.branches > 0);
			assertTrue("No commit events at all.", tester.commits > 0);
		}
	}

	/**
	 * Tests commit and branch event filtering in
	 * {@link KnowledgeBase#getChangeSetReader(com.top_logic.knowledge.service.ReaderConfig)}.
	 */
	public void testBranchAndCommitEventFilter() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		new FilterSetup();
		
		ReaderConfig configuration = ReaderConfigBuilder.createConfig(kb().getRevision(Revision.FIRST_REV), Revision.CURRENT, null, null,
			false, false);
		try (final ChangeSetReader reader = kb().getChangeSetReader(configuration)) {
			EventCounter<?, ?> tester = new EventCounter<>(reader, null);

			assertTrue("No create events at all.", tester.creations > 0);
			assertTrue("No delete events at all.", tester.deletions > 0);
			assertTrue("No update events at all.", tester.updates > 0);
			assertTrue("Ticket #2910: Branch events were not filtered.", tester.branches == 0);
			assertTrue("Ticket #2910: Commit events were not filtered.", tester.commits == 0);
		}
	}
	
	/**
	 * Tests restricting to events of a certain branch in
	 * {@link KnowledgeBase#getChangeSetReader(com.top_logic.knowledge.service.ReaderConfig)}.
	 */
	public void testBranchFilter() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		FilterSetup setup = new FilterSetup();
		
		ReaderConfig configuration = ReaderConfigBuilder.createConfig(kb().getRevision(Revision.FIRST_REV), Revision.CURRENT, null,
			setup.branch2, true, true);
		try (final ChangeSetReader reader = kb().getChangeSetReader(configuration)) {
			final long branch2Id = setup.branch2.getBranchId();
			EventCounter<?, ?> tester = new EventCounter<>(reader, null) {
				@Override
				protected Object visitEvent(KnowledgeEvent event, Object arg) {
					if ((event instanceof ItemEvent) && ((ItemEvent) event).getOwnerBranch() != branch2Id) {
						fail("Ticket #2910: Unexpected event on unrelated branch: " + event);
					}
					return super.visitEvent(event, arg);
				}
			};

			assertTrue("No create events at all.", tester.creations > 0);
			assertTrue("No delete events at all.", tester.deletions > 0);
			assertTrue("No update events at all.", tester.updates > 0);
			assertTrue("No branch events at all.", tester.branches > 0);
			assertTrue("No commit events at all.", tester.commits > 0);
		}
	}

	/**
	 * Scenario with four objects on different branches (with all types of events).
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	class FilterSetup {
		BObj b1;
		CObj c1;
		
		Branch branch2;
		BObj b2;
		CObj c2;

		public FilterSetup() {
			final Revision r1;
			{
				Transaction tx = begin();
				b1 = BObj.newBObj("b1");
				c1 = CObj.newCObj("c1");
				b1.addBC(c1);
				commit(tx);
				
				r1 = tx.getCommitRevision();
			}
			
			{
				Transaction tx = begin();
				b1.setA2("b1a2");
				c1.setA2("b1a2");
				commit(tx);
			}
			
			branch2 = kb().createBranch(kb().getTrunk(), r1, null);
			
			HistoryUtils.setContextBranch(branch2);
			try {
				{
					Transaction tx = begin();
					b2 = BObj.newBObj("b2");
					c2 = CObj.newCObj("c2");
					commit(tx);
					
					tx.getCommitRevision();
				}
			} finally {
				HistoryUtils.setContextBranch(kb().getTrunk());
			}
			
			{
				Transaction tx = begin();
				
				b1.setA2("b1a2new");
				
				b2.setA2("b2a2");
				c2.setA2("b2a2");
				
				b2.tDelete();
				
				commit(tx);
			}
		}
	}

	/**
	 * {@link KnowledgeEventVisitor} that counts events by type.
	 */
	class EventCounter<R, A> implements KnowledgeEventVisitor<R, A> {
		int commits;
		int branches;
		int creations;
		int deletions;
		int updates;

		public EventCounter(ChangeSetReader reader, A arg) {
			ChangeSet cs;
			while ((cs = reader.read()) != null) {
				for (KnowledgeEvent evt : cs.getBranchEvents()) {
					evt.visit(this, arg);
				}
				for (KnowledgeEvent evt : cs.getCreations()) {
					evt.visit(this, arg);
				}
				for (KnowledgeEvent evt : cs.getUpdates()) {
					evt.visit(this, arg);
				}
				for (KnowledgeEvent evt : cs.getDeletions()) {
					evt.visit(this, arg);
				}
				if (cs.getCommit() != null) {
					cs.getCommit().visit(this, arg);
				}
			}
		}

		@Override
		public R visitBranch(BranchEvent event, A arg) {
			branches++;
			return visitEvent(event, arg);
		}

		@Override
		public R visitCommit(CommitEvent event, A arg) {
			commits++;
			return visitEvent(event, arg);
		}

		@Override
		public R visitCreateObject(ObjectCreation event, A arg) {
			creations++;
			return visitEvent(event, arg);
		}

		@Override
		public R visitDelete(ItemDeletion event, A arg) {
			deletions++;
			return null;
		}

		@Override
		public R visitUpdate(ItemUpdate event, A arg) {
			updates++;
			return visitEvent(event, arg);
		}
		
		protected R visitEvent(KnowledgeEvent event, A arg) {
			return null;
		}

	}
	
	/**
	 * Tests type filtering in
	 * {@link KnowledgeBase#getChangeSetReader(com.top_logic.knowledge.service.ReaderConfig)}.
	 */
	public void testCommitFilter() {
		long startRev = kb().getLastRevision();
		{
			Transaction tx = begin();
			BObj.newBObj("b1");
			CObj.newCObj("c1");
			commit(tx);
		}
		
		ReaderConfig configuration =
			ReaderConfigBuilder.createConfig(kb().getRevision(startRev + 1), Revision.CURRENT, B_NAME, null,
			true, true);
		try (ChangeSetReader reader = kb().getChangeSetReader(configuration)) {
			ChangeSet cs = reader.read();
			assertEquals(1, cs.getCreations().size());
			assertNotEquals("Ticket #2910: Event of unexpected type.", C_NAME, cs.getCreations().get(0).getObjectType()
				.getName());
			assertNull(reader.read());
		}
	}
	
	/**
	 * Test that replay sends regular events that keep KB and application caches
	 * in synch.
	 * 
	 * @see "Ticket #2765"
	 */
	public void testReplayEvents() {
		assertFalse("Precondition: Not a row attribute: " + BObj.F2_NAME, 
			((MOClass) kbNode2().getMORepository().getMetaObject(B_NAME)).hasAttribute(BObj.F2_NAME));

		// create b1 -> b2
		final BObj b1;
		final BObj b2;
		final Revision r1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setF2("f2");
			b2 = BObj.newBObj("b2");
			
			b1.addAB(b2);
			commit(tx);
			
			r1 = tx.getCommitRevision();
		}

		assertEquals(set(b2), b1.getAB());

		// create b1 -> b3
		final BObj b3;
		final Revision r2;
		{
			Transaction tx = begin();
			b3 = BObj.newBObj("b3");
			b1.addAB(b3);
			commit(tx);
			
			r2 = tx.getCommitRevision();
		}
		
		assertEquals(set(b2, b3), b1.getAB());
		
		// delete b2
		final Revision r3;
		{
			Transaction tx = begin();
			b2.tDelete();
			commit(tx);
			
			r3 = tx.getCommitRevision();
		}
		
		assertEquals(set(b3), b1.getAB());

		// Migrate r1
		doMigration(r1, r1);
		assertTrueCopy(r1, r1);

		BObj b1Node2 =
			(BObj) WrapperFactory.getWrapper(kbNode2().getKnowledgeObject(B_NAME,
				KBUtils.getWrappedObjectName(b1)));
		BObj b2Node2 =
			(BObj) WrapperFactory.getWrapper(kbNode2().getKnowledgeObject(B_NAME,
				KBUtils.getWrappedObjectName(b2)));
		assertNotNull(b1Node2);
		assertNotNull(b2Node2);
		
		assertEquals(set(b2Node2), b1Node2.getAB());
		
		// Migrate r2
		doMigration(r2, r2);
		assertTrueCopy(r2, r2);
		
		BObj b3Node2 =
			(BObj) WrapperFactory.getWrapper(
				kbNode2().getKnowledgeObject(B_NAME, KBUtils.getWrappedObjectName(b3)));
		assertNotNull(b3Node2);
		
		assertEquals("Ticket #2765: Association cache not updated.", set(b2Node2, b3Node2), b1Node2.getAB());
		
		// Migrate r3
		doMigration(r3, r3);
		assertTrueCopy(r3, r3);
		
		assertEquals("Ticket #2765: Association cache not updated.", set(b3Node2), b1Node2.getAB());
	}

	/**
	 * Tests that branch creation is possible in a migrated {@link KnowledgeBase}.
	 * 
	 * @see "Ticket #2778"
	 */
	public void testBranchSequenceUpdate() throws UnknownTypeException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch branch1 = kb().createBranch(HistoryUtils.getTrunk(), HistoryUtils.getLastRevision(), null);
		assertNotNull(branch1);
		
		doMigration();
		
		assertTrueCopy();
		
		Branch branch2 = kbNode2().createBranch(kbNode2().getBranch(branch1.getBranchId()), kbNode2().getRevision(kbNode2().getLastRevision()), null);
		assertNotNull(branch2);
	}

	/**
	 * Test of replay with different {@link AbstractBoundWrapper} types that are stored in the same
	 * table.
	 * 
	 * @see "Ticket #2763"
	 */
	public void testSubtypeReplay() throws DataObjectException {
		final SA sa1;
		final SB sb1;
		{
			Transaction tx = begin();
			sa1 = SA.newSAObj("sa1");
			sa1.setF1("sa1.f1");
			sb1 = SB.newSBObj("sb1");
			sb1.setF1(42);
			commit(tx);
		}
		doMigration();
		
		SA sa1Node2 =
			(SA) WrapperFactory.getWrapper(KBUtils.getWrappedObjectName(sa1), S_NAME, kbNode2());
		SB sb1Node2 =
			(SB) WrapperFactory.getWrapper(KBUtils.getWrappedObjectName(sb1), S_NAME, kbNode2());
		
		assertNotNull(sa1Node2);
		assertNotNull(sb1Node2);
		
		assertNotSame(sa1, sa1Node2);
		assertNotSame(sb1, sb1Node2);
		
		assertEquals("sa1", sa1Node2.getS1());
		assertEquals("sa1.f1", sa1Node2.getF1());
		
		assertEquals("sb1", sb1Node2.getS1());
		assertEquals(42, sb1Node2.getF1());
	}
	
	public void testDiffEvents() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final BObj b1;
		{
			Transaction tx = begin(Messages.SETUP_TEST);
			b1 = newBObj("b1");
			commit(tx);
		}

		final Revision r0;
		{
			Transaction tx = begin(Messages.SOME_MODIFICATION);
			b1.setB2("3");
			commit(tx);
			r0 = tx.getCommitRevision();
		}

		final Branch trunk = kb().getTrunk();
		final Branch branch = kb().createBranch(trunk, r0, null);
		final Wrapper b1_branch = WrapperHistoryUtils.getWrapper(branch, b1);

		final KnowledgeObject c1;
		final ObjectBranchId c1Id;
		final Revision r1;
		{
			Transaction tx = begin();
			c1 = newC("c1");
			c1Id = getObjectID(c1);
			b1_branch.setValue(B1_NAME, "5");
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		final Revision r2;
		{
			Transaction tx = begin();
			b1_branch.setValue(B1_NAME, "42");
			b1.setValue(B1_NAME, "42_b");
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		try (final ChangeSetReader emptyReader = kb().getDiffReader(r1, branch, r1, branch, false)) {
			assertNull(emptyReader.read());
		}

		try (final ChangeSetReader backwardReader = kb().getDiffReader(r2, branch, r1, branch, false)) {
			ChangeSet cs = backwardReader.read();
			List<ItemUpdate> updates = cs.getUpdates();
			assertEquals(1, updates.size());
			final ItemUpdate soleEvent = updates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, "5"), soleEvent.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, "42"), soleEvent.getOldValues());

			assertNull(backwardReader.read());
		}

		try (final ChangeSetReader forwardReader = kb().getDiffReader(r1, branch, r2, branch, false)) {
			ChangeSet cs = forwardReader.read();
			List<ItemUpdate> updates = cs.getUpdates();
			assertEquals(1, updates.size());
			final ItemUpdate soleEvent = updates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, "42"), soleEvent.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, "5"), soleEvent.getOldValues());

			assertNull(forwardReader.read());
		}

		try (final ChangeSetReader reader2 = kb().getDiffReader(r0, trunk, r2, branch, true)) {
			ChangeSet firstCS = reader2.read();
			List<ItemUpdate> firstUpdates = firstCS.getUpdates();
			assertEquals(1, firstUpdates.size());
			final ItemUpdate firstUpdate = firstUpdates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, "5"), firstUpdate.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, null), firstUpdate.getOldValues());

			ChangeSet secondCS = reader2.read();
			List<ItemUpdate> secondUpdates = secondCS.getUpdates();
			assertEquals(1, secondUpdates.size());
			final ItemUpdate secondUpdate = secondUpdates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, "42"), secondUpdate.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, "5"), secondUpdate.getOldValues());

			assertNull(reader2.read());
		}

		try (final ChangeSetReader reader2WithoutDetails = kb().getDiffReader(r0, trunk, r2, branch, false)) {
			ChangeSet cs = reader2WithoutDetails.read();
			List<ItemUpdate> updates = cs.getUpdates();
			assertEquals(1, updates.size());
			final ItemUpdate update = updates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, "42"), update.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, null), update.getOldValues());
			
			assertNull(reader2WithoutDetails.read());
		}
		
		try (final ChangeSetReader reader3 = kb().getDiffReader(r2, branch, r0, trunk, true)) {
			ChangeSet firstCS = reader3.read();
			List<ItemUpdate> firstUpdates = firstCS.getUpdates();
			assertEquals(1, firstUpdates.size());
			final ItemUpdate firstUpdate = firstUpdates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, "5"), firstUpdate.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, "42"), firstUpdate.getOldValues());

			ChangeSet secondCS = reader3.read();
			List<ItemUpdate> secondUpdates = secondCS.getUpdates();
			assertEquals(1, secondUpdates.size());
			final ItemUpdate secondUpdate = secondUpdates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, null), secondUpdate.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, "5"), secondUpdate.getOldValues());

			assertNull(reader3.read());
		}

		try (final ChangeSetReader reader3WithoutDetails = kb().getDiffReader(r2, branch, r0, trunk, false)) {
			ChangeSet cs = reader3WithoutDetails.read();
			List<ItemUpdate> updates = cs.getUpdates();
			assertEquals(1, updates.size());
			final ItemUpdate update = updates.get(0);
			assertEquals(Collections.singletonMap(B1_NAME, null), update.getValues());
			assertEquals(Collections.singletonMap(B1_NAME, "42"), update.getOldValues());
			
			assertNull(reader3WithoutDetails.read());
		}
		
		KBUtils.revert(kb(), r1, trunk, r1, branch);
		assertEquals("5", b1.getB1());
		final KnowledgeObject removedC1 = kb().getKnowledgeObject(c1Id.getObjectType().getName(), c1Id.getObjectName());
		assertNull(removedC1);
	}
	
	public void testDiffReplay() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final BObj b1;
		final BObj b2;
		final CObj c1;
		final long r0;
		{
			Transaction tx = begin();
			b1 = newBObj("b1");
			b2 = newBObj("b2");
			c1 = newCObj("c1");
			commit(tx);
			r0 = tx.getCommitRevision().getCommitNumber();
		}
		
		final Branch branch_1 = kb().createBranch(kb().getTrunk(), kb().getRevision(r0), types(B_NAME, C_NAME));
		final Wrapper b1_branch_1 = WrapperHistoryUtils.getWrapper(branch_1, b1);
		final Wrapper b2_branch_1 = WrapperHistoryUtils.getWrapper(branch_1, b2);
		final Wrapper c1_branch_1 = WrapperHistoryUtils.getWrapper(branch_1, c1);
		
		{
			Transaction tx = begin();
			b1.setB1("b1.b1");
			b2.setB1("b2.b1");
			commit(tx);
		}
		
		final long r4;
		{
			Transaction tx = begin();
			b1_branch_1.setValue(AObj.F2_NAME, "b1_branch1.f2");
			c1_branch_1.tDelete();
			commit(tx);
			r4 = tx.getCommitRevision().getCommitNumber();
		}
		final Branch branch_1_1 = kb().createBranch(branch_1, kb().getRevision(r4), types(B_NAME, C_NAME));
		final Wrapper b1_branch_1_1 = WrapperHistoryUtils.getWrapper(branch_1_1, b1);
		final Wrapper b2_branch_1_1 = WrapperHistoryUtils.getWrapper(branch_1_1, b2);
		
		{
			Transaction tx = begin();
			b1_branch_1_1.tDelete();
			commit(tx);
		}
		
		final long r2;
		{
			Transaction tx = begin();
			c1.setF1("c1.f1");
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
		
		final Branch branch_2 = kb().createBranch(kb().getTrunk(), kb().getRevision(r2), types(B_NAME, C_NAME));
		final Wrapper c1_branch_2 = WrapperHistoryUtils.getWrapper(branch_2, c1);
		final Wrapper b2_branch_2 = WrapperHistoryUtils.getWrapper(branch_2, b2);
		
		{
			Transaction tx = begin();
			c1_branch_2.setValue(AObj.F2_NAME, "c1_branch2.f2");
			b2_branch_2.setValue(B1_NAME, null);
			commit(tx);
		}
		
		kb().setContextBranch(branch_2);
		
		{
			Transaction tx = begin();
			final CObj c2_branch_2 = newCObj("c2");
			c2_branch_2.setF2("c2_branch_2.f2");
			kb().setContextBranch(kb().getTrunk());
			commit(tx);
		}
		
		final long stopRev;
		{
			Transaction tx = begin();
			c1_branch_2.setValue(AObj.F2_NAME, "c1_branch2.f2_new");
			commit(tx);
			stopRev = tx.getCommitRevision().getCommitNumber();
		}
		
		kb().setContextBranch(branch_1_1);
		
		final CObj newC1_branch_1_1;
		{
			Transaction tx = begin();
			newC1_branch_1_1 = newCObj("c1");
			kb().setContextBranch(kb().getTrunk());
			commit(tx);
		}
		
		final long startRev;
		{
			Transaction tx = begin();
			b2_branch_1_1.setValue(B2_NAME, "b2_branch3.b2");
			commit(tx);
			startRev = tx.getCommitRevision().getCommitNumber();
		}
		KBUtils.revert(kb(), kb().getRevision(startRev), branch_1_1, kb().getRevision(stopRev), branch_2);
		
		assertBranchEquality(kb(),branch_1_1, branch_2);
		
	}

	public void testBranchMigration() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		BObj b1;
		BObj b2;
		BObj b3_branch1;

		CObj c1;
		CObj c2;
		CObj c3_branch1;

		Wrapper b1_branch1;
		Wrapper c1_branch1;
		Branch trunk = kb().getTrunk();

		long r1;
		{
			Transaction tx = begin(Messages.SETUP_TEST);
			b1 = newBObj("b1");
			c1 = newCObj("c1");
			b1.setA2("b1.a2");
			c1.setA2("c1.a2");
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}

		Branch branch1 = kb().createBranch(trunk, kb().getRevision(r1), null);

		b1_branch1 = WrapperHistoryUtils.getWrapper(branch1, b1);
		c1_branch1 = WrapperHistoryUtils.getWrapper(branch1, c1);
		assertNotNull(b1_branch1);
		assertNotNull(c1_branch1);

		{
			Transaction tx = begin(Messages.MULTI_BRANCH_MODIFICATION);
			b2 = newBObj("b2");
			c2 = newCObj("c2");

			kb().setContextBranch(branch1);

			b3_branch1 = newBObj("b3");
			c3_branch1 = newCObj("c3");

			kb().setContextBranch(trunk);
			commit(tx);
		}

		assertEquals(branch1.getBranchId(), b3_branch1.tHandle().getBranchContext());
		assertEquals(branch1.getBranchId(), c3_branch1.tHandle().getBranchContext());

		long r2 = kb().getLastRevision();
		Branch branch2 = kb().createBranch(trunk, kb().getRevision(r2), null);
		Branch branch3 = kb().createBranch(branch1, kb().getRevision(r2), null);

		{
			Transaction tx = begin(); // Unnamed transaction.
			b2.setA2("b2.a2");
			c2.setA2("c2.a2");

			b3_branch1.setA2("b3.a2");
			c3_branch1.setA2("c3.a2");
			commit(tx);
		}

		doMigration();
		
		assertTrueCopy();
	}

	private void assertTrueCopy(final Revision firstRevision, final Revision lastRevision) {
		Set<ChangeSet> expectedEvents =
			getAllEvents(kb(), kb().getRevision(firstRevision.getCommitNumber()),
				lastRevision.isCurrent() ? Revision.CURRENT : kb().getRevision(lastRevision.getCommitNumber()));
		Set<ChangeSet> migratedEvents =
			getAllEvents(kbNode2(), kbNode2().getRevision(firstRevision.getCommitNumber()),
				lastRevision.isCurrent() ? Revision.CURRENT : kbNode2().getRevision(lastRevision.getCommitNumber()));
		// translate to correct KnowledgeBase
		migratedEvents = toSet(translate(migratedEvents, kb()));

		BasicTestCase.assertEquals(expectedEvents, migratedEvents);
	}

	/**
	 * Translates the given events to be adequate for the given {@link KnowledgeBase}.
	 */
	private List<ChangeSet> translate(Iterable<ChangeSet> changeSets, DBKnowledgeBase kb) {
		CachingEventWriter out = new CachingEventWriter();
		EventWriter converter = KnowledgeEventConverter.createEventConverter(kb.getMORepository(), out);
		for (ChangeSet cs : changeSets) {
			converter.write(cs);
		}
		return out.getAllEvents();
	}

	private void assertTrueCopy() {
		Set<ChangeSet> expectedEvents =
			getAllEvents(kb(), kb().getHistoryManager().getRevision(Revision.FIRST_REV), Revision.CURRENT);
		Set<ChangeSet> migratedEvents =
			getAllEvents(kbNode2(), kbNode2().getHistoryManager().getRevision(Revision.FIRST_REV), Revision.CURRENT);
		// translate to correct KnowledgeBase
		migratedEvents = toSet(translate(migratedEvents, kb()));
		
		BasicTestCase.assertEquals(expectedEvents, migratedEvents);
	}

	private HashSet<ChangeSet> getAllEvents(KnowledgeBase kb, Revision firstRevision, Revision stopRevision) {
		try (ChangeSetReader reader = getEventsInRange(kb, firstRevision, stopRevision)) {
			return getAllEvents(reader);
		}
	}

	/**
	 * Reads all events from the given reader and returns them as set.
	 * 
	 * <p>
	 * Note: The given reader is closed after all events have been read.
	 * </p>
	 * 
	 * @param <T>
	 *        The event type.
	 * @param reader
	 *        The reader to read events from.
	 * @return All events read from the given reader.
	 */
	static <T> HashSet<T> getAllEvents(EventReader<? extends T> reader) {
		try {
			HashSet<T> events = new HashSet<>();
			T event;
			while ((event = reader.readEvent()) != null) {
				events.add(event);
			}
			return events;
		} finally {
			reader.close();
		}
	}

	static HashSet<ChangeSet> getAllEvents(ChangeSetReader reader) {
		HashSet<ChangeSet> events = new HashSet<>();
		ChangeSet cs;
		while ((cs = reader.read()) != null) {
			events.add(cs);
		}
		return events;
	}

	/**
	 * Tests the renaming and value conversion of attribute values during migration.
	 * 
	 * <ul>
	 * <li>Rename {@link KnowledgeBaseTestScenarioConstants#B2_NAME} to
	 * {@link KnowledgeBaseMigrationTestScenario#T_B2_NAME}.</li>
	 * <li>Parse integer in {@link KnowledgeBaseTestScenarioConstants#C1_NAME} and store int value
	 * in {@link KnowledgeBaseMigrationTestScenario#T_C3_NAME}.</li>
	 * <li>Parse integer in {@link KnowledgeBaseTestScenarioConstants#C2_NAME} and store int value
	 * attribute {@link KnowledgeBaseMigrationTestScenario#T_C2_NAME} with modified type.</li>
	 * </ul>
	 */
	public void testAttributeConversion() {
		BObj b1;
		CObj c1;
		CObj c2;
		{
			Transaction tx = begin();
			b1 = newBObj("b1");
			b1.setB2("v2");

			c1 = newCObj("c1");
			c1.setC1("17");

			c2 = newCObj("c2");
			c2.setC2("42");

			commit(tx);
		}

		{
			Transaction tx = begin();
			b1.setB2("new v2");
			commit(tx);
		}

		{
			Transaction tx = begin();
			b1.setB2(null);
			commit(tx);
		}

		{
			Transaction tx = begin();
			b1.setB2("current v2");
			commit(tx);
		}

		{
			Transaction tx = begin();
			c1.setC1("117");
			c2.setC2("142");
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			c1.setC1("217");
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			c2.setC2("242");
			commit(tx);
		}

		doMigration(Arrays.asList(
			AttributeRenaming.newAttributeRenaming(B_NAME, B2_NAME, T_B2_NAME),
			new AttributeValueConversion(C_NAME, C2_NAME, ParseIntMapping.INSTANCE),
			new AttributeValueConversion(C_NAME, C1_NAME, T_C3_NAME, ParseIntMapping.INSTANCE)
			));

		List<?> tb1 =
			kbNode2().search(
				queryUnresolved(filter(anyOf(T_B_NAME), eqBinary(attribute(T_B_NAME, T_B2_NAME), literal("current v2")))));
		assertEquals(1, tb1.size());
		assertEquals(b1.tHandle().getObjectName(), ((KnowledgeItem) tb1.get(0)).getObjectName());
		
		List<?> tc1 =
			kbNode2().search(
				queryUnresolved(filter(anyOf(T_C_NAME), eqBinary(attribute(T_C_NAME, T_C3_NAME), literal(217)))));
		assertEquals(1, tc1.size());
		assertEquals(c1.tHandle().getObjectName(), ((KnowledgeItem) tc1.get(0)).getObjectName());
		
		List<?> tc2 =
			kbNode2().search(
				queryUnresolved(filter(anyOf(T_C_NAME), eqBinary(attribute(T_C_NAME, T_C2_NAME), literal(242)))));
		assertEquals(1, tc2.size());
		assertEquals(c2.tHandle().getObjectName(), ((KnowledgeItem) tc2.get(0)).getObjectName());
	}

	public void testFlexAttributeMigration() {
		BObj b1;
		BObj b2;
		BObj b3;
		{
			Transaction tx = begin(Messages.SETUP_TEST);
			b1 = newBObj("b1");
			b2 = newBObj("b2");
			b3 = newBObj("b3");

			b1.setF1("flex f1");
			b2.setF2("flex f2");
			b3.setF3("row f3");
			commit(tx);
		}

		{
			Transaction tx = begin(Messages.SOME_MODIFICATION);
			b1.setF1("new flex f1");
			commit(tx);
		}

		{
			Transaction tx = begin();
			b2.setF2("new flex f2");
			commit(tx);
		}

		{
			Transaction tx = begin();
			b3.setF3("new row f3");
			commit(tx);
		}

		{
			Transaction tx = begin();
			b1.setF1(null);
			commit(tx);
		}

		{
			Transaction tx = begin();
			b2.setF2(null);
			commit(tx);
		}

		{
			Transaction tx = begin();
			b3.setF3(null);
			commit(tx);
		}

		{
			Transaction tx = begin();
			b1.setF1("current flex f1");
			commit(tx);
		}

		{
			Transaction tx = begin();
			b2.setF2("current flex f2");
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			b3.setF3("current row f3");
			commit(tx);
		}
		
		doMigration();
		
		List<?> tb1 =
			kbNode2().search(
				queryUnresolved(filter(anyOf(T_B_NAME), eqBinary(attribute(T_A_NAME, T_F1_NAME), literal("current flex f1")))));
		assertEquals(1, tb1.size());
		assertEquals(b1.tHandle().getObjectName(), ((KnowledgeItem) tb1.get(0)).getObjectName());
		
		List<?> tb2 =
			kbNode2().search(
				queryUnresolved(filter(anyOf(T_B_NAME),
				eqBinary(flex(MOPrimitive.STRING, AObj.F2_NAME), literal("current flex f2")))));
		assertEquals(1, tb2.size());
		assertEquals(b2.tHandle().getObjectName(), ((KnowledgeItem) tb2.get(0)).getObjectName());
		
		List<?> tb3 =
			kbNode2().search(
				queryUnresolved(filter(anyOf(T_B_NAME), eqBinary(flex(MOPrimitive.STRING, F3_NAME), literal("current row f3")))));
		assertEquals(1, tb3.size());
		assertEquals(b3.tHandle().getObjectName(), ((KnowledgeItem) tb3.get(0)).getObjectName());
		
		assertTrueCopy();
	}
	
	
	/**
	 * Tests creation of {@link ItemCreation} events.
	 */
	public void testCreateEvent() throws DataObjectException {
		final KnowledgeObject b1;
		Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			assertNotNull(b1);
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r1)) {
			ChangeSet cs = r.read();
			ItemCreation e1 = cs.getCreations().iterator().next();
			CommitEvent e2 = cs.getCommit();
			assertNull(r.read());
			
			assertEquals("b1", e1.getValues().get(A1_NAME));
			assertEquals(TLContext.TRUNK_ID, e1.getOwnerBranch());
			assertEquals(r1.getCommitNumber(), e2.getRevision());
		}
	}
	
	/**
	 * Tests creation of {@link ItemUpdate} events.
	 */
	public void testUpdateEvent() throws DataObjectException {
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			setA1(b1, "b1.a1.new");
			setA2(b1, "b1.a2.new");
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r1)) {
			ChangeSet cs = r.read();
			assertEquals(1, cs.getUpdates().size());
			ItemUpdate e1 = cs.getUpdates().get(0);
			CommitEvent e2 = cs.getCommit();
			assertNull(r.read());
			
			assertEquals("b1.a1.new", e1.getValues().get(A1_NAME));
			assertEquals("b1.a2.new", e1.getValues().get(A2_NAME));
			
			assertEquals("b1", e1.getOldValues().get(A1_NAME));
			assertNull(e1.getOldValues().get(A2_NAME));
			
			assertEquals(r1.getCommitNumber(), e2.getRevision());
		}
	}
	
	/**
	 * Tests creation of {@link ItemUpdate} events for flex attributes.
	 */
	public void testFlexUpdateEvent() throws DataObjectException {
		final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setF1("b1.f1");
			commit(tx);
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			b1.setF1("b1.f1.new");
			b1.setF2("b1.f2.new");
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r1)) {
			ChangeSet cs = r.read();
			ItemUpdate e1 = cs.getUpdates().get(0);
			CommitEvent e2 = cs.getCommit();
			assertNull(r.read());
			
			assertEquals("b1.f1.new", e1.getValues().get(BObj.F1_NAME));
			assertEquals("b1.f2.new", e1.getValues().get(BObj.F2_NAME));
			
			assertEquals("b1.f1", e1.getOldValues().get(BObj.F1_NAME));
			assertNull(e1.getOldValues().get(BObj.F2_NAME));
			
			assertEquals(r1.getCommitNumber(), e2.getRevision());
		}
	}
	
	/**
	 * Tests creation of {@link ItemDeletion} events.
	 */
	public void testDeleteEvent() throws DataObjectException {
		final KnowledgeObject d1;
		long d1CreateRevision;
		{
			Transaction tx = begin();
			d1 = newD("d1");
			commit(tx);
			d1CreateRevision = tx.getCommitRevision().getCommitNumber();
		}
		
		KnowledgeObject reference;
		{
			Transaction tx = begin();
			setA2(d1, "d1.a2");
			reference = newD("reference");
			d1.setAttributeValue(REFERENCE_MONO_CUR_GLOBAL_NAME, reference);
			commit(tx);
		}
		
		final Revision r1;
		{
			Transaction tx = begin();
			d1.delete();
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r1)) {
			ChangeSet cs = r.read();
			ItemDeletion e1 = cs.getDeletions().get(0);
			Object expectedOldValues = new MapBuilder<String, Object>()
				.put(A1_NAME, "d1")
				.put(A2_NAME, "d1.a2")
				.put(REFERENCE_MONO_CUR_GLOBAL_NAME, reference.tId())
				.put(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, d1CreateRevision)
				.toMap();
			assertEquals(expectedOldValues, e1.getValues());
			CommitEvent e2 = cs.getCommit();
			assertNull(r.read());
			
			assertEquals(d1.getObjectName(), e1.getObjectName());
			assertEquals(r1.getCommitNumber(), e2.getRevision());
		}
	}

	/**
	 * Tests that association deletions are reported before object deletions and
	 * that association creations are reported after object creations.
	 */
	public void testAssociationEventOrder() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		final KnowledgeAssociation b1b2;
		final KnowledgeAssociation b1b3;
		final KnowledgeAssociation b2b3;
		Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b2");
			b1b2 = newAB(b1, b2);
			b1b3 = newBC(b1, b3);
			b2b3 = newBC(b2, b3);
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			b1.delete();
			b2.delete();
			commit(tx);
		}
		
		// Association is automatically deleted.
		assertFalse(b1b2.isAlive());
		assertFalse(b1b3.isAlive());
		assertFalse(b2b3.isAlive());
		
		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r1)) {
			ChangeSet cs1 = r.read();
			BasicTestCase.assertEquals(
				set(getObjectID(b1), getObjectID(b2), getObjectID(b3), getObjectID(b1b2), getObjectID(b1b3),
					getObjectID(b2b3)),
				TestChangeSet.index(cs1.getCreations()).keySet());
			
			ObjectCreation b1b2Creation = TestChangeSet.index(cs1.getCreations()).get(getObjectID(b1b2));
			ObjectKey sourceKey =
				(ObjectKey) b1b2Creation.getValues().get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
			assertEquals(b1.getObjectName(), sourceKey.getObjectName());
			ObjectKey destKey = (ObjectKey) b1b2Creation.getValues().get(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
			assertEquals(b2.getObjectName(), destKey.getObjectName());
			assertNotNull(cs1.getCommit());

			ChangeSet cs2 = r.read();
			assertEquals(5, cs2.getDeletions().size());
			assertNotNull(cs2.getCommit());

			assertNull(r.read());
		}
	}
	
	/**
	 * Tests creation of {@link BranchEvent}s.
	 */
	public void testBranchEvent() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final Revision r0;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			commit(tx);
			r0 = tx.getCommitRevision();
		}

		Branch branch0 = kb().createBranch(kb().getTrunk(), r0, null);
		long r3 = kb().getLastRevision();
		
		final long r1;
		{
			Transaction tx = begin();
			setA2(b1, "b1.a2");
			commit(tx);
			r1 = kb().getLastRevision();
		}
		
		final long r4;
		{
			Transaction tx = begin();
			setA2(b1, "b1.a2.new");
			commit(tx);
			r4 = kb().getLastRevision();
		}
		
		Branch branch1 = kb().createBranch(kb().getTrunk(), kb().getRevision(r1), null);
		long r2 = kb().getLastRevision();
		
		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r0)) {
			ChangeSet cs1 = r.read();
			assertEquals(2, cs1.getCreations().size());
			assertNotNull(cs1.getCommit());
			assertEquals(r0.getCommitNumber(), cs1.getRevision());
			
			ChangeSet cs2 = r.read();
			BranchEvent e18 = cs2.getBranchEvents().get(0);
			CommitEvent e19 = cs2.getCommit();
			
			assertEquals(branch0.getBranchId(), e18.getBranchId());
			assertEquals(r3, e19.getRevision());
			
			ChangeSet cs3 = r.read();
			ItemUpdate e4 = cs3.getUpdates().get(0);
			CommitEvent e5 = cs3.getCommit();
			
			assertEquals(b1.getObjectName(), e4.getObjectName());
			assertEquals(r1, e5.getRevision());
			
			ChangeSet cs4 = r.read();
			ItemUpdate e6 = cs4.getUpdates().get(0);
			CommitEvent e7 = cs4.getCommit();
			assertEquals(r4, e7.getRevision());

			assertEquals(b1.getObjectName(), e6.getObjectName());
			
			ChangeSet cs5 = r.read();
			BranchEvent e8 = cs5.getBranchEvents().get(0);
			CommitEvent e9 = cs5.getCommit();

			assertNull(r.read());
			
			assertEquals(branch1.getBranchId(), e8.getBranchId());
			assertEquals(branch1.getBaseBranch().getBranchId(), e8.getBaseBranchId());
			assertEquals(branch1.getBaseRevision().getCommitNumber(), e8.getBaseRevisionNumber());
			assertEquals(r2, e8.getRevision());
			assertEquals(r2, e9.getRevision());
		}
	}
	
	/**
	 * Tests that the given revision range is respected in event queries.
	 */
	public void testEventRevisionRange() throws DataObjectException {
		final KnowledgeObject d0; // Deleted just before the requested revision.
		final KnowledgeObject b0; // Deleted after the requested revision.
		final KnowledgeObject b1; // Updated in the requested revision.
		{
			Transaction tx = begin();
			d0 = newD("d0");
			
			b0 = newB("b0");
			b1 = newB("b1");
			commit(tx);
		}

		final KnowledgeObject b2; // Deleted in the requested revision.
		final KnowledgeObject d2; // Updated after the requested revision.
		final KnowledgeObject d3; // Never touched and stays alive until current.
		{
			Transaction tx = begin();
			b2 = newB("b2");
			d0.delete();
			d2 = newD("d2");
			d3 = newD("d3");
			commit(tx);
		}
		
		final KnowledgeObject b3; // Created in the requested revision.
		final long r1;
		{
			Transaction tx = begin();
			b2.delete();
			b3 = newB("b3");
			setA2(b1, "b1.a2");
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		final KnowledgeObject d1; // Created just after the requested revision and deleted afterwards.
		final KnowledgeObject d4; // Created just after the requested revision and stays alive until current.
		{
			Transaction tx = begin();
			d1 = newD("d1");
			d4 = newD("d4");
			b0.delete();
			b1.delete();
			b3.delete();
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			d1.delete();
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			setA2(d2, "d2.a2");
			commit(tx);
		}
		
		assertTrue(d3.isAlive());
		assertTrue(d4.isAlive());
		
		// Check, whether only the deletion, update and creation are reported.
		try (ChangeSetReader r =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(r1), kb().getRevision(r1)))) {
			ChangeSet cs = r.read();
			assertEquals(1, cs.getCreations().size());
			assertEquals(getObjectID(b3), cs.getCreations().get(0).getObjectId());
			assertEquals(1, cs.getDeletions().size());
			assertEquals(getObjectID(b2), cs.getDeletions().get(0).getObjectId());
			assertEquals(1, cs.getUpdates().size());
			assertEquals(getObjectID(b1), cs.getUpdates().get(0).getObjectId());
			assertNotNull(cs.getCommit());

			assertNull("Unexpected events.", r.read());
		}
	}
	
	/**
	 * Tests creation of various {@link KnowledgeEvent}s and their contents.
	 */
	public void testEvents() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeAssociation b1b2;
		final Revision r1;
		{
			// Make changes
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b1b2 = newAB(b1, b2);
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		final long r2;
		{
			Transaction tx = begin();
			setA2(b1, "change");
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}

		final long r3;
		{
			Transaction tx = begin();
			b2.delete();
			commit(tx);
			r3 = tx.getCommitRevision().getCommitNumber();
		}
		
		Branch branch1 = kb().createBranch(kb().getTrunk(), kb().getRevision(r2), null);
		long r4 = kb().getLastRevision();

		// Check, whether events represent the changes above.
		try (ChangeSetReader r = getEventsSince(kb(), r1)) {
			// Check events for object creations.
			ChangeSet cs1 = r.read();
			BasicTestCase.assertEquals(
				set(getObjectID(b1), getObjectID(b2), getObjectID(b1b2)),
				TestChangeSet.index(cs1.getCreations()).keySet());
			CommitEvent e4 = cs1.getCommit();
			assertEquals(r1.getCommitNumber(), e4.getRevision());
			
			// Check events for object update.
			ChangeSet cs2 = r.read();
			ItemUpdate e5 = cs2.getUpdates().get(0);
			CommitEvent e6 = cs2.getCommit();

			assertNotNull(e5);
			assertEquals(EventKind.update, e5.getKind());
			assertEquals(B_NAME, e5.getObjectType().getName());
			assertEquals(kb().getTrunk().getBranchId(), e5.getOwnerBranch());
			assertEquals(b1.getObjectName(), e5.getObjectName());
			assertEquals(r2, e5.getRevision());
			assertEquals(Collections.singletonMap(A2_NAME, "change"), e5.getValues());
			assertNull(e5.getOldValues().get(A2_NAME));
			assertEquals(r2, e6.getRevision());
			
			// Check events for object deletion.
			ChangeSet cs3 = r.read();
			CommitEvent e9 = cs3.getCommit();
			BasicTestCase.assertEquals(
				set(getObjectID(b2), getObjectID(b1b2)),
				set(cs3.getDeletions().get(0).getObjectId(), cs3.getDeletions().get(1).getObjectId()));
			assertEquals(r3, e9.getRevision());

			// Check branch event.
			ChangeSet cs4 = r.read();
			BranchEvent e10 = cs4.getBranchEvents().get(0);
			
			assertEquals(r4, e10.getRevision());
			assertEquals(r2, e10.getBaseRevisionNumber());
			assertEquals(branch1.getBranchId(), e10.getBranchId());
			assertEquals(branch1.getBaseBranch().getBranchId(), e10.getBaseBranchId());
			
			DBTypeRepository typeRepository = (DBTypeRepository) kb().getMORepository();
			// All Object types are branched, Associations are branches as instance therefore they
			// do not occur in branch event
			List<? extends MetaObject> branchedTypes =
				typeRepository.getConcreteSubtypes(type(BasicTypes.OBJECT_TYPE_NAME));
			for (MetaObject type : branchedTypes) {
				assertTrue("Branched type " + type.getName() + " is not contained in bracnh event.", e10
					.getBranchedTypeNames().contains(type.getName()));
			}
		}
	}

	public void testMigration() throws DataObjectException {
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject b3;

		KnowledgeAssociation b1b2;
		KnowledgeAssociation b2b3;
		{
			Transaction tx = begin(Messages.SETUP_TEST);
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			commit(tx);
		}

		{
			Transaction tx = begin(Messages.SOME_MODIFICATION);
			setB1(b1, "v1");
			setB1(b2, "v2");
			setB1(b3, "v3");
			
			b1b2 = newBC(b1, b2);
			b2b3 = newBC(b2, b3);
			commit(tx);
		}

		{
			Transaction tx = begin();
			setBC2(b1b2, "v4");
			setBC2(b2b3, "v5");
			commit(tx);
		}

		{
			Transaction tx = begin();
			setBC2(b2b3, "new v5");
			commit(tx);
		}

		{
			Transaction tx = begin();
			setBC2(b1b2, "new v4");
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			b3.delete();
			commit(tx);
		}
		
		
		doMigration();
		
		KnowledgeObject tb1 = kbNode2().getKnowledgeObject(T_B_NAME, KBUtils.getObjectName(b1));
		assertEquals(b1.getAttributeValue(B1_NAME), tb1.getAttributeValue(T_B1_NAME));
		
		KnowledgeObject tb2 = kbNode2().getKnowledgeObject(T_B_NAME, KBUtils.getObjectName(b2));
		assertEquals(b1.getAttributeValue(B2_NAME), tb1.getAttributeValue(T_B2_NAME));
		
		KnowledgeAssociation tb1b2 =
			kbNode2().getKnowledgeAssociation(T_BC_NAME, KBUtils.getObjectName(b1b2));
		assertNotNull(tb1b2);
		assertEquals(tb2, tb1b2.getDestinationObject());
		assertEquals(tb1, tb1b2.getSourceObject());
		assertEquals("new v4", tb1b2.getAttributeValue(T_BC2_NAME));
		
		KnowledgeAssociation tb2b3 =
			kbNode2().getKnowledgeAssociation(T_BC_NAME, KBUtils.getObjectName(b2b3));
		assertNull(tb2b3);
		
		KnowledgeObject tb3 = kbNode2().getKnowledgeObject(T_B_NAME, KBUtils.getObjectName(b3));
		assertNull(tb3);
		
		assertTrueCopy();
	}

	/**
	 * Test that events are correctly synthesized, even if objects with large
	 * identifier ranges (that only differ by a single character that is either
	 * a digit or a number) take part in the changes.
	 * 
	 * <p>
	 * By default Oracle sorts in natural language order, which corrupts the
	 * event reading logic, that depends on {@link String#compareTo(String)}
	 * order of identifiers.
	 * </p>
	 * 
	 * @see "Ticket #2918"
	 */
	public void testTicket2918() {
		int objCnt = 32;
		BObj[] objects = new BObj[objCnt];
		
		{
			Transaction tx = begin();
			for (int n = 0; n < objCnt; n++) {
				objects[n] = BObj.newBObj("b" + n);
			}
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			for (int n = 0; n < objCnt; n++) {
				objects[n].setA2("b" + n + ".a2");
				objects[n].setF2("b" + n + ".f2");
			}
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			for (int n = 0; n < objCnt; n++) {
				objects[n].setA2(null);
				objects[n].setF2(null);
			}
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			for (int n = 0; n < objCnt; n++) {
				objects[n].tDelete();
			}
			commit(tx);
		}
		
		doMigration();
		assertTrueCopy();
	}

	/**
	 * Tests that a {@link KBUtils#getReplayWriter(KnowledgeBase) replay writer} can be closed, also
	 * if no event were written.
	 */
	public void testClosingEmptyWriter() {
		try {
			KBUtils.getReplayWriter(kbNode2()).close();
		} catch (Exception ex) {
			throw fail("Ticket #18448: Closing ReplayWriter without modification must not fail.", ex);
		}
	}

	/**
	 * A cumulative {@link Test} for all Tests in {@link TestKnowledgeEvent}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestKnowledgeEvent.class, "");
		}
		return suite(TestKnowledgeEvent.class);
	}

}

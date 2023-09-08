/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event.convert;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseMigrationTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseMigrationTestScenario;
import test.com.top_logic.knowledge.service.db2.TestKnowledgeEvent;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.RevisionModificator;
import com.top_logic.knowledge.event.convert.RevisionModificator.Modus;
import com.top_logic.knowledge.event.convert.RevisionModificator.RevisionAttributes;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemUtil;
import com.top_logic.knowledge.service.db2.StaticKnowledgeObjectFactory;

/**
 * The class {@link TestRevisionModificator} tests the {@link RevisionModificator}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestRevisionModificator extends AbstractDBKnowledgeBaseMigrationTest implements
		KnowledgeBaseMigrationTestScenario {

	private static final String REVISION_ATTR = "revision";

	private static final String WITH_REVISION_ATTR = "withRevisionAttr";

	private static final RevisionAttributes REV_ATTRIBUTES = new RevisionAttributes() {

		@Override
		public Set<String> map(String input) {
			if (WITH_REVISION_ATTR.equals(input)) {
				return Collections.singleton(REVISION_ATTR);
			}
			return null;
		}
	};

	KnowledgeObject createRevAttrObject(KnowledgeBase kb, TLID objectName) throws DataObjectException {
		return kb.createKnowledgeObject(objectName, WITH_REVISION_ATTR);
	}

	Long setRevAttr(KnowledgeObject ko, Long rev) throws DataObjectException {
		return (Long) ko.setAttributeValue(REVISION_ATTR, rev);
	}

	Long getRevAttr(KnowledgeObject ko) throws DataObjectException {
		return (Long) ko.getAttributeValue(REVISION_ATTR);
	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		final TypeProvider typeWithRevAttr = new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				final MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(WITH_REVISION_ATTR);
				MOKnowledgeItemUtil.setImplementationFactory(type, StaticKnowledgeObjectFactory.INSTANCE);
				type.setSuperclass(BasicTypes.getObjectType(typeRepository));
				try {
					type.addAttribute(new MOAttributeImpl(REVISION_ATTR, MOPrimitive.LONG, false));
				} catch (DuplicateAttributeException ex) {
					throw new UnreachableAssertion("Single attribute " + REVISION_ATTR);
				}
				try {
					typeRepository.addMetaObject(type);
				} catch (DuplicateTypeException ex) {
					log.error("Duplicate type " + WITH_REVISION_ATTR, ex);
				}
			}
		};
		DBKnowledgeBaseMigrationTestSetup setup = new DBKnowledgeBaseMigrationTestSetup(self);
		setup.addAdditionalTypes(typeWithRevAttr);
		setup.addAdditionalMigrationTypes(typeWithRevAttr);
		return setup;
	}

	public MetaObject getTypeWithRevAttr(KnowledgeBase kb) {
		try {
			return kb.getMORepository().getMetaObject(WITH_REVISION_ATTR);
		} catch (UnknownTypeException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void testDuplicateChange() {
		final RevisionModificator revisionModificator = new RevisionModificator(REV_ATTRIBUTES, 2);

		revisionModificator.setModus(Modus.replay);
		assertSame(Modus.replay, revisionModificator.getModus());
		revisionModificator.setModus(Modus.fakingHistory);
		assertSame(Modus.fakingHistory, revisionModificator.getModus());
		revisionModificator.setModus(Modus.replay);
		assertSame(Modus.replay, revisionModificator.getModus());
		revisionModificator.setModus(Modus.fakingHistory);
		assertSame(Modus.fakingHistory, revisionModificator.getModus());

		revisionModificator.setModus(Modus.replay);
		assertSame(Modus.replay, revisionModificator.getModus());
		revisionModificator.setModus(Modus.replay);
		assertSame(Modus.replay, revisionModificator.getModus());

		revisionModificator.setModus(Modus.fakingHistory);
		assertSame(Modus.fakingHistory, revisionModificator.getModus());
		revisionModificator.setModus(Modus.fakingHistory);
		assertSame(Modus.fakingHistory, revisionModificator.getModus());
	}

	public void testCorrectOldValues() {
		final RevisionModificator revisionModificator = new RevisionModificator(REV_ATTRIBUTES, 2);

		CachingEventWriter out = new CachingEventWriter();
		ObjectBranchId id = new ObjectBranchId(1L, getTypeWithRevAttr(kb()), StringID.valueOf("someName"));

		ChangeSet untouched = new ChangeSet(2L).addCreation(new ObjectCreation(2L, id)).setCommit(commitEvt(2L));
		revisionModificator.rewrite(untouched, out);

		{
			revisionModificator.setModus(Modus.fakingHistory);
			ItemUpdate additionalChange = new ItemUpdate(3, id, false);
			additionalChange.setValue("someAttr", "before", "after");
			revisionModificator.rewrite(new ChangeSet(3L).addUpdate(additionalChange).setCommit(commitEvt(3L)), out);
			revisionModificator.setModus(Modus.replay);
		}

		ItemUpdate updateWithoutOldValues = new ItemUpdate(3, id, false);
		updateWithoutOldValues.setValue(REVISION_ATTR, null, 2L);
		revisionModificator.rewrite(new ChangeSet(3).addUpdate(updateWithoutOldValues), out);

		ItemUpdate updateWithOldValues = new ItemUpdate(3, id, true);
		updateWithOldValues.setValue(REVISION_ATTR, 3L, 2L);
		revisionModificator.rewrite(new ChangeSet(3).addUpdate(updateWithOldValues), out);

		List<ChangeSet> evts = out.getAllEvents();
		assertEquals(untouched, evts.get(0));
		ItemUpdate writtenWithoutOldValues = evts.get(2).getUpdates().get(0);
		assertEquals(3L + 1L, writtenWithoutOldValues.getRevision());
		assertEquals(2L, writtenWithoutOldValues.getValues().get(REVISION_ATTR));
		assertEquals(null, writtenWithoutOldValues.getOldValues());
		ItemUpdate writtenWithOldValues = evts.get(3).getUpdates().get(0);
		assertEquals(3L + 1L, writtenWithOldValues.getRevision());
		assertEquals(2L, writtenWithOldValues.getValues().get(REVISION_ATTR));
		assertEquals(3L + 1L, writtenWithOldValues.getOldValues().get(REVISION_ATTR));
	}

	CommitEvent commitEvt(long rev) {
		return TestKnowledgeEvent.newCommit(rev, TestRevisionModificator.class);
	}

	/**
	 * Tests that revision before the first modification are not moved
	 */
	public void testRevisionBeforeModificationNotMoved() throws DataObjectException {
		for (int i = 0; i < 5; i++) {
			final Transaction tx = begin();
			newB("b" + i);
			tx.commit();
		}

		final KnowledgeObject koRevBeforeIncrease;
		final ObjectBranchId koIDBeforeIncrease;
		{
			final Transaction tx = begin();
			koRevBeforeIncrease = createRevAttrObject(kb(), null);
			setRevAttr(koRevBeforeIncrease, 1L);
			tx.commit();
			koIDBeforeIncrease = getObjectID(koRevBeforeIncrease);
		}

		try (final ChangeSetReader eventReader =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(2L), Revision.CURRENT))) {
			try (final EventWriter replayWriter = KBUtils.getReplayWriter(kbNode2())) {
				final RevisionModificator revisionModificator =
					new RevisionModificator(REV_ATTRIBUTES, kbNode2().getLastRevision() + 1);
				{
					revisionModificator.setModus(Modus.fakingHistory);
					// must be done such that commit has some effect to the revision number
					final TLID someFunnyName = kbNode2().createID();
					final ObjectBranchId object =
						new ObjectBranchId(trunk().getBranchId(), type2(E_NAME), someFunnyName);
					final ObjectCreation createEvent = new ObjectCreation(kbNode2().getLastRevision() + 1, object);
					createEvent.setValue(A1_NAME, null, "funnya1");
					ChangeSet cs =
						new ChangeSet(createEvent.getRevision()).add(createEvent).setCommit(newCommitEvt());
					revisionModificator.rewrite(cs, replayWriter);
					revisionModificator.setModus(Modus.replay);
				}

				// copy the rest
				ChangeSet cs;
				while ((cs = eventReader.read()) != null) {
					revisionModificator.rewrite(cs, replayWriter);

				}
			}
		}

		final KnowledgeObject koOnNode2 =
			kbNode2().getKnowledgeObject(koIDBeforeIncrease.getObjectType().getName(),
				koIDBeforeIncrease.getObjectName());
		assertEquals((Long) 1L, getRevAttr(koOnNode2));
	}

	/**
	 * Tests that revision after the last modification are moved
	 */
	public void testRevisionAfterModificationMoved() throws DataObjectException {

		for (int i = 0; i < 5; i++) {
			final Transaction tx = begin();
			newB("b" + i);
			tx.commit();
		}

		final KnowledgeObject koRevAfterIncrease;
		final ObjectBranchId koIDAfterIncrease;
		{
			final Transaction tx = begin();
			koRevAfterIncrease = createRevAttrObject(kb(), null);
			setRevAttr(koRevAfterIncrease, 3L);
			tx.commit();
			koIDAfterIncrease = getObjectID(koRevAfterIncrease);
		}

		try (final ChangeSetReader eventReader =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(2L), Revision.CURRENT))) {
			try (final EventWriter replayWriter = KBUtils.getReplayWriter(kbNode2())) {
				final RevisionModificator revisionModificator =
					new RevisionModificator(REV_ATTRIBUTES, kbNode2().getLastRevision() + 1);
				{

					// must be done such that commit has some effect to the revision number
					revisionModificator.setModus(Modus.fakingHistory);
					final TLID someFunnyName = kbNode2().createID();
					ObjectBranchId object =
						new ObjectBranchId(trunk().getBranchId(), type2(E_NAME), someFunnyName);
					long revision = kbNode2().getLastRevision() + 1;
					ObjectCreation createEvent = new ObjectCreation(revision, object);
					createEvent.setValue(A1_NAME, null, "funnya1");
					ChangeSet cs = new ChangeSet(revision).add(createEvent).setCommit(newCommitEvt());
					revisionModificator.rewrite(cs, replayWriter);
					revisionModificator.setModus(Modus.replay);
				}

				// copy the rest
				ChangeSet cs;
				while ((cs = eventReader.read()) != null) {
					revisionModificator.rewrite(cs, replayWriter);

				}
			}
		}

		final KnowledgeObject koOnNode2 =
			kbNode2().getKnowledgeObject(koIDAfterIncrease.getObjectType().getName(),
				koIDAfterIncrease.getObjectName());
		assertEquals((Long) 4L, getRevAttr(koOnNode2));
	}

	/**
	 * Tests that revision at that points to an modificated revision is moved
	 */
	public void testRevisionAtModificationMoved() throws DataObjectException {

		for (int i = 0; i < 5; i++) {
			final Transaction tx = begin();
			newB("b" + i);
			tx.commit();
		}

		final KnowledgeObject koRevAtModificationIncrease;
		final ObjectBranchId koIDAtModificationIncrease;
		{
			final Transaction tx = begin();
			koRevAtModificationIncrease = createRevAttrObject(kb(), null);
			setRevAttr(koRevAtModificationIncrease, 2L);
			tx.commit();
			koIDAtModificationIncrease = getObjectID(koRevAtModificationIncrease);
		}

		try (final ChangeSetReader eventReader =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(2L), Revision.CURRENT))) {
			try (final EventWriter replayWriter = KBUtils.getReplayWriter(kbNode2())) {
				final RevisionModificator revisionModificator =
					new RevisionModificator(REV_ATTRIBUTES, kbNode2().getLastRevision() + 1);
				{
					revisionModificator.setModus(Modus.fakingHistory);
					// must be done such that commit has some effect to the revision number
					final TLID someFunnyName = kbNode2().createID();
					ObjectBranchId object =
						new ObjectBranchId(trunk().getBranchId(), type2(E_NAME), someFunnyName);
					long revision = kbNode2().getLastRevision() + 1;
					ObjectCreation createEvent = new ObjectCreation(revision, object);
					createEvent.setValue(A1_NAME, null, "funnya1");
					ChangeSet cs = new ChangeSet(revision).add(createEvent).setCommit(newCommitEvt());
					revisionModificator.rewrite(cs, replayWriter);
					revisionModificator.setModus(Modus.replay);
				}

				// copy the rest
				ChangeSet cs;
				while ((cs = eventReader.read()) != null) {
					revisionModificator.rewrite(cs, replayWriter);

				}
			}
		}

		final KnowledgeObject koOnNode2 =
			kbNode2().getKnowledgeObject(koIDAtModificationIncrease.getObjectType().getName(),
				koIDAtModificationIncrease.getObjectName());
		assertEquals((Long) 3L, getRevAttr(koOnNode2));
	}

	private CommitEvent newCommitEvt() {
		final Revision lastRev = kbNode2().getRevision(kbNode2().getLastRevision());
		long revision = lastRev.getCommitNumber() + 1;
		CommitEvent evt = TestKnowledgeEvent.newCommit(revision, TestRevisionModificator.class);
		return evt;
	}

	/**
	 * Tests that references to historic objects are updated
	 */
	public void testObjectKeyValuesUpdate() throws DataObjectException {
		final KnowledgeObject d1;
		{
			final Transaction tx = begin();
			d1 = newD("b1");
			tx.commit();
		}

		final Revision r1;
		final KnowledgeObject objectWithReference;
		final ObjectBranchId objectWithReferenceID;
		{
			final Transaction tx = begin();
			objectWithReference = newE("objectWithReference");
			d1.setAttributeValue(A2_NAME, "a2");
			tx.commit();
			r1 = tx.getCommitRevision();
			objectWithReferenceID = getObjectID(objectWithReference);
		}
		{
			final Transaction tx = begin();
			setReference(objectWithReference, HistoryUtils.getKnowledgeItem(r1, d1), true, HistoryType.HISTORIC, true);
			tx.commit();
		}

		try (final ChangeSetReader eventReader =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(2L), Revision.CURRENT))) {
			try (final EventWriter replayWriter = KBUtils.getReplayWriter(kbNode2())) {
				final RevisionModificator revisionModificator =
					new RevisionModificator(RevisionAttributes.NO_REVISION_ATTRIBUTES, kbNode2().getLastRevision() + 1);
				final Long lastRev;
				{
					ChangeSet create = eventReader.read();
					revisionModificator.rewrite(create, replayWriter);

					final CommitEvent commit = create.getCommit();
					lastRev = commit.getRevision();
				}
				{

					revisionModificator.setModus(Modus.fakingHistory);
					// no op commit
					CommitEvent commit = TestKnowledgeEvent.newCommit(lastRev + 1, TestRevisionModificator.class);

					revisionModificator.rewrite(new ChangeSet(commit.getRevision()).setCommit(commit), replayWriter);
					revisionModificator.setModus(Modus.replay);
				}

				// copy the rest
				ChangeSet cs;
				while ((cs = eventReader.read()) != null) {
					revisionModificator.rewrite(cs, replayWriter);

				}
			}
		}
		final Branch trunkNode2 = kbNode2().getBranch(trunk().getBranchId());
		final ObjectBranchId objectWithReferenceIDNode2 =
			KBUtils.transformId(kbNode2().getMORepository(), objectWithReferenceID);
		final KnowledgeItem objectWithRefOnNode2 =
			kbNode2().getKnowledgeItem(trunkNode2, Revision.CURRENT, objectWithReferenceIDNode2.getObjectType(),
				objectWithReferenceIDNode2.getObjectName());
		final KnowledgeItem reference = getReference(objectWithRefOnNode2, true, HistoryType.HISTORIC, true);
		assertInstanceof(reference, KnowledgeItem.class);
		assertEquals("Reference to historic object not updated", "a2", reference.getAttributeValue(A2_NAME));

	}

	/**
	 * Tests that dropping an event is possible
	 */
	public void testDropEvent() throws DataObjectException {
		final KnowledgeObject b1;
		{
			final Transaction tx = begin();
			b1 = newB("b1");
			tx.commit();
		}

		{
			final Transaction tx = begin();
			b1.setAttributeValue(A1_NAME, "b1new");
			tx.commit();
		}

		{
			final Transaction tx = begin();
			b1.setAttributeValue(A2_NAME, "b2");
			tx.commit();
		}

		try (final ChangeSetReader eventReader =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(2L), Revision.CURRENT))) {
			try (final EventWriter replayWriter = KBUtils.getReplayWriter(kbNode2())) {
				final RevisionModificator revisionModificator =
					new RevisionModificator(RevisionAttributes.NO_REVISION_ATTRIBUTES, kbNode2().getLastRevision() + 1);
				{
					final ChangeSet cs = eventReader.read();
					assertTrue(!cs.getCreations().isEmpty());
					revisionModificator.rewrite(cs, replayWriter);
				}
				{
					// update is ignored
					final ChangeSet updateCS = eventReader.read();
					List<ItemUpdate> updates = updateCS.getUpdates();
					assertEquals(1, updates.size());
					ItemUpdate update = updates.get(0);
					assertEquals("b1", update.getOldValues().get(A1_NAME));
					assertEquals("b1new", update.getValues().get(A1_NAME));

					revisionModificator.setModus(Modus.fakingHistory);
					revisionModificator.dropRevision();
					revisionModificator.setModus(Modus.replay);
				}
				{
					final ChangeSet create = eventReader.read();
					revisionModificator.rewrite(create, replayWriter);
				}
			}
		}
	}

	/**
	 * Tests that adding a new event is possible
	 */
	public void testChangesOnBranches() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject b1;
		final ObjectBranchId b1Id;
		final Revision commitRevision;
		{
			final Transaction tx = begin();
			b1 = newB("b1");
			b1.setAttributeValue(A2_NAME, "a2");
			tx.commit();
			commitRevision = tx.getCommitRevision();
			b1Id = getObjectID(b1);
		}
		final Branch branch = kb().createBranch(trunk(), commitRevision, null);

		String newA1Value = "b1New";
		String newA2Value = "b2New";

		try (final ChangeSetReader eventReader =
			kb().getChangeSetReader(ReaderConfigBuilder.createConfig(kb().getRevision(2L), Revision.CURRENT))) {
			try (final EventWriter replayWriter = KBUtils.getReplayWriter(kbNode2())) {
				long lastRev = kbNode2().getLastRevision();
				Long lastRevDate = kbNode2().getHistoryManager().getRevision(lastRev).getDate();
				final RevisionModificator revisionModificator =
					new RevisionModificator(RevisionAttributes.NO_REVISION_ATTRIBUTES, lastRev + 1);
				CommitEvent originalCommit;
				{
					final ChangeSet create = eventReader.read();
					originalCommit = create.getCommit();
					create.setCommit(null);
					revisionModificator.rewrite(create, replayWriter);
				}
				{
					revisionModificator.setModus(Modus.fakingHistory);
					long revision = lastRev + 1;
					ItemUpdate update = new ItemUpdate(revision, b1Id, true);
					update.getValues().put(A2_NAME, newA2Value);
					CommitEvent commit =
						new CommitEvent(revision, TestRevisionModificator.class.getName(), lastRevDate,
							"Updated values");
					ChangeSet fakeCS = new ChangeSet(revision).add(update).setCommit(commit);
					revisionModificator.rewrite(fakeCS, replayWriter);
					revisionModificator.setModus(Modus.replay);
				}
				{
					// Branch bases on this revision
					revisionModificator.rewrite(new ChangeSet(originalCommit.getRevision()).setCommit(originalCommit),
						replayWriter);
					// must take after writing as revision is adopted after writing
					lastRev = originalCommit.getRevision();
					lastRevDate = originalCommit.getDate();
				}
				{
					revisionModificator.setModus(Modus.fakingHistory);
					long revision = lastRev + 1;
					ItemUpdate update = new ItemUpdate(revision, b1Id, true);
					update.getValues().put(A1_NAME, newA1Value);
					CommitEvent commit =
						new CommitEvent(revision, TestRevisionModificator.class.getName(), lastRevDate,
							"Updated values");
					ChangeSet fakeCs = new ChangeSet(revision).add(update).setCommit(commit);
					revisionModificator.rewrite(fakeCs, replayWriter);
					revisionModificator.setModus(Modus.replay);
				}

				// copy the rest
				ChangeSet cs;
				while ((cs = eventReader.read()) != null) {
					revisionModificator.rewrite(cs, replayWriter);

				}
			}
		}

		final KnowledgeObject b1Node2 =
			kbNode2().getKnowledgeObject(b1Id.getObjectType().getName(), b1Id.getObjectName());
		assertEquals(newA1Value, b1Node2.getAttributeValue(A1_NAME));
		assertEquals(newA2Value, b1Node2.getAttributeValue(A2_NAME));

		// tests that the base revision is moved
		Branch branchOnNode2 = kbNode2().getBranch(branch.getBranchId());
		final ObjectBranchId b1IDNode2 = KBUtils.transformId(kbNode2().getMORepository(), b1Id);
		final KnowledgeItem b1OnBranchOnNode2 =
			kbNode2().getKnowledgeItem(branchOnNode2, Revision.CURRENT, b1IDNode2.getObjectType(),
				b1IDNode2.getObjectName());

		// test that A1 is not branched
		assertEquals("b1", b1OnBranchOnNode2.getAttributeValue(A1_NAME));
		assertEquals(newA2Value, b1OnBranchOnNode2.getAttributeValue(A2_NAME));
	}

	public static Test suite() {
		return suite(TestRevisionModificator.class);
	}
}

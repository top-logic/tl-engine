/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.migrate.tl.skip;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.KnowledgeEventConverter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.migrate.tl.skip.DefaultSkippedReferenceHandle;
import com.top_logic.migrate.tl.skip.ItemEventSkip;
import com.top_logic.migrate.tl.skip.TypeSkip;

/**
 * The class {@link TestItemEventSkip} tests {@link ItemEventSkip}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestItemEventSkip extends AbstractDBKnowledgeBaseMigrationTest {

	/**
	 * The class {@link SkipAllReferences} skips all references to skipped objects.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SkipAllReferences extends DefaultSkippedReferenceHandle {

		@Override
		public boolean handleSkippedReference(ObjectCreation event, MOReference attribute,
				Set<ObjectBranchId> skippedObjects) {
			// skip all creations that reference to skipped objects.
			return true;
		}
	}

	/**
	 * Test for Ticket #19848.
	 */
	public void testSkipFollowupDeletion() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1_skipped");
		KnowledgeObject c1 = newC("c1");
		KnowledgeAssociation bc1 = newBC(b1, c1);
		commit(tx);

		ItemEventSkip skip = ItemEventSkip.newItemEventSkip(TypeSkip.skip(B_NAME), new AssertProtocol());
		copy(tx.getCommitRevision(), skip, kb(), kbNode2());

		ObjectKey b1node2Key = node2Key(b1);
		assertNull("Events of type " + B_NAME + " are skipped.", kbNode2().resolveObjectKey(b1node2Key));
		ObjectKey bc1node2Key = node2Key(bc1);
		assertNull("Association to skipped object must be skipped.", kbNode2().resolveObjectKey(bc1node2Key));
		assertNotNull(kbNode2().resolveObjectKey(node2Key(c1)));

		Transaction deleteTX = begin();
		b1.delete();
		commit(deleteTX);

		class CollectionRewriter implements EventRewriter {

			List<ChangeSet> _changeSets = new ArrayList<>();

			@Override
			public void rewrite(ChangeSet cs, EventWriter out) {
				_changeSets.add(cs);
				out.write(cs);
			}
		}

		CollectionRewriter checkRewriter = new CollectionRewriter();
		copy(deleteTX.getCommitRevision(), skip, kb(), kbNode2(), checkRewriter);
		assertEquals("Expected one CS for one commit.", 1, checkRewriter._changeSets.size());
		ChangeSet cs = checkRewriter._changeSets.get(0);
		List<ItemDeletion> deletions = cs.getDeletions();
		if (deletions.isEmpty()) {
			// Each create event for deleted object is skipped
			return;
		}
		for (ItemDeletion deletion : deletions) {
			assertNotEquals("Ticket #19848: Deletion of association is skipped, because creation is skipped.",
				ObjectBranchId.toObjectBranchId(bc1node2Key), deletion.getObjectId());
			assertEquals("Deletion of item is skipped, because creation is skipped.",
				ObjectBranchId.toObjectBranchId(b1node2Key), deletion.getObjectId());
		}
	}

	/**
	 * Simple test that {@link ObjectCreation} of a certain type are skipped.
	 */
	public void testSkipCreation() throws DataObjectException {
		Revision startRevision;
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1_skipped");
		KnowledgeObject c1 = newC("c1");
		commit(tx);
		startRevision = tx.getCommitRevision();

		ItemEventSkip skip = ItemEventSkip.newItemEventSkip(TypeSkip.skip(B_NAME), new AssertProtocol());
		copy(startRevision, skip, kb(), kbNode2());

		assertNull("Events of type " + B_NAME + " are skipped.", kbNode2().resolveObjectKey(node2Key(b1)));
		assertNotNull(kbNode2().resolveObjectKey(node2Key(c1)));
	}

	/**
	 * Simple test update of a skipped object is not applied, i.e. no error is thrown.
	 */
	public void testSkipUpdateOfSkippedCreation() throws DataObjectException {
		Revision startRevision;
		Transaction create = begin();
		KnowledgeObject b1 = newB("b1_skipped");
		KnowledgeObject c1 = newC("c1");
		commit(create);
		startRevision = create.getCommitRevision();

		Transaction update = begin();
		b1.setAttributeValue(A2_NAME, "update");
		commit(update);

		ItemEventSkip skip = ItemEventSkip.newItemEventSkip(TypeSkip.skip(B_NAME), new AssertProtocol());
		copy(startRevision, skip, kb(), kbNode2());

		assertNull("Events of type " + B_NAME + " are skipped.", kbNode2().resolveObjectKey(node2Key(b1)));
		assertNotNull(kbNode2().resolveObjectKey(node2Key(c1)));
	}

	/**
	 * Tests that the reference value is set to null, when referenced object is skipped.
	 */
	public void testReferenceOfSkippedReferenceCreation() throws DataObjectException {
		Revision startRevision;
		Transaction create = begin();
		KnowledgeObject d1 = newD("d1_skipped");
		KnowledgeObject c1 = newC("c1");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL), d1);
		commit(create);
		startRevision = create.getCommitRevision();

		ItemEventSkip skip = ItemEventSkip.newItemEventSkip(TypeSkip.skip(D_NAME), new AssertProtocol());

		copy(startRevision, skip, kb(), kbNode2());

		assertNull("Events of type " + D_NAME + " are skipped.", kbNode2().resolveObjectKey(node2Key(d1)));
		KnowledgeItem migratedE = kbNode2().resolveObjectKey(node2Key(e1));
		assertNotNull(migratedE);
		assertNull(e1 + " points to skipped " + d1 + ".",
			migratedE.getAttributeValue(getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL)));
		assertNotNull(kbNode2().resolveObjectKey(node2Key(c1)));
	}

	/**
	 * Tests that the event containing a reference to a skipped object is skipped.
	 */
	public void testReferenceOfSkippedReferenceCreation2() throws DataObjectException, ConfigurationException {
		Revision startRevision;
		Transaction create = begin();
		KnowledgeObject d1 = newD("d1_skipped");
		KnowledgeObject c1 = newC("c1");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL), d1);
		commit(create);
		startRevision = create.getCommitRevision();

		PolymorphicConfiguration<SkipAllReferences> handler = TypedConfiguration
			.createConfigItemForImplementationClass(SkipAllReferences.class);
		ItemEventSkip skip = ItemEventSkip.newItemEventSkip(TypeSkip.skip(D_NAME), handler, new AssertProtocol());

		copy(startRevision, skip, kb(), kbNode2());

		assertNull("Events of type " + D_NAME + " are skipped.", kbNode2().resolveObjectKey(node2Key(d1)));
		assertNull(e1 + " points to skipped " + d1 + ".", kbNode2().resolveObjectKey(node2Key(e1)));
		assertNotNull(kbNode2().resolveObjectKey(node2Key(c1)));
	}

	private void copy(Revision startRevision, ItemEventSkip skip, DBKnowledgeBase sourceKB, DBKnowledgeBase destKB,
			EventRewriter... additionalRewriters) {
		EventRewriter[] allRewriters = new EventRewriter[additionalRewriters.length + 2];
		allRewriters[0] = new KnowledgeEventConverter(destKB.getMORepository());
		allRewriters[1] = skip;
		System.arraycopy(additionalRewriters, 0, allRewriters, 2, additionalRewriters.length);
		try (EventWriter writer = StackedEventWriter.createWriter(0, destKB.getReplayWriter(), allRewriters)) {
			ReaderConfig config = ReaderConfigBuilder.createConfig(startRevision, lastRevision());
			try (ChangeSetReader changeSetReader = sourceKB.getChangeSetReader(config)) {
				ChangeSet cs;
				while ((cs = changeSetReader.read()) != null) {
					writer.write(cs);
				}
			}
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestItemEventSkip}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestItemEventSkip.class, "testReferenceOfSkippedReferenceCreation2");
		}
		return suite(TestItemEventSkip.class);
	}
}

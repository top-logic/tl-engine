/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.migrate.tl.changeType;

import static test.com.top_logic.knowledge.service.db2.KnowledgeBaseMigrationTestScenario.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseMigrationTestScenario;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.migrate.tl.changeType.GenericKOTypeChange;
import com.top_logic.migrate.tl.changeType.SimpleKOTypeChange;

/**
 * Test for {@link SimpleKOTypeChange}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestChangeKOType extends AbstractDBKnowledgeBaseMigrationTest {

	private GenericKOTypeChange.Matcher _matcher;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_matcher = new GenericKOTypeChange.Matcher() {

			@Override
			public boolean matches(MetaObject objectType, Object objectName, Map<String, Object> creationValues) {
				if (creationValues.containsKey(KnowledgeBaseMigrationTestScenario.T_C1_NAME)) {
					return true;
				}
				if (creationValues.containsKey(KnowledgeBaseMigrationTestScenario.T_C2_NAME)) {
					return true;
				}
				if (creationValues.containsKey(KnowledgeBaseMigrationTestScenario.T_C3_NAME)) {
					return true;
				}
				return false;
			}
		};
	}

	public void testCorrectElementMoved() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx1 = begin();
		BObj moved = BObj.newBObj("moved");
		// Need these attribute for migration.
		moved.setValue(KnowledgeBaseMigrationTestScenario.T_C1_NAME, "flex1");
		moved.setValue(KnowledgeBaseMigrationTestScenario.T_C2_NAME, 44);

		BObj notMovedDoesNotMatch = BObj.newBObj("notMoved");
		notMovedDoesNotMatch.setA2("newA2");
		KnowledgeItem notMovedWrongType = newD("d2");
		commit(tx1);

		Branch branch = kb().createBranch(trunk(), tx1.getCommitRevision(), types(B_NAME, C_NAME));

		migrateBtoC(_matcher);

		Branch trunk2 = kbNode2().getHistoryManager().getTrunk();
		assertNotNull(
			"Object was not moved",
			kbNode2().getKnowledgeItem(trunk2, Revision.CURRENT, type2(T_B_NAME),
				notMovedDoesNotMatch.tHandle().getObjectName()));
		assertNotNull("Object was not moved",
			kbNode2().getKnowledgeItem(trunk2, Revision.CURRENT, type2(T_D_NAME), notMovedWrongType.getObjectName()));
		assertNull("Object was moved to different table",
			kbNode2().getKnowledgeItem(trunk2, Revision.CURRENT, type2(T_B_NAME),
				moved.tHandle().getObjectName()));
		KnowledgeItem migratedC =
			kbNode2().getKnowledgeItem(trunk2, Revision.CURRENT, type2(T_C_NAME),
				moved.tHandle().getObjectName());
		assertNotNull("Object '" + moved + "'was moved to '" + T_C_NAME + "'", migratedC);
		assertEquals("flex1", migratedC.getAttributeValue(T_C1_NAME));
		assertEquals(44, migratedC.getAttributeValue(T_C2_NAME));
		assertEquals(null, migratedC.getAttributeValue(T_C3_NAME));

		KnowledgeItem branchedMovedObject = HistoryUtils.getKnowledgeItem(kbNode2(), branch, migratedC);
		assertNotNull("MovedObject was not branched.", branchedMovedObject);
	}

	public void testMovedObjectReceivesUpdates() {
		Transaction tx1 = begin();
		BObj moved = BObj.newBObj("moved");
		// Need this attribute for migration.
		moved.setValue(KnowledgeBaseMigrationTestScenario.T_C2_NAME, 44);
		commit(tx1);

		Transaction tx2 = begin();
		moved.setValue(KnowledgeBaseMigrationTestScenario.T_C2_NAME, 56);
		commit(tx2);

		migrateBtoC(_matcher);

		Branch trunk2 = kbNode2().getHistoryManager().getTrunk();
		KnowledgeItem migratedC =
			kbNode2().getKnowledgeItem(trunk2, tx2.getCommitRevision(), type2(T_C_NAME),
				moved.tHandle().getObjectName());
		assertEquals("Update was not proceeded on moved object.", 56, migratedC.getAttributeValue(T_C2_NAME));
	}

	public void testMovedObjectReceivesDeletion() {
		Transaction tx1 = begin();
		BObj moved = BObj.newBObj("moved");
		// Need this attribute for migration.
		moved.setValue(KnowledgeBaseMigrationTestScenario.T_C2_NAME, 44);
		// fetch here because object is deleted later and access is not possible.
		TLID movedName = moved.tHandle().getObjectName();
		commit(tx1);

		Transaction tx2 = begin();
		moved.tDelete();
		commit(tx2);

		migrateBtoC(_matcher);

		Branch trunk2 = kbNode2().getHistoryManager().getTrunk();
		KnowledgeItem migratedC =
			kbNode2().getKnowledgeItem(trunk2, tx2.getCommitRevision(), type2(T_C_NAME), movedName);
		assertNull("Deletion was not proceeded on moved object.", migratedC);
	}

	public void testAssociationChangedEnd() throws DataObjectException {
		Transaction tx1 = begin();
		BObj movedWrapper = BObj.newBObj("moved");
		// Need this attribute for migration.
		movedWrapper.setValue(KnowledgeBaseMigrationTestScenario.T_C2_NAME, 44);
		KnowledgeObject moved = movedWrapper.tHandle();
		KnowledgeObject someC = newC("someC");
		KnowledgeAssociation newAB = newAB(moved, someC);
		commit(tx1);

		migrateBtoC(_matcher);

		KnowledgeAssociation migratedAssociation =
			kbNode2().getKnowledgeAssociation(T_AB_NAME, newAB.getObjectName());
		assertNotNull(migratedAssociation);
		assertEquals(moved.getObjectName(), migratedAssociation.getSourceIdentity().getObjectName());
		assertEquals(type2(T_C_NAME), migratedAssociation.getSourceIdentity().getObjectType());
	}

	private void migrateBtoC(GenericKOTypeChange.Matcher matcher) {
		AssertProtocol log = new AssertProtocol();
		Iterable<String> srcTypes = Collections.singletonList(B_NAME);
		MetaObject targetType = type2(T_C_NAME);
		boolean newTargetType;
		try {
			MetaObject type = kb().getMORepository().getMetaObject(T_C_NAME);
			newTargetType = type == null;
		} catch (UnknownTypeException ex) {
			newTargetType = true;
		}
		GenericKOTypeChange genericKOTypeChange =
			new GenericKOTypeChange(log, srcTypes, targetType, newTargetType, matcher);
		List<? extends EventRewriter> rewriters = Collections.singletonList(genericKOTypeChange);
		doMigration(rewriters);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestChangeKOType}.
	 */
	public static Test suite() {
		return AbstractDBKnowledgeBaseClusterTest.suite(TestChangeKOType.class);
	}

}


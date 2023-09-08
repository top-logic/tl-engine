/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.basic.message.Message;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TLObject;

/**
 * Base class for test cases that simulate a cluster environment with two node.
 * 
 * <p>
 * The {@link KnowledgeBase} of the first (primary) node is {@link #kb()}, the {@link KnowledgeBase}
 * on the second node is {@link #kbNode2()}. The secondary node can be used for testing data
 * refetch.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDBKnowledgeBaseClusterTest extends AbstractDBKnowledgeBaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Resets the revision of the current session to the last local revision; actually like
		// there is no revision when test starts.
		updateSessionRevisionNode2();
	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseClusterTestSetup(self);
	}
	
	/**
	 * Creates a new {@link KnowledgeBaseTestScenarioConstants#A_NAME} with given
	 * {@link KnowledgeBaseTestScenarioConstants#A1_NAME a1} on {@link #kbNode2() node 2}.
	 * 
	 * @param typeName
	 *        The name of the the concrete sub type of
	 *        {@link KnowledgeBaseTestScenarioConstants#A_NAME}.
	 * @param a1
	 *        Value of {@link KnowledgeBaseTestScenarioConstants#A1_NAME a1}.
	 */
	protected KnowledgeObject newANode2(String typeName, String a1) throws DataObjectException {
		Branch before = inNode2();
		try {
			KnowledgeObject result = kbNode2().createKnowledgeObject(typeName);
			setA1(result, a1);
			return result;
		} finally {
			restore(before);
		}
	}

	/**
	 * Creates a new {@link KnowledgeBaseTestScenarioConstants#B_NAME} on {@link #kbNode2() node 2}.
	 * 
	 * @see #newANode2(String, String)
	 * @see #newB(String)
	 */
	protected KnowledgeObject newBNode2(String a1) throws DataObjectException {
		return newANode2(B_NAME, a1);
	}

	/**
	 * Creates a new {@link KnowledgeBaseTestScenarioConstants#C_NAME} on {@link #kbNode2() node 2}.
	 * 
	 * @see #newANode2(String, String)
	 * @see #newC(String)
	 */
	protected KnowledgeObject newCNode2(String a1) throws DataObjectException {
		return newANode2(C_NAME, a1);
	}

	/**
	 * Creates a new {@link KnowledgeBaseTestScenarioConstants#D_NAME} on {@link #kbNode2() node 2}.
	 * 
	 * @see #newANode2(String, String)
	 * @see #newD(String)
	 */
	protected KnowledgeObject newDNode2(String a1) throws DataObjectException {
		return newANode2(D_NAME, a1);
	}

	/**
	 * Creates a new {@link KnowledgeBaseTestScenarioConstants#E_NAME} on {@link #kbNode2() node 2}.
	 * 
	 * @see #newANode2(String, String)
	 * @see #newE(String)
	 */
	protected KnowledgeObject newENode2(String a1) throws DataObjectException {
		return newANode2(E_NAME, a1);
	}

	/**
	 * Workaround for not having the context branch per KB. When operating on multiple KBs, the
	 * context branch must be set explicitly.
	 * 
	 * @return The former context {@link Branch}.
	 */
	private Branch inNode2() {
		return HistoryUtils.setContextBranch(kbNode2().getTrunk());
	}

	private void restore(Branch old) {
		HistoryUtils.setContextBranch(old);
	}

	protected Transaction beginNode2() {
		return beginNode2(null);
	}
	
	protected Transaction beginNode2(Message modificationDescription) {
		return kbNode2().beginTransaction(modificationDescription);
	}
	
	protected void commitNode2(Transaction tx) {
		commitNode2(tx, null, false);
	}

	protected void commitNode2Fail(Transaction tx) {
		commitNode2(tx, null, true);
	}

	protected void commitNode2(Transaction tx, String testDescription) {
		commitNode2(tx, testDescription, false);
	}

	protected void commitNode2(Transaction tx, String testDescription, boolean expectFailure) {
		assertSame(kbNode2(), tx.getKnowledgeBase());
		internalCommitTx(tx, testDescription, expectFailure);
	}
	
	protected void rollbackNode2(Transaction tx) {
		assertSame(kbNode2(), tx.getKnowledgeBase()); 
		tx.rollback(null);
	}	
	
	protected void refetchNode2() {
		try {
			kbNode2().refetch();
		} catch (RefetchTimeout ex) {
			throw (AssertionError) new AssertionError("Refetch did time out.").initCause(ex);
		}
	}
	
	public static DBKnowledgeBase kbNode2() {
		return DBKnowledgeBaseClusterTestSetup.kbNode2();
	}

	/**
	 * Returns TRUNK of the {@link KnowledgeBase} given by {@link #kbNode2()}.
	 */
	protected static Branch trunk2() {
		return HistoryUtils.getHistoryManager(kbNode2()).getTrunk();
	}

	/**
	 * The {@link ObjectKey} of the given object translated for {@link #kb()}.
	 */
	protected ObjectKey node1Key(KnowledgeItem object) {
		return key(kb(), object);
	}

	/**
	 * Returns the given object in {@link #kb()}.
	 */
	protected KnowledgeItem node1Item(KnowledgeItem item) {
		return kb().resolveObjectKey(node1Key(item));
	}

	/**
	 * Returns the given object in {@link #kbNode2()}.
	 */
	protected <T extends TLObject> T node1Obj(T item) {
		KnowledgeItem node1Item = node1Item(item.tHandle());
		if (node1Item == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		T node1Obj = (T) node1Item.getWrapper();
		return node1Obj;
	}

	/**
	 * The {@link ObjectKey} of the given object translated for {@link #kbNode2()}.
	 */
	protected ObjectKey node2Key(KnowledgeItem object) {
		return key(kbNode2(), object);
	}

	/**
	 * Returns the given object in {@link #kbNode2()}.
	 */
	protected KnowledgeItem node2Item(KnowledgeItem item) {
		return kbNode2().resolveObjectKey(node2Key(item));
	}

	/**
	 * Returns the given object in {@link #kbNode2()}.
	 */
	protected <T extends TLObject> T node2Obj(T item) {
		KnowledgeItem node2Item = node2Item(item.tHandle());
		if (node2Item == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		T node2Obj = (T) node2Item.getWrapper();
		return node2Obj;
	}

	private ObjectKey key(KnowledgeBase kb, KnowledgeItem object) {
		MORepository node1Types = kb.getMORepository();
		try {
			MetaObject node1Type = node1Types.getMetaObject(object.tTable().getName());
			return new DefaultObjectKey(
				object.getBranchContext(), object.getHistoryContext(), node1Type, object.getObjectName());
		} catch (UnknownTypeException ex) {
			throw fail("Type not found.", ex);
		}
	}

	protected <T extends TLObject> T onBranch(Branch branch, T object) {
		KnowledgeItem item = onBranch(branch, object.tHandle());
		if (item == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		T result = (T) item.getWrapper();
		return result;
	}

	protected KnowledgeItem onBranch(Branch branch, KnowledgeItem object) {
		return object.getKnowledgeBase().resolveObjectKey(onBranch(branch, object.tId()));
	}

	protected ObjectKey onBranch(Branch branch, ObjectKey key) {
		return new DefaultObjectKey(
			branch.getBranchId(), key.getHistoryContext(), key.getObjectType(), key.getObjectName());
	}

	/**
	 * Updates the revision of the current thread in {@link #kbNode2()} to the given revision.
	 */
	protected void updateSessionRevisionNode2(UpdateChainLink newSessionRevision) {
		updateSessionRevision(kbNode2().getHistoryManager(), newSessionRevision);
	}

	/**
	 * Updates the revision of the current thread in {@link #kbNode2()} to its last revision.
	 */
	protected void updateSessionRevisionNode2() throws MergeConflictException {
		HistoryManager hm = kbNode2().getHistoryManager();
		hm.updateSessionRevision();
	}

	protected static Set<? extends MetaObject> types2(String... names) {
		HashSet<MetaObject> result = new HashSet<>();
		for (String name : names) {
			result.add(type2(name));
		}
		return result;
	}

	protected static MetaObject type2(String name) {
		try {
			return kbNode2().getMORepository().getMetaObject(name);
		} catch (UnknownTypeException ex) {
			throw fail("Type not found.", ex);
		}
	}

}

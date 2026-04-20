/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.element.changelog.I18NConstants;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests for {@link ChangeSetReverter}.
 */
@SuppressWarnings("javadoc")
public class TestChangeSetReverter extends TestWithModelExtension {

	private static final String MODULE = "test.com.top_logic.element.changelog.TestSubtreeChangeLog";

	private TLModule _testModule;

	private TLClass _nodeType;

	private ModelFactory _factory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Reuses the Node/children composition model defined for TestSubtreeChangeLog.
		extendApplicationModel(TestSubtreeChangeLog.class, "ext.model.xml");
		_testModule = TLModelUtil.findModule(MODULE);
		_nodeType = (TLClass) _testModule.getType("Node");
		_factory = DynamicModelService.getInstance().getFactory(_testModule);
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = beginTX()) {
			List<TLObject> instances = new ArrayList<>();
			for (TLClass clazz : _testModule.getClasses()) {
				instances.addAll(MetaElementUtil.getAllDirectInstancesOf(clazz, TLObject.class));
			}
			KBUtils.deleteAll(instances);
			_testModule.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * A rename inside the subtree is undone; the old name reappears.
	 */
	public void testRevertRenameInSubtree() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");

		Revision snapshot = currentRevision();

		rename(child, "child-modified");
		assertEquals("child-modified", child.tValueByName("name"));

		List<ResKey> problems = ChangeSetReverter.revertSubtreeTo(root, snapshot,
			com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);

		assertEquals("No problems expected for a clean revert.", List.of(), problems);
		assertEquals("child", child.tValueByName("name"));
	}

	/**
	 * A creation inside the subtree is undone; the created object no longer exists in head.
	 */
	public void testRevertCreationInSubtree() {
		TLObject root = createNode(null, "root");

		Revision snapshot = currentRevision();

		TLObject extra = createNode(root, "extra");
		assertTrue(extra.tValid());

		ChangeSetReverter.revertSubtreeTo(root, snapshot,
			com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);

		assertFalse("Created child must be gone after the subtree revert.", extra.tValid());
		Collection<?> remainingChildren = (Collection<?>) root.tValueByName("children");
		assertTrue("Root's children collection must be empty after revert.", remainingChildren.isEmpty());
	}

	/**
	 * Changes outside the subtree are not touched by the revert.
	 */
	public void testRevertLeavesSiblingTreeAlone() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");

		TLObject otherRoot = createNode(null, "other");
		TLObject otherChild = createNode(otherRoot, "otherChild");

		Revision snapshot = currentRevision();

		rename(child, "child-2");
		rename(otherChild, "otherChild-2");

		ChangeSetReverter.revertSubtreeTo(root, snapshot,
			com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);

		assertEquals("child", child.tValueByName("name"));
		assertEquals("Sibling tree must keep its post-snapshot modification.",
			"otherChild-2", otherChild.tValueByName("name"));
	}

	/**
	 * No changes in the configured window yields no-op revert with no problems.
	 */
	public void testRevertWithNoChanges() {
		TLObject root = createNode(null, "root");

		Revision snapshot = currentRevision();

		List<ResKey> problems = ChangeSetReverter.revertSubtreeTo(root, snapshot,
			com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);

		assertEquals(List.of(), problems);
		assertEquals("root", root.tValueByName("name"));
	}

	/**
	 * {@link ChangeSetReverter#undoLast(TLObject, int, boolean)} reverts the newest real change.
	 */
	public void testUndoLast() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");
		rename(child, "child-1");
		rename(child, "child-2");

		List<ResKey> problems = ChangeSetReverter.undoLast(root, 0, true);
		assertEquals(List.of(), problems);

		// Most recent rename was "child-2", so undoLast brings us back to "child-1".
		assertEquals("child-1", child.tValueByName("name"));
	}

	/**
	 * Two calls to {@link ChangeSetReverter#undoLast(TLObject, int, boolean)} undo two
	 * consecutive real changes.
	 */
	public void testUndoLastTwice() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");
		rename(child, "child-1");
		rename(child, "child-2");

		ChangeSetReverter.undoLast(root, 0, true);
		assertEquals("child-1", child.tValueByName("name"));

		ChangeSetReverter.undoLast(root, 0, true);
		assertEquals("child", child.tValueByName("name"));
	}

	/**
	 * {@link ChangeSetReverter#redoLast(TLObject, int, boolean)} re-applies the most recent undo.
	 */
	public void testRedoLastRestoresUndoneChange() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");
		rename(child, "child-1");

		ChangeSetReverter.undoLast(root, 0, true);
		assertEquals("child", child.tValueByName("name"));

		List<ResKey> problems = ChangeSetReverter.redoLast(root, 0, true);
		assertEquals(List.of(), problems);
		assertEquals("child-1", child.tValueByName("name"));
	}

	/**
	 * {@link ChangeSetReverter#redoLast(TLObject, int, boolean)} is a no-op when there is no undo
	 * to re-apply.
	 */
	public void testRedoLastNoop() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");
		rename(child, "child-1");

		List<ResKey> problems = ChangeSetReverter.redoLast(root, 0, true);
		assertEquals(List.of(), problems);
		assertEquals("child-1", child.tValueByName("name"));
	}

	/**
	 * Sequence {@code ch1, ch2, undo, ch3, ch4, undo, redo, redo}: the first redo restores ch4;
	 * the second redo must fail because a regular change (ch3, ch4) intervened between the
	 * older pending undo (of ch2) and the freshly redone state.
	 */
	public void testRedoLastFailsWhenRedoStackBroken() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");

		rename(child, "v1");                           // ch1
		rename(child, "v2");                           // ch2
		ChangeSetReverter.undoLast(root, 0, true);     // undo(ch2) → "v1"
		assertEquals("v1", child.tValueByName("name"));

		rename(child, "v3");                           // ch3
		rename(child, "v4");                           // ch4
		ChangeSetReverter.undoLast(root, 0, true);     // undo(ch4) → "v3"
		assertEquals("v3", child.tValueByName("name"));

		ChangeSetReverter.redoLast(root, 0, true);     // redo → "v4"
		assertEquals("v4", child.tValueByName("name"));

		try {
			ChangeSetReverter.redoLast(root, 0, true);
			fail("Second redo must fail because ch3/ch4 broke the redo stack above ch2.");
		} catch (TopLogicException expected) {
			assertEquals(I18NConstants.ERROR_CANNOT_REDO_CHAIN_BROKEN, expected.getErrorKey());
		}
		assertEquals("State must be unchanged after the failed redo.",
			"v4", child.tValueByName("name"));
	}

	/**
	 * Changes outside the subtree are ignored by {@link ChangeSetReverter#undoLast(TLObject, int, boolean)}.
	 */
	public void testUndoLastIgnoresOtherSubtree() {
		TLObject root = createNode(null, "root");
		TLObject child = createNode(root, "child");
		rename(child, "child-1");

		TLObject other = createNode(null, "other");
		rename(other, "other-2");

		// The newest real change in the application is "other-2", but for root's subtree the
		// newest is the rename of child. undoLast(root) must undo that, not touch "other".
		ChangeSetReverter.undoLast(root, 0, true);

		assertEquals("child", child.tValueByName("name"));
		assertEquals("Sibling subtree must be untouched.", "other-2", other.tValueByName("name"));
	}

	private TLObject createNode(TLObject parent, String name) {
		try (Transaction tx = beginTX()) {
			TLObject node = _factory.createObject(_nodeType, parent);
			node.tUpdateByName("name", name);
			if (parent != null) {
				List<Object> current = new ArrayList<>((Collection<?>) parent.tValueByName("children"));
				current.add(node);
				parent.tUpdateByName("children", current);
			}
			tx.commit();
			return node;
		}
	}

	private void rename(TLObject node, String newName) {
		try (Transaction tx = beginTX()) {
			node.tUpdateByName("name", newName);
			tx.commit();
		}
	}

	private Revision currentRevision() {
		HistoryManager hm = _kb.getHistoryManager();
		return hm.getRevision(hm.getLastRevision());
	}

	public static Test suite() {
		return suite(TestChangeSetReverter.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

import test.com.top_logic.layout.tree.model.AbstractTLTreeModelTest;

import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.IdentityProvider;
import com.top_logic.layout.IgnoreInvalidation;
import com.top_logic.layout.tree.TreeNodeIdentification;
import com.top_logic.layout.tree.TreeUpdateAccumulator.NodeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.SubtreeUpdate;
import com.top_logic.layout.tree.TreeUpdateListener;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * The class {@link TestTreeUpdateListener} tests methods in
 * {@link TreeUpdateListener}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTreeUpdateListener extends AbstractTLTreeModelTest<DefaultMutableTLTreeNode> implements
		IdentifierSource {
	
	private static int id = 0;

	protected TreeUpdateListener updateListener;
	protected TreeUIModel uiModel;
	protected TreeNodeIdentification identityProvider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		identityProvider = new TreeNodeIdentification(this);
		updateListener = createTreeUpdateListener(identityProvider);
		uiModel = createUIModel();
		

		identityProvider.setModel(uiModel);
		identityProvider.attach();

		simulateRendering();
		
		updateListener.setModel(uiModel);
		updateListener.attach();
	}

	@Override
	protected AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> createTreeModel() {
		return new DefaultMutableTLTreeModel();
	}

	private void simulateRendering() {
		uiModel.setExpanded(uiModel.getRoot(), true);
		writeChildren(uiModel, uiModel.getRoot());
	}


	private void writeChildren(TreeUIModel treeUIModel, Object node) {
		uiModel.getUserObject(node);
		final Iterator childIterator = treeUIModel.getChildIterator(node);
		while (childIterator.hasNext()) {
			final Object next = childIterator.next();
			if (treeUIModel.isExpanded(next)) {
				writeChildren(treeUIModel, next);
			}
		}
	}
	
	protected void expandAll() {
		expandNode(uiModel, uiModel.getRoot());
	}


	private void expandNode(TreeUIModel treeUIModel, Object node) {
		uiModel.setExpanded(node, true);
		final Iterator children = uiModel.getChildIterator(node);
		while (children.hasNext()) {
			expandNode(treeUIModel, children.next());
		}
	}

	/**
	 * Creates the model which the {@link TreeUpdateListener} observes.
	 */
	protected TreeUIModel createUIModel() {
		return new DefaultStructureTreeUIModel(treemodel);
	}
	
	/**
	 * Constructs the {@link TreeUpdateListener} under test. Must not be
	 * <code>null</code>. Will be assigned to {@link #updateListener}.
	 */
	protected TreeUpdateListener createTreeUpdateListener(IdentityProvider provider) {
		return new TreeUpdateListener(provider, IgnoreInvalidation.INSTANCE);
	}
	
	@Override
	public String createNewID() {
		return "c" + (id ++);
	}

	@Override
	protected void tearDown() throws Exception {
		updateListener.detach();
		updateListener = null;
		uiModel = null;

		super.tearDown();
	}
	
	public void testAttach() {
		updateListener.attach();
		
		assertTrue(updateListener.isAttached());
		
		assertTrue(updateListener.detach());
		assertFalse(updateListener.isAttached());
		assertFalse(updateListener.detach());
		assertFalse(updateListener.isAttached());
		
		assertTrue(updateListener.attach());
		assertTrue(updateListener.isAttached());
		assertFalse(updateListener.attach());
		assertTrue(updateListener.isAttached());
	}
	
	public void testClear() {
		final Collection<NodeUpdate> updates = updateListener.getUpdates();

		assertFalse(updateListener.hasUpdates());
		assertTrue(updates.isEmpty());
		
		child_2.createChild("2_1");
		
		assertTrue(updateListener.hasUpdates());
		assertEquals(1, updates.size());
		
		updateListener.clear();
		
		assertFalse(updateListener.hasUpdates());
		assertTrue(updates.isEmpty());
	}
	
	public void testNodeCreate() {
		child_2.createChild("2_1");
		final Collection<NodeUpdate> updates = updateListener.getUpdates();
		assertEquals(1, updates.size());
		final NodeUpdate update = updates.iterator().next();
		assertEquals(child_2, update.getNode());
	}
	
	public void testNodeDeletion() {
		child_1.clearChildren();
		final Collection<NodeUpdate> updates = updateListener.getUpdates();
		assertEquals(1, updates.size());
		final NodeUpdate update = updates.iterator().next();
		assertEquals(child_1, update.getNode());
	}
	
	public void testNodeUpdate() {
		child_1.setBusinessObject("new_child1");
		final Collection<NodeUpdate> updates = updateListener.getUpdates();
		assertEquals(1, updates.size());
		final NodeUpdate update = updates.iterator().next();
		assertEquals(child_1, update.getNode());
	}
	
	public void testNodeMove() {
		// must create child for not producing a noop
		child_1.createChild("1_2");
		
		child_1_1.moveTo(child_1);
		final Collection<NodeUpdate> updates = updateListener.getUpdates();
		assertEquals(1, updates.size());
		final NodeUpdate update = updates.iterator().next();
		assertEquals(child_1, update.getNode());
	}
	
	public void testMultipleUpdates() {
		final Collection<NodeUpdate> updates = updateListener.getUpdates();

		child_1.setBusinessObject("new_child1");
		assertEquals(1, updates.size());
		final NodeUpdate nodeUpdate = updates.iterator().next();
		assertEquals(child_1, nodeUpdate.getNode());
		
		child_1.createChild("1_2");
		assertEquals("Updates were not accumulated", 1, updates.size());
		final NodeUpdate subtreeUpdate = updates.iterator().next();
		assertTrue(subtreeUpdate instanceof SubtreeUpdate);
		assertEquals(child_1, subtreeUpdate.getNode());
	}
	
	public void testMultipleUpdates2() {
		final Collection<NodeUpdate> updates = updateListener.getUpdates();
		
		child_1.createChild("1_2");
		assertEquals(1, updates.size());
		final NodeUpdate subtreeUpdate = updates.iterator().next();
		assertTrue(subtreeUpdate instanceof SubtreeUpdate);
		assertEquals(child_1, subtreeUpdate.getNode());

		child_1.setBusinessObject("new_child1");
		assertEquals("Updates were not accumulated", 1, updates.size());
		final NodeUpdate nodeUpdate = updates.iterator().next();
		assertEquals(subtreeUpdate, nodeUpdate);
	}
	
	public void testSelectionChangeInResponseToStructureChange() {
		final Collection<NodeUpdate> updates = updateListener.getUpdates();

		rootNode.clearChildren();
		updateListener.notifySelectionChanged(null, set(child_1), set(rootNode));

		// No update must be recorded for de-selecting the removed node.
		assertEquals(1, updates.size());
		final NodeUpdate subtreeUpdate = updates.iterator().next();
		assertTrue(subtreeUpdate instanceof SubtreeUpdate);
		assertEquals(rootNode, subtreeUpdate.getNode());
	}

	public static Test suite() {
		return suite(TestTreeUpdateListener.class);
	}

}


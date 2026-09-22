/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.NodeLocator;
import com.top_logic.layout.view.model.TreeNodes;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.model.impl.TransientTLObjectImpl;

/**
 * Tests that the selection of a tree and a {@link ViewChannel} follow each other.
 *
 * <p>
 * What a display bound to the selection channel sees is the business object of the selected node,
 * the set of those objects while several nodes are selected, and nothing while none is - never the
 * tree nodes themselves. The other way round, an object another writer puts on the channel is
 * revealed and selected in the tree.
 * </p>
 *
 * <p>
 * The tree the test builds is a structure of strings: below the root the objects {@link #A},
 * {@link #B} and {@link #C}, and below {@link #A} the objects {@link #A1} and {@link #A2}.
 * </p>
 */
public class TestTreeSelectionBinding extends TestCase {

	/** The business object at the root of the test tree. */
	private static final String ROOT = "root";

	/** The business object of a node below the root. */
	private static final String A = "a";

	/** The business object of a node below the root. */
	private static final String B = "b";

	/** The business object of a node below the root. */
	private static final String C = "c";

	/** The business object of a node below {@link #A}. */
	private static final String A1 = "a1";

	/** The business object of a node below {@link #A}. */
	private static final String A2 = "a2";

	/** The business object of a node below {@link #B}. */
	private static final String B1 = "b1";

	/** An object the tree holds no node for. */
	private static final String FOREIGN = "foreign";

	/** The children of each object, as the tree computes them. */
	private final Map<Object, List<Object>> _children = new HashMap<>();

	/** How often a child list was computed, to see which parts of the tree were touched. */
	private int _childListCalls;

	private ViewChannel _channel;

	private DefaultTreeUINodeModel _tree;

	private ReactTreeControl _treeControl;

	private SelectionModel<Object> _selectionModel;

	private TreeSelectionBinding _binding;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel("selection");
		_children.put(ROOT, new ArrayList<>(List.of(A, B, C)));
		_children.put(A, new ArrayList<>(List.of(A1, A2)));
		_children.put(B, new ArrayList<>(List.of(B1)));
		_children.put(B1, new ArrayList<>(List.of("b1a")));
	}

	/**
	 * Tests that the node a tree selecting a single node holds is written to the channel as its
	 * business object, and that giving the selection up clears the channel.
	 */
	public void testSingleSelection() {
		bind(false, NodeLocator.SEARCHING);

		_selectionModel.setSelected(node(A), true);
		assertEquals("The business object of the selected node is the selection.", A, _channel.get());

		_selectionModel.setSelected(node(B), true);
		assertEquals("Selecting another node replaces the value.", B, _channel.get());

		_selectionModel.clear();
		assertNull("Nothing is selected any more.", _channel.get());
	}

	/**
	 * Tests that a tree selecting any number of nodes writes one selected node as its business
	 * object, several as the set of their business objects, and none as nothing.
	 */
	public void testMultiSelection() {
		bind(true, NodeLocator.SEARCHING);

		_selectionModel.setSelected(node(A), true);
		assertEquals("A single selected node is its business object, as in a tree selecting one.",
			A, _channel.get());

		_selectionModel.setSelected(node(C), true);
		assertEquals("Several selected nodes are the set of their business objects.",
			Set.of(A, C), _channel.get());

		_selectionModel.setSelected(node(B), true);
		assertEquals(Set.of(A, B, C), _channel.get());

		_selectionModel.setSelected(node(A), false);
		assertEquals(Set.of(B, C), _channel.get());

		_selectionModel.setSelected(node(B), false);
		assertEquals("One node left of the selection is that object again.", C, _channel.get());

		_selectionModel.clear();
		assertNull("Nothing is selected any more.", _channel.get());
	}

	/**
	 * Tests that an object written to the channel is selected in the tree, and that the subtrees
	 * above its node are opened so that it is displayed.
	 */
	public void testChannelValueIsSelectedAndRevealed() {
		bind(false, NodeLocator.SEARCHING);
		assertFalse("The subtree the object hangs in is closed.", node(A).isExpanded());

		_channel.set(A1);

		assertEquals("The node of the object on the channel is the selection.",
			Set.of(node(A1)), _selectionModel.getSelection());
		assertTrue("The subtree the selected node hangs in is opened.", node(A).isExpanded());
		assertEquals("Applying the value does not write the channel back.", A1, _channel.get());
	}

	/**
	 * Tests that a set on the channel of a tree selecting any number of nodes selects the nodes of
	 * its objects.
	 */
	public void testSetSelectsSeveralNodes() {
		bind(true, NodeLocator.SEARCHING);

		_channel.set(Set.of(A1, C));

		assertEquals("Every object of the set is selected.",
			Set.of(node(A1), node(C)), _selectionModel.getSelection());
		assertTrue("The subtree a selected node hangs in is opened.", node(A).isExpanded());
	}

	/**
	 * Tests that a tree selecting a single node shows no selection for a set, which it cannot
	 * display, and leaves the value alone.
	 */
	public void testSetSelectsNothingInASingleSelectionTree() {
		bind(false, NodeLocator.SEARCHING);
		_selectionModel.setSelected(node(A), true);

		_channel.set(Set.of(A1, C));

		assertTrue("A tree selecting one node at a time cannot display a selection of several.",
			_selectionModel.getSelection().isEmpty());
		assertEquals("The value another writer put on the channel is left alone.",
			Set.of(A1, C), _channel.get());
	}

	/**
	 * Tests that an object the tree holds no node for leaves the channel and the selection alone:
	 * it names nothing this tree shows, and the object may well get a node later.
	 */
	public void testForeignValueLeavesChannelAndSelectionAlone() {
		bind(false, NodeLocator.SEARCHING);
		_selectionModel.setSelected(node(A), true);

		_channel.set(FOREIGN);

		assertEquals("The tree keeps showing what it has.", Set.of(node(A)), _selectionModel.getSelection());
		assertEquals("The value another writer put on the channel is left alone.", FOREIGN, _channel.get());
	}

	/**
	 * Tests that the tree's own selection coming back through the channel is not applied again: a
	 * subtree the user closed since stays closed.
	 */
	public void testEchoOfTheOwnSelectionIsNotApplied() {
		bind(false, NodeLocator.SEARCHING);
		DefaultTreeUINode nodeA = node(A);
		nodeA.setExpanded(false);

		_selectionModel.setSelected(node(A1), true);

		assertEquals("The selection made in the tree is written to the channel.", A1, _channel.get());
		assertFalse("The tree's own selection coming back through the channel is not applied again.",
			nodeA.isExpanded());
	}

	/**
	 * Tests that a value the tree had no node for is applied as soon as the node exists - the
	 * object a create command wrote to the channel before the tree caught up with it.
	 */
	public void testStructureChangeAppliesThePendingValue() {
		bind(false, NodeLocator.SEARCHING);
		_channel.set(FOREIGN);
		assertTrue("The tree has no node for the object yet.", _selectionModel.getSelection().isEmpty());

		node(A).createChild(FOREIGN);
		_binding.structureChanged();

		assertEquals("The object is selected as soon as the tree holds a node for it.",
			Set.of(node(FOREIGN)), _selectionModel.getSelection());
		assertEquals(FOREIGN, _channel.get());
	}

	/**
	 * Tests that the surviving selection is written to the channel when a node the tree displayed
	 * as selected is gone.
	 */
	public void testVanishedNodeWritesTheSurvivorsBack() {
		bind(true, NodeLocator.SEARCHING);
		_selectionModel.setSelection(Set.of(node(A), node(C)));
		assertEquals(Set.of(A, C), _channel.get());

		removeChild(root(), C);
		_binding.structureChanged();

		assertEquals("What is left of the selection replaces the value naming a node nobody sees.",
			A, _channel.get());
		assertEquals(Set.of(node(A)), _selectionModel.getSelection());
	}

	/**
	 * Tests that a selection of which nothing is left clears the channel.
	 */
	public void testVanishedSelectionClearsTheChannel() {
		bind(false, NodeLocator.SEARCHING);
		_selectionModel.setSelected(node(A), true);
		assertEquals(A, _channel.get());

		removeChild(root(), A);
		_binding.structureChanged();

		assertNull("Nothing of the selection is left.", _channel.get());
		assertTrue(_selectionModel.getSelection().isEmpty());
	}

	/**
	 * Tests that a tree saying what holds an object computes the child lists on the way from its
	 * root to that object, and nothing else.
	 */
	public void testParentsLocatorComputesTheWayOnly() {
		DefaultTreeUINodeModel tree = new DefaultTreeUINodeModel(builder(), ROOT);
		int callsBefore = _childListCalls;

		DefaultTreeUINode node = NodeLocator.byParents(this::parentOf).locate(tree.getRoot(), A2);

		assertNotNull("The object has a node in this tree.", node);
		assertEquals(A2, node.getBusinessObject());
		assertEquals("Only the child lists of the root and of the node holding the object were computed.",
			callsBefore + 2, _childListCalls);
	}

	/**
	 * Tests that the subtrees beside the way from the root to the object of the channel are left
	 * uncomputed, which a search through the tree would build.
	 */
	public void testParentsLocatorLeavesTheOtherSubtreesAlone() {
		bind(false, NodeLocator.byParents(this::parentOf));

		_channel.set(A2);

		boolean besideTheWayComputed = findChild(findChild(root(), B), B1).isInitialized();
		DefaultTreeUINode nodeA2 = findChild(findChild(root(), A), A2);

		assertEquals("The node of the object is the selection.",
			Set.of(nodeA2), _selectionModel.getSelection());
		assertFalse("A subtree beside the way to the object keeps its children uncomputed.",
			besideTheWayComputed);
	}

	/**
	 * Tests that an object whose chain of holders does not reach the root of the tree has no node
	 * there.
	 */
	public void testParentsLocatorRejectsAForeignObject() {
		bind(false, NodeLocator.byParents(this::parentOf));
		_selectionModel.setSelected(node(A), true);

		_channel.set(FOREIGN);

		assertEquals("The tree keeps showing what it has.", Set.of(node(A)), _selectionModel.getSelection());
		assertEquals("The value another writer put on the channel is left alone.", FOREIGN, _channel.get());
	}

	/**
	 * Tests that a disposed binding neither writes the channel nor follows it any more.
	 */
	public void testDisposedBindingFollowsNothing() {
		bind(false, NodeLocator.SEARCHING);
		_binding.dispose();

		_selectionModel.setSelected(node(A), true);
		assertNull("A tree nobody displays does not write the selection.", _channel.get());

		_channel.set(A1);
		assertEquals("A tree nobody displays does not follow the channel.",
			Set.of(node(A)), _selectionModel.getSelection());
	}

	/**
	 * Tests that a deleted object of the selection is not handed to the locator - nothing can be
	 * computed over an object that is gone - and that what is left of the selection takes its place
	 * on the channel.
	 */
	public void testDeletedObjectIsNotLookedFor() {
		Item survivor = new Item("survivor");
		Item deleted = new Item("deleted");
		_children.put(ROOT, new ArrayList<>(List.of(survivor, deleted)));
		List<Object> lookedFor = new ArrayList<>();
		bind(true, recordingLocator(lookedFor));
		_selectionModel.setSelection(Set.of(node(survivor), node(deleted)));
		assertEquals(Set.of(survivor, deleted), _channel.get());

		deleted.markDeleted();
		removeChild(root(), deleted);
		lookedFor.clear();
		_binding.structureChanged();

		assertFalse("A deleted object is nowhere in the tree and is not computed over.",
			lookedFor.contains(deleted));
		assertEquals("What is left of the selection replaces the value naming the deleted object.",
			survivor, _channel.get());
		assertEquals(Set.of(node(survivor)), _selectionModel.getSelection());
	}

	/**
	 * Tests that a selection of which nothing but a deleted object is left clears the channel,
	 * instead of leaving the deleted object on it for everybody reading it to fail over.
	 */
	public void testDeletedSelectionClearsTheChannel() {
		Item item = new Item("item");
		_children.put(ROOT, new ArrayList<>(List.of(item)));
		bind(false, recordingLocator(new ArrayList<>()));
		_selectionModel.setSelected(node(item), true);
		assertEquals(item, _channel.get());

		item.markDeleted();
		removeChild(root(), item);
		_binding.structureChanged();

		assertNull("Nothing of the selection is left.", _channel.get());
		assertTrue(_selectionModel.getSelection().isEmpty());
	}

	/**
	 * A locator that records what it was asked for, and refuses an object that is gone, as a
	 * locator computing over the object does.
	 */
	private static NodeLocator recordingLocator(List<Object> lookedFor) {
		return (root, businessObject) -> {
			assertTrue("Nothing can be computed over a deleted object.",
				!(businessObject instanceof Item item) || item.tValid());
			lookedFor.add(businessObject);
			return NodeLocator.SEARCHING.locate(root, businessObject);
		};
	}

	/**
	 * Object of the displayed structure that can be deleted.
	 */
	private static class Item extends TransientTLObjectImpl {

		private final String _name;

		private boolean _deleted;

		Item(String name) {
			super(null, null);
			_name = name;
		}

		/**
		 * Makes this object one that was deleted.
		 */
		void markDeleted() {
			_deleted = true;
		}

		@Override
		public boolean tValid() {
			return !_deleted;
		}

		@Override
		public String toString() {
			return _name;
		}
	}

	/**
	 * Builds the tree, its display and the binding under test.
	 *
	 * @param multi
	 *        Whether the tree selects any number of nodes instead of a single one.
	 * @param locator
	 *        How the node of an object of the channel is found.
	 */
	private void bind(boolean multi, NodeLocator locator) {
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_tree = new DefaultTreeUINodeModel(builder(), ROOT);
		_selectionModel = multi
			? new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER)
			: new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);
		_treeControl = new ReactTreeControl(reactContext, _tree, _selectionModel,
			(context, model) -> new ReactTextControl(context, String.valueOf(model)));

		_binding = new TreeSelectionBinding(_treeControl, _selectionModel, () -> _tree, locator, _channel);
	}

	/**
	 * The builder computing the children of a node from the structure the test holds.
	 */
	private TreeBuilder<DefaultTreeUINode> builder() {
		return new TreeBuilder<>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				_childListCalls++;
				List<DefaultTreeUINode> children = new ArrayList<>();
				for (Object child : _children.getOrDefault(node.getBusinessObject(), List.of())) {
					children.add(createNode(node.getModel(), node, child));
				}
				return children;
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		};
	}

	/**
	 * What holds the given object in the structure the test holds, {@code null} at its root and for
	 * an object it does not hold at all.
	 */
	private Object parentOf(Object businessObject) {
		for (Map.Entry<Object, List<Object>> entry : _children.entrySet()) {
			if (entry.getValue().contains(businessObject)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * The root of the test tree.
	 */
	private DefaultTreeUINode root() {
		return _tree.getRoot();
	}

	/**
	 * The node of the test tree standing for the given business object.
	 */
	private DefaultTreeUINode node(Object businessObject) {
		DefaultTreeUINode node = TreeNodes.findNode(root(), businessObject);
		if (node == null) {
			throw new IllegalArgumentException("No node for '" + businessObject + "'.");
		}
		return node;
	}

	/**
	 * The child of the given node standing for the given business object, {@code null} where there
	 * is none.
	 */
	private static DefaultTreeUINode findChild(DefaultTreeUINode node, Object businessObject) {
		for (DefaultTreeUINode child : node.getChildren()) {
			if (businessObject.equals(child.getBusinessObject())) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Takes the node standing for the given business object out of the given node, as a change of
	 * the displayed model does.
	 */
	private static void removeChild(DefaultTreeUINode node, Object businessObject) {
		node.removeChild(node.getChildren().indexOf(findChild(node, businessObject)));
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTreeSelectionBinding.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}

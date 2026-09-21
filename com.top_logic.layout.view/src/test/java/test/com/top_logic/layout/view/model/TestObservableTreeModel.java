/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.LongID;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.service.Revision;
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
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.TreeNodes;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests that a tree follows the model it displays: a node whose object is gone disappears, an
 * object that appeared gets a node, and the subtrees the user opened stay open through all of it.
 *
 * <p>
 * The structure the tree is built from is a map of objects to their children, which the test edits
 * as a command editing the persistent model does, and the changes are delivered through a
 * {@link ModelScope} standing in for the one of a browser window.
 * </p>
 */
public class TestObservableTreeModel extends TestCase {

	/** Name of the channel the root object is held on. */
	private static final String ROOT_CHANNEL = "root";

	/** The children of each object, as the tree computes them. */
	private final Map<TLObject, List<TLObject>> _children = new HashMap<>();

	/** How often a child list was computed, to see which parts of the tree were touched. */
	private int _childListCalls;

	private TLClass _itemType;

	private ReportingScope _scope;

	private ViewChannel _input;

	private ObservableTreeModel _observer;

	private ReactTreeControl _treeControl;

	private Item _root;

	private Item _a;

	private Item _b;

	private Item _a1;

	private Item _a2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.observableTree");
		_itemType = TLModelUtil.addClass(module, "Item");

		_scope = new ReportingScope();
		_input = new DefaultViewChannel(ROOT_CHANNEL);

		_root = item("root");
		_a = item("a");
		_b = item("b");
		_a1 = item("a1");
		_a2 = item("a2");
		_children.put(_root, new ArrayList<>(List.of(_a, _b)));
		_children.put(_a, new ArrayList<>(List.of(_a1, _a2)));
		_input.set(_root);
	}

	@Override
	protected void tearDown() throws Exception {
		if (_observer != null) {
			_observer.detach();
		}
		_observer = null;
		_treeControl = null;
		_scope = null;
		_input = null;
		_children.clear();

		super.tearDown();
	}

	/**
	 * Tests that an object taken out of the child list of an updated object loses its node, while
	 * the nodes of its siblings are the ones the display was working with and stay open.
	 */
	public void testChildTakenOutOfAnUpdatedObject() {
		startTree(Set.of());
		DefaultTreeUINode nodeA = expand(_a);

		_children.put(_root, new ArrayList<>(List.of(_a)));
		_scope.report(ModelChangeEvent.ChangeType.UPDATED, _root);

		assertEquals("The object taken out of the child list is not displayed any more.",
			List.of(_a), childObjects(root()));
		assertNull("The node of the object that is no longer a child is gone.", node(_b));
		assertSame("The node of the object that stayed is the one the display was working with.",
			nodeA, node(_a));
		assertTrue("The subtree the user opened is still open.", nodeA.isExpanded());
		assertEquals("The subtree the user opened is untouched.", List.of(_a1, _a2), childObjects(nodeA));
	}

	/**
	 * Tests that a created object of an observed type gets a node where the child list has it, while
	 * the tree keeps its nodes and the subtrees the user opened.
	 */
	public void testCreatedObjectAppears() {
		startTree(Set.of(_itemType));
		DefaultTreeUINode nodeA = expand(_a);

		Item c = item("c");
		_children.put(_root, new ArrayList<>(List.of(_a, c, _b)));
		_scope.report(ModelChangeEvent.ChangeType.CREATED, c);

		assertEquals("The created object is displayed where the child list has it.",
			List.of(_a, c, _b), childObjects(root()));
		assertSame("The nodes that were there are the ones the display was working with.",
			nodeA, node(_a));
		assertTrue("The subtree the user opened stays open although the tree grew.", nodeA.isExpanded());
	}

	/**
	 * Tests that a deleted object loses its node, and that the node it hung in stays open.
	 */
	public void testDeletedObjectDisappears() {
		startTree(Set.of());
		DefaultTreeUINode nodeA = expand(_a);

		_children.put(_a, new ArrayList<>(List.of(_a2)));
		_scope.report(ModelChangeEvent.ChangeType.DELETED, _a1);

		assertNull("The node of the deleted object is gone.", node(_a1));
		assertEquals("What is left of the child list is displayed.", List.of(_a2), childObjects(nodeA));
		assertTrue("The node the deleted object hung in stays open.", nodeA.isExpanded());
	}

	/**
	 * Tests that an input naming another root builds the tree anew, opening the subtrees that were
	 * open again where the new tree holds their objects.
	 */
	public void testInputChangeRebuildsAndKeepsTheOpenSubtrees() {
		startTree(Set.of());
		DefaultTreeUINode nodeA = expand(_a);

		Item otherRoot = item("otherRoot");
		Item c = item("c");
		_children.put(otherRoot, new ArrayList<>(List.of(_a, c)));
		_input.set(otherRoot);

		assertSame("The tree is built from the root the input names.",
			otherRoot, root().getBusinessObject());
		assertEquals(List.of(_a, c), childObjects(root()));
		assertNotSame("The tree built anew has its own nodes.", nodeA, node(_a));
		assertTrue("The subtree the user opened is open again.", node(_a).isExpanded());
		assertEquals(List.of(_a1, _a2), childObjects(node(_a)));
	}

	/**
	 * Tests that a display returning on screen shows the model as it is now: what changed while
	 * nobody was observing reaches the tree when the observation begins again.
	 */
	public void testChangeWhileDetachedIsCaughtUp() {
		startTree(Set.of());
		DefaultTreeUINode nodeA = expand(_a);

		_observer.detach();
		Item c = item("c");
		_children.put(_root, new ArrayList<>(List.of(_a, c)));
		assertEquals("A suspended observation follows nothing.", List.of(_a, _b), childObjects(root()));

		_observer.attach(_scope);

		assertEquals("The observation resuming shows the children as they are now.",
			List.of(_a, c), childObjects(root()));
		assertSame("The node of the object that stayed is the one the display was working with.",
			nodeA, node(_a));
		assertTrue("The subtree the user opened is still open.", nodeA.isExpanded());
	}

	/**
	 * Tests that a change of an object the tree does not display leaves it alone: no child list is
	 * computed for an object nobody displays.
	 */
	public void testUnrelatedChangeIsIgnored() {
		startTree(Set.of(_itemType));
		expand(_a);
		int callsBefore = _childListCalls;

		_scope.report(ModelChangeEvent.ChangeType.UPDATED, item("unrelated"));

		assertEquals("Nothing was computed again for an object nobody displays.",
			callsBefore, _childListCalls);
		assertEquals(List.of(_a, _b), childObjects(root()));
	}

	/**
	 * Builds the tree over the structure the test holds and begins observing it.
	 *
	 * @param observedTypes
	 *        The types whose creates the tree expects.
	 */
	private void startTree(Set<TLStructuredType> observedTypes) {
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		TreeBuilder<DefaultTreeUINode> builder = builder();
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, _input.get());
		_treeControl = new ReactTreeControl(reactContext, treeModel,
			new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER),
			(context, model) -> new ReactTextControl(context, String.valueOf(model)));

		_observer = new ObservableTreeModel(_treeControl, treeModel, args -> args[0], builder,
			observedTypes, List.of(_input));
		_observer.attach(_scope);
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
				for (TLObject child : _children.getOrDefault(node.getBusinessObject(), List.of())) {
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
	 * Opens the subtree of the node standing for the given object, as the user does.
	 */
	private DefaultTreeUINode expand(TLObject businessObject) {
		DefaultTreeUINode node = node(businessObject);
		assertNotNull("No node for '" + businessObject + "'.", node);
		node.setExpanded(true);
		_treeControl.updateVisibleState();
		return node;
	}

	/**
	 * The root of the tree displayed now.
	 */
	private DefaultTreeUINode root() {
		return _observer.getTreeModel().getRoot();
	}

	/**
	 * The node standing for the given object, {@code null} where the tree holds none.
	 */
	private DefaultTreeUINode node(TLObject businessObject) {
		return TreeNodes.findComputedNode(root(), businessObject);
	}

	/**
	 * The objects the children of the given node stand for.
	 */
	private static List<Object> childObjects(DefaultTreeUINode node) {
		List<Object> result = new ArrayList<>();
		for (DefaultTreeUINode child : node.getChildren()) {
			result.add(child.getBusinessObject());
		}
		return result;
	}

	/**
	 * An object of the observed type with the given name.
	 */
	private Item item(String name) {
		return new Item(_itemType, name);
	}

	/**
	 * Object of the displayed structure, identified so that its changes can be observed.
	 */
	private static class Item extends TransientTLObjectImpl {

		private static final MOClassImpl TABLE = new MOClassImpl("test");

		private static long _nextId = 1;

		private final ObjectKey _id =
			new DefaultObjectKey(1, Revision.CURRENT_REV, TABLE, LongID.valueOf(_nextId++));

		private final String _name;

		Item(TLStructuredType type, String name) {
			super(type, null);
			_name = name;
		}

		@Override
		public ObjectKey tId() {
			return _id;
		}

		@Override
		public String toString() {
			return _name;
		}
	}

	/**
	 * {@link ModelScope} the test delivers changes through.
	 *
	 * <p>
	 * A change reaches everybody observing anything, so that a display filtering the changes it acts
	 * on is asked to do that filtering.
	 * </p>
	 */
	private static class ReportingScope implements ModelScope {

		private final Set<ModelListener> _listeners = new LinkedHashSet<>();

		@Override
		public boolean addModelListener(ModelListener listener) {
			return _listeners.add(listener);
		}

		@Override
		public boolean addModelListener(TLStructuredType type, ModelListener listener) {
			return _listeners.add(listener);
		}

		@Override
		public boolean addModelListener(TLObject object, ModelListener listener) {
			return _listeners.add(listener);
		}

		@Override
		public boolean removeModelListener(ModelListener listener) {
			return _listeners.remove(listener);
		}

		@Override
		public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
			return true;
		}

		@Override
		public boolean removeModelListener(TLObject object, ModelListener listener) {
			return true;
		}

		/**
		 * Reports the given change of the given object.
		 */
		void report(ModelChangeEvent.ChangeType kind, TLObject object) {
			ModelChangeEvent event = new Change(kind, object);
			for (ModelListener listener : Set.copyOf(_listeners)) {
				listener.notifyChange(event);
			}
		}
	}

	/**
	 * The change of a single object, as a {@link ModelScope} reports it.
	 *
	 * @param kind
	 *        What happened to the object.
	 * @param object
	 *        The object that changed.
	 */
	private record Change(ModelChangeEvent.ChangeType kind, TLObject object) implements ModelChangeEvent {

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return existingObject == object ? kind : ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return objects(ChangeType.UPDATED);
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return ofType(getUpdated(), type);
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return objects(ChangeType.CREATED);
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return ofType(getCreated(), type);
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return objects(ChangeType.DELETED);
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return ofType(getDeleted(), type);
		}

		private Stream<? extends TLObject> objects(ChangeType reported) {
			return kind == reported ? Stream.of(object) : Stream.empty();
		}

		private static Stream<? extends TLObject> ofType(Stream<? extends TLObject> objects,
				TLStructuredType type) {
			return objects.filter(object -> object.tType() == type);
		}
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObservableTreeModel.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}

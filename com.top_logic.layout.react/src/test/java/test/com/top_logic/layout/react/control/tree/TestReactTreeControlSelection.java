/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.tree.ActivateNodeArguments;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.react.control.tree.SelectNodeArguments;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.table.SelectionMode;

/**
 * Tests that a selection gesture on a {@link ReactTreeControl} reaches the selection model as a
 * single event.
 *
 * <p>
 * Every display, command and channel following the selection runs once per event, so a gesture
 * that replaced the selection in more than one step would show an intermediate selection nobody
 * made - an empty one between the former and the new node for a click, a growing one for a range.
 * </p>
 *
 * <p>
 * The gestures are sent the way the client sends them, through
 * {@link ReactTreeControl#executeCommand(String, Map)}. The tree the test builds is flat: below its
 * hidden root the objects {@link #A}, {@link #B}, {@link #C} and {@link #D}.
 * </p>
 */
public class TestReactTreeControlSelection extends TestCase {

	/** The business object at the hidden root of the test tree. */
	private static final String ROOT = "root";

	/** The business object of the first node below the root. */
	private static final String A = "a";

	/** The business object of the second node below the root. */
	private static final String B = "b";

	/** The business object of the third node below the root. */
	private static final String C = "c";

	/** The business object of the fourth node below the root. */
	private static final String D = "d";

	/** The children of each object, as the tree computes them. */
	private final Map<Object, List<Object>> _children = new HashMap<>();

	private DefaultTreeUINodeModel _tree;

	private SelectionModel<Object> _selectionModel;

	private ReactTreeControl _treeControl;

	/** The selections the model reported, one entry per selection event, in notification order. */
	private List<Set<?>> _selections;

	/** The id the tree gave each node, keyed by the node's business object. */
	private Map<Object, String> _nodeIds;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_children.put(ROOT, new ArrayList<>(List.of(A, B, C, D)));
		_selections = new ArrayList<>();
	}

	/**
	 * Tests that a click on a node of a tree selecting a single node reports the new selection in
	 * one event, without the empty selection in between that giving the former one up would show.
	 */
	public void testClickReplacesTheSelectionInOneEvent() {
		build(false);

		click(A);
		assertEquals("The click reports the selection once.", 1, _selections.size());
		assertEquals(Set.of(node(A)), _selections.get(0));

		click(B);
		assertEquals("The click reports the selection once.", 2, _selections.size());
		assertEquals(List.of(Set.of(node(A)), Set.of(node(B))), _selections);
	}

	/**
	 * Tests that a click on the node that is already the selection changes nothing and therefore
	 * reports nothing.
	 */
	public void testClickOnTheSelectedNodeReportsNothing() {
		build(false);
		click(B);
		_selections.clear();

		click(B);

		assertEquals("Nothing changed, so nothing is reported.", List.of(), _selections);
		assertEquals(Set.of(node(B)), _selectionModel.getSelection());
	}

	/**
	 * Tests that a plain click in a tree selecting any number of nodes replaces the selection in
	 * one event.
	 */
	public void testPlainClickInAMultiSelectionTreeIsOneEvent() {
		build(true);
		click(A);
		click(B);
		_selections.clear();

		click(C);

		assertEquals(List.of(Set.of(node(C))), _selections);
	}

	/**
	 * Tests that a {@code Shift} click adds the whole range between the anchor and the clicked node
	 * in one event, instead of one event per node of the range.
	 */
	public void testShiftRangeIsOneEvent() {
		build(true);
		click(A);
		_selections.clear();

		shiftClick(C);

		assertEquals("The range reaches the listeners as one selection.", 1, _selections.size());
		assertEquals(Set.of(node(A), node(B), node(C)), _selections.get(0));
		assertEquals(Set.of(node(A), node(B), node(C)), _selectionModel.getSelection());
	}

	/**
	 * Tests that a {@code Shift} click following a {@code Ctrl} click that gave a node up takes the
	 * whole range out of the selection in one event.
	 */
	public void testShiftRangeTakingNodesOutIsOneEvent() {
		build(true);
		click(A);
		ctrlClick(B);
		ctrlClick(C);
		ctrlClick(A);
		assertEquals(Set.of(node(B), node(C)), _selectionModel.getSelection());
		_selections.clear();

		shiftClick(C);

		assertEquals("The range reaches the listeners as one selection.", 1, _selections.size());
		assertEquals(Set.of(), _selections.get(0));
		assertEquals(Set.of(), _selectionModel.getSelection());
	}

	/**
	 * Tests that a {@code Ctrl} click adds a node to the selection in one event.
	 */
	public void testCtrlClickIsOneEvent() {
		build(true);
		click(A);
		_selections.clear();

		ctrlClick(C);

		assertEquals(List.of(Set.of(node(A), node(C))), _selections);
	}

	/**
	 * Tests that opening a node by a double-click or {@code Enter} makes it the selection in one
	 * event.
	 */
	public void testActivateIsOneEvent() {
		build(false);
		click(A);
		_selections.clear();

		activate(B);

		assertEquals(List.of(Set.of(node(B))), _selections);
	}

	/**
	 * Sends the click on the node of the given business object.
	 */
	private void click(Object businessObject) {
		select(businessObject, false, false);
	}

	/**
	 * Sends the {@code Ctrl} click on the node of the given business object.
	 */
	private void ctrlClick(Object businessObject) {
		select(businessObject, true, false);
	}

	/**
	 * Sends the {@code Shift} click on the node of the given business object.
	 */
	private void shiftClick(Object businessObject) {
		select(businessObject, false, true);
	}

	private void select(Object businessObject, boolean ctrlKey, boolean shiftKey) {
		Map<String, Object> arguments = new LinkedHashMap<>();
		arguments.put(SelectNodeArguments.NODE_ID, nodeId(businessObject));
		arguments.put(SelectNodeArguments.CTRL_KEY, Boolean.valueOf(ctrlKey));
		arguments.put(SelectNodeArguments.SHIFT_KEY, Boolean.valueOf(shiftKey));
		_treeControl.executeCommand(ReactTreeControl.SELECT_COMMAND, arguments);
	}

	/**
	 * Sends the opening of the node of the given business object.
	 */
	private void activate(Object businessObject) {
		_treeControl.executeCommand(ReactTreeControl.ACTIVATE_COMMAND,
			Map.of(ActivateNodeArguments.NODE_ID, nodeId(businessObject)));
	}

	/**
	 * Builds the tree, its display and the recording of the selection events.
	 *
	 * @param multi
	 *        Whether the tree selects any number of nodes instead of a single one.
	 */
	private void build(boolean multi) {
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_tree = new DefaultTreeUINodeModel(builder(), ROOT);
		_tree.setRootVisible(false);
		_selectionModel = multi
			? new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER)
			: new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);
		_selectionModel.addSelectionListener(
			(model, event) -> _selections.add(new LinkedHashSet<>(event.getNewSelection())));
		_treeControl = new ReactTreeControl(reactContext, _tree, _selectionModel,
			(context, model) -> new ReactTextControl(context, String.valueOf(model)));
		if (multi) {
			_treeControl.setSelectionMode(SelectionMode.MULTI);
		}
		_nodeIds = readNodeIds();
	}

	/**
	 * The id the tree gave each of its nodes, read from the state it sends to the client.
	 */
	private Map<Object, String> readNodeIds() {
		List<?> nodeStates = (List<?>) state().get(ReactTreeControl.NODES);
		List<DefaultTreeUINode> nodes = _tree.getRoot().getChildren();
		assertEquals("The tree displays a node per object below its hidden root.",
			nodes.size(), nodeStates.size());
		Map<Object, String> result = new LinkedHashMap<>();
		for (int n = 0, cnt = nodes.size(); n < cnt; n++) {
			Map<?, ?> nodeState = (Map<?, ?>) nodeStates.get(n);
			result.put(nodes.get(n).getBusinessObject(),
				(String) nodeState.get(ReactTreeControl.NODE_ID));
		}
		return result;
	}

	/**
	 * The state the tree sends to the client.
	 */
	private Map<?, ?> state() {
		try {
			return (Map<?, ?>) JSON.fromString(_treeControl.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * The id of the node standing for the given business object, as the client knows it.
	 */
	private String nodeId(Object businessObject) {
		String result = _nodeIds.get(businessObject);
		if (result == null) {
			throw new IllegalArgumentException("The tree displays no node for '" + businessObject + "'.");
		}
		return result;
	}

	/**
	 * The node of the test tree standing for the given business object.
	 */
	private DefaultTreeUINode node(Object businessObject) {
		for (DefaultTreeUINode child : _tree.getRoot().getChildren()) {
			if (businessObject.equals(child.getBusinessObject())) {
				return child;
			}
		}
		throw new IllegalArgumentException("No node for '" + businessObject + "'.");
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
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactTreeControlSelection.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}

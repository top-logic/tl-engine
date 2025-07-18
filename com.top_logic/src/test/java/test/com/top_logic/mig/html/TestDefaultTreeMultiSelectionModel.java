/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeUIModelUpgrade;
import com.top_logic.mig.html.DefaultTreeMultiSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.TreeSelectionModel.NodeSelectionState;

/**
 * Test case for {@link DefaultTreeMultiSelectionModel}.
 */
@SuppressWarnings("javadoc")
public class TestDefaultTreeMultiSelectionModel extends TestCase {

	private DefaultTreeMultiSelectionModel<DefaultMutableTLTreeNode> _selection;

	private DefaultMutableTLTreeModel _tree;

	private DefaultMutableTLTreeNode _a;

	private DefaultMutableTLTreeNode _b;

	private DefaultMutableTLTreeNode _c;

	private DefaultMutableTLTreeNode _a1;

	private DefaultMutableTLTreeNode _a2;

	private DefaultMutableTLTreeNode _a3;

	private DefaultMutableTLTreeNode _b1;

	private DefaultMutableTLTreeNode _root;

	private SelectionListener _listener;

	protected SelectionEvent _selectedEvent;

	protected int _events;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_tree = new DefaultMutableTLTreeModel("root");

		_root = _tree.getRoot();

		_a = _root.createChild("a");
		_b = _root.createChild("b");

		// Node without children
		_c = _root.createChild("c");

		// Multiple children
		_a1 = _a.createChild("a1");
		_a2 = _a.createChild("a2");
		_a3 = _a.createChild("a3");

		// Single child
		_b1 = _b.createChild("b1");

		assertTrue(!_c.getModel().hasChildren(_c));

		TreeUIModelUpgrade<DefaultMutableTLTreeNode> uiTree = new TreeUIModelUpgrade<>(_tree);

		_selection = new DefaultTreeMultiSelectionModel<>(null, () -> uiTree);

		_listener = new SelectionListener() {
			@Override
			public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
				_events++;
				_selectedEvent = event;
			}
		};
	}

	private void listen() {
		_events = 0;
		_selectedEvent = null;
		_selection.addSelectionListener(_listener);
	}

	public void testSelect() {
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_root));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_b));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_b1));

		_selection.setSelected(_b1, true);

		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_b1));
		assertEquals(NodeSelectionState.ALL_DESCENDANTS, _selection.getNodeSelectionState(_b));
		assertEquals(NodeSelectionState.SOME_DESCENDANTS, _selection.getNodeSelectionState(_root));

		_selection.setSelected(_a, true);
		assertEquals(NodeSelectionState.SELECTED_NO_DESCENDANTS, _selection.getNodeSelectionState(_a));
		_selection.setSelected(_a1, true);
		assertEquals(NodeSelectionState.SELECTED_SOME_DESCENDANTS, _selection.getNodeSelectionState(_a));
		_selection.setSelected(_a2, true);
		_selection.setSelected(_a3, true);
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a1));
		assertEquals(NodeSelectionState.SOME_DESCENDANTS, _selection.getNodeSelectionState(_root));

		_selection.setSelected(_a3, false);
		assertEquals(NodeSelectionState.SELECTED_SOME_DESCENDANTS, _selection.getNodeSelectionState(_a));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a1));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a3));
		assertEquals(NodeSelectionState.SOME_DESCENDANTS, _selection.getNodeSelectionState(_root));
	}

	public void testSelectSubtree() {
		_selection.setSelectedSubtree(_root, true);

		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_root));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a1));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a2));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a3));

		_selection.setSelected(_a3, false);

		assertEquals(NodeSelectionState.SELECTED_SOME_DESCENDANTS, _selection.getNodeSelectionState(_root));
		assertEquals(NodeSelectionState.SELECTED_SOME_DESCENDANTS, _selection.getNodeSelectionState(_a));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a1));
		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_a2));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a3));

		_selection.setSelected(_a2, false);
		_selection.setSelected(_a1, false);

		assertEquals(NodeSelectionState.SELECTED_SOME_DESCENDANTS, _selection.getNodeSelectionState(_root));
		assertEquals(NodeSelectionState.SELECTED_NO_DESCENDANTS, _selection.getNodeSelectionState(_a));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a1));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a2));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a3));

		assertEquals(NodeSelectionState.FULL, _selection.getNodeSelectionState(_b));

		_selection.setSelectedSubtree(_root, false);

		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_root));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_a1));
		assertEquals(NodeSelectionState.NONE, _selection.getNodeSelectionState(_b));
	}

	public void testRedundantSelection() {
		listen();
		_selection.setSelected(_a, false);
		assertEquals(0, _events);
		
		_selection.setSelected(_a, true);
		
		listen();
		_selection.setSelected(_a, true);
		assertEquals(0, _events);
	}

	public void testFilteredSelection() {
		_selection.setSelected(_a, true);
		_selection.setSelected(_b, true);

		_selection.setSelectionFilter(n -> n != _c);
		_selection.setDeselectionFilter(n -> n != _b);

		listen();
		_selection.setSelected(_c, true);
		assertFalse(_selection.isSelected(_c));
		assertEquals(0, _events);

		listen();
		_selection.setSelected(_b, false);
		assertTrue(_selection.isSelected(_b));
		assertEquals(0, _events);

		listen();
		assertTrue(_selection.isSelected(_a));
		_selection.setSelected(_a, false);
		assertFalse(_selection.isSelected(_a));
		assertEquals(1, _events);
	}

	public void testBulkUpdate() {
		listen();
		_selection.startBulkUpdate();

		_selection.setSelectedSubtree(_a, true);
		_selection.setSelectedSubtree(_b, true);
		_selection.setSelectedSubtree(_c, true);

		assertEquals(0, _events);

		_selection.completeBulkUpdate();
		assertEquals(1, _events);

		assertEquals(_selection, _selectedEvent.getSender());
		assertEquals(new HashSet<>(), _selectedEvent.getOldSelection());
		assertEquals(new HashSet<>(Arrays.asList(_a, _a1, _a2, _a3, _b, _b1, _c)), _selectedEvent.getNewSelection());
		BasicTestCase.assertEquals(new HashSet<>(Arrays.asList(_root, _a, _a1, _a2, _a3, _b, _b1, _c)),
			_selectedEvent.getUpdatedObjects());
	}

	public void testMaximalSubtreeEvent() {
		listen();
		_selection.setSelectedSubtree(_root, true);
		assertEquals(1, _events);
		BasicTestCase.assertEquals(new HashSet<>(Arrays.asList(_root, _a, _a1, _a2, _a3, _b, _b1, _c)),
			_selectedEvent.getUpdatedObjects());

		listen();
		_selection.setSelectedSubtree(_root, false);
		assertEquals(1, _events);
		BasicTestCase.assertEquals(new HashSet<>(Arrays.asList(_root, _a, _a1, _a2, _a3, _b, _b1, _c)),
			_selectedEvent.getUpdatedObjects());
	}

	public void testMinimalSubtreeEvent() {
		_selection.setSelectedSubtree(_a, true);

		listen();
		_selection.setSelectedSubtree(_root, true);
		assertEquals(1, _events);
		BasicTestCase.assertEquals(new HashSet<>(Arrays.asList(_root, _b, _b1, _c)),
			_selectedEvent.getUpdatedObjects());

		_selection.setSelectedSubtree(_a, false);
		assertFalse(_selection.isSelected(_a));
		assertFalse(_selection.isSelected(_a1));

		listen();
		_selection.setSelectedSubtree(_root, false);
		assertEquals(1, _events);
		BasicTestCase.assertEquals(new HashSet<>(Arrays.asList(_root, _b, _b1, _c)),
			_selectedEvent.getUpdatedObjects());
	}

}

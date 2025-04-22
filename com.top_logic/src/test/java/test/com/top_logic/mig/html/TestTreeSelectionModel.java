/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.mig.html.DefaultTreeSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.TreeSelectionModel;
import com.top_logic.mig.html.TriState;

/**
 * Test for {@link TreeSelectionModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTreeSelectionModel extends BasicTestCase {

	private DefaultMutableTLTreeNode _root;
	private DefaultMutableTLTreeNode _child_1;
	private DefaultMutableTLTreeNode _child_2;
	private DefaultMutableTLTreeNode _child_1_1;
	private DefaultMutableTLTreeNode _child_2_1;
	private DefaultMutableTLTreeNode _child_2_2;
	private DefaultMutableTLTreeNode _child_1_1_1;
	private DefaultMutableTLTreeNode _child_2_1_1;
	private DefaultMutableTLTreeNode _child_2_2_1;
	private DefaultMutableTLTreeNode _child_1_1_2;
	private DefaultMutableTLTreeNode _child_2_1_2;
	private DefaultMutableTLTreeNode _child_2_2_2;

	private TreeSelectionModel<DefaultMutableTLTreeNode> _selectionModel;

	private Map<?, TriState> _states;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel();
		_root = model.getRoot();

		_child_1 = _root.createChild("1");
		_child_2 = _root.createChild("2");
		// _child_1_1 is child without Sibling!
		_child_1_1 = _child_1.createChild("1.1");
		_child_2_1 = _child_2.createChild("2.1");
		_child_2_2 = _child_2.createChild("2.2");
		_child_1_1_1 = _child_1_1.createChild("1.1.1");
		_child_1_1_2 = _child_1_1.createChild("1.1.2");
		_child_2_1_1 = _child_2_1.createChild("2.1.1");
		_child_2_1_2 = _child_2_1.createChild("2.1.2");
		_child_2_2_1 = _child_2_2.createChild("2.2.1");
		_child_2_2_2 = _child_2_2.createChild("2.2.2");

		_selectionModel =
			new DefaultTreeSelectionModel<>(SelectionModelOwner.NO_OWNER, DefaultMutableTLTreeNode.class, model);

		@SuppressWarnings("unchecked")
		Map<?, TriState> statesFieldMap = (Map<?, TriState>) ReflectionUtils.getValue(_selectionModel, "_states");
		_states = Collections.unmodifiableMap(statesFieldMap);
	}

	public void testSelectionModel1() {
		_selectionModel.setSelected(_child_2_1, true);

		assertState(_child_2_1, TriState.SELECTED);
		assertState(_child_2, TriState.INDETERMINATE);
		assertState(_child_2_2, TriState.NOT_SELECTED);
		assertState(_child_1, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(5);
		assertSelection(_child_2_1, _child_2_1_1, _child_2_1_2);

		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_1));
		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_1_1));
		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_1_2));
		assertEquals(TriState.NOT_SELECTED, _selectionModel.getState(_child_2_2));
		assertEquals(TriState.NOT_SELECTED, _selectionModel.getState(_child_2_2_1));
		assertEquals(TriState.INDETERMINATE, _selectionModel.getState(_child_2));
		assertEquals(TriState.NOT_SELECTED, _selectionModel.getState(_child_1));
		assertEquals(TriState.INDETERMINATE, _selectionModel.getState(_root));
	}

	public void testSelectionModel2() {
		_selectionModel.setSelected(_child_2, true);
		_selectionModel.setSelected(_child_2_2_2, false);

		assertState(_child_2_2_2, TriState.NOT_SELECTED);
		assertState(_child_2_2_1, TriState.SELECTED);
		assertState(_child_2_2, TriState.INDETERMINATE);
		assertState(_child_2_1, TriState.SELECTED);
		assertState(_child_2, TriState.INDETERMINATE);
		assertState(_child_1, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(7);
		assertSelection(_child_2_1, _child_2_1_1, _child_2_1_2, _child_2_2_1);

		assertEquals(TriState.INDETERMINATE, _selectionModel.getState(_child_2));
		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_1));
		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_1_1));
		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_1_2));
		assertEquals(TriState.INDETERMINATE, _selectionModel.getState(_child_2_2));
		assertEquals(TriState.SELECTED, _selectionModel.getState(_child_2_2_1));
		assertEquals(TriState.NOT_SELECTED, _selectionModel.getState(_child_2_2_2));
		assertEquals(TriState.INDETERMINATE, _selectionModel.getState(_root));
	}

	public void testSelectionModel3() {
		_selectionModel.setSelected(_child_2, true);
		_selectionModel.setSelected(_child_2_2_2, false);
		_selectionModel.setSelected(_child_2_2_2, true);

		assertState(_child_2, TriState.SELECTED);
		assertState(_child_1, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(3);
		assertSelection(_child_2, _child_2_1, _child_2_1_1, _child_2_1_2, _child_2_2, _child_2_2_1, _child_2_2_2);
	}

	public void testSelectionModel4() {
		_selectionModel.setSelected(_child_2, true);
		_selectionModel.setSelected(_child_2_2_2, false);
		_selectionModel.setSelected(_child_2_2_1, false);

		assertState(_child_2_2, TriState.NOT_SELECTED);
		assertState(_child_2_1, TriState.SELECTED);
		assertState(_child_2, TriState.INDETERMINATE);
		assertState(_child_1, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(5);
		assertSelection(_child_2_1, _child_2_1_1, _child_2_1_2);
	}

	public void testSelectionModel5() {
		_selectionModel.setSelected(_child_2, true);
		_selectionModel.setSelected(_child_2_2_2, true);

		assertState(_child_2, TriState.SELECTED);
		assertState(_child_1, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(3);
		assertSelection(_child_2, _child_2_1, _child_2_1_1, _child_2_1_2, _child_2_2, _child_2_2_1, _child_2_2_2);
	}

	public void testSelectionModel6() {
		_selectionModel.setSelected(_child_2, true);
		_selectionModel.setSelected(_child_2_2, false);
		_selectionModel.setSelected(_child_2_2_2, true);

		assertState(_child_2_2_2, TriState.SELECTED);
		assertState(_child_2_2_1, TriState.NOT_SELECTED);
		assertState(_child_2_2, TriState.INDETERMINATE);
		assertState(_child_2_1, TriState.SELECTED);
		assertState(_child_2, TriState.INDETERMINATE);
		assertState(_child_1, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(7);
		assertSelection(_child_2_1, _child_2_1_1, _child_2_1_2, _child_2_2_2);
	}

	public void testSelectionModel7() {
		_selectionModel.setSelected(_child_1, true);
		_selectionModel.setSelected(_child_1_1, false);

		assertNumberStates(0);
		assertSelection();
	}

	public void testSelectionModel8() {
		_selectionModel.setSelected(_child_1, true);
		_selectionModel.setSelected(_child_1_1_1, false);

		assertState(_child_1_1_1, TriState.NOT_SELECTED);
		assertState(_child_1_1_2, TriState.SELECTED);
		assertState(_child_1_1, TriState.INDETERMINATE);
		assertState(_child_1, TriState.INDETERMINATE);
		assertState(_child_2, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(6);
		assertSelection(_child_1_1_2);

		_selectionModel.setSelected(_child_1_1_2, false);

		assertNumberStates(0);
		assertSelection();

	}

	public void testSelectionModel9() {
		_selectionModel.setSelected(_child_1_1, true);

		assertState(_child_1, TriState.SELECTED);
		assertState(_child_2, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(3);
		assertSelection(_child_1, _child_1_1, _child_1_1_1, _child_1_1_2);

		_selectionModel.setSelected(_child_1_1_2, false);

		assertState(_child_1_1_2, TriState.NOT_SELECTED);
		assertState(_child_1_1_1, TriState.SELECTED);
		assertState(_child_1_1, TriState.INDETERMINATE);
		assertState(_child_1, TriState.INDETERMINATE);
		assertState(_child_2, TriState.NOT_SELECTED);
		assertState(_root, TriState.INDETERMINATE);
		assertNumberStates(6);
		assertSelection(_child_1_1_1);
	}

	private void assertSelection(DefaultMutableTLTreeNode... selection) {
		assertEquals(Set.of(selection), _selectionModel.calculateAllSelected());
	}

	private void assertState(DefaultMutableTLTreeNode node, TriState expected) {
		assertEquals(expected, _states.get(node));
	}

	private void assertNumberStates(int expected) {
		assertEquals(expected, _states.size());
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestTreeSelectionModel}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestTreeSelectionModel.class);
	}
}

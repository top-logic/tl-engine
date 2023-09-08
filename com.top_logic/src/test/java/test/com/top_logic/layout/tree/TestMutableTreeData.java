/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree;


import junit.framework.TestCase;

import test.com.top_logic.layout.tree.model.DummyTreeUIModel;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.tree.MutableTreeData;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.ConfigurableTreeRenderer;
import com.top_logic.layout.tree.renderer.DefaultTreeContentRenderer;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Test of {@link MutableTreeData}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestMutableTreeData extends TestCase {

	private SelectionModel _selection;

	private TreeUIModel _treeUIModel;

	private TreeRenderer _renderer;

	private MutableTreeData _treeData;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_treeUIModel = new DefaultTreeUINodeModel(new DefaultTreeUINodeModel.DefaultTreeUIBuilder(), "root");
		_selection = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		_selection.setSelected(_treeUIModel.getRoot(), true);
		_renderer = DefaultTreeRenderer.INSTANCE;
		_treeData = new MutableTreeData(Maybe.<TreeDataOwner> none(), _treeUIModel, _selection, _renderer);
	}

	public void testAnnotatable() {
		Property<String> property = TypedAnnotatable.property(String.class, "prop");

		assertFalse(_treeData.isSet(property));
		assertNull(_treeData.get(property));
		assertNull(_treeData.reset(property));

		_treeData.set(property, "val1");
		assertTrue(_treeData.isSet(property));
		assertEquals("val1", _treeData.get(property));
		assertEquals("val1", _treeData.reset(property));

		assertFalse(_treeData.isSet(property));
		assertNull(_treeData.get(property));
		assertNull(_treeData.reset(property));
	}

	public void testAnnotatable2() {
		Property<String> property = TypedAnnotatable.property(String.class, "prop");
		Property<String> otherProperty = TypedAnnotatable.property(String.class, "prop");

		_treeData.set(property, "val1");
		assertFalse(_treeData.isSet(otherProperty));
		assertNull(_treeData.get(otherProperty));
		assertNull(_treeData.reset(otherProperty));

		_treeData.set(otherProperty, "val2");
		assertTrue(_treeData.isSet(otherProperty));
		assertEquals("val2", _treeData.get(otherProperty));
		assertEquals("val2", _treeData.reset(otherProperty));
	}

	public void testSendingSelectionChangedEvent() {
		TDListener listener = new TDListener();
		_treeData.addTreeDataListener(listener);
		SelectionModel selection = _treeData.getSelectionModel();
		DefaultSingleSelectionModel newSelection = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		_treeData.setSelectionModel(newSelection);
		assertEquals(selection, listener.oldObj);
		assertEquals(newSelection, listener.newObj);
		listener.clear();
		_treeData.setSelectionModel(newSelection);
		assertNull("Setting same object should not trigger event.", listener.treeData);
	}

	public void testSendingModelChangedEvent() {
		TDListener listener = new TDListener();
		_treeData.addTreeDataListener(listener);
		TreeUIModel tree = _treeData.getTreeModel();
		TreeUIModel newTree = new DummyTreeUIModel();
		_treeData.setTreeModel(newTree);
		assertEquals(tree, listener.oldObj);
		assertEquals(newTree, listener.newObj);
		listener.clear();
		_treeData.setTreeModel(newTree);
		assertNull("Setting same object should not trigger event.", listener.treeData);
	}

	public void testSendingRendererChangedEvent() {
		TDListener listener = new TDListener();
		_treeData.addTreeDataListener(listener);
		TreeRenderer renderer = _treeData.getTreeRenderer();
		TreeRenderer newRenderer =
			new ConfigurableTreeRenderer(HTMLConstants.DIV, HTMLConstants.DIV, DefaultTreeContentRenderer.INSTANCE);
		_treeData.setTreeRenderer(newRenderer);
		assertEquals(renderer, listener.oldObj);
		assertEquals(newRenderer, listener.newObj);
		listener.clear();
		_treeData.setTreeRenderer(newRenderer);
		assertNull("Setting same object should not trigger event.", listener.treeData);
	}

	public void testRemoveListener() {
		TDListener listener = new TDListener();
		_treeData.addTreeDataListener(listener);
		TreeUIModel tree = _treeData.getTreeModel();
		TreeUIModel newTree = new DummyTreeUIModel();
		_treeData.setTreeModel(newTree);
		assertEquals(tree, listener.oldObj);
		assertEquals(newTree, listener.newObj);
		listener.clear();
		_treeData.removeTreeDataListener(listener);
		_treeData.setTreeModel(tree);
		assertNull("Listener was removed.", listener.treeData);
	}

}


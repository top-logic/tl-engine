/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.layout.form.treetable.component.FixedTreeTableRenderer;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNode;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNodeImpl;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.util.Utils;

/**
 * {@link GuiInspectorCommand} inspecting {@link TreeControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeInspector extends GuiInspectorCommand<TreeControl, AssertionTreeNode<?>> {

	/**
	 * Singleton {@link TreeInspector} instance.
	 */
	public static final TreeInspector INSTANCE = new TreeInspector();
	
	private static Pattern FIXED_TREE_TABLE_NODE_ID_PATTERN = FixedTreeTableRenderer.createNodeIdPattern();

	private TreeInspector() {
		// Singleton constructor.
	}

	@Override
	protected AssertionTreeNode<?> findInspectedGuiElement(TreeControl control, Map<String, Object> arguments)
			throws AssertionError {
		return findInspectedTreeNode(control, arguments);
	}

	private AssertionTreeNodeImpl<?> findInspectedTreeNode(TreeControl treeControl, Map<String, Object> arguments) {
		assert Utils.isTrue((Boolean) arguments.get("isTreeNode"));
		String treeNodeId = (String) arguments.get("treeNodeId");
		Object node = getNode(treeControl, treeNodeId);
		TreeData tree = treeControl.getData();
		return new AssertionTreeNodeImpl<>(tree, node);
	}

	private Object getNode(TreeControl treeControl, String treeNodeId) throws AssertionError {
		Object node = treeControl.getNodeById(treeNodeId);
		if (node != null) {
			return node;
		}
		Matcher matcher = FIXED_TREE_TABLE_NODE_ID_PATTERN.matcher(treeNodeId);
		if (!matcher.matches()) {
			throw new AssertionError("No node with id " + treeNodeId + " found.");
		}
		String origNodeId = matcher.group(1);
		node = treeControl.getNodeById(origNodeId);
		if (node == null) {
			throw new AssertionError("No node with id " + origNodeId + " in fixed tree table found.");
		}
		return node;
	}

	@Override
	protected void buildInspector(InspectorModel inspector, AssertionTreeNode<?> model) {
		GuiInspectorPluginFactory.createTreeNodeAssertions(inspector, model);
	}
}

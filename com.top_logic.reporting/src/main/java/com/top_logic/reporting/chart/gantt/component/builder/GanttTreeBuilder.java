/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link TreeBuilder} with an additional fixed child map. For this objects the fixed children
 * will be used instead of the inner builder.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class GanttTreeBuilder implements TreeBuilder<DefaultMutableTLTreeNode> {

	/** The inner builder to use. */
	private TreeBuilder<DefaultMutableTLTreeNode> _innerBuilder;

	/** Map<business object parent, business object child>, for which elements the inner builder is skipped in createChildList method. */
	private Map<Object, Object> _fixedChildMap;

	/**
	 * Creates a new {@link GanttTreeBuilder}.
	 */
	public GanttTreeBuilder(TreeBuilder<DefaultMutableTLTreeNode> innerBuilder, Map<Object, Object> fixedChildMap) {
		_innerBuilder = innerBuilder;
		_fixedChildMap = fixedChildMap == null ? Collections.emptyMap() : fixedChildMap;
	}

	@Override
	public DefaultMutableTLTreeNode createNode(AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model,
			DefaultMutableTLTreeNode parent, Object userObject) {
		return _innerBuilder.createNode(model, parent, userObject);
	}

	@Override
	public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
		Object child = _fixedChildMap.get(node.getBusinessObject());
		if (child != null) {
			return Collections.singletonList(createNode(node.getModel(), node, child));
		}
		return _innerBuilder.createChildList(node);
	}

	@Override
	public boolean isFinite() {
		return _innerBuilder.isFinite();
	}

}
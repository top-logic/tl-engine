/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.layout.grid.StructuredElementTreeGridModelBuilder;
import com.top_logic.element.layout.table.tree.StructuredElementTreeTableBuilder;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * The class {@link StructuredElementTreeBuilder} is used to create
 * {@link DefaultMutableTLTreeModel} from {@link StructuredElement}, i.e. it
 * translates the {@link StructuredElement} child hierarchy into a
 * {@link DefaultMutableTLTreeNode} tree hierarchy.
 * 
 * @see StructuredElementTreeTableBuilder
 * @see StructuredElementTreeModelBuilder
 * @see StructuredElementTreeGridModelBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredElementTreeBuilder extends AbstractTLTreeNodeBuilder {

	private final Filter<? super StructuredElement> _filter;

	public StructuredElementTreeBuilder() {
		this._filter = null;
	}

	public StructuredElementTreeBuilder(Filter<? super StructuredElement> seFilter) {
		this._filter = seFilter;
	}

	@Override
	public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
		final Object userObject = node.getBusinessObject();
		if (!(userObject instanceof StructuredElement)) {
			throw new IllegalArgumentException("The user object '" + userObject + "' of the given node '" + node
					+ "' is no " + StructuredElement.class.getSimpleName());
		}
		final List<? extends StructuredElement> children;
		if (_filter == null) {
			children = ((StructuredElement) userObject).getChildren();
		} else {
			children = ((StructuredElement) userObject).getChildren(_filter);
		}
		ArrayList<DefaultMutableTLTreeNode> result = new ArrayList<>(children.size());
		for (StructuredElement childElement : children) {
			result.add(createNode(node.getModel(), node, childElement));
		}
		return result;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.tree;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * An {@link DefaultTreeTableBuilder} for {@link StructuredElement}s as
 * {@link DefaultTreeTableNode#getBusinessObject() business objects}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractStructuredElementTreeTableBuilder extends DefaultTreeTableBuilder {

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		final Object businessObject = node.getBusinessObject();
		if (!(businessObject instanceof StructuredElement)) {
			throw new IllegalArgumentException("The business object '" + businessObject
				+ "' of the given node '" + node + "' is no " + StructuredElement.class.getSimpleName());
		}
		List<? extends StructuredElement> children = ((StructuredElement) businessObject).getChildren();
		ArrayList<DefaultTreeTableNode> result = new ArrayList<>(children.size());
		for (StructuredElement childElement : children) {
			result.add(createNode(node.getModel(), node, childElement));
		}
		return result;
	}

}


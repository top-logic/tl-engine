/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.demo.model.plain.DemoPlainA;
import com.top_logic.demo.model.types.DemoTypesL;
import com.top_logic.element.layout.table.tree.AbstractStructuredElementTreeTableBuilder;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * An {@link AbstractStructuredElementTreeTableBuilder} that creates synthetic nodes between the
 * {@link DemoTypesL} and their {@link DemoTypesL#getPlainChildren() children}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoSyntheticNodeTreeTableBuilder extends AbstractStructuredElementTreeTableBuilder {

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		Object businessObject = node.getBusinessObject();
		if (businessObject instanceof DemoTypesL) {
			return createNodes(node, DemoYearGroup.createFrom((DemoTypesL) businessObject));
		}
		if (businessObject instanceof DemoYearGroup) {
			return createNodes(node, ((DemoYearGroup) businessObject).getChildren());
		}
		if (businessObject instanceof DemoPlainA) {
			return emptyList();
		}
		return super.createChildList(node);
	}

	private List<DefaultTreeTableNode> createNodes(DefaultTreeTableNode parent, Collection<?> children) {
		List<DefaultTreeTableNode> result = new ArrayList<>();
		for (Object child : children) {
			result.add(createNode(parent.getModel(), parent, child));
		}
		return result;
	}

	@Override
	public DefaultTreeTableNode createNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
			DefaultTreeTableNode parent, Object businessObject) {
		DefaultTreeTableNode result = super.createNode(model, parent, businessObject);
		if (businessObject instanceof DemoYearGroup) {
			AbstractTreeTableModel.markSynthetic(result);
		}
		return result;
	}

}

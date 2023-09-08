/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.gantt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.demo.model.types.DemoTypesCAll;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * This class is used to create {@link DefaultMutableTLTreeModel} from {@link StructuredElement},
 * i.e. it translates the {@link StructuredElement} child hierarchy into a
 * {@link DefaultMutableTLTreeNode} tree hierarchy.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DemoGanttTreeBuilder extends AbstractTLTreeNodeBuilder {

	@Override
	public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode aParent) {
		List<DefaultMutableTLTreeNode> result = null;
		Object businessObject = aParent.getBusinessObject();
		if (businessObject instanceof StructuredElement) {
			List<? extends StructuredElement> children = ((StructuredElement) businessObject).getChildren();
			if (!CollectionUtil.isEmptyOrNull(children)) {
				for (StructuredElement child : children) {
					if ((child instanceof DemoTypesCAll)) {
						// Used as milestone.
						continue;
					}
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(createNode(aParent.getModel(), aParent, child));
				}
			}
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return result;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

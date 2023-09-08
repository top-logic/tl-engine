/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * Builder for CompoundSecurityLayout based tree
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class LayoutComponentNodeBuilder implements TreeBuilder<LayoutComponentNode> {
	@Override
	public List<LayoutComponentNode> createChildList(LayoutComponentNode node) {
		List<LayoutComponentNode> theChildren = new ArrayList<>();
		Object representedComponent = node.getBusinessObject();
		if (!(representedComponent instanceof BoundChecker)) {
			return Collections.emptyList();
		}
		BoundChecker boundComponent = (BoundChecker) representedComponent;

		List<BoundChecker> boundChildren = this.getChildren(boundComponent);
		for (BoundChecker boundChecker : boundChildren) {
			theChildren.add(this.createNode(node.getModel(), node, boundChecker));
		}

		return theChildren;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	@Override
	public LayoutComponentNode createNode(AbstractMutableTLTreeModel<LayoutComponentNode> model,
			LayoutComponentNode parent,
			Object userObject) {
		return new LayoutComponentNode(model, parent, userObject);
	}

	/** 
	 * Get the CompoundSecurityLayout children
	 * 
	 * @param aChecker the current checker
	 * @return the list of CompoundSecurityLayout children
	 */
	protected List<BoundChecker> getChildren(BoundChecker aChecker) {
		List<BoundChecker> theChildren = new ArrayList<>();
		Collection<? extends BoundChecker> childCheckers = aChecker.getChildCheckers();
		if (childCheckers == null) {
			return theChildren;
		}
		Iterator<? extends BoundChecker> theBoundChildrenIt = childCheckers.iterator();
		while (theBoundChildrenIt.hasNext()) {
			BoundChecker theBoundChild = theBoundChildrenIt.next();

			if (theBoundChild instanceof CompoundSecurityLayout) { // show this
				theChildren.add(theBoundChild);
			} else if (theBoundChild instanceof LayoutContainer) {
				theChildren.addAll(this.getChildren(theBoundChild));
			}
		}

		return theChildren;
	}
}
/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerMixin;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * {@link AbstractTreeModelBuilder} whose nodes have the {@link CompoundSecurityLayout} descendants
 * of the parent as children.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CompoundSecurityLayoutBuilder extends AbstractTreeModelBuilder<LayoutComponent> {

	@Override
	public Collection<? extends LayoutComponent> getParents(LayoutComponent contextComponent, LayoutComponent node) {
		LayoutComponent parent = findParent(node);
		Collection<? extends LayoutComponent> result;
		if (parent == null) {
			result = Collections.emptyList();
		} else {
			result = Collections.singletonList(parent);
		}
		return result;
	}

	/**
	 * Finds the {@link BoundChecker} whose {@link BoundChecker#getChildCheckers()} contains given
	 * component.
	 * 
	 * <p>
	 * Normally the children are created using
	 * {@link BoundCheckerMixin#getChildCheckers(Collection, Collection)} from the children and the
	 * dialogs.
	 * </p>
	 */
	private LayoutComponent findParent(LayoutComponent component) {
		LayoutComponent parent = component.getParent();
		if (parent == null) {
			return null;
		}
		if (component.openedAsDialog()) {
			LayoutComponent dialogTopLayout = component.getDialogTopLayout();
			while (parent != null) {
				if (supportsNode(parent)) {
					return parent;
				}
				if (parent == dialogTopLayout) {
					return getTreeParent(component.getDialogParent());
				}
				parent = parent.getParent();
			}
			return parent;
		} else {
			return getTreeParent(parent);
		}

	}

	private LayoutComponent getTreeParent(LayoutComponent component) {
		while (component != null) {
			if (supportsNode(component)) {
				return component;
			}
			component = component.getParent();
		}
		return component;
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return supportsNode(node);
	}

	private boolean supportsNode(Object node) {
		return node instanceof CompoundSecurityLayout || node instanceof MainLayout;
	}

	@Override
	public Iterator<? extends LayoutComponent> getChildIterator(LayoutComponent node) {
		if (!supportsNode(node)) {
			return IteratorUtil.emptyIterator();
		}
		BoundChecker checker = (BoundChecker) node;
		ArrayList<LayoutComponent> children = new ArrayList<>();
		addCompoundSecurityLayouts(checker, children);
		return children.iterator();
	}

	private void addCompoundSecurityLayouts(BoundChecker checker, ArrayList<LayoutComponent> children) {
		Collection<? extends BoundChecker> childCheckers = checker.getChildCheckers();
		if (childCheckers == null) {
			return;
		}
		for (BoundChecker childChecker : childCheckers) {
			if (childChecker instanceof CompoundSecurityLayout) {
				children.add((LayoutComponent) childChecker);
			} else {
				addCompoundSecurityLayouts(childChecker, children);
			}
		}
	}

}

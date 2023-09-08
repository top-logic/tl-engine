/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * {@link AbstractTreeModelBuilder} having {@link LayoutComponent} as nodes.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LayoutComponentTreeModelBuilder extends AbstractTreeModelBuilder<LayoutComponent> {

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof LayoutComponent;
	}

	@Override
	public Collection<? extends LayoutComponent> getParents(LayoutComponent contextComponent, LayoutComponent node) {
		LayoutComponent parent = node.getParent();
		Collection<? extends LayoutComponent> result;
		if (parent != null) {
			result = Collections.singletonList(parent);
		} else {
			result = Collections.emptyList();
		}
		return result;
	}

	@Override
	public Iterator<? extends LayoutComponent> getChildIterator(LayoutComponent node) {
		Iterator<LayoutComponent> result;
		if (node instanceof LayoutContainer) {
			result = ((LayoutContainer) node).getChildList().iterator();
		} else {
			result = IteratorUtil.emptyIterator();
		}
		return result;
	}

}

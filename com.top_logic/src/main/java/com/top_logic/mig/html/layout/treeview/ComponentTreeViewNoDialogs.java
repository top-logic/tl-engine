/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.treeview;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.TreeView;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * A {@link TreeView} of the {@link LayoutComponent} hierarchy of {@link LayoutContainer}s that
 * ignores dialogs.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ComponentTreeViewNoDialogs implements TreeView<LayoutComponent> {

	@Override
	public final boolean isLeaf(LayoutComponent node) {
		return children(node).isEmpty();
	}

	@Override
	public final Iterator<LayoutComponent> getChildIterator(LayoutComponent node) {
		return children(node).iterator();
	}

	/**
	 * Returns the children of the given {@link LayoutComponent}.
	 */
	protected List<LayoutComponent> children(LayoutComponent node) {
		List<LayoutComponent> childList;
		if (node instanceof LayoutContainer) {
			childList = ((LayoutContainer) node).getChildList();
		} else {
			// Exclude dialogs.
			childList = Collections.<LayoutComponent> emptyList();
		}
		return childList;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

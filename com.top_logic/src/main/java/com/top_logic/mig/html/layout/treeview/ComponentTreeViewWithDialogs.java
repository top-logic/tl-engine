/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.treeview;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.ConcatenatedIterable;
import com.top_logic.basic.col.TreeView;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * A {@link TreeView} of the {@link LayoutComponent} hierarchy of {@link LayoutContainer}s and
 * {@link LayoutComponent#getDialogs() dialogs}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ComponentTreeViewWithDialogs implements TreeView<LayoutComponent> {

	@Override
	public boolean isLeaf(LayoutComponent node) {
		return getNormalChildren(node).isEmpty() && getDialogChildren(node).isEmpty();
	}

	@Override
	public Iterator<LayoutComponent> getChildIterator(LayoutComponent node) {
		return getAllChildren(node).iterator();
	}

	private Iterable<LayoutComponent> getAllChildren(LayoutComponent node) {
		return ConcatenatedIterable.concat(Arrays.asList(
			getNormalChildren(node),
			getDialogChildren(node)));
	}

	/**
	 * Returns the {@link LayoutContainer#getChildList() normal} (non-dialog) children of the given
	 * component.
	 */
	protected Collection<LayoutComponent> getNormalChildren(LayoutComponent layout) {
		if (layout instanceof LayoutContainer) {
			return ((LayoutContainer) layout).getChildList();
		}
		return Collections.<LayoutComponent> emptyList();
	}

	/** Returns the {@link LayoutComponent#getDialogs() dialogs} of the given component. */
	protected List<? extends LayoutComponent> getDialogChildren(LayoutComponent layout) {
		return layout.getDialogs();
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

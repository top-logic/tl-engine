/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.basic.util.AbstractObservable;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataEvent.DisplayModelChanged;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataEvent.SelectionModelChanged;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataEvent.TreeChanged;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * The class {@link AbstractBreadcrumbData} implements the &quot;listener&quot;
 * part of {@link BreadcrumbData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractBreadcrumbData extends AbstractObservable<BreadcrumbDataListener, BreadcrumbDataEvent>
		implements BreadcrumbData {
	
	@Override
	public void addBreadcrumbDataListener(BreadcrumbDataListener listener) {
		addListener(listener);
	}

	@Override
	public void removeBreadcrumbDataListener(BreadcrumbDataListener listener) {
		removeListener(listener);
	}

	@Override
	protected void sendEvent(BreadcrumbDataListener listener, BreadcrumbDataEvent event) {
		event.dispatch(listener);
	}

	/**
	 * Sends a {@link SelectionModelChanged} event.
	 */
	protected final void notifySelectionModelChange(SingleSelectionModel newSelectionModel, SingleSelectionModel oldSelectionModel) {
		if (hasListeners()) {
			notifyListeners(new SelectionModelChanged(this, oldSelectionModel, newSelectionModel));
		}
	}

	/**
	 * Sends a {@link DisplayModelChanged} event.
	 */
	protected final void notifyDisplayModelChange(SingleSelectionModel newDisplayModel, SingleSelectionModel oldDisplayModel) {
		if (hasListeners()) {
			notifyListeners(new DisplayModelChanged(this, oldDisplayModel, newDisplayModel));
		}
	}

	/**
	 * Sends a {@link TreeChanged} event.
	 */
	protected final void notifyTreeChange(TLTreeModel<?> newTree, TLTreeModel<?> oldTree) {
		if (hasListeners()) {
			notifyListeners(new TreeChanged(this, oldTree, newTree));
		}
	}
}


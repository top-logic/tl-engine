/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Event dispatched to {@link BreadcrumbDataListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class BreadcrumbDataEvent {

	private final BreadcrumbData _sender;

	/**
	 * Creates a {@link BreadcrumbDataEvent}.
	 * 
	 * @param sender
	 *        See {@link #getSender()}.
	 */
	public BreadcrumbDataEvent(BreadcrumbData sender) {
		_sender = sender;
	}

	/**
	 * The changed model.
	 */
	public BreadcrumbData getSender() {
		return _sender;
	}

	/**
	 * Dispatches the concrete event to one of the specialized listener methods.
	 * 
	 * @param listener
	 *        The listener to be informed about this event.
	 */
	public abstract void dispatch(BreadcrumbDataListener listener);

	/**
	 * Event for
	 * {@link BreadcrumbDataListener#notifySelectionModelChanged(BreadcrumbData, SingleSelectionModel, SingleSelectionModel)}
	 * .
	 */
	public static class SelectionModelChanged extends BreadcrumbDataEvent {
	
		private final SingleSelectionModel _oldSelectionModel;
	
		private final SingleSelectionModel _newSelectionModel;
	
		/**
		 * Creates a {@link SelectionModelChanged}.
		 */
		public SelectionModelChanged(BreadcrumbData sender, SingleSelectionModel oldSelectionModel,
				SingleSelectionModel newSelectionModel) {
			super(sender);
			_oldSelectionModel = oldSelectionModel;
			_newSelectionModel = newSelectionModel;
		}
	
		@Override
		public void dispatch(BreadcrumbDataListener listener) {
			listener.notifySelectionModelChanged(getSender(), _oldSelectionModel, _newSelectionModel);
		}
	
	}

	/**
	 * Event for
	 * {@link BreadcrumbDataListener#notifyDisplayModelChanged(BreadcrumbData, SingleSelectionModel, SingleSelectionModel)}
	 * .
	 */
	public static class DisplayModelChanged extends BreadcrumbDataEvent {
	
		private final SingleSelectionModel _oldDisplayModel;
	
		private final SingleSelectionModel _newDisplayModel;
	
		/**
		 * Creates a {@link DisplayModelChanged}.
		 */
		public DisplayModelChanged(BreadcrumbData sender, SingleSelectionModel oldDisplayModel,
				SingleSelectionModel newDisplayModel) {
			super(sender);
			_oldDisplayModel = oldDisplayModel;
			_newDisplayModel = newDisplayModel;
		}
	
		@Override
		public void dispatch(BreadcrumbDataListener listener) {
			listener.notifyDisplayModelChanged(getSender(), _oldDisplayModel, _newDisplayModel);
		}
	}

	/**
	 * Event for
	 * {@link BreadcrumbDataListener#notifyTreeChanged(BreadcrumbData, TLTreeModel, TLTreeModel)} .
	 */
	public static class TreeChanged extends BreadcrumbDataEvent {
	
		private final TLTreeModel<?> _oldTree;
	
		private final TLTreeModel<?> _newTree;
	
		/**
		 * Creates a {@link TreeChanged}.
		 */
		public TreeChanged(BreadcrumbData sender, TLTreeModel<?> oldTree, TLTreeModel<?> newTree) {
			super(sender);
			_oldTree = oldTree;
			_newTree = newTree;
		}
	
		@Override
		public void dispatch(BreadcrumbDataListener listener) {
			listener.notifyTreeChanged(getSender(), _oldTree, _newTree);
		}
	
	}

}

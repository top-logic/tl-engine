/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import com.top_logic.layout.Attachable;
import com.top_logic.layout.basic.AbstractAttachable;

/**
 * Base class for {@link ListModel} implementations that represent a (partial)
 * view of a base {@link ListModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractListModelView extends AbstractListModel implements
		ListDataListener {

	/**
	 * The secondary {@link ListModel} on which this {@link ListModel} is based
	 * on.
	 */
	protected final ListModel base;

	/**
	 * Delegation to {@link AbstractAttachable} for implementing the
	 * {@link Attachable} interface.
	 */
	private final AbstractAttachable attachmentDelegate = new AbstractAttachable() {
		@Override
		protected void internalAttach() {
			AbstractListModelView.this.internalAttach();
		}

		@Override
		protected void internalDetach() {
			AbstractListModelView.this.internalDetach();
		}
	};

	/**
	 * {@link ListEventHandlingAccessor} implementation to allow using service
	 * methods from {@link ListModelUtilities} for event dispatching.
	 */
	protected final ListEventHandlingAccessor accessor = new ListEventHandlingAccessor() {
		@Override
		public void fireContentsChanged(Object sender, int index0, int index1) {
			AbstractListModelView.this.fireContentsChanged(sender, index0,
					index1);
		}

		@Override
		public void fireIntervalAdded(Object sender, int index0, int index1) {
			AbstractListModelView.this
					.fireIntervalAdded(sender, index0, index1);
		}

		@Override
		public void fireIntervalRemoved(Object sender, int index0, int index1) {
			AbstractListModelView.this.fireIntervalRemoved(sender, index0,
					index1);
		}
	};

	public AbstractListModelView(ListModel base) {
		this.base = base;
	}

	public final boolean isAttached() {
		return attachmentDelegate.isAttached();
	}

	/**
	 * Attaches this view as listener to its base model.
	 */
	protected void internalAttach() {
		base.addListDataListener(this);
	}

	/**
	 * Detaches this view as listener from its the base model.
	 */
	protected void internalDetach() {
		base.removeListDataListener(this);
	}

	/**
	 * If a listeners will be registered, the view starts to observe its ListModel.
	 * 
	 * @see javax.swing.AbstractListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener l) {
		attachmentDelegate.attach();
		super.addListDataListener(l);
	}

	/**
	 * If the last listener is removed, the view stops to observe its ListModel.
	 * 
	 * @see javax.swing.AbstractListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener l) {
		super.removeListDataListener(l);
		if (getListDataListeners().length == 0) {
			attachmentDelegate.detach();
		}
	}

	/** 
	 * This method throws an {@link IllegalStateException} if the view is not attached.
	 * 
	 */
	protected final void checkAttachException() {
		if (!isAttached())
			throw new IllegalStateException(
					"View does not observe its model. State is possibly not up-to-date!");
	}

}

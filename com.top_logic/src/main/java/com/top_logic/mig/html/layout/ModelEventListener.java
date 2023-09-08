/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.model.listen.ModelListener;

/**
 * Listener interface for component model events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelEventListener {
	
	/** 
	 * Event type to announce a change of the visibility of a component. 
	 * 
	 * <p>
	 * This event is a component internal event that is always sent from within
	 * a component tree.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 */
	public static final int SECURITY_CHANGED = 4;
	
	/** 
	 * Event type to announce that a dialog was opened by a component.
	 *
	 * <p>
	 * This event is a component internal event that is always sent from within
	 * a component tree.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 */
	public static final int DIALOG_OPENED = 10;
	
	/** 
	 * Event type to announce that a dialog was closed.
	 *  
	 * <p>
	 * This event is a component internal event that is always sent from within
	 * a component tree.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 */
	public static final int DIALOG_CLOSED = 11;
	
	/** 
	 * Event type to announce that a separate window was opened. 
	 * 
	 * <p>
	 * This event is a component internal event that is always sent from within
	 * a component tree.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 */
	public static final int WINDOW_OPENED = 20;
	
	/** 
	 * Event type to announce that a separate window was closed.
	 * 
	 * <p>
	 * This event is a component internal event that is always sent from within
	 * a component tree.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 */
	public static final int WINDOW_CLOSED = 21;
	
	
	// Global events that are triggered (also) outside of components (sender is irrelevant).

	/**
	 * Event type to announce that a business model has internally changed its state.
	 * 
	 * <p>
	 * This event is an external event that may be fired to a component tree from external sources.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 * @deprecated Use {@link ModelListener}
	 */
	@Deprecated
	public static final int MODEL_MODIFIED = 1;
	
	/**
	 * Event type to announce that a business model was newly created.
	 * 
	 * <p>
	 * This event is an external event that may be fired to a component tree from external sources.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 * @deprecated Use {@link ModelListener}
	 */
	@Deprecated
	public static final int MODEL_CREATED = 2;
	
	/**
	 * Event type to announce that a business model was deleted.
	 * 
	 * <p>
	 * This event is an external event that may be fired to a component tree from external sources.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 * @deprecated Use {@link ModelListener}
	 */
	@Deprecated
	public static final int MODEL_DELETED = 3;
	
	/** 
	 * Event type to announce that a refresh of the UI was requested (F5 in the bowser). 
	 * 
	 * <p>
	 * This event is an external event that may be fired to a component tree
	 * from external sources.
	 * </p>
	 * 
	 * @see MainLayout#broadcastModelEvent(Object, Object, int)
	 */
	public static final int GLOBAL_REFRESH = 30;

	
	/**
	 * Notifies this receiver that the given business model was changed by the
	 * given sender.
	 * 
	 * @param businessModel
	 *        The affected business model.
	 * @param sender
	 *        The component which triggered the event. Not declared as
	 *        {@link LayoutComponent} due to a design flaw.
	 * @param eventType
	 *        one of the constants defined in {@link ModelEventListener}.
	 */
	void handleModelEvent(Object businessModel, Object sender, int eventType);

}

/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType;

/**
 * Container that can be collapsed and expanded.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Collapsible extends com.top_logic.basic.listener.PropertyObservable {

	/**
	 * Property that is fired when {@link #isCollapsed()} changes.
	 * 
	 * @see CollapsedListener
	 */
	EventType<CollapsedListener, Collapsible, Boolean> COLLAPSED_PROPERTY =
		new EventType<>("collapsed") {

			@Override
			public Bubble dispatch(CollapsedListener listener, Collapsible sender, Boolean oldValue, Boolean newValue) {
				return listener.handleCollapsed(sender, oldValue, newValue);
			}

		};

	/**
	 * Whether this container is displayed collapsed.
	 * 
	 * <p>
	 * The children of a collapsed container are not displayed. The container view is replaced with
	 * a stub that can be expanded by the user. A view of a container is not required to support
	 * collapsing. If the view does not support collapsing, the collapsed state of this container
	 * model is ignored.
	 * </p>
	 */
	boolean isCollapsed();

	/**
	 * Programatically changes the collapsed state of this container.
	 * 
	 * <p>
	 * If a view supports collapsing, this change is dynamically collapsed after changing this
	 * property.
	 * </p>
	 * 
	 * @see #isCollapsed()
	 */
	void setCollapsed(boolean value);

}

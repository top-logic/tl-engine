/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyObservable;

/**
 * Observable model containing a {@link ToolBar}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolBarOwner extends PropertyObservable {

	/**
	 * {@link EventType} for observing changes to the {@link #getToolBar()} property.
	 */
	EventType<ToolBarChangeListener, ToolBarOwner, ToolBar> TOOLBAR_PROPERTY =
		new EventType<>("toolbar") {
			@Override
			public Bubble dispatch(ToolBarChangeListener listener, ToolBarOwner sender,
					ToolBar oldValue, ToolBar newValue) {
				listener.notifyToolbarChange(sender, oldValue, newValue);
				return Bubble.CANCEL_BUBBLE;
			}
		};

	/**
	 * The {@link ToolBar} with operations related to this instance.
	 */
	public ToolBar getToolBar();

	/**
	 * @see #getToolBar()
	 */
	public void setToolBar(ToolBar newToolBar);

}

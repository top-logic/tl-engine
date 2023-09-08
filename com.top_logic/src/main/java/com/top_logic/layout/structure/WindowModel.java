/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.layout.toolbar.ToolBar;

/**
 * Model of a {@link WindowControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WindowModel extends LayoutModel {

	/**
	 * @see #getWindowTitle()
	 * @see TitleChangedListener
	 */
	EventType<TitleChangedListener, Object, HTMLFragment> TITLE_PROPERTY =
		new NoBubblingEventType<>("title") {

			@Override
			protected void internalDispatch(TitleChangedListener listener, Object sender, HTMLFragment oldValue,
					HTMLFragment newValue) {
				listener.handleTitleChanged(sender, oldValue, newValue);
			}

		};
	
	/**
	 * @see #isMaximized()
	 */
	EventType<MaximalityChangeListener, Object, Boolean> MAXIMIZED_PROPERTY =
		new NoBubblingEventType<>("maximized") {

			@Override
			protected void internalDispatch(MaximalityChangeListener listener, Object sender, Boolean oldValue,
					Boolean newValue) {
				listener.maximalityChanged(sender, newValue);
			}

		};
	
	/**
	 * The title of the window.
	 */
	HTMLFragment getWindowTitle();

	/**
	 * The border size of the window decorations.
	 */
	int getBorderSize();

	/**
	 * Whether the title bar of this window is shown.
	 */
	boolean isTitleBarShown();

	/**
	 * The height of the window title bar.
	 */
	int getTitleBarHeight();

	/**
	 * The window toolbar.
	 */
	ToolBar getToolbar();

	/**
	 * Whether this window is maximized.
	 */
	boolean isMaximized();

	/**
	 * Sets the maximized state of the window.
	 */
	void setMaximized(boolean newValue);

}

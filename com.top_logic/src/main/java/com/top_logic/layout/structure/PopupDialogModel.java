/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The model for a PopupDialogControl
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public interface PopupDialogModel extends PropertyObservable {
	
	/**
	 * @see #isClosed()
	 * 
	 * @see DialogClosedListener
	 */
	EventType<DialogClosedListener, Object, Boolean> POPUP_DIALOG_CLOSED_PROPERTY = DialogModel.CLOSED_PROPERTY;
	
	/**
	 * Whether this dialog is closed.
	 */
	public boolean isClosed();
	
	/**
	 * Whether the dialog has a fixed height, or not
	 */
	public boolean hasFixedHeight();
	
	/**
	 * Whether the dialog has a fixed width, or not
	 */
	public boolean hasFixedWidth();

	/**
	 * The title of the dialog.
	 */
	public HTMLFragment getDialogTitle();
	
	/**
	 * The {@link ToolBar} of this popup dialog.
	 */
	public ToolBar getToolBar();

	/**
	 * The {@link LayoutData} that describes the current layout information.
	 */
	public LayoutData getLayoutData();
	
	/**
	 * The border width of the popup dialog
	 */
	public int getBorderWidth();
	
	/**
	 * The border color of the popup dialog
	 */
	public String getBorderColor();

	/**
	 * When the popup dialog becomes closed
	 */
	public void setClosed();
	
	/**
	 * Set a new popup dialog title
	 */
	public void setDialogTitle(HTMLFragment popupDialogTitle);
	
	/**
	 * Whether this dialog has a title bar
	 */
	public boolean hasTitleBar();
	
	/**
	 * Whether this dialog has a border
	 */
	public boolean hasBorder();
	
	/**
	 * returns the css class
	 */
	public default String getCssClass() {
		return "";
	}

	/**
	 * Whether this dialog has a css class
	 */
	public default boolean hasCssClass() {
		return false;
	}

	/**
	 * Action that closes this {@link PopupDialogModel}.
	 */
	default Command getCloseAction() {
		return (DisplayContext context) -> {
			setClosed();
			return HandlerResult.DEFAULT_RESULT;
		};
	}

}

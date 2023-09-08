/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.toolbar.ToolBar;

/**
 * Default {@link PopupDialogModel}.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class DefaultPopupDialogModel extends PropertyObservableBase implements PopupDialogModel {

	private LayoutData layout;
	private boolean isClosed;
	private boolean hasTitleBar;
	private boolean hasBorder;
	private int borderWidth;
	private String borderColor;

	private final ToolBar _toolBar;
	
	/**
	 * Create a new DefaultPopupDialogModel, which has a title bar and a customized border (width
	 * and color)
	 * 
	 * @param dialogTitle
	 *        The title of the dialog. If <code>null</code>, {@link #hasTitleBar} becomes
	 *        <code>false</code>.
	 * @param layout
	 *        The layout definition of the dialog.
	 * @param borderWidth
	 *        The border width of the dialog in <code>px</code>.
	 * @param borderColor
	 *        The border color of the dialog.
	 */
	public DefaultPopupDialogModel(HTMLFragment dialogTitle, LayoutData layout, int borderWidth, String borderColor) {
		_toolBar =
			new DefaultToolBar(this, new DefaultExpandable(), dialogTitle, false, Decision.FALSE, Decision.FALSE);
		this.layout = layout;
		isClosed = false;
		hasTitleBar = (dialogTitle != null);
		hasBorder = (borderWidth > 0);
		this.borderColor = (borderColor != null) ? borderColor : "";
		this.borderWidth = hasBorder ? borderWidth : 0;
	}	
	
	/**
	 * Create a new DefaultPopupDialogModel, which has a title bar and a border (color = theme
	 * based)
	 * 
	 * @param dialogTitle
	 *        See {@link #DefaultPopupDialogModel(HTMLFragment, LayoutData, int, String)}.
	 * @param layout
	 *        The layout definition of the dialog.
	 * @param borderWidth
	 *        The border width of the dialog in <code>px</code>.
	 */
	public DefaultPopupDialogModel(HTMLFragment dialogTitle, LayoutData layout, int borderWidth) {
		this(dialogTitle, layout, borderWidth, "");
	}	
	
	/**
	 * Create a new DefaultPopupDialogModel, which has a title bar.
	 * 
	 * @param dialogTitle
	 *        See {@link #DefaultPopupDialogModel(HTMLFragment, LayoutData, int, String)}
	 * @param layout
	 *        The layout definition of the dialog.
	 */
	public DefaultPopupDialogModel(HTMLFragment dialogTitle, LayoutData layout) {
		this(dialogTitle, layout, 0);
	}
	
	/**
	 * Create a new DefaultPopupDialogModel, which has a title bar and a customized border (width and color)
	 * 
	 * @param layout - the layout definition of the dialog
	 * @param borderWidth - the border width of the dialog in px
	 * @param borderColor - the border color of the dialog
	 */
	public DefaultPopupDialogModel(LayoutData layout, int borderWidth, String borderColor) {
		this(null, layout, borderWidth, borderColor);
	}	
	
	/**
	 * Create a new DefaultPopupDialogModel, which has a title bar and a border (color = theme based)
	 * 
	 * @param layout - the layout definition of the dialog
	 * @param borderWidth - the border width of the dialog in px
	 */
	public DefaultPopupDialogModel(LayoutData layout, int borderWidth) {
		this(layout, borderWidth, "");
	}
	
	/**
	 * Create a new pure DefaultPopupDialogModel
	 *
	 * @param layout - the layout definition of the dialog
	 */
	public DefaultPopupDialogModel(LayoutData layout) {
		this(layout, 0);
	}
	
	/** 
	 * @see com.top_logic.layout.structure.PopupDialogModel#getDialogTitle()
	 */
	@Override
	public HTMLFragment getDialogTitle() {
		return _toolBar.getTitle();
	}

	/**	
	 * @see com.top_logic.layout.structure.PopupDialogModel#getLayoutData()
	 */
	@Override
	public LayoutData getLayoutData() {
		return layout;
	}

	/**	
	 * @see com.top_logic.layout.structure.PopupDialogModel#hasTitleBar()
	 */
	@Override
	public boolean hasTitleBar() {
		return hasTitleBar;
	}

	/**	 
	 * @see com.top_logic.layout.structure.PopupDialogModel#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return isClosed;
	}	

	/**	 
	 * @see com.top_logic.layout.structure.PopupDialogModel#setClosed()
	 */
	@Override
	public void setClosed() {
		isClosed = true;
		notifyListeners(POPUP_DIALOG_CLOSED_PROPERTY, this, false, true);
	}
	
	/**	 
	 * @see com.top_logic.layout.structure.PopupDialogModel#getBorderWidth()
	 */
	@Override
	public int getBorderWidth() {
		return borderWidth;
	}
	
	/**	 
	 * @see com.top_logic.layout.structure.PopupDialogModel#getBorderColor()
	 */
	@Override
	public String getBorderColor() {
		return borderColor;
	}

	/**	
	 * @see com.top_logic.layout.structure.PopupDialogModel#hasBorder()
	 */
	@Override
	public boolean hasBorder() {
		return hasBorder;
	}

	/**
	 * @see com.top_logic.layout.structure.PopupDialogModel#setDialogTitle(HTMLFragment)
	 */
	@Override
	public void setDialogTitle(HTMLFragment popupDialogTitle) {
		if (hasTitleBar && !popupDialogTitle.equals(getDialogTitle())) {
			_toolBar.setTitle(popupDialogTitle);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasFixedHeight() {
		return layout.getHeight() > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasFixedWidth() {
		return layout.getWidth() > 0;
	}

	@Override
	public ToolBar getToolBar() {
		return _toolBar;
	}

}

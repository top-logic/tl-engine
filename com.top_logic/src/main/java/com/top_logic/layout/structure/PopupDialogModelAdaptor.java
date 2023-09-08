/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.toolbar.ToolBar;

/**
 * Adaptor for an {@link PopupDialogModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PopupDialogModelAdaptor implements PopupDialogModel {

	/** @see PopupDialogModelAdaptor#implementation() */
	private final PopupDialogModel _impl;

	/**
	 * Creates a {@link PopupDialogModelAdaptor} with the given {@link PopupDialogModel} as
	 * delegate.
	 * 
	 * @param impl
	 *        {@link PopupDialogModel} to dispatch all methods to. Must not be <code>null</code>.
	 */
	public PopupDialogModelAdaptor(PopupDialogModel impl) {
		_impl = impl;
	}

	/**
	 * Returns the {@link PopupDialogModel} which is used to dispatch all methods to.
	 */
	protected PopupDialogModel implementation() {
		return _impl;
	}

	@Override
	public boolean isClosed() {
		return _impl.isClosed();
	}

	@Override
	public boolean hasFixedHeight() {
		return _impl.hasFixedHeight();
	}

	@Override
	public boolean hasFixedWidth() {
		return _impl.hasFixedWidth();
	}

	@Override
	public HTMLFragment getDialogTitle() {
		return _impl.getDialogTitle();
	}

	@Override
	public ToolBar getToolBar() {
		return _impl.getToolBar();
	}
	
	@Override
	public LayoutData getLayoutData() {
		return _impl.getLayoutData();
	}

	@Override
	public int getBorderWidth() {
		return _impl.getBorderWidth();
	}

	@Override
	public String getBorderColor() {
		return _impl.getBorderColor();
	}

	@Override
	public void setClosed() {
		_impl.setClosed();
	}

	@Override
	public void setDialogTitle(HTMLFragment popupDialogTitle) {
		_impl.setDialogTitle(popupDialogTitle);
	}

	@Override
	public boolean hasTitleBar() {
		return _impl.hasTitleBar();
	}

	@Override
	public boolean hasBorder() {
		return _impl.hasBorder();
	}

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		return _impl.addListener(type, listener);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		return _impl.removeListener(type, listener);
	}

}

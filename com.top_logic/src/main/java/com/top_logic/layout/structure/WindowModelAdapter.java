/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.toolbar.ToolBar;

/**
 * Adapter for a {@link WindowModel}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class WindowModelAdapter extends LayoutModelAdapter implements WindowModel {

	/**
	 * Dispatches to {@link #getWindowModelImplementation()}
	 * 
	 * @see com.top_logic.layout.structure.LayoutModelAdapter#getLayoutModelImplementation()
	 */
	@Override
	protected LayoutModel getLayoutModelImplementation() {
		return getWindowModelImplementation();
	}

	/**
	 * Returns the actual {@link WindowModel} implementation to dispatch to.
	 * 
	 * @return not <code>null</code>
	 */
	protected abstract WindowModel getWindowModelImplementation();

	@Override
	public HTMLFragment getWindowTitle() {
		return getWindowModelImplementation().getWindowTitle();
	}

	@Override
	public int getBorderSize() {
		return getWindowModelImplementation().getBorderSize();
	}

	@Override
	public boolean isTitleBarShown() {
		return getWindowModelImplementation().isTitleBarShown();
	}

	@Override
	public int getTitleBarHeight() {
		return getWindowModelImplementation().getTitleBarHeight();
	}

	@Override
	public ToolBar getToolbar() {
		return getWindowModelImplementation().getToolbar();
	}

	@Override
	public boolean isMaximized() {
		return getWindowModelImplementation().isMaximized();
	}

	@Override
	public void setMaximized(boolean newValue) {
		getWindowModelImplementation().setMaximized(newValue);
	}

}


/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.Decision;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.toolbar.ToolBar;

/**
 * Default {@link WindowModel} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultWindowModel extends DefaultLayoutModel implements WindowModel {

	private boolean maximized;
	private boolean titleBarShown;
	private int borderSize;
	private int titleBarHeight;

	private final ToolBar _toolbar;

	public DefaultWindowModel(LayoutData layoutData, HTMLFragment title) {
		super(layoutData);
		_toolbar = new DefaultToolBar(this, new DefaultExpandable(), title, true, Decision.TRUE, Decision.FALSE,
			Decision.FALSE);
		this.titleBarShown = title != null;

		Theme theme = ThemeFactory.getTheme();
		this.borderSize = theme.getValue(Icons.WINDOWLAYOUT_LEFT_SPACER_WIDTH);
		this.titleBarHeight = theme.getValue(Icons.WINDOWLAYOUT_BAR_HEIGHT);
	}

	@Override
	public boolean isTitleBarShown() {
		return titleBarShown;
	}

	@Override
	public HTMLFragment getWindowTitle() {
		return _toolbar.getTitle();
	}

	public void setWindowTitle(HTMLFragment newValue) {
		_toolbar.setTitle(newValue);
	}
	
	@Override
	public int getBorderSize() {
		return borderSize;
	}

	@Override
	public ToolBar getToolbar() {
		return _toolbar;
	}
	
	@Override
	public int getTitleBarHeight() {
		return titleBarHeight;
	}

	@Override
	public boolean isMaximized() {
		return maximized;
	}

	@Override
	public void setMaximized(boolean newValue) {
		boolean oldValue = this.maximized;
		if (newValue == oldValue) {
			return;
		}
		this.maximized = newValue;
		notifyListeners(MAXIMIZED_PROPERTY, this, oldValue, newValue);
	}
	
}

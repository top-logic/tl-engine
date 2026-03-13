/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

/**
 * Configuration for a programmatically opened React window.
 */
public class WindowOptions {

	private int _width = 800;

	private int _height = 600;

	private String _title = "";

	private boolean _resizable = true;

	/** Window width in pixels. */
	public int getWidth() {
		return _width;
	}

	/** @see #getWidth() */
	public WindowOptions setWidth(int width) {
		_width = width;
		return this;
	}

	/** Window height in pixels. */
	public int getHeight() {
		return _height;
	}

	/** @see #getHeight() */
	public WindowOptions setHeight(int height) {
		_height = height;
		return this;
	}

	/** Window title. */
	public String getTitle() {
		return _title;
	}

	/** @see #getTitle() */
	public WindowOptions setTitle(String title) {
		_title = title;
		return this;
	}

	/** Whether the window is resizable by the user. */
	public boolean isResizable() {
		return _resizable;
	}

	/** @see #isResizable() */
	public WindowOptions setResizable(boolean resizable) {
		_resizable = resizable;
		return this;
	}
}

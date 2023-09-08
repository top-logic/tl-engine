/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.bal;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT wrapper for mouse coordinates value holder.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Coordinates extends JavaScriptObject {
	
	/**
	 * Must not be called.
	 */
	protected Coordinates() {
		super();
	}

	/**
	 * X coordinate.
	 */
	public native double getX() /*-{
		return this.x;
	}-*/;

	/**
	 * Y coordinate.
	 */
	public native double getY() /*-{
		return this.y;
	}-*/;

}

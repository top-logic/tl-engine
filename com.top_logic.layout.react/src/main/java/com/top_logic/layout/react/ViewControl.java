/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Rendering contract for new-world view controls.
 *
 * <p>
 * Unlike {@link com.top_logic.layout.Control}, this interface does not require a
 * {@link com.top_logic.layout.ControlScope} or {@link com.top_logic.layout.FrameScope}. Controls
 * that need old-world compatibility additionally implement
 * {@link com.top_logic.base.services.simpleajax.HTMLFragment}.
 * </p>
 */
public interface ViewControl {

	/**
	 * The control's unique ID (assigned during {@link #write}).
	 */
	String getID();

	/**
	 * Renders this control into the given writer.
	 *
	 * @param context
	 *        The view rendering context.
	 * @param out
	 *        The writer to render into.
	 */
	void write(ViewDisplayContext context, TagWriter out) throws IOException;
}

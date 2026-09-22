/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * What the user hit when clicking an element in {@link ElementPicker pick mode}.
 *
 * <p>
 * One variant per {@link PickKind}: the source of the view that rendered the element, or the
 * control that owns it.
 * </p>
 *
 * @see ElementPicker#start(com.top_logic.layout.react.ReactContext,
 *      com.top_logic.layout.react.ReactContext, PickKind, java.util.function.Consumer)
 */
public sealed interface PickResult permits PickResult.ViewPicked, PickResult.ControlPicked {

	/**
	 * The kind of pick this result answers.
	 */
	PickKind kind();

	/**
	 * The window the user clicked in.
	 */
	String windowName();

	/**
	 * The result of a {@link PickKind#VIEW} pick.
	 *
	 * @param windowName
	 *        The window the user clicked in.
	 * @param sourcePath
	 *        Path of the view source file that rendered the clicked element, as the client read it
	 *        from the element's {@code data-view-source} attribute.
	 */
	record ViewPicked(String windowName, String sourcePath) implements PickResult {

		@Override
		public PickKind kind() {
			return PickKind.VIEW;
		}
	}

	/**
	 * The result of a {@link PickKind#CONTROL} pick.
	 *
	 * @param windowName
	 *        The window the user clicked in.
	 * @param queue
	 *        The update queue of that window, which holds the control and receives its updates.
	 * @param control
	 *        The innermost control rendering the clicked element.
	 */
	record ControlPicked(String windowName, SSEUpdateQueue queue, ReactControl control) implements PickResult {

		@Override
		public PickKind kind() {
			return PickKind.CONTROL;
		}
	}
}

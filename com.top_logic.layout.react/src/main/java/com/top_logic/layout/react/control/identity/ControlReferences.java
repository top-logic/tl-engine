/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import com.top_logic.layout.react.control.ReactControl;

/**
 * The controls of one window, by the references a command argument names them with.
 *
 * <p>
 * A command argument naming another control of the window (see
 * {@link DisplayedObjectId#control()}) holds either the control id, which holds only in the
 * session, or the semantic address of the control, which a recorded step carries and which
 * resolves in every session displaying the same view.
 * </p>
 *
 * @see com.top_logic.layout.react.scripting.ScriptingSession
 */
public interface ControlReferences {

	/**
	 * The control of the window the given reference designates.
	 *
	 * @param reference
	 *        The control id or the semantic address of the control.
	 * @return The control, {@code null} if the reference designates no control of the window.
	 */
	ReactControl controlOf(String reference);

	/**
	 * The replay-stable reference of the given control: its semantic address.
	 *
	 * @param control
	 *        A control of the window.
	 * @return The address, {@code null} if the control has none, e.g. one that is not part of the
	 *         visible projection.
	 */
	String addressOf(ReactControl control);

}

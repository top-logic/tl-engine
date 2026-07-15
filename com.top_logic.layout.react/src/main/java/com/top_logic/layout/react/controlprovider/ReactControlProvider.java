/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.controlprovider;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Generic provider that creates {@link ReactControl}s for model objects.
 *
 * <p>
 * Used by composite controls (e.g. tree) to delegate content rendering. Given a model object,
 * creates an appropriate {@link ReactControl} to display it.
 * </p>
 */
@FunctionalInterface
public interface ReactControlProvider {

	/**
	 * Creates a {@link ReactControl} for the given model object.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The application model object.
	 * @return A {@link ReactControl} to render the object. Must not be {@code null}.
	 */
	ReactControl createControl(ReactContext context, Object model);
}

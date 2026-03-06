/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

/**
 * Deprecated compatibility alias.
 *
 * <p>
 * The canonical location is {@link com.top_logic.layout.react.ViewCommandAction}.
 * </p>
 *
 * @deprecated Use {@link com.top_logic.layout.react.ViewCommandAction} instead.
 */
@Deprecated
@FunctionalInterface
public interface ViewCommandAction extends com.top_logic.layout.react.ViewCommandAction {
	// Re-export from the canonical location.
}

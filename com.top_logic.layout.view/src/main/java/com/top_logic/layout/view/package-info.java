/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Declarative view configuration layer.
 *
 * <p>
 * A view is a tree of {@link com.top_logic.layout.view.UIElement} configurations parsed from
 * {@code .view.xml} files. The UIElement tree is shared across all sessions (stateless factory).
 * Each session gets its own {@link com.top_logic.layout.Control} tree by calling
 * {@link com.top_logic.layout.view.UIElement#createControl(ViewContext)}.
 * </p>
 */
package com.top_logic.layout.view;

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Headless interface over the React view-system control tree.
 *
 * <p>
 * The view system already exposes a server-side state tree to the browser and accepts commands back;
 * this package turns that same tree into a non-browser control surface. An {@link
 * com.top_logic.layout.react.scripting.ScriptingSession} lets a consumer {@link
 * com.top_logic.layout.react.scripting.ScriptingSession#observe() observe} the current screen as an
 * addressable {@link com.top_logic.layout.react.scripting.ScriptingNodeView} and {@link
 * com.top_logic.layout.react.scripting.ScriptingSession#act act} on it by semantic address — reusing the
 * very same {@link com.top_logic.layout.react.control.ReactControl#executeCommand command dispatch}
 * the browser uses.
 * </p>
 *
 * <p>
 * The two consumers are the script recorder (which captures a user's actions as address+command
 * tuples for replay as tests or data-setup scripts) and an agent driving a session on a user's
 * behalf. Both are clients of the same interface; the
 * load-bearing piece is the {@link com.top_logic.layout.react.scripting.ScriptingTreeProjector semantic
 * addressing} that gives controls stable, meaningful handles independent of their opaque per-session
 * IDs. A control may implement {@link com.top_logic.layout.react.scripting.ScriptingNode} to refine its
 * advertised role, name, state and action schema.
 * </p>
 *
 * <p>
 * The interface is in-process. Reaching it from outside the server is a separate concern with its
 * own admission rules and is therefore an application's choice: the optional module
 * {@code tl-layout-view-agent} publishes it over HTTP for a consumer that is not a browser.
 * </p>
 */
package com.top_logic.layout.react.scripting;

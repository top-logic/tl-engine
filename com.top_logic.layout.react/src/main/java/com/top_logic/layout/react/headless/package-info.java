/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Headless agent interface over the React view-system control tree.
 *
 * <p>
 * The view system already exposes a server-side state tree to the browser and accepts commands back;
 * this package turns that same tree into a non-browser control surface. An {@link
 * com.top_logic.layout.react.headless.AgentSession} lets a consumer {@link
 * com.top_logic.layout.react.headless.AgentSession#observe() observe} the current screen as an
 * addressable {@link com.top_logic.layout.react.headless.AgentNodeView} and {@link
 * com.top_logic.layout.react.headless.AgentSession#act act} on it by semantic address — reusing the
 * very same {@link com.top_logic.layout.react.control.ReactControl#executeCommand command dispatch}
 * the browser uses.
 * </p>
 *
 * <p>
 * The two intended consumers are the script recorder (which captures a user's actions as
 * address+command tuples for replay as tests or data-setup scripts) and an AI agent (which observes,
 * decides, and acts to drive a session autonomously). Both are clients of the same interface; the
 * load-bearing piece is the {@link com.top_logic.layout.react.headless.AgentTreeProjector semantic
 * addressing} that gives controls stable, meaningful handles independent of their opaque per-session
 * IDs. A control may implement {@link com.top_logic.layout.react.headless.AgentNode} to refine its
 * advertised role, name, state and action schema.
 * </p>
 */
package com.top_logic.layout.react.headless;

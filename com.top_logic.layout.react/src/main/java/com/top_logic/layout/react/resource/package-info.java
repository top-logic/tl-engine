/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Consolidated client-resource registry for the new React-based UI layer.
 *
 * <p>
 * Feature modules contribute their client resources (ES module scripts, stylesheets) to the single
 * {@link com.top_logic.layout.react.resource.ClientResources} service. At page rendering time the
 * service emits the corresponding head references (stylesheet links, an aggregated import map and
 * module scripts) in dependency order.
 * </p>
 *
 * <p>
 * The registry only resolves and references resources; it never aggregates them at runtime.
 * Optional bundling and minification for a production deployment is performed by a separate deploy
 * step that re-uses the same {@link com.top_logic.layout.react.resource.ResourceResolver}.
 * </p>
 */
package com.top_logic.layout.react.resource;

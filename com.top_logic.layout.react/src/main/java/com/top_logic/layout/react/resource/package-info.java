/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Consolidated client-resource registry for the React-based UI layer.
 *
 * <p>
 * Feature modules contribute their client resources (ES module scripts, stylesheets) to the single
 * {@link com.top_logic.layout.react.resource.ClientResources} service. At page rendering time the
 * service emits the corresponding head references (stylesheet links, an aggregated import map and
 * module scripts) in dependency order.
 * </p>
 *
 * <p>
 * The registry resolves and references resources; it does not aggregate them.
 * </p>
 */
package com.top_logic.layout.react.resource;

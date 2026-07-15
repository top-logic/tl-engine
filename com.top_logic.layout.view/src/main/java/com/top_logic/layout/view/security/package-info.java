/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Access control for the declarative view system.
 *
 * <p>
 * Security scopes are defined centrally in the {@link com.top_logic.layout.view.security.SecurityScopeService}
 * (a tree of {@code id}/{@code label} entries with grouping labels) and referenced from removable UI
 * units (nav-items, tabs, tiles) via an {@code <access-control>} reference. A unit guarded by an
 * inaccessible scope is omitted entirely. Units without an {@code <access-control>} are always shown.
 * </p>
 *
 * @see com.top_logic.layout.view.security.SecurityScopeService
 * @see com.top_logic.layout.view.security.AccessControl
 */
package com.top_logic.layout.view.security;

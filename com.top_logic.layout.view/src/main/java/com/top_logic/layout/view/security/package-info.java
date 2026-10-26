/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Access control for the declarative view system.
 *
 * <p>
 * The decisions are made by the model access rights (the grants of the
 * {@link com.top_logic.model.security.SecurityConfigurationService}). UI units (navigation items,
 * sidebar groups, tabs, accordion sections, tiles) declare with an
 * {@link com.top_logic.layout.view.security.AccessControl} which operation is checked on which
 * object; {@link com.top_logic.layout.view.security.ModelAccessPolicy} maps a decision to hiding or
 * disabling, and {@link com.top_logic.layout.view.security.ModelAccessRule} checks the operation of a
 * command.
 * </p>
 *
 * @see com.top_logic.layout.view.security.AccessControl
 * @see com.top_logic.layout.view.security.UnitAccess
 * @see com.top_logic.layout.view.security.SecuredObject
 */
package com.top_logic.layout.view.security;

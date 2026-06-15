/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Minimal administration area for the declarative view system: account, group and role management.
 *
 * <p>
 * Most of the admin UI is composed declaratively in {@code /WEB-INF/views/admin/*.view.xml}
 * (master-detail tables and create dialogs). This package holds the few Java building blocks that
 * cannot be expressed in TL-Script, notably {@link com.top_logic.layout.view.admin.CreateAccountAction}
 * (an account needs an authentication device, so it cannot be created by a plain {@code new(...)}).
 * </p>
 */
package com.top_logic.layout.view.admin;

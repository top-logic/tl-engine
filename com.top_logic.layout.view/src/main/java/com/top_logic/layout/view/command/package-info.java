/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Command infrastructure for the view system.
 *
 * <p>
 * Provides {@link com.top_logic.layout.view.command.ViewCommand} as a replacement for the legacy
 * {@link com.top_logic.tool.boundsec.CommandHandler} which depends on
 * {@link com.top_logic.mig.html.layout.LayoutComponent}. View commands are stateless handlers
 * instantiated from configuration. Data flows through channels, not untyped argument maps.
 * </p>
 *
 * @see com.top_logic.layout.view.command.ViewCommand
 * @see com.top_logic.layout.view.command.ViewExecutabilityRule
 * @see com.top_logic.layout.view.command.ViewCommandConfirmation
 */
package com.top_logic.layout.view.command;

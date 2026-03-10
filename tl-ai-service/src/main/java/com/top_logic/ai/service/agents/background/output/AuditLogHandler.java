/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.output;

/**
 * Output handler that logs background agent results to the audit log.
 *
 * <p>
 * The presence of this handler in a background agent's configuration enables audit logging for the
 * agent's results. No additional properties are required.
 * </p>
 */
public interface AuditLogHandler extends OutputHandler {

	// No additional properties — presence triggers audit logging.
}

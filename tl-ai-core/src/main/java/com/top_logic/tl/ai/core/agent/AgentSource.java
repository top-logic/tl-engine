/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Origin of an agent or skill definition.
 */
public enum AgentSource {

	/** Defined in service configuration XML (TopLogic-provided, read-only). */
	CONFIG,

	/** Defined in the knowledge base (application-specific). */
	KB,

	/** KB agent that overrides a config-defined agent with the same name. */
	KB_OVERRIDE;
}

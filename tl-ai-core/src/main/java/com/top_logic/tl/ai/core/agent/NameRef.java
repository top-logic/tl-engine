/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.config.NamedConfigMandatory;

/**
 * Named reference to a tool, skill, agent, or MCP server.
 *
 * <p>
 * Used as the entry type for keyed lists in
 * {@link TopLogicAgentDefinitions.AgentConfig}. Storing the references as keyed list entries
 * (rather than a single comma-separated string) enables per-entry overlay across configuration
 * layers: an application module may add or remove a single entry without restating the entire
 * platform-provided list.
 * </p>
 */
public interface NameRef extends NamedConfigMandatory {

	// No additional properties — the name is the entry.

}

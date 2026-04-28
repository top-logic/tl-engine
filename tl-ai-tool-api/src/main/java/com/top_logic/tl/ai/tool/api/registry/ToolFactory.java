/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api.registry;

import java.util.List;

import com.top_logic.tl.ai.tool.api.Tool;

/**
 * Factory for creating {@link Tool} instances.
 *
 * <p>
 * A tool factory is responsible for producing a list of tools that can be used by AI agents. Each
 * factory can create multiple tools, and the tools are identified by their names within the
 * factory's namespace.
 * </p>
 */
public interface ToolFactory {

	/**
	 * Creates the list of tools provided by this factory.
	 *
	 * <p>
	 * This method is typically called once during service initialization. The returned tools will
	 * be registered in the {@link ToolRegistryService} and made available to AI agents.
	 * </p>
	 *
	 * @return A list of tools created by this factory. Must not be null.
	 */
	List<Tool> createTools();

}

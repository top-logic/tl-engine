/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.registry;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * A tool that combines a {@link ToolSpecification} with its corresponding {@link ToolExecutor}.
 *
 * <p>
 * This class bundles the metadata (specification) and execution logic (executor) of a tool,
 * making it easier to manage tools that can be used by AI agents.
 * </p>
 */
public class ToolImplementation {

	private final ToolSpecification _specification;

	private final ToolExecutor _executor;

	/**
	 * Creates a new {@link ToolImplementation}.
	 *
	 * @param specification
	 *        The tool specification describing the tool's interface.
	 * @param executor
	 *        The executor that implements the tool's functionality.
	 */
	public ToolImplementation(ToolSpecification specification, ToolExecutor executor) {
		_specification = specification;
		_executor = executor;
	}

	/**
	 * Returns the tool specification.
	 *
	 * <p>
	 * The specification contains metadata about the tool, including its name, description, and
	 * parameters.
	 * </p>
	 */
	public ToolSpecification getSpecification() {
		return _specification;
	}

	/**
	 * Returns the tool executor.
	 *
	 * <p>
	 * The executor is responsible for actually running the tool when invoked by an AI agent.
	 * </p>
	 */
	public ToolExecutor getExecutor() {
		return _executor;
	}

	/**
	 * Returns the name of this tool.
	 *
	 * <p>
	 * This is a convenience method that returns the name from the tool specification.
	 * </p>
	 */
	public String getName() {
		return _specification.name();
	}

}

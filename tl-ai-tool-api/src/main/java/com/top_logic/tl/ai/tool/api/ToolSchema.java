/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.Objects;

/**
 * Schema definition for a tool that the LLM can call.
 *
 * @param name            the tool name
 * @param description     human-readable description of what the tool does
 * @param inputSchemaJson JSON Schema for the tool's input parameters (raw JSON string)
 */
public record ToolSchema(
		String name,
		String description,
		String inputSchemaJson) {

	/** */
	public ToolSchema {
		Objects.requireNonNull(name, "Tool name must not be null.");
		Objects.requireNonNull(description, "Description must not be null.");
		Objects.requireNonNull(inputSchemaJson, "Input schema must not be null.");
	}
}

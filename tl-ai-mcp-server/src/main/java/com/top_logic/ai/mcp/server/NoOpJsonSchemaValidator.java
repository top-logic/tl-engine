/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.util.Map;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * A no-op {@link JsonSchemaValidator} that accepts all tool results without validation.
 *
 * <p>
 * The MCP SDK's default validator ({@code DefaultJsonSchemaValidator}) depends on
 * {@code com.networknt:json-schema-validator:1.5.7}. However, the TopLogic parent POM manages
 * that library to version 3.0.0, which has an incompatible API (the class
 * {@code com.networknt.schema.SpecVersion$VersionFlag} was removed in 3.0.0). This causes a
 * {@link NoClassDefFoundError} at startup.
 * </p>
 *
 * <p>
 * Instead of forcing every consuming application to override the dependency version in their
 * POM, this validator replaces the default entirely. Schema validation is only used by the MCP
 * SDK to validate structured tool results against an {@code outputSchema}. Since our tools
 * return plain text content (not structured output with schemas), this validator is never
 * called in practice.
 * </p>
 */
public class NoOpJsonSchemaValidator implements JsonSchemaValidator {

	@Override
	public ValidationResponse validate(Map<String, Object> schema, Object structuredContent) {
		String json = (structuredContent instanceof String s) ? s : String.valueOf(structuredContent);
		return ValidationResponse.asValid(json);
	}

}

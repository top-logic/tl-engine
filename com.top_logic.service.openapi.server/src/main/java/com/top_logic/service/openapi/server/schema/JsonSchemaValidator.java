/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.schema;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * A compiled JSON schema that checks JSON values.
 *
 * <p>
 * Instances are created by {@link JsonSchemaCompiler#compile(String)}. A {@link JsonSchemaValidator}
 * is thread-safe.
 * </p>
 */
public class JsonSchemaValidator {

	/**
	 * Location of the whole value in a {@link #validate(String) violation}.
	 */
	public static final String ROOT_LOCATION = "$";

	private final Schema _schema;

	/**
	 * Creates a {@link JsonSchemaValidator}.
	 *
	 * @param schema
	 *        The compiled schema.
	 */
	JsonSchemaValidator(Schema schema) {
		_schema = schema;
	}

	/**
	 * Checks the given JSON value against the schema.
	 *
	 * @param json
	 *        The JSON value in textual form.
	 * @return The violations of the schema, each in the form <code>location: reason</code>, where
	 *         the location is a JSON path starting with {@link #ROOT_LOCATION}, e.g.
	 *         <code>$.release.version: integer found, string expected</code>. The result is empty,
	 *         if the value matches the schema. A value that is not valid JSON results in a single
	 *         violation for the whole value.
	 */
	public List<String> validate(String json) {
		JsonNode value;
		try {
			value = JsonMapperFactory.getInstance().readTree(json);
		} catch (JacksonException ex) {
			return Collections.singletonList(ROOT_LOCATION + ": invalid JSON: " + ex.getOriginalMessage());
		}
		List<Error> errors = _schema.validate(value);
		return errors.stream().map(JsonSchemaValidator::format).collect(Collectors.toList());
	}

	private static String format(Error error) {
		return error.getInstanceLocation() + ": " + error.getMessage();
	}

}

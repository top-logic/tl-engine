/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Controls the format of the LLM response.
 *
 * <p>
 * Three shapes, in increasing strength: {@link #TEXT} says nothing (the provider default),
 * {@link #JSON} asks for a JSON object without saying which, and
 * {@link #jsonSchema(String, Map, boolean) a JSON schema} says exactly which. Only the last one
 * makes the answer parseable without defensive handling, which is why it exists: a JSON mode still
 * lets the model choose its own field names.
 * </p>
 *
 * <p>
 * Not every provider can honour every shape - Anthropic has structured output but no untyped JSON
 * mode, the Bedrock Converse API has neither. A request builder that cannot express what was asked
 * for fails with a {@link ProviderException} of kind
 * {@link ProviderException.Kind#INVALID_REQUEST}; it never drops the constraint silently, because a
 * caller that asked for JSON and receives prose has no way to tell that its request was ignored.
 * </p>
 *
 * @param kind
 *        Which of the three shapes this is; never <code>null</code>.
 * @param schemaName
 *        The name the schema is registered under with the provider (<code>OpenAI</code> requires
 *        one); <code>null</code> unless {@link #kind()} is {@link Kind#JSON_SCHEMA}.
 * @param schema
 *        The JSON schema the answer must satisfy, as the nested {@link Map}/{@link java.util.List}
 *        structure of its JSON; <code>null</code> unless {@link #kind()} is
 *        {@link Kind#JSON_SCHEMA}.
 * @param strict
 *        Whether the provider must reject its own output when it does not validate, for the APIs
 *        that have such a flag. Only meaningful for {@link Kind#JSON_SCHEMA}. The strict mode of
 *        <code>OpenAI</code> additionally requires {@code additionalProperties: false} and a
 *        {@code required} entry for every property, so it is off unless the caller asks for it.
 */
public record ResponseFormat(ResponseFormat.Kind kind, String schemaName, Map<String, Object> schema,
		boolean strict) {

	/** The shape of a {@link ResponseFormat}. */
	public enum Kind {

		/** Plain text response (default). */
		TEXT,

		/** Some JSON object, with no schema saying which. */
		JSON,

		/** A JSON object satisfying the schema this format carries. */
		JSON_SCHEMA;
	}

	/** The name a schema gets when the caller did not name it. */
	public static final String DEFAULT_SCHEMA_NAME = "response";

	/** Plain text response (default). */
	public static final ResponseFormat TEXT = new ResponseFormat(Kind.TEXT, null, null, false);

	/** Structured JSON object response, with no schema constraining it. */
	public static final ResponseFormat JSON = new ResponseFormat(Kind.JSON, null, null, false);

	/** */
	public ResponseFormat {
		Objects.requireNonNull(kind, "Kind must not be null.");
		if (kind == Kind.JSON_SCHEMA) {
			Objects.requireNonNull(schema, "A JSON schema response format must carry a schema.");
			Objects.requireNonNull(schemaName, "A JSON schema response format must carry a name.");
		}
		// A defensive copy that tolerates null values: a JSON schema may legitimately contain
		// "default": null, which Map.copyOf() rejects. LinkedHashMap also keeps the authored order,
		// so the schema reads on the wire as it was written.
		schema = schema == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(schema));
	}

	/**
	 * A response constrained to the given JSON schema.
	 *
	 * @param name
	 *        The name to register the schema under; <code>null</code> or blank uses
	 *        {@link #DEFAULT_SCHEMA_NAME}.
	 * @param schema
	 *        The JSON schema as the nested structure of its JSON; must not be <code>null</code>.
	 * @param strict
	 *        See {@link #strict()}.
	 */
	public static ResponseFormat jsonSchema(String name, Map<String, Object> schema, boolean strict) {
		String effectiveName = name == null || name.isBlank() ? DEFAULT_SCHEMA_NAME : name;
		return new ResponseFormat(Kind.JSON_SCHEMA, effectiveName, schema, strict);
	}

	/** Whether this format asks for JSON at all - with or without a schema. */
	public boolean isJson() {
		return kind != Kind.TEXT;
	}
}

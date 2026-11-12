/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON Schema of the arguments of a command - of its typed argument interface or of its
 * {@link #forParams(String, List) hand-declared parameters} -, under the id an observation lists it
 * by.
 *
 * <p>
 * An observation describes each schema once, in its {@link ScriptingNodeView#SCHEMAS} map, and an
 * action refers to it by {@link #reference()}. References inside the schema (to its own
 * {@code $defs}) are relative to the observation as well, so the schema is valid where the
 * observation lists it.
 * </p>
 *
 * @param id
 *        The key of the schema in the {@link ScriptingNodeView#SCHEMAS} map of an observation:
 *        readable and unique among the argument schemas of the application.
 * @param schema
 *        The JSON Schema as parsed JSON value ({@code Map}/{@code List}/scalar).
 */
public record ArgsSchema(String id, Object schema) {

	/**
	 * Prefix of {@link #reference()}: the path of the {@link ScriptingNodeView#SCHEMAS} map in an
	 * observation as JSON pointer fragment.
	 */
	public static final String REFERENCE_PREFIX = "#/" + ScriptingNodeView.SCHEMAS + "/";

	private static final String SCHEMA_TYPE = "type";

	private static final String SCHEMA_DESCRIPTION = "description";

	private static final String SCHEMA_PROPERTIES = "properties";

	private static final String SCHEMA_REQUIRED = "required";

	private static final String TYPE_OBJECT = "object";

	/**
	 * The JSON pointer of this schema within an observation, e.g.
	 * {@code "#/schemas/SelectRowArguments"}.
	 */
	public String reference() {
		return reference(id());
	}

	/**
	 * The object schema of the arguments of a command that takes its arguments as a raw
	 * {@link java.util.Map}, described by hand-declared parameters.
	 *
	 * @param id
	 *        See {@link #id()}.
	 * @param params
	 *        The parameters of the command.
	 * @return An object schema with one property per parameter, its type and description, and the
	 *         required parameters.
	 */
	public static ArgsSchema forParams(String id, List<ScriptingParam> params) {
		Map<String, Object> properties = new LinkedHashMap<>();
		List<String> required = new ArrayList<>();
		for (ScriptingParam param : params) {
			Map<String, Object> property = new LinkedHashMap<>();
			String type = jsonType(param.type());
			if (type != null) {
				property.put(SCHEMA_TYPE, type);
			}
			if (param.description() != null && !param.description().isEmpty()) {
				property.put(SCHEMA_DESCRIPTION, param.description());
			}
			properties.put(param.name(), property);
			if (param.required()) {
				required.add(param.name());
			}
		}
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put(SCHEMA_TYPE, TYPE_OBJECT);
		schema.put(SCHEMA_PROPERTIES, properties);
		if (!required.isEmpty()) {
			schema.put(SCHEMA_REQUIRED, required);
		}
		return new ArgsSchema(id, schema);
	}

	/**
	 * The JSON Schema type of an informal {@link ScriptingParam#type() parameter type}, or
	 * {@code null} if it names none.
	 */
	private static String jsonType(String type) {
		if (type == null) {
			return null;
		}
		switch (type) {
			case "int":
				return "integer";
			case "double":
				return "number";
			case "string":
			case "number":
			case "integer":
			case "boolean":
			case TYPE_OBJECT:
			case "array":
				return type;
			default:
				return null;
		}
	}

	/**
	 * The JSON pointer of the schema with the given id within an observation.
	 */
	public static String reference(String id) {
		return REFERENCE_PREFIX + id;
	}

}

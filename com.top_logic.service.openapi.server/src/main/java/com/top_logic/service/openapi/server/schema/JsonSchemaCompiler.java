/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.schema;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.path.PathType;
import com.networknt.schema.serialization.JsonMapperFactory;

import com.top_logic.service.openapi.common.document.ComponentsObject;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Compiles <i>OpenAPI</i> 3.0 schemas into {@link JsonSchemaValidator}s.
 *
 * <p>
 * A schema may reference the global schemas of the compiler in the form used in an <i>OpenAPI</i>
 * document, i.e. <code>#/components/schemas/&lt;name&gt;</code>. Global schemas may reference
 * each other the same way. To resolve these references, the global schemas are embedded into each
 * compiled schema under the {@link OpenapiDocument#COMPONENTS} member, so that a reference resolves
 * within the compiled document itself.
 * </p>
 *
 * <p>
 * A {@link JsonSchemaCompiler} is meant to compile all schemas of one service once, when the
 * service is set up. The resulting {@link JsonSchemaValidator}s are thread-safe.
 * </p>
 */
public class JsonSchemaCompiler {

	private static final Dialect DIALECT = Dialect.builder(Dialects.getOpenApi30())
		// The embedded global schemas are not a schema keyword.
		.keyword(new NonValidationKeyword(OpenapiDocument.COMPONENTS))
		.build();

	private final Map<String, SchemaObject> _globalSchemas;

	private final SchemaRegistry _registry;

	private ObjectNode _components;

	/**
	 * Creates a {@link JsonSchemaCompiler}.
	 *
	 * @param globalSchemas
	 *        The schemas that compiled schemas can reference by their name, see
	 *        {@link SchemaObject#getName()}.
	 */
	public JsonSchemaCompiler(Map<String, SchemaObject> globalSchemas) {
		_globalSchemas = globalSchemas;
		SchemaRegistryConfig config = SchemaRegistryConfig.builder()
			.pathType(PathType.LEGACY)
			.locale(Locale.ENGLISH)
			.build();
		_registry = SchemaRegistry.withDefaultDialect(DIALECT, builder -> builder.schemaRegistryConfig(config));
	}

	/**
	 * Whether the given schema value from a configuration defines a schema.
	 *
	 * <p>
	 * A <code>null</code> value or a value that consists of white space only defines no schema.
	 * </p>
	 */
	public static boolean isSchema(String schema) {
		return schema != null && !schema.isBlank();
	}

	/**
	 * Compiles the given schema.
	 *
	 * @param schema
	 *        The schema in JSON form, see {@link #isSchema(String)}.
	 * @return The {@link JsonSchemaValidator} checking values against the given schema.
	 * @throws TopLogicException
	 *         If the schema is not valid JSON, references a schema that does not exist, or cannot
	 *         be compiled for other reasons.
	 */
	public JsonSchemaValidator compile(String schema) throws TopLogicException {
		JsonNode schemaNode;
		try {
			schemaNode = readTree(schema);
		} catch (JacksonException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCHEMA_NOT_JSON__PROBLEM.fill(ex.getOriginalMessage()),
				ex);
		}
		if (!schemaNode.isObject()) {
			throw new TopLogicException(I18NConstants.ERROR_SCHEMA_NOT_OBJECT);
		}
		ObjectNode document = (ObjectNode) schemaNode;
		document.set(OpenapiDocument.COMPONENTS, components());

		try {
			Schema compiled = _registry.getSchema(document);
			// Resolves all references.
			compiled.initializeValidators();
			return new JsonSchemaValidator(compiled);
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCHEMA_NOT_COMPILABLE__PROBLEM.fill(message(ex)), ex);
		}
	}

	private static String message(RuntimeException ex) {
		if (ex instanceof SchemaException schemaProblem && schemaProblem.getError() != null) {
			return schemaProblem.getError().toString();
		}
		String message = ex.getMessage();
		return message != null ? message : ex.getClass().getName();
	}

	/**
	 * The {@link OpenapiDocument#COMPONENTS} member embedded into each compiled schema.
	 */
	private ObjectNode components() {
		if (_components == null) {
			ObjectNode schemas = JsonNodeFactory.instance.objectNode();
			for (Entry<String, SchemaObject> entry : _globalSchemas.entrySet()) {
				String name = entry.getKey();
				try {
					schemas.set(name, readTree(entry.getValue().getSchema()));
				} catch (JacksonException ex) {
					throw new TopLogicException(
						I18NConstants.ERROR_GLOBAL_SCHEMA_NOT_JSON__NAME__PROBLEM.fill(name, ex.getOriginalMessage()),
						ex);
				}
			}
			ObjectNode components = JsonNodeFactory.instance.objectNode();
			components.set(ComponentsObject.SCHEMAS, schemas);
			_components = components;
		}
		return _components;
	}

	private static JsonNode readTree(String json) throws JacksonException {
		return JsonMapperFactory.getInstance().readTree(json);
	}

}

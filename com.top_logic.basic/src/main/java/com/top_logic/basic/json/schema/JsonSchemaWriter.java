/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.schema;

import java.io.IOException;

import com.top_logic.basic.json.schema.model.Schema;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Utility for writing <i>JSON Schema</i> objects to JSON format.
 *
 * <p>
 * Provides convenient methods for serializing <i>JSON Schema</i> objects to JSON strings according
 * to the <i>JSON Schema 2020-12 specification</i>.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>
 * ObjectSchema personSchema = ObjectSchema.create()
 * 	.putProperty("name", StringSchema.create().setMinLength(1))
 * 	.putProperty("age", NumericSchema.create().setIntegerOnly(true).setMinimum(0))
 * 	.addRequired("name");
 *
 * JsonSchemaDocument document = JsonSchemaDocument.create()
 * 	.setSchemaDialectUri("https://json-schema.org/draft/2020-12/schema")
 * 	.setTitle("Person")
 * 	.setDescription("A person with name and age")
 * 	.setSchema(personSchema);
 *
 * String json = JsonSchemaWriter.toJson(document);
 * // Result:
 * // {
 * //   "$schema": "https://json-schema.org/draft/2020-12/schema",
 * //   "title": "Person",
 * //   "description": "A person with name and age",
 * //   "schema": {
 * //     "type": "object",
 * //     "properties": {
 * //       "name": {"type": "string", "minLength": 1},
 * //       "age": {"type": "integer", "minimum": 0}
 * //     },
 * //     "required": ["name"]
 * //   }
 * // }
 * </pre>
 */
public class JsonSchemaWriter {

	/**
	 * Serializes a <i>JSON Schema</i> to a JSON string.
	 *
	 * @param schema
	 *        The schema to serialize.
	 * @return The JSON representation of the schema.
	 * @throws RuntimeException
	 *         If serialization fails.
	 */
	public static String toJson(Schema schema) {
		return toJson(schema, false);
	}

	/**
	 * Serializes a <i>JSON Schema</i> to a JSON string with optional pretty-printing.
	 *
	 * @param schema
	 *        The schema to serialize.
	 * @param prettyPrint
	 *        Whether to format the JSON with indentation and line breaks.
	 * @return The JSON representation of the schema.
	 * @throws RuntimeException
	 *         If serialization fails.
	 */
	public static String toJson(Schema schema, boolean prettyPrint) {
		StringW buffer = new StringW();
		try (JsonWriter writer = new JsonWriter(buffer)) {
			if (prettyPrint) {
				writer.setIndent("\t");
			}
			schema.visit(JsonSchemaSerializer.INSTANCE, writer);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to serialize JSON Schema: " + ex.getMessage(), ex);
		}
		return buffer.toString();
	}

	/**
	 * Writes a <i>JSON Schema</i> to a {@link JsonWriter}.
	 *
	 * @param schema
	 *        The schema to write.
	 * @param writer
	 *        The {@link JsonWriter} to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void write(Schema schema, JsonWriter writer) throws IOException {
		schema.visit(JsonSchemaSerializer.INSTANCE, writer);
	}
}

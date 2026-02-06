/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json.schema;

import java.io.IOException;

import com.top_logic.basic.json.schema.model.AllOfSchema;
import com.top_logic.basic.json.schema.model.AnyOfSchema;
import com.top_logic.basic.json.schema.model.ArraySchema;
import com.top_logic.basic.json.schema.model.BooleanSchema;
import com.top_logic.basic.json.schema.model.ConditionalSchema;
import com.top_logic.basic.json.schema.model.ConstSchema;
import com.top_logic.basic.json.schema.model.DynamicRefSchema;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.FalseSchema;
import com.top_logic.basic.json.schema.model.JsonSchemaDocument;
import com.top_logic.basic.json.schema.model.NotSchema;
import com.top_logic.basic.json.schema.model.NullSchema;
import com.top_logic.basic.json.schema.model.NumericSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.OneOfSchema;
import com.top_logic.basic.json.schema.model.RefSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;
import com.top_logic.basic.json.schema.model.TrueSchema;

import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Visitor for serializing JSON Schema objects to JSON format.
 *
 * <p>
 * This visitor implements the JSON Schema 2020-12 specification serialization,
 * converting the object-oriented schema representation into standard JSON Schema format.
 * </p>
 *
 * <h3>Usage:</h3>
 * <pre>
 * Schema schema = ObjectSchema.create()
 *     .putProperty("name", StringSchema.create())
 *     .addRequired("name");
 *
 * JsonWriter writer = new JsonWriter(output);
 * schema.visit(JsonSchemaSerializer.INSTANCE, writer);
 * </pre>
 */
public class JsonSchemaSerializer implements Schema.Visitor<Void, JsonWriter, IOException> {

	/**
	 * Singleton instance.
	 */
	public static final JsonSchemaSerializer INSTANCE = new JsonSchemaSerializer();

	@Override
	public Void visit(TrueSchema self, JsonWriter out) throws IOException {
		// Boolean schema true - serializes as: true
		out.value(true);
		return null;
	}

	@Override
	public Void visit(FalseSchema self, JsonWriter out) throws IOException {
		// Boolean schema false - serializes as: false
		out.value(false);
		return null;
	}

	@Override
	public Void visit(RefSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeNonEmptyString(out, "$ref", self.getRef());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(DynamicRefSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeNonEmptyString(out, "$dynamicRef", self.getDynamicRef());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(NullSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		out.name("type");
		out.value("null");
		out.endObject();
		return null;
	}

	@Override
	public Void visit(BooleanSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		out.name("type");
		out.value("boolean");
		out.endObject();
		return null;
	}

	@Override
	public Void visit(StringSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		out.name("type");
		out.value("string");

		// String-specific validation
		writeNonZeroInt(out, "maxLength", self.getMaxLength());
		writeNonZeroInt(out, "minLength", self.getMinLength());
		writeNonEmptyString(out, "pattern", self.getPattern());
		writeNonEmptyString(out, "format", self.getFormat());
		writeNonEmptyString(out, "contentEncoding", self.getContentEncoding());
		writeNonEmptyString(out, "contentMediaType", self.getContentMediaType());
		writeSchema(out, "contentSchema", self.getContentSchema());

		out.endObject();
		return null;
	}

	@Override
	public Void visit(NumericSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		out.name("type");
		out.value(self.isIntegerOnly() ? "integer" : "number");

		// Numeric validation
		writeNonZeroDouble(out, "multipleOf", self.getMultipleOf());
		writeNonZeroDouble(out, "maximum", self.getMaximum());
		writeNonZeroDouble(out, "exclusiveMaximum", self.getExclusiveMaximum());
		writeNonZeroDouble(out, "minimum", self.getMinimum());
		writeNonZeroDouble(out, "exclusiveMinimum", self.getExclusiveMinimum());

		out.endObject();
		return null;
	}

	@Override
	public Void visit(ArraySchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		out.name("type");
		out.value("array");

		// Array validation
		writeNonZeroInt(out, "maxItems", self.getMaxItems());
		writeNonZeroInt(out, "minItems", self.getMinItems());
		if (self.isUniqueItems()) {
			out.name("uniqueItems");
			out.value(true);
		}

		// Array applicators
		writeSchemaArray(out, "prefixItems", self.getPrefixItems());
		writeSchema(out, "items", self.getItems());
		writeSchema(out, "contains", self.getContains());
		writeNonZeroInt(out, "minContains", self.getMinContains());
		writeNonZeroInt(out, "maxContains", self.getMaxContains());

		out.endObject();
		return null;
	}

	@Override
	public Void visit(ObjectSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		out.name("type");
		out.value("object");

		// Object validation
		writeNonZeroInt(out, "maxProperties", self.getMaxProperties());
		writeNonZeroInt(out, "minProperties", self.getMinProperties());
		writeStringArray(out, "required", self.getRequired());

		// Dependent required
		if (!self.getDependentRequired().isEmpty()) {
			out.name("dependentRequired");
			out.beginObject();
			for (var entry : self.getDependentRequired().entrySet()) {
				out.name(entry.getKey());
				out.beginArray();
				for (String value : entry.getValue().getValues()) {
					out.value(value);
				}
				out.endArray();
			}
			out.endObject();
		}

		// Object applicators
		writeSchemaMap(out, "properties", self.getProperties());
		writeSchemaMap(out, "patternProperties", self.getPatternProperties());
		writeSchema(out, "additionalProperties", self.getAdditionalProperties());
		writeSchema(out, "propertyNames", self.getPropertyNames());
		writeSchemaMap(out, "dependentSchemas", self.getDependentSchemas());

		out.endObject();
		return null;
	}

	@Override
	public Void visit(EnumSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);

		// Enum values (JSON-serialized)
		if (!self.getEnumLiterals().isEmpty()) {
			out.name("enum");
			out.beginArray();
			for (String literal : self.getEnumLiterals()) {
				out.value(literal);
			}
			out.endArray();
		}

		out.endObject();
		return null;
	}

	@Override
	public Void visit(ConstSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeNonEmptyString(out, "const", self.getConstValue());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(AllOfSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeSchemaArray(out, "allOf", self.getAllOf());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(AnyOfSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeSchemaArray(out, "anyOf", self.getAnyOf());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(OneOfSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeSchemaArray(out, "oneOf", self.getOneOf());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(NotSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeSchema(out, "not", self.getNot());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(ConditionalSchema self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeSchema(out, "if", self.getCondition());
		writeSchema(out, "then", self.getThenSchema());
		writeSchema(out, "else", self.getElseSchema());
		out.endObject();
		return null;
	}

	@Override
	public Void visit(JsonSchemaDocument self, JsonWriter out) throws IOException {
		out.beginObject();
		writeBaseFields(self, out);
		writeNonEmptyString(out, "$schema", self.getSchemaDialectUri());

		// Vocabulary (for meta-schemas)
		if (!self.getVocabulary().isEmpty()) {
			out.name("$vocabulary");
			out.beginObject();
			for (var entry : self.getVocabulary().entrySet()) {
				out.name(entry.getKey());
				out.value(entry.getValue());
			}
			out.endObject();
		}

		// Delegate to schema content if present
		if (self.getSchema() != null) {
			// Inline the schema content (merge with document)
			// For now, write as separate property
			writeSchema(out, "schema", self.getSchema());
		}

		out.endObject();
		return null;
	}

	// ===== Helper Methods =====

	/**
	 * Writes base schema fields common to all schema types.
	 */
	private void writeBaseFields(Schema schema, JsonWriter out) throws IOException {
		// Core vocabulary
		writeNonEmptyString(out, "$id", schema.getId());
		writeNonEmptyString(out, "$anchor", schema.getAnchor());
		writeNonEmptyString(out, "$dynamicAnchor", schema.getDynamicAnchor());
		writeNonEmptyString(out, "$comment", schema.getComment());

		// Definitions
		writeSchemaMap(out, "$defs", schema.getDefinitions());

		// Metadata annotations
		writeNonEmptyString(out, "title", schema.getTitle());
		writeNonEmptyString(out, "description", schema.getDescription());
		writeNonEmptyString(out, "default", schema.getDefaultValue());

		if (schema.isDeprecated()) {
			out.name("deprecated");
			out.value(true);
		}
		if (schema.isReadOnly()) {
			out.name("readOnly");
			out.value(true);
		}
		if (schema.isWriteOnly()) {
			out.name("writeOnly");
			out.value(true);
		}

		writeStringArray(out, "examples", schema.getExamples());
	}

	/**
	 * Writes a string field only if value is non-null and non-empty.
	 */
	private void writeNonEmptyString(JsonWriter out, String fieldName, String value) throws IOException {
		if (value != null && !value.isEmpty()) {
			out.name(fieldName);
			out.value(value);
		}
	}

	/**
	 * Writes an integer field only if value is non-null.
	 */
	private void writeNonZeroInt(JsonWriter out, String fieldName, Integer value) throws IOException {
		if (value != null) {
			out.name(fieldName);
			out.value(value);
		}
	}

	/**
	 * Writes a double field only if value is non-null.
	 */
	private void writeNonZeroDouble(JsonWriter out, String fieldName, Double value) throws IOException {
		if (value != null) {
			out.name(fieldName);
			out.value(value);
		}
	}

	/**
	 * Writes a string array field only if list is non-empty.
	 */
	private void writeStringArray(JsonWriter out, String fieldName, java.util.List<String> values)
			throws IOException {
		if (values != null && !values.isEmpty()) {
			out.name(fieldName);
			out.beginArray();
			for (String value : values) {
				out.value(value);
			}
			out.endArray();
		}
	}

	/**
	 * Writes a schema field only if schema is non-null.
	 */
	private void writeSchema(JsonWriter out, String fieldName, Schema schema) throws IOException {
		if (schema != null) {
			out.name(fieldName);
			schema.visit(this, out);
		}
	}

	/**
	 * Writes an array of schemas.
	 */
	private void writeSchemaArray(JsonWriter out, String fieldName, java.util.List<Schema> schemas)
			throws IOException {
		if (schemas != null && !schemas.isEmpty()) {
			out.name(fieldName);
			out.beginArray();
			for (Schema schema : schemas) {
				schema.visit(this, out);
			}
			out.endArray();
		}
	}

	/**
	 * Writes a map of schemas.
	 */
	private void writeSchemaMap(JsonWriter out, String fieldName, java.util.Map<String, Schema> schemaMap)
			throws IOException {
		if (schemaMap != null && !schemaMap.isEmpty()) {
			out.name(fieldName);
			out.beginObject();
			for (var entry : schemaMap.entrySet()) {
				out.name(entry.getKey());
				entry.getValue().visit(this, out);
			}
			out.endObject();
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.io.IOException;
import java.util.List;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLType;
import com.top_logic.model.search.configured.ScriptParameter;

/**
 * Builds a JSON Schema (Draft 2020-12 object schema) string describing the input parameters of a
 * configured TL-Script tool.
 *
 * <p>
 * Object-typed parameters (TLClass) are exposed as strings carrying TopLogic IDs (TIDs); the
 * executor resolves them to wrappers before binding into the script scope.
 * </p>
 */
public final class ToolJsonSchema {

	private ToolJsonSchema() {
		// utility
	}

	/**
	 * Build the JSON Schema (Draft 2020-12 object schema) string for the given parameter list.
	 *
	 * @param parameters
	 *        Tool parameter declarations to describe. Must not be {@code null}.
	 * @return A JSON object string with {@code type}, {@code properties}, and {@code required}
	 *         members.
	 * @throws IllegalStateException
	 *         If the underlying {@link JsonWriter} fails — should not occur with the in-memory
	 *         buffer used here.
	 */
	public static String build(List<ScriptParameter> parameters) {
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("type").value("object");

				json.name("properties");
				json.beginObject();
				for (ScriptParameter p : parameters) {
					json.name(p.getName());
					json.beginObject();
					writeProperty(json, p);
					json.endObject();
				}
				json.endObject();

				json.name("required");
				json.beginArray();
				for (ScriptParameter p : parameters) {
					if (p.isMandatory()) {
						json.value(p.getName());
					}
				}
				json.endArray();

				json.endObject();
			}
			return buffer.toString();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to build tool input schema", ex);
		}
	}

	private static void writeProperty(JsonWriter json, ScriptParameter p) throws IOException {
		TLType type = p.getType().resolveType();
		if (p.isMultiple()) {
			json.name("type").value("array");
			json.name("items");
			json.beginObject();
			writeScalarType(json, type);
			json.endObject();
		} else {
			writeScalarType(json, type);
		}
		String desc = ToolDescriptions.englishPlainText(p.getDescription());
		if (type instanceof TLClass) {
			String hint = "Object ID (TID) of a " + type.getName()
				+ ". Format 'TypeName:id'. Obtain via a search or listing tool first.";
			if (desc.isEmpty()) {
				desc = hint;
			} else if (desc.endsWith(".")) {
				desc = desc + " " + hint;
			} else {
				desc = desc + ". " + hint;
			}
		}
		if (!desc.isEmpty()) {
			json.name("description").value(desc);
		}
	}

	private static void writeScalarType(JsonWriter json, TLType type) throws IOException {
		if (type instanceof TLPrimitive prim) {
			writePrimitive(json, prim.getKind());
		} else if (type instanceof TLEnumeration enumType) {
			json.name("type").value("string");
			json.name("enum");
			json.beginArray();
			for (TLClassifier c : enumType.getClassifiers()) {
				json.value(c.getName());
			}
			json.endArray();
		} else {
			// TLClass or anything else: pass as string (TID for objects).
			json.name("type").value("string");
		}
	}

	private static void writePrimitive(JsonWriter json, Kind kind) throws IOException {
		switch (kind) {
			case BOOLEAN, TRISTATE -> json.name("type").value("boolean");
			case INT              -> json.name("type").value("integer");
			case FLOAT            -> json.name("type").value("number");
			case DATE             -> {
				json.name("type").value("string");
				json.name("format").value("date-time");
			}
			case BINARY           -> {
				json.name("type").value("string");
				json.name("format").value("byte");
			}
			case STRING, CUSTOM   -> json.name("type").value("string");
		}
	}
}

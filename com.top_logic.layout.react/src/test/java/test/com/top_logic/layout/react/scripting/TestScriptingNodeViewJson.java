/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.react.scripting.ArgsSchema;
import com.top_logic.layout.react.scripting.ScriptingAction;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingParam;

/**
 * Test of the JSON rendering of {@link ScriptingNodeView}: the streaming variant
 * {@link ScriptingNodeView#writeJson(Appendable)} and the string variant
 * {@link ScriptingNodeView#toJson()} produce the same indented document, which lists each argument
 * schema once.
 */
public class TestScriptingNodeViewJson extends TestCase {

	/**
	 * The streamed JSON of an observation is exactly its string rendering, in the indented format.
	 */
	public void testStreamedEqualsString() throws Exception {
		ScriptingNodeView view = sampleTree();

		StringWriter streamed = new StringWriter();
		view.writeJson(streamed);

		assertEquals(view.toJson(), streamed.toString());
		assertEquals(EXPECTED, streamed.toString());
	}

	/**
	 * A value embedded with {@link ScriptingNodeView#writeValue(JsonWriter, Object)} into an
	 * enclosing document reads back as the map of the view.
	 */
	public void testEmbedded() throws Exception {
		ScriptingNodeView view = sampleTree();

		StringWriter out = new StringWriter();
		JsonWriter json = ScriptingNodeView.jsonWriter(out);
		json.beginObject();
		json.name("success").value(true);
		json.name("observation");
		ScriptingNodeView.writeValue(json, view.toMap());
		json.endObject();

		Map<?, ?> parsed = (Map<?, ?>) JSON.fromString(out.toString());
		assertEquals(Boolean.TRUE, parsed.get("success"));
		assertEquals(JSON.fromString(view.toJson()), parsed.get("observation"));
	}

	/**
	 * Control characters in a string are escaped, so that the document stays valid JSON.
	 */
	public void testControlCharacters() throws Exception {
		String text = "a\u0001b\"c\\d\ne";
		String json = ScriptingNodeView.toJson(Map.of("text", text));
		assertEquals("{\n  \"text\": \"a\\u0001b\\\"c\\\\d\\ne\"\n}", json);
		assertEquals(text, ((Map<?, ?>) JSON.fromString(json)).get("text"));
	}

	private static ScriptingNodeView sampleTree() {
		Map<String, Object> rowState = new LinkedHashMap<>();
		rowState.put("rows", List.of(row("r1", "Row 1"), row("r2", "Row 2")));
		rowState.put("totalRowCount", 2);
		rowState.put("empty", Map.of());
		rowState.put("none", List.of());
		rowState.put("missing", null);

		ScriptingAction select = new ScriptingAction("select", "select", SELECT_SCHEMA);
		ScriptingAction scroll = new ScriptingAction("scroll", "scroll", SCROLL_SCHEMA);
		ScriptingNodeView table = new ScriptingNodeView("/table[Items]", "table", "Items", "TLTable", rowState,
			List.of(select, scroll), List.of());
		ScriptingNodeView other = new ScriptingNodeView("/table[Other]", "table", "Other", "TLTable", Map.of(),
			List.of(scroll), List.of());
		return new ScriptingNodeView("/", "app", null, null, Map.of(), List.of(), List.of(table, other));
	}

	/**
	 * Each argument schema is listed once in the {@link ScriptingNodeView#SCHEMAS} of the
	 * observation, and every reference of an action resolves to it.
	 */
	public void testSchemasListedOnce() throws Exception {
		Map<?, ?> document = (Map<?, ?>) JSON.fromString(sampleTree().toJson());
		Map<?, ?> schemas = (Map<?, ?>) document.get(ScriptingNodeView.SCHEMAS);
		assertEquals(Map.of(SCROLL_SCHEMA.id(), SCROLL_SCHEMA.schema(), SELECT_SCHEMA.id(), SELECT_SCHEMA.schema()),
			schemas);

		int references = 0;
		for (Object child : (List<?>) document.get(ScriptingNodeView.CHILDREN)) {
			for (Object action : (List<?>) ((Map<?, ?>) child).get(ScriptingNodeView.ACTIONS)) {
				Object argsSchema = ((Map<?, ?>) action).get(ScriptingNodeView.ARGS_SCHEMA);
				if (argsSchema != null) {
					String ref = (String) ((Map<?, ?>) argsSchema).get(ScriptingNodeView.REF);
					assertTrue(ref, SCROLL_SCHEMA.reference().equals(ref) || SELECT_SCHEMA.reference().equals(ref));
					assertEquals(schemas.get(ref.substring(ArgsSchema.REFERENCE_PREFIX.length())),
						resolve(document, ref));
					references++;
				}
			}
		}
		assertEquals(3, references);
	}

	/**
	 * The value a JSON pointer fragment names in the given document.
	 */
	static Object resolve(Object document, String ref) {
		assertTrue(ref, ref.startsWith("#/"));
		Object result = document;
		for (String segment : ref.substring(2).split("/")) {
			result = ((Map<?, ?>) result).get(segment);
			assertNotNull("Unresolved reference: " + ref, result);
		}
		return result;
	}

	/**
	 * The compact rendering is the same JSON value as the indented one, without any whitespace.
	 */
	public void testCompact() throws Exception {
		ScriptingNodeView view = sampleTree();
		StringBuilder compact = new StringBuilder();
		ScriptingNodeView.writeJson(compact, view.toMap(), false);

		assertEquals(JSON.fromString(view.toJson()), JSON.fromString(compact.toString()));
		assertFalse(compact.toString().contains("\n"));
		assertFalse(compact.toString().contains(": "));
	}

	private static final ArgsSchema SELECT_SCHEMA = ArgsSchema.forParams("TableControl.select",
		List.of(new ScriptingParam("rowId", "string", true, "The row.")));

	private static final ArgsSchema SCROLL_SCHEMA = new ArgsSchema("ScrollArguments", Map.of("type", "object"));

	private static Map<String, Object> row(String id, String label) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", id);
		result.put("label", label);
		return result;
	}

	private static final String EXPECTED = String.join("\n",
		"{",
		"  \"address\": \"/\",",
		"  \"role\": \"app\",",
		"  \"children\": [",
		"    {",
		"      \"address\": \"/table[Items]\",",
		"      \"role\": \"table\",",
		"      \"name\": \"Items\",",
		"      \"module\": \"TLTable\",",
		"      \"state\": {",
		"        \"rows\": [",
		"          {",
		"            \"id\": \"r1\",",
		"            \"label\": \"Row 1\"",
		"          },",
		"          {",
		"            \"id\": \"r2\",",
		"            \"label\": \"Row 2\"",
		"          }",
		"        ],",
		"        \"totalRowCount\": 2,",
		"        \"empty\": {},",
		"        \"none\": [],",
		"        \"missing\": null",
		"      },",
		"      \"actions\": [",
		"        {",
		"          \"command\": \"select\",",
		"          \"label\": \"select\",",
		"          \"argsSchema\": {",
		"            \"$ref\": \"#/schemas/TableControl.select\"",
		"          }",
		"        },",
		"        {",
		"          \"command\": \"scroll\",",
		"          \"label\": \"scroll\",",
		"          \"argsSchema\": {",
		"            \"$ref\": \"#/schemas/ScrollArguments\"",
		"          }",
		"        }",
		"      ]",
		"    },",
		"    {",
		"      \"address\": \"/table[Other]\",",
		"      \"role\": \"table\",",
		"      \"name\": \"Other\",",
		"      \"module\": \"TLTable\",",
		"      \"actions\": [",
		"        {",
		"          \"command\": \"scroll\",",
		"          \"label\": \"scroll\",",
		"          \"argsSchema\": {",
		"            \"$ref\": \"#/schemas/ScrollArguments\"",
		"          }",
		"        }",
		"      ]",
		"    }",
		"  ],",
		"  \"schemas\": {",
		"    \"ScrollArguments\": {",
		"      \"type\": \"object\"",
		"    },",
		"    \"TableControl.select\": {",
		"      \"type\": \"object\",",
		"      \"properties\": {",
		"        \"rowId\": {",
		"          \"type\": \"string\",",
		"          \"description\": \"The row.\"",
		"        }",
		"      },",
		"      \"required\": [",
		"        \"rowId\"",
		"      ]",
		"    }",
		"  }",
		"}");

}

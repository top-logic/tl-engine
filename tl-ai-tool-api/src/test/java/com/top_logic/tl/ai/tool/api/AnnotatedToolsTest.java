/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for {@link AnnotatedTools} schema generation and argument coercion with complex types.
 */
public class AnnotatedToolsTest extends TestCase {

    public record Item(String name, int qty) {}
    public record Order(String customer, List<Item> items) {}

    public static class TestTools {
        // Marking is off here so that these tests assert the coerced arguments and nothing else;
        // what the framework puts around an answer is TestAnnotatedToolMarking's subject.
        @ToolMethod(description = "Submit an order", marking = AnswerMarking.NONE)
        public String submit(@P("the order") Order order) {
            return "ok:" + order.customer() + ":" + order.items().size();
        }
    }

    /** */
    public void testRecordSchemaIsNestedObject() {
        List<Tool> tools = AnnotatedTools.from(new TestTools());
        assertEquals(1, tools.size());
        String schema = tools.get(0).schema().inputSchemaJson();
        // Order is a record → should be an object with properties, not a String fallback.
        assertTrue("Schema should contain 'type:object' for order: " + schema,
            schema.contains("\"type\":\"object\""));
        assertTrue("Schema should describe customer and items: " + schema,
            schema.contains("\"customer\"") && schema.contains("\"items\""));
        // Nested record (Item) inside the array should also be an object.
        assertTrue("Schema should describe nested Item fields: " + schema,
            schema.contains("\"qty\""));
    }

    /** */
    public void testCoercionAcceptsMap() {
        Map<String, Object> orderArg = Map.of(
            "customer", "Alice",
            "items", List.of(Map.of("name", "Book", "qty", 2)));
        Tool tool = AnnotatedTools.from(new TestTools()).get(0);
        ToolResult result = tool.execute(Map.of("order", orderArg), null);
        assertTrue(result.text().startsWith("ok:Alice:1"));
    }

    /** */
    public void testCoercionAcceptsStringifiedJson() {
        // Some LLMs stringify nested args. We must handle that.
        String stringifiedOrder = "{\"customer\":\"Bob\",\"items\":[{\"name\":\"Pen\",\"qty\":5}]}";
        Tool tool = AnnotatedTools.from(new TestTools()).get(0);
        ToolResult result = tool.execute(Map.of("order", stringifiedOrder), null);
        assertTrue("Expected ok:Bob:1, got " + result.text(),
            result.text().startsWith("ok:Bob:1"));
    }

    /** Tools that take a map and a list of records, the shapes a model stringifies. */
    public static class FieldTools {

        @ToolMethod(description = "Set fields", marking = AnswerMarking.NONE)
        public String set(@P("the fields") Map<String, Object> fields) {
            return "fields:" + fields.get("definition");
        }

        @ToolMethod(description = "Submit orders", marking = AnswerMarking.NONE)
        public String orders(@P("the orders") List<Order> orders) {
            return "orders:" + orders.get(0).customer();
        }
    }

    /**
     * A stringified value whose German quote is closed with a plain quotation mark is read with
     * the typographic closing mark: the JSON is broken only by that mark.
     */
    public void testAQuoteClosedByAPlainMarkIsRead() {
        Tool tool = AnnotatedTools.from(new FieldTools()).get(0);
        String broken = "{\"definition\": \"Der \u201EBericht\" kommt montags.\", \"kind\": \"INFO\"}";

        ToolResult result = tool.execute(Map.of("fields", broken), null);

        assertEquals("fields:Der \u201EBericht\u201C kommt montags.", result.text());
    }

    /** The same inside a list of records, and for an English opening mark. */
    public void testAQuoteInAListOfRecordsIsRead() {
        Tool tool = AnnotatedTools.from(new FieldTools()).get(1);
        String broken = "[{\"customer\": \"The \u201Cbig\" shop\", \"items\": []}]";

        ToolResult result = tool.execute(Map.of("orders", broken), null);

        assertEquals("orders:The \u201Cbig\u201D shop", result.text());
    }

    /** A text that is no JSON for another reason says where it breaks, not only its type. */
    public void testATextThatIsNoJsonNamesTheFault() {
        Tool tool = AnnotatedTools.from(new FieldTools()).get(0);

        try {
            tool.execute(Map.of("fields", "{\"definition\": \"x\" \"kind\": 1}"), null);
            fail("A text that is no JSON was taken.");
        } catch (ToolArgumentCoercionException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("no valid JSON: "));
            assertTrue(ex.getMessage(), ex.getMessage().contains("column"));
            assertTrue(ex.getMessage(), ex.getMessage().contains("not as a string"));
        }
    }

    /** Valid JSON is read as it is: a plain mark that ends a string after an open quote stays. */
    public void testValidJsonIsNotRepaired() {
        Tool tool = AnnotatedTools.from(new FieldTools()).get(0);

        ToolResult result = tool.execute(Map.of("fields", "{\"definition\": \"\u201Eopen\"}"), null);

        assertEquals("fields:\u201Eopen", result.text());
    }

    /** Proves each element in a {@code List<Record>} is coerced, not left as a raw Map. */
    public void testListRecordCoercionWithMultipleElements() {
        Map<String, Object> orderArg = Map.of(
            "customer", "Carol",
            "items", List.of(
                Map.of("name", "Pen", "qty", 3),
                Map.of("name", "Notebook", "qty", 7),
                Map.of("name", "Eraser", "qty", 1)));
        Tool tool = AnnotatedTools.from(new TestTools()).get(0);
        ToolResult result = tool.execute(Map.of("order", orderArg), null);
        assertEquals("ok:Carol:3", result.text());
    }

    /** Tools whose group and mode the annotation declares, and one that declares neither. */
    public static class GroupedTools {

        @ToolMethod(description = "Read data", mode = ToolMode.READ, group = ToolGroups.DATA_READ)
        public String listThings() {
            return "";
        }

        @ToolMethod(description = "Forgot to say")
        public String unclassified() {
            return "";
        }
    }

    /**
     * The group declared on {@code @ToolMethod} reaches {@link Tool#group()}.
     *
     * <p>
     * The whole role-to-tool policy is decided on this value, so a broken hand-off here would silently
     * make every annotated tool ungrouped - which denies it. Worth pinning.
     * </p>
     */
    public void testDeclaredGroupReachesTheTool() {
        Tool listThings = byName(AnnotatedTools.from(new GroupedTools()), "listThings");

        assertEquals(ToolGroups.DATA_READ, listThings.group());
    }

    /** A tool that declares no group is ungrouped, which is what denies it by default. */
    public void testUndeclaredGroupIsEmpty() {
        Tool unclassified = byName(AnnotatedTools.from(new GroupedTools()), "unclassified");

        assertEquals("", unclassified.group());
    }

    private static Tool byName(List<Tool> tools, String name) {
        for (Tool tool : tools) {
            if (name.equals(tool.name())) {
                return tool;
            }
        }
        throw new AssertionError("No tool '" + name + "' among " + tools.size() + " scanned.");
    }
}

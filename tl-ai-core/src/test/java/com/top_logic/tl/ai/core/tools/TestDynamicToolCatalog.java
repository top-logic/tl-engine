/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests for {@link DynamicToolCatalog}.
 */
public class TestDynamicToolCatalog extends TestCase {

	/** Base tools are visible before any overlay is added. */
	public void testBaseToolVisible() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		assertNotNull(catalog.getTool("baseTool"));
	}

	/** Overlay tools become visible after addOverlay. */
	public void testOverlayToolVisible() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		ToolCatalog overlay = catalogWith("overlayTool");
		catalog.addOverlay(overlay);
		assertNotNull(catalog.getTool("overlayTool"));
	}

	/** Base tool is still reachable after overlay added. */
	public void testBaseStillVisibleAfterOverlay() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.addOverlay(catalogWith("overlayTool"));
		assertNotNull(catalog.getTool("baseTool"));
	}

	/** Unknown tool returns null. */
	public void testUnknownToolReturnsNull() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		assertNull(catalog.getTool("nonexistent"));
	}

	/** toolSchemas merges base and overlay schemas. */
	public void testToolSchemasMergesLayers() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.addOverlay(catalogWith("overlayTool"));
		List<ToolSchema> schemas = catalog.toolSchemas(t -> true);
		assertEquals(2, schemas.size());
	}

	/** Duplicate tool across layers is deduplicated (first registration wins). */
	public void testDuplicateToolDeduplication() {
		DynamicToolCatalog catalog = catalogWithBase("shared");
		catalog.addOverlay(catalogWith("shared"));
		List<ToolSchema> schemas = catalog.toolSchemas(t -> true);
		assertEquals(1, schemas.size());
	}

	/** toolSchemas respects the filter predicate. */
	public void testToolSchemasFilter() {
		DynamicToolCatalog catalog = catalogWithBase("readTool");
		catalog.addOverlay(catalogWith("writeTool"));
		List<ToolSchema> schemas = catalog.toolSchemas(t -> t.name().equals("readTool"));
		assertEquals(1, schemas.size());
		assertEquals("readTool", schemas.get(0).name());
	}

	/** Multiple overlays are searched in order. */
	public void testMultipleOverlays() {
		DynamicToolCatalog catalog = catalogWithBase("base");
		catalog.addOverlay(catalogWith("first"));
		catalog.addOverlay(catalogWith("second"));
		assertNotNull(catalog.getTool("first"));
		assertNotNull(catalog.getTool("second"));
		List<ToolSchema> schemas = catalog.toolSchemas(t -> true);
		assertEquals(3, schemas.size());
	}

	/**
	 * A tool of a skill that is not loaded yet is answered with "load that skill", not "no such tool".
	 *
	 * <p>
	 * The two absences look identical from the catalog - {@link DynamicToolCatalog#getTool(String)}
	 * returns <code>null</code> for both - and they take opposite actions from the model: one is
	 * corrected by picking another name, the other by loading a skill.
	 * </p>
	 */
	public void testToolOfAnUnloadedSkillSaysToLoadIt() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.setReachableBySkill(Set.of("skillTool"));

		assertTrue(catalog.isReachableBySkill("skillTool"));
		assertNull("An unloaded skill's tool must not be callable.", catalog.getTool("skillTool"));

		String error = errorOf(catalog, "skillTool");
		assertTrue(error, error.contains("skill of yours that is not loaded yet"));
		assertFalse("It exists, so this is not the unknown-tool case: " + error,
			error.contains("Unknown tool"));
	}

	/** A name no skill declares and nothing registers stays the plain unknown-tool answer. */
	public void testNameNothingKnowsStaysUnknown() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.setReachableBySkill(Set.of("skillTool"));

		String error = errorOf(catalog, "listEverythingEverywhere");
		assertTrue(error, error.contains("Unknown tool: 'listEverythingEverywhere'"));
		assertTrue("The hint the model corrects itself with must stay: " + error,
			error.contains("Available:"));
	}

	// ===== Helpers =====

	/** The message a call for the given name produces, with no security context involved. */
	private static String errorOf(DynamicToolCatalog catalog, String toolName) {
		ToolResultMessage result = catalog.execute(new ToolCall("c1", toolName, "{}"), null);
		assertTrue("Expected a refused call, got: " + result.state(),
			result.state() instanceof ToolState.Failed);
		return ((ToolState.Failed) result.state()).error();
	}

	private static DynamicToolCatalog catalogWithBase(String toolName) {
		ToolCatalog base = catalogWith(toolName);
		return new DynamicToolCatalog(base);
	}

	private static ToolCatalog catalogWith(String toolName) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(stubTool(toolName));
		catalog.freeze();
		return catalog;
	}

	private static Tool stubTool(String name) {
		return new Tool() {
			@Override public String name() { return name; }
			@Override public String description() { return "Stub tool: " + name; }
			@Override public ToolSchema schema() {
				return new ToolSchema(name, description(), "{\"type\":\"object\",\"properties\":{}}");
			}
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("ok");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
	}
}

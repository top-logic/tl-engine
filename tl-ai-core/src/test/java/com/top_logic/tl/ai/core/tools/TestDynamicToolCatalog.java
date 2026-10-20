/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.tool.api.Tool;
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

	/**
	 * The stable prefix comes first however late a framework overlay is added.
	 *
	 * <p>
	 * The shape {@code AgentFactory} really builds: the {@code SkillTool} restores a session's skills
	 * while it is constructed, and the framework overlays carrying {@code loadSkill} and
	 * {@code listTools} are added after it. Advertised in call order, a restored skill would sit
	 * where a loaded one never sits, and the two runs of one session would send the same tools as
	 * different bytes (Gitea #346).
	 * </p>
	 */
	public void testTheFrameworkPrefixPrecedesEverySkillOverlay() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.addSkillOverlay("restored", catalogWith("restoredTool"));
		catalog.addOverlay(catalogWith("loadSkill"));
		catalog.addSkillOverlay("loaded", catalogWith("loadedTool"));

		assertEquals(List.of("baseTool", "loadSkill", "restoredTool", "loadedTool"), names(catalog));
	}

	/** The boundary the providers place a cache breakpoint on is where the skills begin. */
	public void testTheStableCountIsTheBaseAndTheFrameworkOverlays() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.addOverlay(catalogWith("loadSkill"));
		assertEquals(2, catalog.stableToolCount(t -> true));

		catalog.addSkillOverlay("alpha", catalogWith("alphaTool"));
		assertEquals("A loaded skill must not move the boundary.", 2,
			catalog.stableToolCount(t -> true));
		assertEquals(3, catalog.toolSchemas(t -> true).size());
	}

	/** The boundary is counted through the same filter the schemas are. */
	public void testTheStableCountFollowsTheFilter() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.addOverlay(catalogWith("loadSkill"));
		catalog.addSkillOverlay("alpha", catalogWith("alphaTool"));

		Predicate<Tool> notLoadSkill = t -> !"loadSkill".equals(t.name());
		assertEquals(1, catalog.stableToolCount(notLoadSkill));
		assertEquals(List.of("baseTool", "alphaTool"),
			catalog.toolSchemas(notLoadSkill).stream().map(ToolSchema::name).toList());
	}

	/** Past the ceiling, the skill whose tools were used longest ago leaves. */
	public void testTheLeastRecentlyUsedSkillIsEvicted() {
		DynamicToolCatalog catalog = new DynamicToolCatalog(catalogWith("baseTool"), 2);
		catalog.addSkillOverlay("alpha", catalogWith("alphaTool"));
		catalog.addSkillOverlay("beta", catalogWith("betaTool"));

		// alpha is used again, so beta is now the one untouched for longest.
		catalog.execute(new ToolCall("c1", "alphaTool", "{}"), null);

		assertEquals(List.of("beta"), catalog.addSkillOverlay("gamma", catalogWith("gammaTool")));
		assertEquals(List.of("alpha", "gamma"), catalog.loadedSkills());
		assertNull("An evicted skill's tool is not callable.", catalog.getTool("betaTool"));
		assertNotNull(catalog.getTool("alphaTool"));
	}

	/** An evicted skill's tool is answered with "load that skill", not "no such tool". */
	public void testAnEvictedToolStaysReachableBySkill() {
		DynamicToolCatalog catalog = new DynamicToolCatalog(catalogWith("baseTool"), 1);
		catalog.setReachableBySkill(Set.of("alphaTool", "betaTool"));
		catalog.addSkillOverlay("alpha", catalogWith("alphaTool"));
		catalog.addSkillOverlay("beta", catalogWith("betaTool"));

		String error = errorOf(catalog, "alphaTool");
		assertTrue(error, error.contains("skill of yours that is not loaded yet"));
	}

	/** Loading the same skill twice changes nothing and costs nobody their place. */
	public void testLoadingTheSameSkillTwiceEvictsNothing() {
		DynamicToolCatalog catalog = new DynamicToolCatalog(catalogWith("baseTool"), 2);
		catalog.addSkillOverlay("alpha", catalogWith("alphaTool"));
		catalog.addSkillOverlay("beta", catalogWith("betaTool"));

		assertEquals(List.of(), catalog.addSkillOverlay("alpha", catalogWith("alphaTool")));
		assertEquals(List.of("alpha", "beta"), catalog.loadedSkills());
	}

	/** A compaction reaches whoever handed text to the conversation, and leaves the tools alone. */
	public void testACompactionIsAnnouncedAndChangesNoTool() {
		DynamicToolCatalog catalog = catalogWithBase("baseTool");
		catalog.addSkillOverlay("alpha", catalogWith("alphaTool"));
		List<String> heard = new ArrayList<>();
		catalog.onHistoryCompacted(() -> heard.add("compacted"));

		catalog.historyCompacted();

		assertEquals(List.of("compacted"), heard);
		assertEquals(List.of("alpha"), catalog.loadedSkills());
		assertNotNull(catalog.getTool("alphaTool"));
	}

	private static List<String> names(DynamicToolCatalog catalog) {
		return catalog.toolSchemas(t -> true).stream().map(ToolSchema::name).toList();
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
		return ToolStubs.builder(name).description("Stub tool: " + name).build();
	}
}

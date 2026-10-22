/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Which tools {@code <exposed-tools>} publishes, and under which name, title and description.
 */
public class TestMcpExposure extends TestCase {

	private final Map<String, Tool> _tools = new LinkedHashMap<>();

	private final Map<String, String> _groups = new LinkedHashMap<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		register("model-browser.listModules", ToolGroups.MODEL_READ);
		register("model-browser.listTypes", ToolGroups.MODEL_READ);
		register("data.search", ToolGroups.DATA_READ);
		register("script-exec.changeData", ToolGroups.DATA_WRITE);
	}

	/** Nothing configured, nothing published. */
	public void testNothingIsPublishedByDefault() {
		assertEquals(List.of(), resolve());
	}

	/** A tool entry publishes that tool and no other, with the defaults where it says nothing. */
	public void testAToolEntryWithDefaults() {
		List<McpExposure.Published> published = resolve(tool("data.search"));

		assertEquals(1, published.size());
		McpExposure.Published search = published.get(0);
		assertEquals("data.search", search.configName());
		assertEquals("The registry name, which is a valid MCP name.", "data__search", search.name());
		assertEquals("Search", search.title());
		assertEquals("Internal description of search.", search.description());
	}

	/** A tool entry gives the tool the name, title and description an external model reads. */
	public void testAToolEntryOverridesWhatIsPublished() {
		ExposedTool entry = tool("data.search");
		entry.setName("search_records");
		entry.setTitle("Search the records");
		entry.setDescription("\n  Searches the records the caller may read.\n");

		McpExposure.Published search = resolve(entry).get(0);

		assertEquals("search_records", search.name());
		assertEquals("Search the records", search.title());
		assertEquals("Searches the records the caller may read.", search.description());
	}

	/**
	 * A group entry publishes the group's tools, in the group the access policy sees, and a tool that
	 * has its own entry keeps what that entry says.
	 */
	public void testAGroupEntry() {
		ExposedTool modules = tool("model-browser.listModules");
		modules.setName("list_modules");

		List<McpExposure.Published> published = resolve(group(ToolGroups.MODEL_READ), modules);

		assertEquals(List.of("list_modules", "model-browser__listTypes"), names(published));
	}

	/** A tool the application does not have widens nothing and is skipped. */
	public void testAnUnknownToolIsSkipped() {
		assertEquals(List.of("data__search"), names(resolve(tool("no-such.tool"), tool("data.search"))));
	}

	/** An entry names a tool or a group: both, or neither, is refused. */
	public void testAnEntryNamesExactlyOneThing() {
		ExposedTool both = tool("data.search");
		both.setGroup(ToolGroups.DATA_READ);
		assertRefused(both);

		assertRefused(TypedConfiguration.newConfigItem(ExposedTool.class));
	}

	/** A name, title or description describes one tool, so a group entry cannot set one. */
	public void testAGroupEntryCannotRenameItsTools() {
		ExposedTool entry = group(ToolGroups.DATA_READ);
		entry.setTitle("Everything");
		assertRefused(entry);
	}

	/** A tool listed twice would say two things about how it is published. */
	public void testAToolIsListedOnce() {
		assertRefused(tool("data.search"), tool("data.search"));
	}

	/** A name a client's model API would refuse is refused here, where it is written. */
	public void testAnInvalidNameIsRefused() {
		ExposedTool dotted = tool("data.search");
		dotted.setName("data.search");
		assertRefused(dotted);

		ExposedTool tooLong = tool("data.search");
		tooLong.setName("a".repeat(65));
		assertRefused(tooLong);
	}

	/** Two tools under one name would make one of them uncallable. */
	public void testTwoToolsCannotShareAName() {
		ExposedTool search = tool("data.search");
		search.setName("find");
		ExposedTool modules = tool("model-browser.listModules");
		modules.setName("find");
		assertRefused(search, modules);
	}

	/** A camel-case tool name reads as a sentence-case title, and an acronym stays one. */
	public void testDerivedTitles() {
		assertEquals("List modules", McpExposure.titleOf("listModules"));
		assertEquals("Get TL script reference", McpExposure.titleOf("getTLScriptReference"));
		assertEquals("Web fetch", McpExposure.titleOf("webFetch"));
		assertEquals("Search", McpExposure.titleOf("search"));
	}

	private List<McpExposure.Published> resolve(ExposedTool... entries) {
		return McpExposure.resolve(List.of(entries), _tools, _groups::get);
	}

	private void assertRefused(ExposedTool... entries) {
		try {
			resolve(entries);
			fail("Refusal expected.");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	private static List<String> names(List<McpExposure.Published> published) {
		List<String> result = new ArrayList<>();
		for (McpExposure.Published tool : published) {
			result.add(tool.name());
		}
		return result;
	}

	private static ExposedTool tool(String configName) {
		ExposedTool entry = TypedConfiguration.newConfigItem(ExposedTool.class);
		entry.setTool(configName);
		return entry;
	}

	private static ExposedTool group(String group) {
		ExposedTool entry = TypedConfiguration.newConfigItem(ExposedTool.class);
		entry.setGroup(group);
		return entry;
	}

	private void register(String configName, String group) {
		String name = configName.substring(configName.indexOf('.') + 1);
		_tools.put(configName, new StubTool(name));
		_groups.put(configName, group);
	}

	private record StubTool(String name) implements Tool {

		@Override
		public String description() {
			return "Internal description of " + name + ".";
		}

		@Override
		public ToolSchema schema() {
			return new ToolSchema(name, description(), "{\"type\":\"object\",\"properties\":{}}");
		}

		@Override
		public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
			throw new AssertionError("Not called: only the declarations are published.");
		}
	}
}

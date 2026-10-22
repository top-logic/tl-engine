/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.mcp.McpTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests the hints this server publishes about its own tools, and what they do to a tl-ai consuming
 * them.
 *
 * @see McpToolAnnotations
 */
public class TestMcpToolAnnotations extends TestCase {

	/** A read publishes itself as one, and says so in both fields a client may consult. */
	public void testRead() {
		McpSchema.ToolAnnotations annotations = publish(ToolMode.READ, ToolGroups.MODEL_READ);

		assertEquals(Boolean.TRUE, annotations.readOnlyHint());
		assertEquals("Stated although a read cannot be destructive: the default of the field is"
			+ " TRUE, and a client may read it without the other.",
			Boolean.FALSE, annotations.destructiveHint());
	}

	/**
	 * A user-scoped change is a write, and the non-destructive kind.
	 *
	 * <p>
	 * The decision the ticket left open. Something did change, so {@code readOnlyHint} cannot be
	 * true; what it changed belongs to the account that called, so of the two kinds of write it is
	 * the one a client need not shout about.
	 * </p>
	 */
	public void testUserScoped() {
		McpSchema.ToolAnnotations annotations = publish(ToolMode.USER_SCOPED, ToolGroups.WORKSPACE);

		assertEquals(Boolean.FALSE, annotations.readOnlyHint());
		assertEquals(Boolean.FALSE, annotations.destructiveHint());
	}

	/** A write to shared data is destructive. */
	public void testDataWrite() {
		McpSchema.ToolAnnotations annotations = publish(ToolMode.DATA_WRITE, ToolGroups.DATA_WRITE);

		assertEquals(Boolean.FALSE, annotations.readOnlyHint());
		assertEquals(Boolean.TRUE, annotations.destructiveHint());
	}

	/** So is a change to the application's structure. */
	public void testStructural() {
		McpSchema.ToolAnnotations annotations =
			publish(ToolMode.STRUCTURAL, ToolGroups.MODEL_STRUCTURE);

		assertEquals(Boolean.FALSE, annotations.readOnlyHint());
		assertEquals(Boolean.TRUE, annotations.destructiveHint());
	}

	/** A call that lands in another system is destructive and open-world by definition. */
	public void testExternal() {
		McpSchema.ToolAnnotations annotations = publish(ToolMode.EXTERNAL, ToolGroups.EGRESS);

		assertEquals(Boolean.FALSE, annotations.readOnlyHint());
		assertEquals(Boolean.TRUE, annotations.destructiveHint());
		assertEquals(Boolean.TRUE, annotations.openWorldHint());
	}

	/**
	 * {@code openWorldHint} follows the group, not the mode.
	 *
	 * <p>
	 * {@code webFetch} is the case: a {@link ToolMode#READ} tool that reaches the whole internet,
	 * which is the specification's own example of an open world. Read the mode alone and this server
	 * would publish "closed world" about the best egress primitive it has.
	 * </p>
	 */
	public void testEgressReadIsOpenWorld() {
		McpSchema.ToolAnnotations annotations = publish(ToolMode.READ, ToolGroups.EGRESS);

		assertEquals(Boolean.TRUE, annotations.readOnlyHint());
		assertEquals(Boolean.TRUE, annotations.openWorldHint());
	}

	/** Everything that neither leaves the application nor is grouped as egress is closed-world. */
	public void testClosedWorld() {
		assertEquals(Boolean.FALSE, publish(ToolMode.READ, ToolGroups.DATA_READ).openWorldHint());
		assertEquals(Boolean.FALSE,
			publish(ToolMode.DATA_WRITE, ToolGroups.DATA_WRITE).openWorldHint());
		assertEquals("An ungrouped tool claims nothing it has not been told.",
			Boolean.FALSE, publish(ToolMode.STRUCTURAL, "").openWorldHint());
	}

	/**
	 * No mode publishes an idempotency claim.
	 *
	 * <p>
	 * Nothing in the tool contract answers the question, so nothing is said about it and the client's
	 * default - {@code false}, the careful reading - stands.
	 * </p>
	 */
	public void testNoIdempotencyClaim() {
		for (ToolMode mode : ToolMode.values()) {
			assertNull(mode.name(), publish(mode, ToolGroups.DATA_READ).idempotentHint());
		}
	}

	/** Every mode produces a complete set of the three hints this server does make. */
	public void testEveryModeIsAnswered() {
		for (ToolMode mode : ToolMode.values()) {
			McpSchema.ToolAnnotations annotations = publish(mode, ToolGroups.DATA_READ);

			assertNotNull(mode.name(), annotations.readOnlyHint());
			assertNotNull(mode.name(), annotations.destructiveHint());
			assertNotNull(mode.name(), annotations.openWorldHint());
			assertEquals("Only a READ is read-only.", mode == ToolMode.READ,
				annotations.readOnlyHint().booleanValue());
		}
	}

	/**
	 * The display name is in both places, and neither of them is the wire name.
	 *
	 * <p>
	 * A client's display name for a tool is {@code title}, then {@code annotations.title}, then
	 * {@code name}. The first field arrived with the 2025-06-18 revision, so an older client finds a
	 * readable name only in the second - and {@code name} is the wire name, which is not readable.
	 * </p>
	 */
	public void testTitle() {
		McpSchema.Tool definition = definition(new StubTool("listModules", ToolMode.READ,
			ToolGroups.MODEL_READ));

		assertEquals("model-browser__listModules", definition.name());
		assertEquals("List modules", definition.title());
		assertEquals("List modules", definition.annotations().title());
	}

	/**
	 * The hints survive the wire.
	 *
	 * <p>
	 * A published field the JSON mapper drops is published to nobody, and this is the only place that
	 * would notice.
	 * </p>
	 */
	public void testSerialization() throws IOException {
		McpJsonMapper mapper = McpJsonDefaults.getMapper();

		McpSchema.Tool published =
			definition(new StubTool("listModules", ToolMode.READ, ToolGroups.MODEL_READ));
		String json = mapper.writeValueAsString(published);

		assertTrue(json, json.contains("\"readOnlyHint\":true"));
		assertTrue(json, json.contains("\"destructiveHint\":false"));
		assertTrue(json, json.contains("\"openWorldHint\":false"));
		assertFalse("Nothing is claimed about idempotency, so the field is absent.",
			json.contains("idempotentHint"));

		McpSchema.Tool received = mapper.readValue(json, McpSchema.Tool.class);
		assertEquals(published.annotations(), received.annotations());
	}

	/**
	 * Round trip: what a tl-ai publishes, consumed by a tl-ai, is {@link ToolMode#EXTERNAL} in every
	 * mode.
	 *
	 * <p>
	 * The asymmetry is deliberate, and this test is here to keep it from being read as a bug. The
	 * hints are a server's claims about itself and the specification tells clients not to base trust
	 * decisions on them, so the consuming side reads name, description and input schema and nothing
	 * else: a server could otherwise auto-approve itself by sending {@code readOnlyHint: true}. The
	 * mode does not survive the round trip, therefore, and must not - what it is replaced by says the
	 * truer thing about the call, which is that it left the application.
	 * </p>
	 *
	 * <p>
	 * The consuming wrapper is built here from exactly the three fields
	 * {@code McpServerRegistry.createTool} takes out of a {@code tools/list} entry; that the
	 * annotations are not among them is the point being asserted. That no combination of hints
	 * changes the answer is pinned on the other side, in {@code TestMcpToolMode}.
	 * </p>
	 */
	public void testRoundTripThroughAConsumingTlAi() throws IOException {
		McpJsonMapper mapper = McpJsonDefaults.getMapper();

		for (ToolMode mode : ToolMode.values()) {
			McpSchema.Tool published = definition(new StubTool("doThing", mode, ToolGroups.DATA_READ));

			// Over the wire and back, as a real client receives it.
			McpSchema.Tool received =
				mapper.readValue(mapper.writeValueAsString(published), McpSchema.Tool.class);

			assertNotNull("Published for " + mode, received.annotations());

			McpTool consumed = new McpTool("remote-tl", received.name(), received.description(),
				mapper.writeValueAsString(received.inputSchema()), null);

			assertEquals("Published as " + mode, ToolMode.EXTERNAL, consumed.mode());
			assertEquals("mcp__remote-tl__model-browser__doThing", consumed.name());
		}
	}

	/** The annotations of the given tool, as the adapter publishes them. */
	private static McpSchema.ToolAnnotations publish(ToolMode mode, String group) {
		return definition(new StubTool("aTool", mode, group)).annotations();
	}

	/** The MCP definition the adapter builds for the given tool, published under its defaults. */
	private static McpSchema.Tool definition(Tool tool) {
		String configName = "model-browser." + tool.name();
		ExposedTool entry = TypedConfiguration.newConfigItem(ExposedTool.class);
		entry.setTool(configName);
		List<McpExposure.Published> published =
			McpExposure.resolve(List.of(entry), Map.of(configName, tool), name -> null);
		McpServerFeatures.SyncToolSpecification specification = McpToolAdapter.adapt(published.get(0));
		return specification.tool();
	}

	/**
	 * A tool that is nothing but its declarations: the adapter reads no more of one than this.
	 */
	private static final class StubTool implements Tool {

		private final String _name;

		private final ToolMode _mode;

		private final String _group;

		StubTool(String name, ToolMode mode, String group) {
			_name = name;
			_mode = mode;
			_group = group;
		}

		@Override
		public String name() {
			return _name;
		}

		@Override
		public String description() {
			return "Does something.";
		}

		@Override
		public ToolSchema schema() {
			return new ToolSchema(_name, description(), "{\"type\":\"object\",\"properties\":{}}");
		}

		@Override
		public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
			throw new UnsupportedOperationException("Not called: only the declarations are adapted.");
		}

		@Override
		public ToolMode mode() {
			return _mode;
		}

		@Override
		public String group() {
			return _group;
		}
	}

}

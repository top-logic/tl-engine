/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicRequestBuilder;
import com.top_logic.tl.ai.core.provider.bedrock.BedrockConverseRequestBuilder;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.json.JsonUtil;

/**
 * Where the builders that place explicit cache breakpoints put the one for the tool array.
 *
 * <p>
 * Anthropic renders a request as tools, then system, then messages, and a breakpoint caches
 * everything in front of it. The breakpoint on the system block therefore covers the whole tool
 * array - which is right until a run loads a skill: the appended tool moves the system block, and
 * the whole prefix is written again. {@link ProviderRequest#stableToolCount()} says where the array
 * stops being the same bytes, and a breakpoint there keeps that part readable across the load
 * (Gitea #346).
 * </p>
 */
public class TestToolArrayCacheBoundary extends TestCase {

	private static final List<Message> ONE_TURN = List.of(new UserMessage("u1",
		Instant.parse("2026-09-19T12:00:00Z"), List.of(new TextContent("Hello"))));

	/** With no loaded skill the system breakpoint already covers the array, so none is spent here. */
	public void testNoBreakpointWhenTheWholeArrayIsStable() throws Exception {
		String json = anthropic(request(List.of("readModel", "loadSkill"), 2));
		assertEquals(List.of(), cachedToolsOf(json, "name"));
	}

	/** The breakpoint lands on the last tool that cannot move. */
	public void testTheBreakpointLandsOnTheLastStableTool() throws Exception {
		String json = anthropic(request(List.of("readModel", "loadSkill", "alphaTool"), 2));
		assertEquals(List.of("loadSkill"), cachedToolsOf(json, "name"));
	}

	/** A caller that promises nothing gets the shape it always got. */
	public void testAnUnknownBoundaryPlacesNoBreakpoint() throws Exception {
		String json = anthropic(request(List.of("readModel", "loadSkill", "alphaTool"), 0));
		assertEquals(List.of(), cachedToolsOf(json, "name"));
	}

	/** Bedrock spells the same breakpoint as a {@code cachePoint} entry after the stable tools. */
	public void testBedrockPlacesACachePointAfterTheStableTools() throws Exception {
		String json = BedrockConverseRequestBuilder
			.buildRequestJson(request(List.of("readModel", "loadSkill", "alphaTool"), 2));

		@SuppressWarnings("unchecked")
		Map<String, Object> toolConfig =
			(Map<String, Object>) asMap(JsonUtil.parseJsonValue(json)).get("toolConfig");
		List<?> tools = (List<?>) toolConfig.get("tools");

		assertEquals("Two tool specs, one cachePoint, one tool spec.", 4, tools.size());
		assertEquals("loadSkill", toolSpecName(tools.get(1)));
		assertNotNull("The cachePoint must follow the last stable tool.",
			asMap(tools.get(2)).get("cachePoint"));
		assertEquals("alphaTool", toolSpecName(tools.get(3)));
	}

	/** A Bedrock request whose whole array is stable keeps the shape it always had. */
	public void testBedrockPlacesNoCachePointWhenTheWholeArrayIsStable() throws Exception {
		String json = BedrockConverseRequestBuilder
			.buildRequestJson(request(List.of("readModel", "loadSkill"), 2));

		@SuppressWarnings("unchecked")
		Map<String, Object> toolConfig =
			(Map<String, Object>) asMap(JsonUtil.parseJsonValue(json)).get("toolConfig");
		assertEquals(2, ((List<?>) toolConfig.get("tools")).size());
	}

	/** A boundary that is not a prefix of the tool list is a programming error. */
	public void testTheBoundaryMustBeAPrefix() {
		try {
			request(List.of("readModel"), 2);
			fail("A boundary past the end of the tool list must be refused.");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("stableToolCount"));
		}
	}

	// ===== Helpers =====

	private static ProviderRequest request(List<String> toolNames, int stableToolCount) {
		List<ToolSchema> tools = new ArrayList<>();
		for (String name : toolNames) {
			tools.add(new ToolSchema(name, "Stub tool: " + name,
				"{\"type\":\"object\",\"properties\":{}}"));
		}
		return new ProviderRequest("claude-haiku-4-5-20251001", "You are a test assistant.",
			ONE_TURN, tools, 256, Map.of(), null, null, null, stableToolCount);
	}

	private static String anthropic(ProviderRequest request) throws Exception {
		return AnthropicRequestBuilder.buildRequestJson(request);
	}

	/** The names of the tool entries that carry a {@code cache_control} block. */
	private static List<String> cachedToolsOf(String json, String nameField) throws Exception {
		List<?> tools = (List<?>) asMap(JsonUtil.parseJsonValue(json)).get("tools");
		List<String> cached = new ArrayList<>();
		for (Object tool : tools) {
			Map<String, Object> entry = asMap(tool);
			if (entry.containsKey("cache_control")) {
				cached.add((String) entry.get(nameField));
			}
		}
		return cached;
	}

	private static String toolSpecName(Object toolEntry) {
		return (String) asMap(asMap(toolEntry).get("toolSpec")).get("name");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		assertTrue("Expected a JSON object, got: " + value, value instanceof Map);
		return (Map<String, Object>) value;
	}
}

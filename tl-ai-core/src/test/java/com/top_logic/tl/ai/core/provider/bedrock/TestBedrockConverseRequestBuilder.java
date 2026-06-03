/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.json.JsonUtil;

/**
 * Tests for {@link BedrockConverseRequestBuilder}.
 */
public class TestBedrockConverseRequestBuilder extends TestCase {

	/** Test A: system prompt, user text message, tool schema, and maxOutputTokens. */
	public void testBasicRequestWithSystemAndTool() throws Exception {
		String schemaJson =
			"{\"type\":\"object\",\"properties\":{\"q\":{\"type\":\"string\"}},\"required\":[\"q\"]}";

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello, what can you do?")));

		ToolSchema tool = new ToolSchema("search", "Searches for information.", schemaJson);

		ProviderRequest request = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0",
			"You are a helpful assistant.",
			List.of(user),
			List.of(tool),
			1024);

		String json = BedrockConverseRequestBuilder.buildRequestJson(request);

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);

		// --- system ---
		@SuppressWarnings("unchecked")
		java.util.List<Object> system = (java.util.List<Object>) root.get("system");
		assertNotNull("system must be present", system);
		// system text block + trailing cachePoint
		assertEquals(2, system.size());
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> sysBlock = (java.util.Map<String, Object>) system.get(0);
		assertEquals("You are a helpful assistant.", sysBlock.get("text"));
		assertTrue("system must end with a cachePoint", isCachePoint(system.get(1)));

		// --- messages ---
		@SuppressWarnings("unchecked")
		java.util.List<Object> messages = (java.util.List<Object>) root.get("messages");
		assertNotNull(messages);
		assertEquals(1, messages.size());

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> msg0 = (java.util.Map<String, Object>) messages.get(0);
		assertEquals("user", msg0.get("role"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> content0 = (java.util.List<Object>) msg0.get("content");
		assertNotNull(content0);
		// user text block + trailing cachePoint (last user turn is cached)
		assertEquals(2, content0.size());
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> textBlock = (java.util.Map<String, Object>) content0.get(0);
		assertEquals("Hello, what can you do?", textBlock.get("text"));
		assertTrue("last user turn must end with a cachePoint", isCachePoint(content0.get(1)));

		// --- inferenceConfig ---
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> inferenceConfig =
			(java.util.Map<String, Object>) root.get("inferenceConfig");
		assertNotNull("inferenceConfig must be present", inferenceConfig);
		assertEquals(Long.valueOf(1024), inferenceConfig.get("maxTokens"));

		// --- toolConfig ---
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolConfig =
			(java.util.Map<String, Object>) root.get("toolConfig");
		assertNotNull("toolConfig must be present", toolConfig);

		@SuppressWarnings("unchecked")
		java.util.List<Object> tools = (java.util.List<Object>) toolConfig.get("tools");
		assertNotNull(tools);
		assertEquals(1, tools.size());

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolEntry = (java.util.Map<String, Object>) tools.get(0);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolSpec =
			(java.util.Map<String, Object>) toolEntry.get("toolSpec");
		assertNotNull(toolSpec);
		assertEquals("search", toolSpec.get("name"));
		assertEquals("Searches for information.", toolSpec.get("description"));

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> inputSchema =
			(java.util.Map<String, Object>) toolSpec.get("inputSchema");
		assertNotNull("inputSchema must be present", inputSchema);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> inputSchemaJson =
			(java.util.Map<String, Object>) inputSchema.get("json");
		assertNotNull("inputSchema.json must be present and parsed (not a string)", inputSchemaJson);
		assertEquals("object", inputSchemaJson.get("type"));

		// no model field at top level
		assertFalse("must not contain top-level 'model' field", root.containsKey("model"));
	}

	/** Test B: tool-call / tool-result round-trip with Completed state. */
	public void testToolCallAndCompletedResult() throws Exception {
		String toolCallId = "tool_use_abc";
		String toolArgsJson = "{\"q\":\"weather Berlin\"}";
		String toolResultText = "Sunny, 22°C";

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("What is the weather in Berlin?")));

		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(
				new TextContent("Let me look that up."),
				new ToolCall(toolCallId, "get_weather", toolArgsJson)),
			new TokenUsage(10, 20, 0, 0, 0),
			"tool_use");

		ToolResultMessage result = new ToolResultMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			toolCallId,
			"get_weather",
			new ToolState.Completed(toolResultText, Instant.now(), Instant.now(), false));

		ProviderRequest request = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0",
			null,
			List.of(user, assistant, result),
			List.of(),
			512);

		String json = BedrockConverseRequestBuilder.buildRequestJson(request);

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);

		// no system block when systemPrompt is null
		assertNull("system must be absent when no system prompt", root.get("system"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> messages = (java.util.List<Object>) root.get("messages");
		assertNotNull(messages);
		// user, assistant, user-with-tool-result
		assertEquals(3, messages.size());

		// --- assistant message ---
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> assistantMsg =
			(java.util.Map<String, Object>) messages.get(1);
		assertEquals("assistant", assistantMsg.get("role"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> assistantContent =
			(java.util.List<Object>) assistantMsg.get("content");
		assertNotNull(assistantContent);
		assertEquals(2, assistantContent.size());

		// first block: text
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> textBlock =
			(java.util.Map<String, Object>) assistantContent.get(0);
		assertEquals("Let me look that up.", textBlock.get("text"));

		// second block: toolUse
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolUseBlock =
			(java.util.Map<String, Object>) assistantContent.get(1);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolUse =
			(java.util.Map<String, Object>) toolUseBlock.get("toolUse");
		assertNotNull("toolUse block required", toolUse);
		assertEquals(toolCallId, toolUse.get("toolUseId"));
		assertEquals("get_weather", toolUse.get("name"));
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolInput =
			(java.util.Map<String, Object>) toolUse.get("input");
		assertNotNull("toolUse.input must be a parsed object", toolInput);
		assertEquals("weather Berlin", toolInput.get("q"));

		// --- tool result message (role = user) ---
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> trMsg = (java.util.Map<String, Object>) messages.get(2);
		assertEquals("user", trMsg.get("role"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> trContent = (java.util.List<Object>) trMsg.get("content");
		assertNotNull(trContent);
		// toolResult block + trailing cachePoint (this is one of the last two user turns)
		assertEquals(2, trContent.size());
		assertTrue("tool-result turn must end with a cachePoint", isCachePoint(trContent.get(1)));

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> trBlock = (java.util.Map<String, Object>) trContent.get(0);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolResult =
			(java.util.Map<String, Object>) trBlock.get("toolResult");
		assertNotNull("toolResult block required", toolResult);
		assertEquals(toolCallId, toolResult.get("toolUseId"));
		assertEquals("success", toolResult.get("status"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> trResultContent =
			(java.util.List<Object>) toolResult.get("content");
		assertNotNull(trResultContent);
		assertEquals(1, trResultContent.size());
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> trTextBlock =
			(java.util.Map<String, Object>) trResultContent.get(0);
		assertEquals(toolResultText, trTextBlock.get("text"));
	}

	/** Test C: failed tool result emits status "error". */
	public void testFailedToolResult() throws Exception {
		String toolCallId = "tool_use_xyz";

		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new ToolCall(toolCallId, "run_task", "{}")),
			new TokenUsage(5, 10, 0, 0, 0),
			"tool_use");

		ToolResultMessage failed = new ToolResultMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			toolCallId,
			"run_task",
			new ToolState.Failed("Timeout after 30s", Instant.now(), Instant.now()));

		// Bedrock requires alternating roles; prepend a user message.
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Run the task.")));

		ProviderRequest request = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0",
			null,
			List.of(user, assistant, failed),
			List.of(),
			256);

		String json = BedrockConverseRequestBuilder.buildRequestJson(request);

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);
		@SuppressWarnings("unchecked")
		java.util.List<Object> messages = (java.util.List<Object>) root.get("messages");
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> trMsg = (java.util.Map<String, Object>) messages.get(2);
		@SuppressWarnings("unchecked")
		java.util.List<Object> trContent = (java.util.List<Object>) trMsg.get("content");
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> trBlock = (java.util.Map<String, Object>) trContent.get(0);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolResult =
			(java.util.Map<String, Object>) trBlock.get("toolResult");
		assertEquals("error", toolResult.get("status"));
		@SuppressWarnings("unchecked")
		java.util.List<Object> errContent = (java.util.List<Object>) toolResult.get("content");
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> errBlock = (java.util.Map<String, Object>) errContent.get(0);
		assertEquals("Timeout after 30s", errBlock.get("text"));
	}

	/** Test D: ToolChoice.REQUIRED maps to {"any":{}}; NONE omits toolChoice. */
	public void testToolChoiceMapping() throws Exception {
		ToolSchema tool = new ToolSchema(
			"do_something", "Does something.",
			"{\"type\":\"object\",\"properties\":{}}");

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Call a tool.")));

		// REQUIRED → any
		ProviderRequest requiredReq = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0", null,
			List.of(user), List.of(tool), 256,
			java.util.Map.of(), null, ToolChoice.REQUIRED);

		String requiredJson = BedrockConverseRequestBuilder.buildRequestJson(requiredReq);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> reqRoot =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(requiredJson);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolConfigReq =
			(java.util.Map<String, Object>) reqRoot.get("toolConfig");
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> tcRequired =
			(java.util.Map<String, Object>) toolConfigReq.get("toolChoice");
		assertNotNull("toolChoice must be present for REQUIRED", tcRequired);
		assertNotNull("toolChoice.any must be present", tcRequired.get("any"));

		// NONE → toolChoice omitted
		ProviderRequest noneReq = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0", null,
			List.of(user), List.of(tool), 256,
			java.util.Map.of(), null, ToolChoice.NONE);

		String noneJson = BedrockConverseRequestBuilder.buildRequestJson(noneReq);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> noneRoot =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(noneJson);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolConfigNone =
			(java.util.Map<String, Object>) noneRoot.get("toolConfig");
		assertFalse("toolChoice must be absent for NONE",
			toolConfigNone.containsKey("toolChoice"));
	}

	/** Test E: ToolChoice.AUTO maps to {"auto":{}} in toolConfig.toolChoice. */
	public void testToolChoiceAuto() throws Exception {
		ToolSchema tool = new ToolSchema(
			"do_something", "Does something.",
			"{\"type\":\"object\",\"properties\":{}}");

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Call a tool.")));

		ProviderRequest autoReq = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0", null,
			List.of(user), List.of(tool), 256,
			java.util.Map.of(), null, ToolChoice.AUTO);

		String json = BedrockConverseRequestBuilder.buildRequestJson(autoReq);

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolConfig =
			(java.util.Map<String, Object>) root.get("toolConfig");
		assertNotNull("toolConfig must be present", toolConfig);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> toolChoice =
			(java.util.Map<String, Object>) toolConfig.get("toolChoice");
		assertNotNull("toolChoice must be present for AUTO", toolChoice);
		assertNotNull("toolChoice.auto must be present", toolChoice.get("auto"));
		assertFalse("toolChoice must not contain 'any'", toolChoice.containsKey("any"));
	}

	/** Test F: two consecutive ToolResultMessages are batched into one user message. */
	public void testConsecutiveToolResultsBatched() throws Exception {
		String id1 = "tool_use_1";
		String id2 = "tool_use_2";

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Do both tasks.")));

		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(
				new ToolCall(id1, "task_one", "{}"),
				new ToolCall(id2, "task_two", "{}")),
			new TokenUsage(10, 20, 0, 0, 0),
			"tool_use");

		ToolResultMessage result1 = new ToolResultMessage(
			UUID.randomUUID().toString(), Instant.now(),
			id1, "task_one",
			new ToolState.Completed("result-one", Instant.now(), Instant.now(), false));

		ToolResultMessage result2 = new ToolResultMessage(
			UUID.randomUUID().toString(), Instant.now(),
			id2, "task_two",
			new ToolState.Completed("result-two", Instant.now(), Instant.now(), false));

		ProviderRequest request = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0", null,
			List.of(user, assistant, result1, result2),
			List.of(), 512);

		String json = BedrockConverseRequestBuilder.buildRequestJson(request);

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);
		@SuppressWarnings("unchecked")
		java.util.List<Object> messages = (java.util.List<Object>) root.get("messages");

		// user + assistant + ONE batched user message (not two)
		assertEquals("Two consecutive tool results must be batched into one user message",
			3, messages.size());

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> batchedMsg =
			(java.util.Map<String, Object>) messages.get(2);
		assertEquals("user", batchedMsg.get("role"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> content = (java.util.List<Object>) batchedMsg.get("content");
		assertNotNull(content);
		// two toolResult blocks + trailing cachePoint (this is the last user turn)
		assertEquals("batched user message: two toolResult blocks + a cachePoint", 3, content.size());
		assertTrue("batched tool-result turn must end with a cachePoint", isCachePoint(content.get(2)));

		// first block → id1
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> block0 = (java.util.Map<String, Object>) content.get(0);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> tr0 = (java.util.Map<String, Object>) block0.get("toolResult");
		assertNotNull("first block must be a toolResult", tr0);
		assertEquals(id1, tr0.get("toolUseId"));

		// second block → id2
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> block1 = (java.util.Map<String, Object>) content.get(1);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> tr1 = (java.util.Map<String, Object>) block1.get("toolResult");
		assertNotNull("second block must be a toolResult", tr1);
		assertEquals(id2, tr1.get("toolUseId"));
	}

	/** Test G: temperature and topP from modelOptions appear in inferenceConfig. */
	public void testSamplingParams() throws Exception {
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Hello.")));

		ProviderRequest request = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0", null,
			List.of(user), List.of(), 256,
			java.util.Map.of("temperature", "0.7", "topP", "0.9"),
			null, null);

		String json = BedrockConverseRequestBuilder.buildRequestJson(request);

		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> inferenceConfig =
			(java.util.Map<String, Object>) root.get("inferenceConfig");
		assertNotNull(inferenceConfig);
		assertEquals(0.7, ((Number) inferenceConfig.get("temperature")).doubleValue(), 1e-9);
		assertEquals(0.9, ((Number) inferenceConfig.get("topP")).doubleValue(), 1e-9);
	}

	/** Test H: cache points land on the system prefix and the last two user turns only. */
	public void testCachePointPlacement() throws Exception {
		UserMessage u1 = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("first")));
		AssistantMessage a1 = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("ok one")), new TokenUsage(1, 1, 0, 0, 0), "end_turn");
		UserMessage u2 = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("second")));
		AssistantMessage a2 = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("ok two")), new TokenUsage(1, 1, 0, 0, 0), "end_turn");
		UserMessage u3 = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("third")));

		ProviderRequest request = new ProviderRequest(
			"anthropic.claude-3-haiku-20240307-v1:0",
			"You are helpful.",
			List.of(u1, a1, u2, a2, u3),
			List.of(),
			256);

		String json = BedrockConverseRequestBuilder.buildRequestJson(request);
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> root =
			(java.util.Map<String, Object>) JsonUtil.parseJsonValue(json);

		// system prefix is cached, with the expected {"cachePoint":{"type":"default"}} shape
		@SuppressWarnings("unchecked")
		java.util.List<Object> system = (java.util.List<Object>) root.get("system");
		Object sysLast = system.get(system.size() - 1);
		assertTrue("system prefix must be cached", isCachePoint(sysLast));
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> cpInner =
			(java.util.Map<String, Object>) ((java.util.Map<String, Object>) sysLast).get("cachePoint");
		assertEquals("default", cpInner.get("type"));

		@SuppressWarnings("unchecked")
		java.util.List<Object> messages = (java.util.List<Object>) root.get("messages");
		// User turns are at indices 0, 2, 4; only the last two (2, 4) are cached.
		assertFalse("oldest user turn must NOT be cached", lastBlockIsCachePoint(messages.get(0)));
		assertTrue("2nd-newest user turn must be cached", lastBlockIsCachePoint(messages.get(2)));
		assertTrue("newest user turn must be cached", lastBlockIsCachePoint(messages.get(4)));
		// Assistant turns are never cached.
		assertFalse("assistant turn must NOT be cached", lastBlockIsCachePoint(messages.get(1)));
	}

	private static boolean isCachePoint(Object block) {
		return block instanceof java.util.Map<?, ?> map && map.containsKey("cachePoint");
	}

	@SuppressWarnings("unchecked")
	private static boolean lastBlockIsCachePoint(Object message) {
		java.util.List<Object> content =
			(java.util.List<Object>) ((java.util.Map<String, Object>) message).get("content");
		return !content.isEmpty() && isCachePoint(content.get(content.size() - 1));
	}
}

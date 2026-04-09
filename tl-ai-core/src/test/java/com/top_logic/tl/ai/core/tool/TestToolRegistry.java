/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentConfig;
import com.top_logic.tl.ai.core.agent.AgentListener;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * Tests for {@link ToolRegistry}.
 */
public class TestToolRegistry extends TestCase {

	static class MathTools {
		@ToolMethod(description ="Add two numbers", mode = ToolMode.READ, concurrencySafe = true)
		public String add(@P("first") int a, @P("second") int b) {
			return String.valueOf(a + b);
		}

		@ToolMethod(description ="Multiply two numbers", mode = ToolMode.READ, concurrencySafe = true)
		public String multiply(@P("first") int a, @P("second") int b) {
			return String.valueOf(a * b);
		}
	}

	static class EditTools {
		@ToolMethod(description = "Delete a record")
		public String delete(@P("record id") String id) {
			return "Deleted " + id;
		}
	}

	/** Register annotated provider and verify tools are found. */
	public void testRegisterAnnotated() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		assertEquals(2, registry.tools().size());
		assertNotNull(registry.getTool("add"));
		assertNotNull(registry.getTool("multiply"));
	}

	/** Register a single Tool directly. */
	public void testRegisterDirectTool() {
		ToolRegistry registry = new ToolRegistry();
		Tool tool = AnnotatedTools.from(new MathTools()).get(0);
		registry.register(tool);
		assertEquals(1, registry.tools().size());
	}

	/** Duplicate tool name throws. */
	public void testDuplicateNameThrows() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		try {
			registry.register(new MathTools());
			fail("Expected IllegalArgumentException for duplicate tool name");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("add"));
		}
	}

	/** toolSchemas() returns all schemas. */
	public void testToolSchemas() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		registry.register(new EditTools());
		assertEquals(3, registry.toolSchemas().size());
	}

	/** toolSchemas(filter) filters by predicate. */
	public void testToolSchemasFiltered() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		registry.register(new EditTools());

		List<ToolSchema> readOnly = registry.toolSchemas(t -> t.mode() == ToolMode.READ);
		assertEquals(2, readOnly.size());

		List<ToolSchema> global = registry.toolSchemas(t -> t.mode() == ToolMode.GLOBAL);
		assertEquals(1, global.size());
	}

	/** Composing multiple filters with and(). */
	public void testComposedFilters() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		registry.register(new EditTools());

		List<ToolSchema> result = registry.toolSchemas(
			((java.util.function.Predicate<Tool>) t -> t.mode() == ToolMode.READ)
				.and(Tool::isConcurrencySafe));
		assertEquals(2, result.size());
	}

	/** Execute dispatches to the correct tool. */
	public void testExecute() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());

		ToolCall call = new ToolCall("tc1", "add", "{\"a\":17,\"b\":25}");
		ToolResultMessage result = registry.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Completed.class, result.state());
		assertEquals("42", ((ToolState.Completed) result.state()).result());
		assertEquals("tc1", result.toolCallId());
		assertEquals("add", result.toolName());
	}

	/** Execute with unknown tool returns Failed. */
	public void testExecuteUnknownTool() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());

		ToolCall call = new ToolCall("tc1", "nonexistent", "{}");
		ToolResultMessage result = registry.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		String error = ((ToolState.Failed) result.state()).error();
		assertTrue("Error should mention tool name: " + error,
			error.contains("nonexistent"));
		assertTrue("Error should list available tools: " + error,
			error.contains("add"));
	}

	/** Execute with tool that throws returns Failed with sanitized message. */
	public void testExecuteToolError() {
		Object provider = new Object() {
			@ToolMethod(description ="Fails", mode = ToolMode.READ)
			public String fail(@P("input") String input) {
				throw new RuntimeException("Boom");
			}
		};
		ToolRegistry registry = new ToolRegistry();
		registry.register(provider);

		ToolCall call = new ToolCall("tc1", "fail", "{\"input\":\"test\"}");
		ToolResultMessage result = registry.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		String error = ((ToolState.Failed) result.state()).error();
		assertTrue("Error should mention tool name: " + error,
			error.contains("fail"));
		assertTrue("Error should mention the message: " + error,
			error.contains("Boom"));
	}

	/** Execute with empty arguments JSON. */
	public void testExecuteEmptyArgs() {
		Object provider = new Object() {
			@ToolMethod(description ="No args", mode = ToolMode.READ)
			public String noArgs() {
				return "ok";
			}
		};
		ToolRegistry registry = new ToolRegistry();
		registry.register(provider);

		ToolCall call = new ToolCall("tc1", "noArgs", "{}");
		ToolResultMessage result = registry.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Completed.class, result.state());
		assertEquals("ok", ((ToolState.Completed) result.state()).result());
	}

	/** Invalid JSON arguments returns Failed with clean error. */
	public void testExecuteInvalidJson() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());

		ToolCall call = new ToolCall("tc1", "add", "not-json");
		ToolResultMessage result = registry.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		String error = ((ToolState.Failed) result.state()).error();
		assertTrue("Error should mention tool name: " + error,
			error.contains("add"));
	}

	/** Type coercion error returns Failed with parameter-specific message. */
	public void testExecuteCoercionError() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());

		ToolCall call = new ToolCall("tc1", "add", "{\"a\":\"not_a_number\",\"b\":1}");
		ToolResultMessage result = registry.execute(call, ToolContext.EMPTY);

		assertInstanceOf(ToolState.Failed.class, result.state());
		String error = ((ToolState.Failed) result.state()).error();
		assertTrue("Error should mention parameter name: " + error,
			error.contains("'a'"));
	}

	/** SecurityContext.hasToolPermission blocks tool execution in AgentLoop. */
	public void testSecurityBlocksTool() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		registry.register(new EditTools());

		SecurityContext noDelete = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return !tool.name().equals("delete");
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		ToolContext context = new ToolContext(noDelete, ApprovalCallback.AUTO_APPROVE);

		// add should work
		ToolCall addCall = new ToolCall("tc1", "add", "{\"a\":1,\"b\":2}");
		ToolResultMessage addResult = registry.execute(addCall, context);
		assertInstanceOf(ToolState.Completed.class, addResult.state());

		// delete blocked at AgentLoop level, not at ToolRegistry level
		// ToolRegistry is a pure executor — security check happens in AgentLoop
	}

	// --- AgentLoop integration ---

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	/** End-to-end: ToolRegistry as ToolExecutor in AgentLoop. */
	public void testAgentLoopIntegration() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());

		// Turn 1: LLM requests add tool. Turn 2: LLM responds with text.
		ToolCall toolCall = new ToolCall("tc1", "add", "{\"a\":17,\"b\":25}");
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("The answer is 42.")));

		AgentConfig config = new AgentConfig(MODEL, "You can do math.",
			registry.toolSchemas(), 10, 8192);

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", Instant.now(), List.of(new TextContent("What is 17+25?"))));

		AgentLoop loop = new AgentLoop(config, provider, registry);
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());

		// Verify the tool result was fed back into the conversation
		// conv: user + assistant(tool_call) + tool_result + assistant(text) = 4
		assertEquals(4, conv.size());
	}

	/** SecurityContext blocks a tool in the agent loop. */
	public void testAgentLoopSecurityBlock() {
		ToolRegistry registry = new ToolRegistry();
		registry.register(new EditTools());

		ToolCall toolCall = new ToolCall("tc1", "delete", "{\"id\":\"123\"}");
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("I see the tool was denied.")));

		AgentConfig config = new AgentConfig(MODEL, "Test",
			registry.toolSchemas(), 10, 8192);

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", Instant.now(), List.of(new TextContent("Delete 123"))));

		SecurityContext noDeletions = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return !tool.name().equals("delete");
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		ToolContext context = new ToolContext(noDeletions, ApprovalCallback.AUTO_APPROVE);

		AgentLoop loop = new AgentLoop(config, provider, registry);
		AgentResult result = loop.run(conv, context, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());

		// The tool result should be a Failed "Permission denied"
		ToolResultMessage toolResult = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.findFirst().orElse(null);
		assertNotNull("Tool result should exist", toolResult);
		assertInstanceOf(ToolState.Failed.class, toolResult.state());
		assertTrue(((ToolState.Failed) toolResult.state()).error().contains("Permission denied"));
	}

	/** Result truncation in AgentLoop. */
	public void testResultTruncation() {
		ToolRegistry registry = new ToolRegistry();
		Object provider = new Object() {
			@ToolMethod(description = "Returns large output", mode = ToolMode.READ)
			public String largeOutput(@P("size") int size) {
				return "x".repeat(size);
			}
		};
		registry.register(provider);

		ToolCall toolCall = new ToolCall("tc1", "largeOutput", "{\"size\":10000}");
		LLMProvider llm = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Got it.")));

		AgentConfig config = new AgentConfig(MODEL, "Test",
			registry.toolSchemas(), 10, 8192);

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", Instant.now(), List.of(new TextContent("Big output"))));

		int maxChars = 100;
		AgentLoop loop = new AgentLoop(config, llm, registry, maxChars);
		loop.run(conv, new AgentListener() {});

		ToolResultMessage toolResult = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.findFirst().orElse(null);
		assertNotNull(toolResult);
		assertInstanceOf(ToolState.Completed.class, toolResult.state());
		String result = ((ToolState.Completed) toolResult.state()).result();
		assertTrue("Result should be truncated", result.contains("[Result truncated:"));
		assertTrue("Result should mention total size", result.contains("10000 chars total"));
	}

	// --- Helper: mock provider (same pattern as TestAgentLoop) ---

	@SafeVarargs
	private static LLMProvider mockProvider(List<ContentPart>... responses) {
		AtomicInteger callIndex = new AtomicInteger(0);
		List<List<ContentPart>> responseList = List.of(responses);

		return (request, handler) -> {
			int idx = callIndex.getAndIncrement();
			if (idx >= responseList.size()) {
				throw new ProviderException(
					ProviderException.Kind.INVALID_REQUEST, 400,
					"Mock provider: no more responses (call " + idx + ")");
			}
			List<ContentPart> content = responseList.get(idx);

			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall tc) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, tc.id(), tc.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, tc.arguments()));
				}
			}

			String finishReason = content.stream().anyMatch(ToolCall.class::isInstance)
				? "tool_use" : "end_turn";

			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-" + idx, MODEL, finishReason, USAGE, content));
		};
	}

	private static void assertInstanceOf(Class<?> expected, Object actual) {
		assertNotNull("Expected non-null", actual);
		assertTrue("Expected " + expected.getSimpleName() + " but got " + actual.getClass().getSimpleName(),
			expected.isInstance(actual));
	}
}

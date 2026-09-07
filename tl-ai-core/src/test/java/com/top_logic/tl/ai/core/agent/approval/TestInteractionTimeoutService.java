/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests that the two deadlines a human gets are taken from the application configuration and not
 * from a constant in the code.
 *
 * <p>
 * The tl-ai-core test configuration deliberately sets both to values that are neither the shipped
 * default nor each other ({@value #CONFIGURED_APPROVAL_MINUTES} and
 * {@value #CONFIGURED_QUESTION_MINUTES} minutes), so a test that passes cannot be passing on a
 * hard-coded value. The approval deadline is checked where it matters - on the
 * {@link ApprovalRequest} that {@link AgentLoop} hands to the approval channel - rather than only
 * on the accessor.
 * </p>
 */
public class TestInteractionTimeoutService extends TestCase {

	/** The approval timeout the tl-ai-core test configuration sets, in minutes. */
	public static final int CONFIGURED_APPROVAL_MINUTES = 7;

	/** The question timeout the tl-ai-core test configuration sets, in minutes. */
	public static final int CONFIGURED_QUESTION_MINUTES = 11;

	private static final Instant T0 = Instant.parse("2026-04-04T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private static final String MODEL = "test-model";

	/**
	 * The service under test has to be running, and {@link AgentLoop} needs a thread context to
	 * execute a tool call in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(
			ServiceTestSetup.withThreadContext(new TestSuite(TestInteractionTimeoutService.class)),
			InteractionTimeoutService.Module.INSTANCE));
	}

	/** Both defaults are half an hour - the deadline of a person, not of a machine. */
	public void testDefaultsAreHalfAnHour() {
		InteractionTimeoutService.Config<?> defaults =
			TypedConfiguration.newConfigItem(InteractionTimeoutService.Config.class);

		assertEquals(30, defaults.getApprovalTimeoutMinutes());
		assertEquals(30, defaults.getQuestionTimeoutMinutes());
		assertEquals(Duration.ofMinutes(30), InteractionTimeoutService.DEFAULT_APPROVAL_TIMEOUT);
		assertEquals(Duration.ofMinutes(30), InteractionTimeoutService.DEFAULT_QUESTION_TIMEOUT);
	}

	/** The running service reports what the application configured, not the default. */
	public void testConfiguredValuesAreRead() {
		assertEquals(Duration.ofMinutes(CONFIGURED_APPROVAL_MINUTES),
			InteractionTimeoutService.approvalTimeout());
		assertEquals(Duration.ofMinutes(CONFIGURED_QUESTION_MINUTES),
			InteractionTimeoutService.questionTimeout());
	}

	/**
	 * The configured approval deadline reaches the approval channel: the request the agent loop
	 * builds for a tool that needs approval carries it, and so does the event that announces it.
	 */
	public void testAgentLoopUsesConfiguredApprovalTimeout() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema schema = new ToolSchema("edit", "Edits", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "edit", "{}")),
			List.of(new TextContent("Done")));

		ToolCatalog tools = new ToolCatalog();
		tools.register(approvalRequiringTool("edit", schema));
		tools.freeze();

		List<ApprovalRequest> requests = new ArrayList<>();
		ApprovalCallback callback = request -> {
			requests.add(request);
			return new ApprovalResult.Approved();
		};

		List<AgentEvent.ApprovalRequired> announced = new ArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.ApprovalRequired approval) {
				announced.add(approval);
			}
		};

		new AgentLoop(params(), provider, tools)
			.run(conversation, new ToolContext(developmentSecurity(), callback), listener);

		assertEquals(1, requests.size());
		assertEquals("the approval channel must see the configured deadline",
			Duration.ofMinutes(CONFIGURED_APPROVAL_MINUTES), requests.get(0).timeout());
		assertEquals(1, announced.size());
		assertEquals("the UI must be told the same deadline the agent thread waits for",
			Duration.ofMinutes(CONFIGURED_APPROVAL_MINUTES), announced.get(0).timeout());
	}

	private static AgentParams params() {
		return new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/** A {@link ToolMode#STRUCTURAL} tool, which the approval check always routes to the user. */
	private static Tool approvalRequiringTool(String name, ToolSchema schema) {
		return new Tool() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return "";
			}

			@Override
			public ToolSchema schema() {
				return schema;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("Result of " + name);
			}

			@Override
			public ToolMode mode() {
				return ToolMode.STRUCTURAL;
			}
		};
	}

	private static SecurityContext developmentSecurity() {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
	}

	/** A provider that replays the given responses, one per turn. */
	@SafeVarargs
	private static LLMProvider mockProvider(List<ContentPart>... responses) {
		AtomicInteger callIndex = new AtomicInteger(0);
		List<List<ContentPart>> responseList = List.of(responses);
		return (request, handler) -> {
			List<ContentPart> content = responseList.get(callIndex.getAndIncrement());
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				}
			}
			String finishReason = content.stream().anyMatch(ToolCall.class::isInstance)
				? "tool_use" : "end_turn";
			handler.onEvent(new StreamEvent.MessageComplete(
				"msg", MODEL, finishReason, USAGE, content));
		};
	}
}

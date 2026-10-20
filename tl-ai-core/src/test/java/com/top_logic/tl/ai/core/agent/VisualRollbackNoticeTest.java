/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.junit.VisualHarness;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What the model is shown after a turn whose transaction failed to commit - printed for a human.
 *
 * <p>
 * The incident this reproduces: a write tool reported {@code COMPLETED} with an identifier, the
 * turn's commit then failed, and the agent told the user - citing that identifier - that the object
 * had been created. Nothing was persisted. The transcript below is the request the model answers
 * from, so it shows directly whether the correction is in it.
 * </p>
 *
 * <p>
 * This is <b>not test coverage</b>: it asserts nothing, and is gated behind {@link VisualHarness}
 * like the other harnesses - what the loop must do is pinned by
 * {@code TestAgentLoop.testCommitFailureTellsTheModel}. It spends no API call; the provider is a
 * stand-in that answers out of what it was shown, so the last line differs before and after the
 * fix.
 * </p>
 *
 * <pre>
 * mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.visual=true -Dtest=VisualRollbackNoticeTest
 * </pre>
 */
@RunWith(AssumptionAwareRunner.class)
public class VisualRollbackNoticeTest extends TestCase {

	/** The identifier the tool claims to have created, as the reported incident had one. */
	private static final String UUID_CLAIM = "4f2c-9a11-b7e3";

	/** What the engine says when an unexpected {@link Throwable} escaped the commit. */
	private static final String DB_FAILURE = "Database operation failed.";

	/**
	 * Renders the provider request that follows a failed commit.
	 */
	public void testCommitFailureTranscript() {
		VisualHarness.assumeEnabled();

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", Instant.EPOCH,
			List.of(new TextContent("Create a document named 'Q3 report'."))));

		ToolCatalog registry = new ToolCatalog();
		registry.register(createDocumentTool());
		registry.freeze();

		List<List<Message>> requests = new ArrayList<>();
		List<String> events = new ArrayList<>();

		AgentResult result = new AgentLoop(
			new AgentParams("test-model", "You are a test assistant.",
				AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS),
			answersFromWhatItWasShown(requests), registry)
				.run(conv, new ToolContext(permissive(), ApprovalCallback.AUTO_APPROVE,
					failingCommitScope()), e -> events.add(describeTransaction(e)));

		List<Message> shown = requests.get(requests.size() - 1);
		System.out.println();
		System.out.println("=================================================================");
		System.out.println(" WHAT THE MODEL RECEIVES IN THE REQUEST AFTER THE FAILED COMMIT");
		System.out.println("=================================================================");
		for (Message message : shown) {
			System.out.println("  [" + kind(message) + "] " + describe(message));
		}
		System.out.println("-----------------------------------------------------------------");
		System.out.println(" transaction events : " + events.stream().filter(e -> e != null).toList());
		System.out.println(" model's answer     : " + lastAssistantText(conv));
		System.out.println(" stop reason        : " + result.stopReason());
		System.out.println(" framework notice   : " + (notice(shown) == null ? "NONE" : "present"));
		System.out.println("=================================================================");
	}

	/** A write tool of the shape the incident had: it returns a map naming the created object. */
	private static Tool createDocumentTool() {
		ToolSchema schema = new ToolSchema("createDocument", "Creates a document", "{}");
		return new Tool() {
			@Override
			public String name() {
				return "createDocument";
			}

			@Override
			public String description() {
				return "Creates a document";
			}

			@Override
			public ToolSchema schema() {
				return schema;
			}

			@Override
			public ToolMode mode() {
				return ToolMode.STRUCTURAL;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				// The tool's own transaction is nested inside the turn's, so this "success" is a
				// claim about a commit that has not run yet.
				return ToolResult.of("{document=AttributedWrapper(Document), uuid=" + UUID_CLAIM + "}");
			}
		};
	}

	/**
	 * A provider that calls the tool first and then answers out of the history it was given: it
	 * repeats the claim unless the history retracts it. That is the whole point of the transcript -
	 * a real model has no other source either.
	 */
	private static LLMProvider answersFromWhatItWasShown(List<List<Message>> requests) {
		return (request, handler) -> {
			requests.add(List.copyOf(request.messages()));
			int index = requests.size() - 1;
			List<ContentPart> content = index == 0
				? List.of(new ToolCall("tc1", "createDocument", "{\"name\":\"Q3 report\"}"))
				: List.of(new TextContent(notice(request.messages()) == null
					? "I created the document 'Q3 report' (uuid " + UUID_CLAIM + ")."
					: "The document was NOT created - saving it failed and the change was rolled"
						+ " back."));
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				}
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + index, "test-model",
				index == 0 ? "tool_use" : "end_turn", new TokenUsage(100, 50, 0, 0, 0), content));
		};
	}

	/**
	 * A scope whose commit fails the way the engine's does, leaving the transaction intact so the
	 * loop can roll it back. No {@code KnowledgeBase} is needed to show the message flow.
	 */
	private static TransactionScope failingCommitScope() {
		return new TransactionScope() {
			private boolean _active;

			@Override
			public void begin() {
				_active = true;
			}

			@Override
			public boolean isActive() {
				return _active;
			}

			@Override
			public void commit() {
				throw new TransactionException(TransactionScope.COMMIT_FAILED_PREFIX + DB_FAILURE);
			}

			@Override
			public void rollback() {
				_active = false;
			}

			@Override
			public void acquireLock(String operation, com.top_logic.model.TLObject... targets) {
				// No LockService in this harness.
			}
		};
	}

	private static SecurityContext permissive() {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return ModelSecurity.UNCHECKED;
			}
		};
	}

	private static String describeTransaction(AgentEvent event) {
		if (event instanceof AgentEvent.TransactionBegan) {
			return "TransactionBegan";
		}
		if (event instanceof AgentEvent.TransactionCommitted committed) {
			return "TransactionCommitted(rev=" + committed.revisionNumber() + ")";
		}
		if (event instanceof AgentEvent.TransactionRolledBack rolledBack) {
			return "TransactionRolledBack(reason=" + rolledBack.reason() + ")";
		}
		return null;
	}

	private static String kind(Message message) {
		if (message instanceof UserMessage) {
			return "user";
		}
		if (message instanceof AssistantMessage) {
			return "assistant";
		}
		if (message instanceof ToolResultMessage) {
			return "tool_result";
		}
		return message.getClass().getSimpleName();
	}

	private static String describe(Message message) {
		if (message instanceof ToolResultMessage result) {
			return result.toolName() + " -> " + result.state();
		}
		StringBuilder out = new StringBuilder();
		for (ContentPart part : contentOf(message)) {
			if (part instanceof TextContent text) {
				out.append(text.text());
			} else if (part instanceof ToolCall call) {
				out.append("call ").append(call.name()).append(call.arguments());
			} else {
				out.append(part);
			}
		}
		return out.toString();
	}

	private static List<ContentPart> contentOf(Message message) {
		if (message instanceof UserMessage user) {
			return user.content();
		}
		if (message instanceof AssistantMessage assistant) {
			return assistant.content();
		}
		return List.of();
	}

	private static String notice(List<Message> messages) {
		for (Message message : messages) {
			for (ContentPart part : contentOf(message)) {
				if (part instanceof TextContent text && text.text().startsWith("[Framework notice]")) {
					return text.text();
				}
			}
		}
		return null;
	}

	private static String lastAssistantText(Conversation conv) {
		String last = "(none)";
		for (Message message : conv.messages()) {
			if (message instanceof AssistantMessage assistant) {
				for (ContentPart part : assistant.content()) {
					if (part instanceof TextContent text) {
						last = text.text();
					}
				}
			}
		}
		return last;
	}

	/**
	 * The loop resolves the acting sub-session, so the transcript needs the module system - and only
	 * the enabled harness pays for booting it.
	 *
	 * @see VisualAgentLoopTest#suite()
	 */
	public static Test suite() {
		TestSuite tests = new TestSuite(VisualRollbackNoticeTest.class);
		return VisualHarness.enabled()
			? ModuleTestSetup.setupModule(ServiceTestSetup.withThreadContext(tests))
			: tests;
	}

}

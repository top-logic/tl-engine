/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStore.AgentTask;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * An agent keeps its plan as tasks without asking the user for every step, while a write to the
 * application's data is still put in front of the user.
 *
 * <p>
 * The runs are real {@link AgentLoop} runs under the default approval policy
 * ({@link SecurityContext#checkApproval}), with a scripted model and an approval callback that
 * records every question it is asked.
 * </p>
 */
public class TestTaskPlanWithoutApproval extends TestCase {

	/** The agent loop resolves the acting sub-session, so these runs need the module system up. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestTaskPlanWithoutApproval.class)));
	}

	/**
	 * A research lead plans three steps, works through them and records something in the application
	 * on the way: the user is asked once, for the record - not for any step of the plan.
	 */
	public void testOnlyTheWriteToApplicationDataIsApproved() {
		List<String> asked = Collections.synchronizedList(new ArrayList<>());
		TaskStore plan = new TaskStore();

		run(plan, request -> {
			asked.add(request.toolName());
			return new ApprovalResult.Approved();
		},
			List.of(create("Scope the question"), create("Search sources"), create("Write the report")),
			List.of(update("1", "in_progress")),
			List.of(update("1", "completed"), update("2", "in_progress")),
			List.of(new ToolCall("c-save", "saveRecord", "{}")),
			List.of(update("2", "completed"), update("3", "deleted")));

		assertEquals("only the write to application data was put in front of the user",
			List.of("saveRecord"), asked);
		List<AgentTask> tasks = plan.getAllTasks();
		assertEquals(2, tasks.size());
		assertEquals("completed", tasks.get(0).getStatus());
		assertEquals("completed", tasks.get(1).getStatus());
	}

	/**
	 * Tasks created in one turn keep the order the model wrote them in: the plan's numbering is its
	 * order, so the calls run one after the other, not in a concurrent batch.
	 */
	public void testTasksCreatedTogetherKeepTheirOrder() {
		TaskStore plan = new TaskStore();
		List<ToolCall> five = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			five.add(create("Step " + i));
		}

		run(plan, ApprovalCallback.AUTO_APPROVE, five);

		List<AgentTask> tasks = plan.getAllTasks();
		for (int i = 0; i < 5; i++) {
			assertEquals(String.valueOf(i + 1), tasks.get(i).getId());
			assertEquals("Step " + (i + 1), tasks.get(i).getSubject());
		}
	}

	/** A rejected write does not stop the plan: the plan was never in front of the user. */
	public void testTheUserRejectingAWriteLeavesThePlanAlone() {
		TaskStore plan = new TaskStore();

		run(plan, request -> new ApprovalResult.Rejected("Not now."),
			List.of(create("Collect figures")),
			List.of(new ToolCall("c-save", "saveRecord", "{}")),
			List.of(update("1", "completed")));

		assertEquals("completed", plan.getTask("1").getStatus());
	}

	/** Runs a scripted model that makes the given calls, one list per turn, then answers. */
	@SafeVarargs
	private static void run(TaskStore plan, ApprovalCallback approval, List<ToolCall>... turns) {
		Tool saveRecord = ToolStubs.builder("saveRecord").description("Saves a record.")
			.mode(ToolMode.DATA_WRITE).concurrencySafe(false).result("Saved.").build();
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(new TaskTool());
		catalog.register(saveRecord);
		catalog.freeze();

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Research the topic and record the result."))));
		AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You plan with tasks.", turns.length + 2,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), new ScriptedModel(turns), catalog);
		try (TaskStoreBinding.Binding bound = TaskStoreBinding.bind(plan)) {
			loop.run(conversation, new ToolContext(defaultPolicy(), approval), event -> {
				// The plan and the approval callback record what matters.
			});
		}
	}

	private static ToolCall create(String subject) {
		return new ToolCall("c-" + UUID.randomUUID(), "createTask",
			"{\"subject\": \"" + subject + "\", \"description\": \"d\", \"activeForm\": \"Working\"}");
	}

	private static ToolCall update(String id, String status) {
		return new ToolCall("c-" + UUID.randomUUID(), "updateTask",
			"{\"taskId\": \"" + id + "\", \"status\": \"" + status + "\"}");
	}

	/** Every tool permitted, approval by the default policy: every call that changes state is asked. */
	private static SecurityContext defaultPolicy() {
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

	/** A model that makes one list of scripted tool calls per turn, then answers with text. */
	private static final class ScriptedModel implements LLMProvider {

		private final List<ToolCall>[] _turns;

		private int _turn;

		ScriptedModel(List<ToolCall>[] turns) {
			_turns = turns;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) {
			int turn = _turn++;
			List<ContentPart> content = turn < _turns.length
				? new ArrayList<>(_turns[turn])
				: List.of(new TextContent("Done."));
			handler.onEvent(new StreamEvent.StreamStart());
			boolean calls = false;
			for (int i = 0; i < content.size(); i++) {
				ContentPart part = content.get(i);
				if (part instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextStart(i));
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
					handler.onEvent(new StreamEvent.TextEnd(i));
				} else if (part instanceof ToolCall call) {
					calls = true;
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
					handler.onEvent(new StreamEvent.ToolCallEnd(i));
				}
			}
			handler.onEvent(new StreamEvent.MessageComplete("scripted-" + turn, request.model(),
				calls ? "tool_use" : "end_turn", TokenUsage.EMPTY, content));
		}
	}
}

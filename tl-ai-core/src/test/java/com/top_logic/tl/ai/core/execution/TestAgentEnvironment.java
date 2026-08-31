/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.RunCancellationBinding;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Tests for {@link AgentEnvironment}: all five thread-local bindings are established on open and
 * previous values are restored on close.
 */
public class TestAgentEnvironment extends TestCase {

	public void testOpenBindsAllFiveAndCloseRestores() {
		ConversationLogListener listener = new ConversationLogListener(new ConversationLog());
		TaskStore taskStore = new TaskStore();
		UserInteractionStore interactionStore = new UserInteractionStore();
		Set<String> skills = Set.of("skill-a");
		CancellationToken token = new CancellationToken();

		assertNull(ConversationLogListenerBinding.getListener());
		try (AgentEnvironment env =
				AgentEnvironment.open(listener, taskStore, interactionStore, skills, token)) {
			assertSame(listener, ConversationLogListenerBinding.getListener());
			assertSame(taskStore, TaskStoreBinding.getStore());
			assertSame(interactionStore, UserInteractionStoreBinding.getStore());
			assertSame(skills, LoadedSkillsBinding.getSkills());
			assertSame(token, RunCancellationBinding.getToken());
			assertSame(listener, env.logListener());
			assertSame(taskStore, env.taskStore());
			assertSame(token, env.cancellationToken());
		}
		assertNull(ConversationLogListenerBinding.getListener());
		assertNull(TaskStoreBinding.getStore());
		assertNull(UserInteractionStoreBinding.getStore());
		assertNull(LoadedSkillsBinding.getSkills());
		assertNull(RunCancellationBinding.getToken());
	}

	/**
	 * The binding reports cancellation only for a run whose token was actually cancelled - an
	 * unbound thread and an untouched run both report "not cancelled".
	 */
	public void testRunCancelledOnlyForCancelledRun() {
		assertFalse("No run on this thread.", RunCancellationBinding.isRunCancelled());

		CancellationToken token = new CancellationToken();
		try (AgentEnvironment env = AgentEnvironment.open(
				new ConversationLogListener(new ConversationLog()), new TaskStore(),
				new UserInteractionStore(), Set.of(), token)) {
			assertFalse("Run not cancelled yet.", RunCancellationBinding.isRunCancelled());
			token.cancel();
			assertTrue("Run cancelled.", RunCancellationBinding.isRunCancelled());
		}
		assertFalse("Binding released.", RunCancellationBinding.isRunCancelled());
	}

	public void testNestedScopesRestorePrevious() {
		ConversationLogListener outer = new ConversationLogListener(new ConversationLog());
		ConversationLogListener inner = new ConversationLogListener(new ConversationLog());

		try (AgentEnvironment o =
				AgentEnvironment.open(outer, new TaskStore(), new UserInteractionStore(), Set.of(),
					new CancellationToken())) {
			try (AgentEnvironment i =
					AgentEnvironment.open(inner, new TaskStore(), new UserInteractionStore(), Set.of(),
						new CancellationToken())) {
				assertSame(inner, ConversationLogListenerBinding.getListener());
			}
			assertSame(outer, ConversationLogListenerBinding.getListener());
		}
		assertNull(ConversationLogListenerBinding.getListener());
	}

	public void testOpenEphemeralProvidesFreshState() {
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			assertNotNull(env.logListener());
			assertNotNull(env.taskStore());
			assertNotNull(env.interactionStore());
			assertNotNull(env.loadedSkills());
			assertSame(CancellationToken.UNCANCELLABLE, env.cancellationToken());
			assertSame(env.logListener(), ConversationLogListenerBinding.getListener());
		}
		assertNull(ConversationLogListenerBinding.getListener());
	}
}

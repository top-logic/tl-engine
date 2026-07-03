/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;

/**
 * Tests for {@link AgentEnvironment}: all four thread-local bindings are established on open and
 * previous values are restored on close.
 */
public class TestAgentEnvironment extends TestCase {

	public void testOpenBindsAllFourAndCloseRestores() {
		ConversationLogListener listener = new ConversationLogListener(new ConversationLog());
		TaskStore taskStore = new TaskStore();
		UserInteractionStore interactionStore = new UserInteractionStore();
		Set<String> skills = Set.of("skill-a");

		assertNull(ConversationLogListenerBinding.getListener());
		try (AgentEnvironment env = AgentEnvironment.open(listener, taskStore, interactionStore, skills)) {
			assertSame(listener, ConversationLogListenerBinding.getListener());
			assertSame(taskStore, TaskStoreBinding.getStore());
			assertSame(interactionStore, UserInteractionStoreBinding.getStore());
			assertSame(skills, LoadedSkillsBinding.getSkills());
			assertSame(listener, env.logListener());
			assertSame(taskStore, env.taskStore());
		}
		assertNull(ConversationLogListenerBinding.getListener());
		assertNull(TaskStoreBinding.getStore());
		assertNull(UserInteractionStoreBinding.getStore());
		assertNull(LoadedSkillsBinding.getSkills());
	}

	public void testNestedScopesRestorePrevious() {
		ConversationLogListener outer = new ConversationLogListener(new ConversationLog());
		ConversationLogListener inner = new ConversationLogListener(new ConversationLog());

		try (AgentEnvironment o =
				AgentEnvironment.open(outer, new TaskStore(), new UserInteractionStore(), Set.of())) {
			try (AgentEnvironment i =
					AgentEnvironment.open(inner, new TaskStore(), new UserInteractionStore(), Set.of())) {
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
			assertSame(env.logListener(), ConversationLogListenerBinding.getListener());
		}
		assertNull(ConversationLogListenerBinding.getListener());
	}
}

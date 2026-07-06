/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;

/**
 * Scope establishing the thread-local environment an agent run expects.
 *
 * <p>
 * An agent run relies on four thread-local bindings: the {@link ConversationLogListener} for
 * structured recording, the {@link TaskStore} for the todo plan, the
 * {@link UserInteractionStore} for human-in-the-loop requests, and the loaded-skills set. Every
 * entry point used to establish (and clean up) these four bindings by hand; this class bundles
 * them into a single try-with-resources scope:
 * </p>
 *
 * <pre>
 * try (AgentEnvironment env = AgentEnvironment.open(logListener, taskStore, interactionStore, skills)) {
 *     // run the agent
 * }
 * </pre>
 *
 * <p>
 * {@link #openEphemeral()} creates a scope over fresh, throwaway state for one-shot invocations
 * (TL-Script functions, tests) that don't retain anything across calls.
 * </p>
 */
public final class AgentEnvironment implements AutoCloseable {

	private final ConversationLogListener _logListener;

	private final TaskStore _taskStore;

	private final UserInteractionStore _interactionStore;

	private final Set<String> _loadedSkills;

	private final ConversationLogListenerBinding.Binding _listenerBinding;

	private final TaskStoreBinding.Binding _taskStoreBinding;

	private final UserInteractionStoreBinding.Binding _interactionStoreBinding;

	private final LoadedSkillsBinding.Binding _skillsBinding;

	private AgentEnvironment(ConversationLogListener logListener, TaskStore taskStore,
			UserInteractionStore interactionStore, Set<String> loadedSkills) {
		_logListener = logListener;
		_taskStore = taskStore;
		_interactionStore = interactionStore;
		_loadedSkills = loadedSkills;
		_listenerBinding = ConversationLogListenerBinding.bind(logListener);
		_taskStoreBinding = TaskStoreBinding.bind(taskStore);
		_interactionStoreBinding = UserInteractionStoreBinding.bind(interactionStore);
		_skillsBinding = LoadedSkillsBinding.bind(loadedSkills);
	}

	/**
	 * Opens a scope binding the given session state on the current thread.
	 *
	 * <p>
	 * Previously bound values are captured and restored on {@link #close()}, making the scope
	 * safe on cached thread pools.
	 * </p>
	 */
	public static AgentEnvironment open(ConversationLogListener logListener, TaskStore taskStore,
			UserInteractionStore interactionStore, Set<String> loadedSkills) {
		return new AgentEnvironment(logListener, taskStore, interactionStore, loadedSkills);
	}

	/**
	 * Opens a scope over fresh, throwaway state for a one-shot invocation.
	 *
	 * <p>
	 * The recording made into the throwaway {@link ConversationLog} is reachable via
	 * {@link #logListener()} until the scope closes, e.g. to persist it as an execution
	 * transcript.
	 * </p>
	 */
	public static AgentEnvironment openEphemeral() {
		return new AgentEnvironment(new ConversationLogListener(new ConversationLog()),
			new TaskStore(), new UserInteractionStore(), ConcurrentHashMap.newKeySet());
	}

	/** The conversation-log listener bound by this scope. */
	public ConversationLogListener logListener() {
		return _logListener;
	}

	/** The task store bound by this scope. */
	public TaskStore taskStore() {
		return _taskStore;
	}

	/** The user-interaction store bound by this scope. */
	public UserInteractionStore interactionStore() {
		return _interactionStore;
	}

	/** The loaded-skills set bound by this scope. */
	public Set<String> loadedSkills() {
		return _loadedSkills;
	}

	@Override
	public void close() {
		_skillsBinding.close();
		_interactionStoreBinding.close();
		_taskStoreBinding.close();
		_listenerBinding.close();
	}
}

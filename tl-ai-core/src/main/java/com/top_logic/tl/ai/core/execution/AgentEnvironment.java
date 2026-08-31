/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 * Scope establishing the thread-local environment an agent run expects.
 *
 * <p>
 * An agent run relies on five thread-local bindings: the {@link ConversationLogListener} for
 * structured recording, the {@link TaskStore} for the todo plan, the
 * {@link UserInteractionStore} for human-in-the-loop requests, the loaded-skills set, and the
 * run's {@link CancellationToken}. Every entry point used to establish (and clean up) these
 * bindings by hand; this class bundles them into a single try-with-resources scope:
 * </p>
 *
 * <pre>
 * try (AgentEnvironment env =
 *         AgentEnvironment.open(logListener, taskStore, interactionStore, skills, token)) {
 *     // run the agent
 * }
 * </pre>
 *
 * <p>
 * {@link #openEphemeral()} creates a scope over fresh, throwaway state for one-shot invocations
 * (TL-Script functions, tests) that don't retain anything across calls. Such a run has no
 * cancellation channel, so it binds {@link CancellationToken#UNCANCELLABLE}.
 * </p>
 */
public final class AgentEnvironment implements AutoCloseable {

	private final ConversationLogListener _logListener;

	private final TaskStore _taskStore;

	private final UserInteractionStore _interactionStore;

	private final Set<String> _loadedSkills;

	private final CancellationToken _cancellationToken;

	private final ConversationLogListenerBinding.Binding _listenerBinding;

	private final TaskStoreBinding.Binding _taskStoreBinding;

	private final UserInteractionStoreBinding.Binding _interactionStoreBinding;

	private final LoadedSkillsBinding.Binding _skillsBinding;

	private final RunCancellationBinding.Binding _cancellationBinding;

	private AgentEnvironment(ConversationLogListener logListener, TaskStore taskStore,
			UserInteractionStore interactionStore, Set<String> loadedSkills,
			CancellationToken cancellationToken) {
		_logListener = logListener;
		_taskStore = taskStore;
		_interactionStore = interactionStore;
		_loadedSkills = loadedSkills;
		_cancellationToken = cancellationToken;
		_listenerBinding = ConversationLogListenerBinding.bind(logListener);
		_taskStoreBinding = TaskStoreBinding.bind(taskStore);
		_interactionStoreBinding = UserInteractionStoreBinding.bind(interactionStore);
		_skillsBinding = LoadedSkillsBinding.bind(loadedSkills);
		_cancellationBinding = RunCancellationBinding.bind(cancellationToken);
	}

	/**
	 * Opens a scope binding the given session state on the current thread.
	 *
	 * <p>
	 * Previously bound values are captured and restored on {@link #close()}, making the scope
	 * safe on cached thread pools.
	 * </p>
	 *
	 * @param cancellationToken
	 *        The token the run is cancelled through; the same instance the run's
	 *        {@code ToolContext} carries. Pass {@link CancellationToken#UNCANCELLABLE} for a run
	 *        that cannot be cancelled.
	 */
	public static AgentEnvironment open(ConversationLogListener logListener, TaskStore taskStore,
			UserInteractionStore interactionStore, Set<String> loadedSkills,
			CancellationToken cancellationToken) {
		return new AgentEnvironment(logListener, taskStore, interactionStore, loadedSkills,
			cancellationToken);
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
			new TaskStore(), new UserInteractionStore(), ConcurrentHashMap.newKeySet(),
			CancellationToken.UNCANCELLABLE);
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

	/** The cancellation token bound by this scope. */
	public CancellationToken cancellationToken() {
		return _cancellationToken;
	}

	@Override
	public void close() {
		_cancellationBinding.close();
		_skillsBinding.close();
		_interactionStoreBinding.close();
		_taskStoreBinding.close();
		_listenerBinding.close();
	}
}

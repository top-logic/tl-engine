/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Service holding the {@link WorkspaceAgent}s of the {@link FactoryModel#AGENT_CONVERSATION}s:
 * the coding agent that works in a workspace, one process per conversation.
 *
 * <p>
 * {@link AgentConversationResolver} lets the agents answer the chats whose subject is a
 * conversation. Cleaning up a workspace {@link #stopAll(String) stops} its agents; a shutdown
 * stops all of them.
 * </p>
 */
@ServiceDependencies({
	WorkspaceService.Module.class,
	WorkspaceProxyService.Module.class,
	PersistencyLayer.Module.class,
	ModelService.Module.class,
})
public class WorkspaceAgentService extends ConfiguredManagedClass<WorkspaceAgentService.Config<?>> {

	/**
	 * Configuration of {@link WorkspaceAgentService}.
	 */
	public interface Config<I extends WorkspaceAgentService> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name of {@link #getHarness()}. */
		String HARNESS = "harness";

		/** Configuration property name of {@link #getIdleTimeout()}. */
		String IDLE_TIMEOUT = "idle-timeout";

		/** Configuration property name of {@link #getTokenLifetime()}. */
		String TOKEN_LIFETIME = "token-lifetime";

		/** Configuration property name of {@link #getInterruptTimeout()}. */
		String INTERRUPT_TIMEOUT = "interrupt-timeout";

		/** Configuration property name of {@link #getStopTimeout()}. */
		String STOP_TIMEOUT = "stop-timeout";

		/**
		 * The coding agent that runs in the workspaces.
		 *
		 * @see #HARNESS
		 */
		@Name(HARNESS)
		@Mandatory
		PolymorphicConfiguration<? extends AgentHarness> getHarness();

		/**
		 * How long the process of a conversation is kept after its last answer, e.g.
		 * {@code 15min}; the next message starts a new process continuing the conversation.
		 *
		 * @see #IDLE_TIMEOUT
		 */
		@Name(IDLE_TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(15 * 60 * 1000)
		long getIdleTimeout();

		/**
		 * The lifetime of the token of an agent process for the workspace proxy, e.g. {@code 4h}; at
		 * most the {@link WorkspaceProxyService.Config#getMaxTokenLifetime() longest lifetime} of the
		 * proxy.
		 *
		 * <p>
		 * A process whose token has less than half of its lifetime left is replaced by a new one
		 * before its next run, so that a run has at least that half.
		 * </p>
		 *
		 * @see #TOKEN_LIFETIME
		 */
		@Name(TOKEN_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(4 * 60 * 60 * 1000)
		long getTokenLifetime();

		/**
		 * How long a run waits for the agent to stop after the user stopped it, before its process
		 * is terminated.
		 *
		 * @see #INTERRUPT_TIMEOUT
		 */
		@Name(INTERRUPT_TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(30 * 1000)
		long getInterruptTimeout();

		/**
		 * How long a process that is no longer needed has to end after its input was closed,
		 * before it is terminated.
		 *
		 * @see #STOP_TIMEOUT
		 */
		@Name(STOP_TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(10 * 1000)
		long getStopTimeout();

	}

	private final AgentHarness _harness;

	private final Map<Object, WorkspaceAgent> _agents = new ConcurrentHashMap<>();

	private ScheduledExecutorService _scheduler;

	private ExecutorService _watchers;

	private AgentRuntime _runtime;

	/**
	 * Creates a {@link WorkspaceAgentService} from configuration.
	 */
	public WorkspaceAgentService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_harness = context.getInstance(config.getHarness());
	}

	@Override
	protected void startUp() {
		super.startUp();
		_scheduler = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.daemonThreadFactory());
		_watchers = Executors.newCachedThreadPool(DaemonThreadFactory.daemonThreadFactory());
		Config<?> config = getConfig();
		WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
		boolean llmAccess = proxy != null && proxy.isEnabled();
		Duration tokenLifetime = Duration.ofMillis(config.getTokenLifetime());
		if (llmAccess && tokenLifetime.compareTo(proxy.getMaxTokenLifetime()) > 0) {
			tokenLifetime = proxy.getMaxTokenLifetime();
		}
		_runtime = new AgentRuntime(_harness, WorkspaceService.getInstance().getProvider(),
			llmAccess ? proxy : null,
			Duration.ofMillis(config.getIdleTimeout()), tokenLifetime,
			Duration.ofMillis(config.getInterruptTimeout()), Duration.ofMillis(config.getStopTimeout()),
			_scheduler, _watchers, Clock.systemUTC(), WorkspaceAgentService::messages);
	}

	/**
	 * The messages to the given user in the user's language, in the default language for no user.
	 */
	private static I18NBundle messages(Person user) {
		return user == null ? Resources.getSystemInstance() : Resources.getInstance(Resources.findBestLocale(user));
	}

	@Override
	protected void shutDown() {
		for (WorkspaceAgent agent : _agents.values()) {
			agent.stop();
		}
		_agents.clear();
		_scheduler.shutdownNow();
		_watchers.shutdownNow();
		_runtime = null;
		super.shutDown();
	}

	/**
	 * The agent holding the given conversation.
	 *
	 * @param conversation
	 *        A stored {@link FactoryModel#AGENT_CONVERSATION}.
	 */
	public WorkspaceAgent agentFor(TLObject conversation) {
		return _agents.computeIfAbsent(conversation.tId(),
			key -> new WorkspaceAgent(_runtime, new StoredConversation(conversation)));
	}

	/**
	 * Stops the agents of all conversations in the given workspace, e.g. before it is removed.
	 */
	public void stopAll(String workspaceId) {
		List<WorkspaceAgent> stopped = new ArrayList<>();
		_agents.values().removeIf(agent -> {
			if (agent.workspaceId().equals(workspaceId)) {
				stopped.add(agent);
				return true;
			}
			return false;
		});
		for (WorkspaceAgent agent : stopped) {
			agent.stop();
		}
	}

	/**
	 * The {@link WorkspaceAgentService} of the running application, {@code null} if it is not
	 * started.
	 */
	public static WorkspaceAgentService getInstance() {
		return Module.INSTANCE.isActive() ? Module.INSTANCE.getImplementationInstance() : null;
	}

	/**
	 * Module of the {@link WorkspaceAgentService}.
	 */
	public static final class Module extends TypedRuntimeModule<WorkspaceAgentService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<WorkspaceAgentService> getImplementation() {
			return WorkspaceAgentService.class;
		}
	}

}

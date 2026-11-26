/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import com.top_logic.ai.a2a.A2AArtifactContributor;
import com.top_logic.ai.a2a.A2AArtifactSource;
import com.top_logic.ai.a2a.A2ADataHandler;
import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.ExposedAgentConfig;
import com.top_logic.ai.a2a.A2ASubjectResolver;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.ai.core.agent.AgentFactory;
import com.top_logic.ai.core.agent.AgentResult;
import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TransactionScope;
import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.agent.event.CompositeAgentEventListener;
import com.top_logic.ai.core.execution.AgentEnvironment;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentRunner;
import com.top_logic.ai.core.execution.CancelSignalObserver;
import com.top_logic.ai.core.execution.ExecutionRecorder;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.Conversation;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.UserMessage;
import com.top_logic.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.ai.core.security.ReadFilter;
import com.top_logic.ai.core.security.RunEntryPoint;
import com.top_logic.ai.core.security.RunPrincipal;
import com.top_logic.ai.core.security.RunSecurity;
import com.top_logic.ai.core.session.ConversationLog;
import com.top_logic.ai.core.session.ConversationLogListener;
import com.top_logic.ai.core.session.ConversationLogReplay;
import com.top_logic.ai.core.session.Part;
import com.top_logic.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.ai.core.tools.ToolRegistry;
import com.top_logic.ai.core.tools.interaction.ToolRegistryReloadFlag;
import com.top_logic.ai.core.tools.model.TidResolver;
import com.top_logic.ai.core.tools.task.TaskStore;
import com.top_logic.util.TLContextManager;

/**
 * Runs the tl-ai agent behind each A2A task.
 *
 * <ul>
 * <li>One context = one {@link AgentSession} (kind {@code BACKGROUND}) owned by the calling account,
 * with the conversation kept in memory between tasks and its state in the session's metadata, so a
 * follow-up continues it, also after a restart.</li>
 * <li>One task = one recorded run ({@link AgentRunner}), visible in the application's agent
 * activity.</li>
 * <li>The run acts as the calling account ({@link RunEntryPoint#A2A}), and it is a background run:
 * the grants of the account and the {@code <background-allowlist>} bound its tools, also for an
 * administrator account. See {@link #toolContext}.</li>
 * <li>The subject of a context is what the {@link A2ASubjectResolver}s find, else the subject
 * remembered for the context. It is a statement, not yet a disclosure: each run states it, and
 * {@link RunSecurity} keeps it only if the calling account may read it. Only that checked subject
 * reaches the agent (in the environment block of its run), the answer and the artifact
 * contributors. The session gets no subject: a role rule over the subject of a session would open
 * the transcript of the run to each reader of the object. The checked subject is kept in the
 * session's metadata instead, so that the context has it again after a restart.</li>
 * </ul>
 */
public final class AgentRunBackend implements A2ARequestHandler.TaskBackend {

	private static final int MAX_CONVERSATIONS = 500;

	/** How many tasks a context keeps in its session's metadata. */
	private static final int MAX_PERSISTED_TASKS = 100;

	/**
	 * Entry of the {@code a2a} metadata that holds the identifier of the checked subject of a
	 * context, for {@link #restore}.
	 */
	private static final String SUBJECT_ID = "subjectId";

	private final A2AService _service;

	private final ExecutorService _executor = Executors.newCachedThreadPool(job -> {
		Thread thread = new Thread(job, "a2a-run");
		thread.setDaemon(true);
		return thread;
	});

	/** The tokens of the runs in progress, which {@link #shutDown()} cancels. */
	private final Set<CancellationToken> _running = ConcurrentHashMap.newKeySet();

	private final Map<String, Context> _contexts = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Context> eldest) {
			return size() > MAX_CONVERSATIONS && !eldest.getValue()._lock.isLocked();
		}
	};

	/**
	 * The state of one A2A context between its tasks.
	 */
	private static final class Context {

		final String _contextId;

		final ReentrantLock _lock = new ReentrantLock();

		final Conversation _conversation = new Conversation();

		final ConversationLog _log = new ConversationLog();

		final ConversationLogListener _logListener = new ConversationLogListener(_log);

		final TaskStore _taskStore = new TaskStore();

		final Set<String> _loadedSkills = Collections.synchronizedSet(new LinkedHashSet<>());

		ToolRegistry _toolRegistry;

		AgentSession _session;

		/** The subject stated for the context; each run checks it again for the calling account. */
		TLObject _subject;

		/** The {@code a2a} object of the session's metadata: context, agent, caller, peer, tasks. */
		Map<String, Object> _metadata = new LinkedHashMap<>();

		boolean _restoreChecked;

		Context(String contextId) {
			_contextId = contextId;
		}
	}

	/**
	 * Creates an {@link AgentRunBackend}.
	 */
	public AgentRunBackend(A2AService service) {
		_service = service;
	}

	/**
	 * Stops accepting runs, and cancels the runs in progress.
	 *
	 * <p>
	 * Through their tokens, not by interrupting their threads: a run may be reading the database, and
	 * an interrupt during a read closes H2's file for the whole application.
	 * </p>
	 */
	public void shutDown() {
		_executor.shutdown();
		_running.forEach(CancellationToken::cancel);
	}

	@Override
	public void start(A2ATask task, Map<String, Object> message, Map<String, Object> requestMetadata) {
		Context context;
		synchronized (_contexts) {
			context = _contexts.computeIfAbsent(task.contextId(), Context::new);
		}
		CancellationToken token = new CancellationToken();
		ToolContext[] run = new ToolContext[1];
		task.setCancelHandler(() -> {
			token.cancel();
			AgentExecution execution = run[0] == null ? null : run[0].executionHandle().get();
			if (execution != null) {
				try {
					AgentExecutionStore.getInstance().requestCancel(execution);
				} catch (RuntimeException ex) {
					Logger.info("Could not mark the A2A run as cancelled: " + ex.getMessage(), AgentRunBackend.class);
				}
			}
		});
		_running.add(token);
		_executor.execute(() -> {
			try {
				Person person = ThreadContext.<Person, RuntimeException, RuntimeException> inSystemContext(
					AgentRunBackend.class, () -> Person.byName(task.owner()));
				ExposedAgentConfig agent = _service.exposedAgent(task.agent());
				if (person == null || agent == null) {
					fail(task, "The account or the agent of this request no longer exists.");
					return;
				}
				TLContextManager.<Void, RuntimeException, RuntimeException> inPersonContext(person, () -> {
					run(person, task, agent, context, message, requestMetadata, token, run);
					return null;
				});
			} catch (Throwable ex) {
				Logger.error("A2A run of task " + task.id() + " failed: " + ex.getMessage(), ex, AgentRunBackend.class);
				fail(task, "The run failed: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
			} finally {
				_running.remove(token);
			}
		});
	}

	private void run(Person person, A2ATask task, ExposedAgentConfig agent, Context context,
			Map<String, Object> message, Map<String, Object> metadata, CancellationToken token, ToolContext[] run) {
		context._lock.lock();
		try {
			if (!task.moveTo(TaskState.WORKING, null)) {
				// Canceled before it started.
				return;
			}
			String text = A2AJson.text(message, data -> handleData(agent, task, data));

			AgentExecutionStore store = AgentExecutionStore.getInstance();
			if (!context._restoreChecked) {
				restore(store, person, context);
			}
			AgentSession session = context._session != null ? context._session
				: createSession(store, person, task, agent, context, metadata, text);

			ToolContext toolContext = toolContext(person, agent.getAllowWrites(),
				ExecutionRecorder.conversationIdOf(session), context._subject, token);
			TLObject subject = toolContext.security().principal().subject();
			if (subject != null) {
				task.putMetadata(A2AJson.SUBJECT_METADATA, label(subject));
				rememberForRestart(context, subject);
			}
			_service.contexts().rememberSubject(context._contextId, subject);
			persist(task, context);
			run[0] = toolContext;

			String prompt = header(message, task.peer()) + text;
			context._conversation.add(new UserMessage(A2AJson.newId(), Instant.now(),
				List.<ContentPart> of(new TextContent(prompt))));
			context._log.startTurn()
				.setUserParts(List.<Part> of(new Part.TextPart(A2AJson.newId(), Instant.now(), prompt)));

			String model = agent.getModel();
			AgentFactory.RunOverrides overrides = model == null || model.isBlank()
				? AgentFactory.RunOverrides.NONE
				: new AgentFactory.RunOverrides(model.trim(), null);

			AgentRunner.RecordedRun result;
			try (AgentEnvironment env = AgentEnvironment.open(context._logListener, context._taskStore,
					new A2AInteractionStore(task, () -> persist(task, context)), context._loadedSkills, token)) {
				result = AgentRunner.run(agent.getAgent(), context._conversation, null, toolContext,
					new CompositeAgentEventListener(context._logListener, event -> report(task, event),
						new CancelSignalObserver(toolContext.executionHandle(), token)),
					context._toolRegistry, context._log, session, null, overrides);
			} finally {
				toolContext.transaction().close();
				HistoryUtils.updateSessionRevision();
			}
			context._toolRegistry = ToolRegistryReloadFlag.consume() ? null : result.registry();
			finish(task, agent, context, subject, result, token, text);
		} finally {
			try {
				persist(task, context);
			} finally {
				context._lock.unlock();
			}
		}
	}

	/**
	 * The tool context of an A2A run that acts as the given account.
	 *
	 * <p>
	 * The run is a background run. The calling agent answers the questions of the run, but it
	 * approves no tool call, and the person of the account is not present. So the
	 * {@code <background-allowlist>} bounds the tools of the run in addition to the grants of the
	 * account, as for an MCP request. Each tool call that the ceiling admits is approved
	 * automatically.
	 * </p>
	 *
	 * <p>
	 * The run stays attended for questions: {@code askUser} goes to the calling agent
	 * ({@link A2AInteractionStore}). For this reason the context is not made
	 * {@link ToolContext#unattended() unattended}, and it is not built with
	 * {@link ToolContext#forBackgroundRun}, which makes a run unattended and names the entry point MCP.
	 * </p>
	 *
	 * <p>
	 * {@code allow-writes} of the served agent only narrows: without it, the run gets no tool that
	 * changes the application ({@link WriteGuard}), and with it, a write tool also needs an entry in
	 * the allowlist.
	 * </p>
	 *
	 * <p>
	 * The run states the given subject; {@link RunSecurity} keeps it in the principal of the
	 * context only if the account may read it.
	 * </p>
	 *
	 * <p>
	 * Package-visible rather than private so a test can ask what an A2A run is given; it has one
	 * production caller, {@link #run}.
	 * </p>
	 */
	static ToolContext toolContext(Person person, boolean allowWrites, String conversationId,
			TLObject subject, CancellationToken token) {
		SecurityContext security = RunSecurity.contextFor(
			new RunPrincipal(person, RunEntryPoint.A2A, true, conversationId, subject));
		return new ToolContext(allowWrites ? security : new WriteGuard(security),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(token), token);
	}

	/**
	 * Shows the run's progress on the task: its run id, each tool call as a progress note, and the
	 * return to {@link TaskState#WORKING} once a question was answered.
	 */
	private static void report(A2ATask task, AgentEvent event) {
		if (event instanceof AgentEvent.RunStarted started) {
			task.putMetadata("runId", started.runId());
		} else if (event instanceof AgentEvent.ToolCallStarted started && !isAskUser(started.toolName())) {
			String args = started.argsJson() == null ? "" : started.argsJson();
			progress(task, shortName(started.toolName()) + "(" + (args.length() > 120 ? args.substring(0, 119) + "…" : args) + ")");
		} else if (event instanceof AgentEvent.ToolProgress note && note.message() != null && !note.message().isBlank()) {
			progress(task, note.message());
		} else if (event instanceof AgentEvent.ToolCallCompleted completed && isAskUser(completed.toolName())
			&& task.state() == TaskState.INPUT_REQUIRED) {
			// Answered, timed out or cancelled: the run goes on either way.
			task.moveTo(TaskState.WORKING, null);
		}
	}

	private static void progress(A2ATask task, String text) {
		if (task.state() == TaskState.WORKING) {
			task.moveTo(TaskState.WORKING, A2AJson.textMessage(A2AJson.ROLE_AGENT, task.contextId(), task.id(), text));
		}
	}

	private static boolean isAskUser(String toolName) {
		return toolName != null && toolName.endsWith("askUser");
	}

	private static String shortName(String toolName) {
		return toolName == null ? "tool" : toolName.substring(toolName.lastIndexOf('.') + 1);
	}

	/**
	 * What the agent reads for a data part of the message: the answer of the application's
	 * {@link A2ADataHandler}s, or <code>null</code> for the JSON.
	 */
	private String handleData(ExposedAgentConfig agent, A2ATask task, Object data) {
		A2ADataHandler.Request request =
			new A2ADataHandler.Request(agent.getPath(), task.peer(), task.contextId(), task.id(), data);
		return A2AService.ask(_service.dataHandlers(), handler -> handler.handle(request));
	}

	private AgentSession createSession(AgentExecutionStore store, Person person, A2ATask task,
			ExposedAgentConfig agent, Context context, Map<String, Object> metadata, String text) {
		String peer = task.peer();
		AgentSession session = store.createSession(AgentExecutionStore.SessionKind.BACKGROUND, person,
			"A2A " + peer + " → " + A2AService.displayName(agent) + ": " + firstLine(text));
		HistoryUtils.updateSessionRevision();
		context._metadata = Json.map("contextId", task.contextId(), "agent", agent.getPath(), "caller",
			task.owner(), "peer", peer);

		TLObject remembered = _service.contexts().rememberedSubject(task.contextId()) instanceof TLObject object
			? object : null;
		A2ASubjectResolver.Request request = new A2ASubjectResolver.Request(agent.getPath(), agent.getAgent(),
			task.contextId(), task.owner(), peer, metadata, text, remembered);
		TLObject resolved = A2AService.ask(_service.subjectResolvers(), resolver -> resolver.resolveSubject(request));
		bind(context, session, resolved != null ? resolved : remembered);
		return session;
	}

	private void bind(Context context, AgentSession session, TLObject subject) {
		context._session = session;
		context._subject = subject;
		_service.contexts().bind(ExecutionRecorder.conversationIdOf(session), context._contextId);
	}

	private void finish(A2ATask task, ExposedAgentConfig agent, Context context, TLObject subject,
			AgentRunner.RecordedRun run, CancellationToken token, String request) {
		if (task.state().isTerminal()) {
			return;
		}
		AgentResult result = run.result();
		String answer = run.response() == null ? "" : run.response();
		if (result == null || result.isComplete()) {
			if (!answer.isBlank()) {
				addArtifact(task, A2AJson.artifact(A2AJson.ANSWER, "The agent's answer.", List.of(A2AJson.textPart(answer))));
			}
			contributeArtifacts(task, agent, context, subject, request, answer);
			task.moveTo(TaskState.COMPLETED, A2AJson.textMessage(A2AJson.ROLE_AGENT, task.contextId(), task.id(),
				answer.isBlank() ? "(The agent finished without an answer.)" : answer));
		} else if (token.isCancelled() || result.stopReason() == StopReason.ABORTED) {
			task.cancel("The run was cancelled.");
		} else {
			String error = result.errorMessage();
			fail(task, "The run stopped: " + result.stopReason() + (error == null ? "" : " - " + error)
				+ (answer.isBlank() ? "" : "\n\nWhat the agent said so far:\n" + answer));
		}
	}

	private void contributeArtifacts(A2ATask task, ExposedAgentConfig agent, Context context, TLObject subject,
			String request, String answer) {
		A2AArtifactContributor.Request info = new A2AArtifactContributor.Request(agent.getPath(), agent.getAgent(),
			task.contextId(), task.id(), request, answer, subject);
		A2AArtifactContributor.Sink sink = new A2AArtifactContributor.Sink() {
			@Override
			public void text(String name, String description, String text, String mediaType) {
				Map<String, Object> part = A2AJson.textPart(text);
				if (mediaType != null) {
					part.put("mediaType", mediaType);
				}
				addArtifact(task, A2AJson.artifact(name, description, List.of(part)));
			}

			@Override
			public void data(String name, String description, Object json) {
				addArtifact(task, A2AJson.artifact(name, description, List.of(A2AJson.dataPart(json))));
			}

			@Override
			public void reference(String name, String description, String ref, String mediaType) {
				Map<String, Object> artifact = A2AJson.artifact(name, description, List.of(Json.map("url", ref,
					"mediaType", mediaType == null ? ArtifactRef.MEDIA_TYPE : mediaType, "filename", name)));
				artifact.put("metadata", Json.map("ref", ref));
				task.addArtifact(artifact);
			}
		};
		A2AService.ask(_service.artifactContributors(), contributor -> {
			contributor.contribute(info, sink);
			return null;
		});
	}

	/**
	 * Adds an artifact that can be fetched by reference:
	 * <code>tl-artifact://&lt;app&gt;/task/&lt;taskId&gt;/&lt;name&gt;</code> in its metadata.
	 */
	private void addArtifact(A2ATask task, Map<String, Object> artifact) {
		String name = Json.str(artifact, "name");
		artifact.put("metadata", Json.map("ref", ArtifactRef.ofTask(_service.appName(),
			task.id(), name == null ? Json.str(artifact, "artifactId") : name).toString()));
		task.addArtifact(artifact);
	}

	/**
	 * Tells the agent who it talks to: another agent, which answers its questions as the answers of
	 * {@code askUser} and receives its final answer.
	 *
	 * <p>
	 * It does not name the subject: the agent reads that in the environment block of its run, where
	 * every entry point states it and only if the calling account may read it.
	 * </p>
	 */
	private static String header(Map<String, Object> message, String peer) {
		String agent = Json.str(Json.obj(Json.obj(message, "metadata"), A2AJson.CALLER_METADATA), "agent");
		String from = agent != null
			? "the agent " + agent + " of the application '" + peer + "'"
			: "the remote client '" + peer + "'";
		return "[A2A message from " + from + ". You talk to that agent, not to a person: what you ask with"
			+ " askUser goes to it, and your final answer is returned to it.]\n\n";
	}

	/**
	 * Writes the task's state into its session's metadata, so {@code GetTask} and its artifacts outlive
	 * a restart.
	 */
	@SuppressWarnings("unchecked")
	private void persist(A2ATask task, Context context) {
		if (context._session == null) {
			return;
		}
		String json;
		synchronized (context) {
			Map<String, Object> tasks = (Map<String, Object>) context._metadata.computeIfAbsent("tasks",
				key -> new LinkedHashMap<String, Object>());
			tasks.remove(task.id());
			tasks.put(task.id(), Json.map("owner", task.owner(), "task", task.toJson(null)));
			while (tasks.size() > MAX_PERSISTED_TASKS) {
				tasks.remove(tasks.keySet().iterator().next());
			}
			json = Json.write(Json.map("a2a", context._metadata));
		}
		try {
			AgentExecutionStore.getInstance().saveMetadata(context._session, json);
		} catch (RuntimeException ex) {
			Logger.error("Could not persist A2A task " + task.id() + ": " + ex.getMessage(), ex, AgentRunBackend.class);
		}
	}

	/**
	 * Picks up a context this application served before it restarted: its session, its subject and
	 * the conversation, replayed from the transcripts of its runs.
	 */
	private void restore(AgentExecutionStore store, Person person, Context context) {
		context._restoreChecked = true;
		for (AgentSession session : store.sessionsForOwner(person, AgentExecutionStore.SessionKind.BACKGROUND)) {
			Map<String, Object> a2a = a2aMetadata(session);
			if (a2a == null || !context._contextId.equals(Json.str(a2a, "contextId"))) {
				continue;
			}
			for (AgentExecution execution : store.executionsOf(session)) {
				String transcript = execution.getTranscript();
				// A sub-agent's run has a transcript of its own, which is not this conversation.
				if (execution.getParentExecution() == null && transcript != null && !transcript.isBlank()) {
					try {
						ConversationLogReplay.rebuildMemory(ConversationLogSerializer.fromJson(transcript),
							context._conversation);
					} catch (RuntimeException ex) {
						Logger.warn("Skipped an unreadable transcript of A2A context " + context._contextId + ": "
							+ ex.getMessage(), ex, AgentRunBackend.class);
					}
				}
			}
			context._metadata = a2a;
			bind(context, session, restoredSubject(person, a2a));
			return;
		}
	}

	/**
	 * Keeps the identifier of the checked subject of a context in its {@code a2a} metadata, which
	 * {@link #persist} writes and {@link #restore} reads after a restart.
	 *
	 * <p>
	 * Not as the subject of the session: see the class comment. Only a subject that the calling
	 * account may read is kept, so the metadata never names another one.
	 * </p>
	 */
	private static void rememberForRestart(Context context, TLObject subject) {
		String id = ReadFilter.idOf(subject);
		if (id == null) {
			return;
		}
		synchronized (context) {
			context._metadata.put(SUBJECT_ID, id);
		}
	}

	/**
	 * The subject that a context had before the restart, or <code>null</code> for none.
	 *
	 * <p>
	 * An object that was deleted or that the account may no longer read gives none. Each run checks
	 * the subject again.
	 * </p>
	 */
	private static TLObject restoredSubject(Person person, Map<String, Object> a2a) {
		String id = Json.str(a2a, SUBJECT_ID);
		if (id == null) {
			return null;
		}
		try {
			return TidResolver.resolve(id, new ModelAccessRightsSecurity(person));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * The conversation log of the given context, as the agent reads it, or <code>null</code> for a
	 * context this backend does not hold.
	 *
	 * <p>
	 * Package-visible for a test: the text that a message gives the agent must be checkable without
	 * a model, and a run that fails before its first model call records no transcript.
	 * </p>
	 */
	ConversationLog conversationLog(String contextId) {
		Context context;
		synchronized (_contexts) {
			context = _contexts.get(contextId);
		}
		return context == null ? null : context._log;
	}

	@Override
	public A2ATask findTask(String taskId, String owner) {
		return ThreadContext.<A2ATask, RuntimeException, RuntimeException> inSystemContext(
			AgentRunBackend.class, () -> {
				Person person = Person.byName(owner);
				if (person == null) {
					return null;
				}
				for (AgentSession session : AgentExecutionStore.getInstance().sessionsForOwner(person,
					AgentExecutionStore.SessionKind.BACKGROUND)) {
					Map<String, Object> a2a = a2aMetadata(session);
					Map<String, Object> record = Json.obj(Json.obj(a2a, "tasks"), taskId);
					if (Json.obj(record, "task") != null) {
						return A2ATask.restored(Json.obj(record, "task"), Json.str(a2a, "agent"), Json.str(record, "owner"));
					}
				}
				return null;
			});
	}

	/**
	 * The artifact {@code <taskId>/<name>} of a task, for a reader with the given account.
	 */
	public A2AArtifactSource.Artifact taskArtifact(String id, String account) {
		int slash = id.indexOf('/');
		A2ATask task = slash <= 0 ? null : _service.handler().task(id.substring(0, slash), account);
		if (task == null || !task.owner().equals(account)) {
			return null;
		}
		String name = id.substring(slash + 1);
		Map<String, Object> artifact = task.artifact(name);
		if (artifact == null) {
			return null;
		}
		List<Map<String, Object>> parts = A2AJson.parts(artifact);
		if (parts.size() == 1) {
			Map<String, Object> part = parts.get(0);
			ArtifactRef inner = ArtifactRef.parse(A2AJson.partUrl(part));
			if (inner != null && !ArtifactRef.TASK_KIND.equals(inner.kind())) {
				// A reference a contributor handed out: what it names.
				return _service.resolveArtifact(inner, account);
			}
			if (A2AJson.partText(part) != null) {
				String mediaType = Json.str(part, "mediaType");
				return new A2AArtifactSource.Artifact(name, mediaType == null ? "text/plain" : mediaType,
					A2AJson.partText(part));
			}
			if (A2AJson.partData(part) != null) {
				return new A2AArtifactSource.Artifact(name, A2AJson.JSON_MEDIA_TYPE, A2AJson.partData(part));
			}
		}
		return new A2AArtifactSource.Artifact(name, "text/plain", A2AJson.text(artifact));
	}

	private static Map<String, Object> a2aMetadata(AgentSession session) {
		String metadata = session.getMetadata();
		if (metadata == null || !metadata.contains("\"a2a\"")) {
			return null;
		}
		try {
			return Json.obj(Json.parse(metadata), "a2a");
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static String label(TLObject object) {
		return MetaLabelProvider.INSTANCE.getLabel(object) + " (" + object.tType().getName() + ")";
	}

	private static void fail(A2ATask task, String reason) {
		task.moveTo(TaskState.FAILED, A2AJson.textMessage(A2AJson.ROLE_AGENT, task.contextId(), task.id(), reason));
	}

	private static String firstLine(String text) {
		String line = text.strip().lines().findFirst().orElse("");
		return line.length() <= 80 ? line : line.substring(0, 79) + "…";
	}
}

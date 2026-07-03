/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.AgentTask;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiAgentFactory;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.util.model.ModelService;

/**
 * Service that persists and retrieves the <code>tl.ai.agent</code> execution model:
 * {@link AgentSession}, {@link AgentExecution}, {@link InteractionRequest} and {@link AgentTask}.
 *
 * <p>
 * This is the single write path for agent-run persistence. Any entry point that runs an agent
 * (chat, TL-Script, delegation, background jobs) records its run through this service, so all
 * executions are uniformly queryable regardless of how they were started.
 * </p>
 *
 * <p>
 * All mutating methods run on a dedicated writer thread inside a fresh system interaction with
 * their own {@link Transaction}. Callers therefore need neither a transaction nor a committable
 * KB context — recording works from any caller context, including read-only ones such as
 * TL-Script evaluation during rendering (whose {@code ImmutableDBContext} cannot commit).
 * Queries run on the calling thread and require a bound TopLogic context.
 * </p>
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class AgentExecutionStore extends ConfiguredManagedClass<AgentExecutionStore.Config<?>> {

	/**
	 * How an {@link AgentSession} was started. Mirrors the <code>tl.ai.agent:SessionKind</code>
	 * enumeration.
	 */
	public enum SessionKind {
		/** Interactive chat with a person as owner. */
		CHAT,
		/** Background job without an interactive surface. */
		BACKGROUND,
		/** Run triggered by a scheduler. */
		SCHEDULED,
		/** Run started from a TL-Script function. */
		SCRIPT;
	}

	/**
	 * Lifecycle state of an {@link AgentExecution}. Mirrors the
	 * <code>tl.ai.agent:ExecutionStatus</code> enumeration.
	 */
	public enum ExecutionStatus {
		/** The agent loop is currently running (or crashed without reaching a terminal state). */
		RUNNING,
		/** The run finished normally. */
		COMPLETE,
		/** The run failed. */
		ERROR,
		/** The run was cancelled/aborted. */
		CANCELLED,
		/** The run was rejected by an approval decision. */
		REJECTED;
	}

	/**
	 * Kind of human-in-the-loop request. Mirrors the <code>tl.ai.agent:InteractionType</code>
	 * enumeration.
	 */
	public enum InteractionType {
		/** Tool-call approval. */
		APPROVAL,
		/** Free-text question. */
		QUESTION,
		/** Selection from options. */
		CHOICE;
	}

	/**
	 * Lifecycle state of an {@link InteractionRequest}. Mirrors the
	 * <code>tl.ai.agent:InteractionState</code> enumeration.
	 */
	public enum InteractionState {
		/** Waiting for a response. */
		PENDING,
		/** Answered by the responder. */
		RESOLVED,
		/** No response within the allowed time. */
		TIMED_OUT,
		/** Withdrawn, e.g. because the run was aborted. */
		CANCELLED;
	}

	/**
	 * Configuration interface for {@link AgentExecutionStore}.
	 */
	public interface Config<I extends AgentExecutionStore> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties for v1.
	}

	private ExecutorService _writer;

	private volatile Thread _writerThread;

	/**
	 * Creates an {@link AgentExecutionStore}.
	 */
	public AgentExecutionStore(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_writer = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "agent-execution-store");
			thread.setDaemon(true);
			_writerThread = thread;
			return thread;
		});
	}

	@Override
	protected void shutDown() {
		if (_writer != null) {
			_writer.shutdown();
			try {
				if (!_writer.awaitTermination(10, TimeUnit.SECONDS)) {
					_writer.shutdownNow();
				}
			} catch (InterruptedException ex) {
				_writer.shutdownNow();
				Thread.currentThread().interrupt();
			}
			_writer = null;
			_writerThread = null;
		}
		super.shutDown();
	}

	/**
	 * Runs the given mutation on the dedicated writer thread inside a fresh system interaction.
	 *
	 * <p>
	 * A fresh interaction always carries a committable KB context, so writes succeed regardless
	 * of the caller's context — including read-only ones (e.g. TL-Script evaluation during
	 * rendering) where a transaction begun on the calling thread could never commit. It also
	 * keeps run recording independent of any business transaction the caller may have open:
	 * a rolled-back caller transaction cannot take the recording down with it.
	 * </p>
	 */
	private <T> T write(Supplier<T> mutation) {
		if (Thread.currentThread() == _writerThread) {
			// Re-entrant call from a mutation (e.g. deleteSession -> executionsOf is a read,
			// but guard against future nesting): run directly.
			return mutation.get();
		}
		ExecutorService writer = _writer;
		if (writer == null) {
			throw new IllegalStateException("AgentExecutionStore is not started.");
		}
		try {
			return writer.submit(
				() -> ThreadContextManager.inSystemInteraction(AgentExecutionStore.class, mutation::get))
				.get();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while writing agent execution data.", ex);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException runtime) {
				throw runtime;
			}
			throw new RuntimeException("Failed to write agent execution data: " + cause.getMessage(), cause);
		}
	}

	/**
	 * The singleton service instance.
	 */
	public static AgentExecutionStore getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	// ---- AgentSession ----

	/**
	 * Creates a new {@link AgentSession}.
	 *
	 * @param kind
	 *        How the session was started; must not be <code>null</code>.
	 * @param owner
	 *        The owning person; may be <code>null</code> for system-initiated sessions.
	 * @param title
	 *        Display title; may be <code>null</code>.
	 */
	public AgentSession createSession(SessionKind kind, Person owner, String title) {
		return write(() -> {
		try (Transaction tx = beginTx("Create agent session")) {
			AgentSession session = TlAiAgentFactory.getInstance().createAgentSession();
			session.setKind(kindClassifier(kind));
			if (owner != null) {
				session.setOwner(owner);
			}
			session.setTitle(title);
			Date now = new Date();
			session.setCreatedAt(now);
			session.setLastUpdated(now);
			tx.commit();
			return session;
		}
		});
	}

	/**
	 * Stores the (compacted) conversation memory snapshot of the session.
	 */
	public void saveMemory(AgentSession session, String memoryJson) {
		write(() -> {
			try (Transaction tx = beginTx("Save agent session memory")) {
				session.setMemory(memoryJson);
				session.setLastUpdated(new Date());
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * Stores the session metadata bag (approved tools, approval mode, loaded skills, ...).
	 */
	public void saveMetadata(AgentSession session, String metadataJson) {
		write(() -> {
			try (Transaction tx = beginTx("Save agent session metadata")) {
				session.setMetadata(metadataJson);
				session.setLastUpdated(new Date());
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * Updates the session title.
	 */
	public void updateTitle(AgentSession session, String title) {
		write(() -> {
			try (Transaction tx = beginTx("Update agent session title")) {
				session.setTitle(title);
				session.setLastUpdated(new Date());
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * All sessions of the given kind owned by the given person, newest first.
	 *
	 * @param kind
	 *        Filter by session kind; <code>null</code> for all kinds.
	 */
	public List<AgentSession> sessionsForOwner(Person owner, SessionKind kind) {
		List<AgentSession> result = new ArrayList<>();
		if (owner == null) {
			return result;
		}
		TLClassifier kindFilter = kind != null ? kindClassifier(kind) : null;
		for (TLObject obj : allInstances(TlAiAgentFactory.getAgentSessionType())) {
			AgentSession session = (AgentSession) obj;
			if (!owner.equals(session.getOwner())) {
				continue;
			}
			if (kindFilter != null && !kindFilter.equals(session.getKind())) {
				continue;
			}
			result.add(session);
		}
		result.sort(Comparator.comparing(AgentSession::getLastUpdated,
			Comparator.nullsLast(Comparator.reverseOrder())));
		return result;
	}

	// ---- AgentExecution ----

	/**
	 * Records the start of an agent-loop invocation with status {@link ExecutionStatus#RUNNING}.
	 *
	 * <p>
	 * A row that stays {@link ExecutionStatus#RUNNING} without an end date marks a run that
	 * crashed mid-flight.
	 * </p>
	 *
	 * @param session
	 *        The session this run belongs to; must not be <code>null</code>.
	 * @param runId
	 *        The agent loop's run id; must not be <code>null</code>.
	 * @param agentName
	 *        Name of the agent handling the run; must not be <code>null</code>.
	 * @param model
	 *        The LLM model name; may be <code>null</code>.
	 * @param provider
	 *        The LLM provider id; may be <code>null</code>.
	 * @param parent
	 *        The delegating execution for sub-agent runs; <code>null</code> for top-level runs.
	 */
	public AgentExecution beginExecution(AgentSession session, String runId, String agentName,
			String model, String provider, AgentExecution parent) {
		return write(() -> {
		try (Transaction tx = beginTx("Begin agent execution " + runId)) {
			AgentExecution execution = TlAiAgentFactory.getInstance().createAgentExecution();
			execution.setSession(session);
			execution.setRunId(runId);
			execution.setAgentName(agentName);
			execution.setModel(model);
			execution.setProvider(provider);
			execution.setStatus(statusClassifier(ExecutionStatus.RUNNING));
			execution.setStartedAt(new Date());
			if (parent != null) {
				execution.setParentExecution(parent);
			}
			session.setLastUpdated(new Date());
			tx.commit();
			return execution;
		}
		});
	}

	/**
	 * Records the terminal state of an agent-loop invocation.
	 *
	 * @param status
	 *        Terminal status; must not be <code>null</code>.
	 * @param stopReason
	 *        Why the loop stopped; may be <code>null</code> if unknown.
	 * @param errorMessage
	 *        Failure details for {@link ExecutionStatus#ERROR} runs; may be <code>null</code>.
	 * @param usage
	 *        Total token usage of this run (excluding child executions); may be
	 *        <code>null</code>.
	 * @param transcript
	 *        The serialized per-run conversation-log transcript; may be <code>null</code>.
	 */
	public void finishExecution(AgentExecution execution, ExecutionStatus status,
			StopReason stopReason, String errorMessage, TokenUsage usage, String transcript) {
		write(() -> {
		try (Transaction tx = beginTx("Finish agent execution " + execution.getRunId())) {
			execution.setStatus(statusClassifier(status));
			execution.setStopReason(stopReason != null ? stopReason.name() : null);
			execution.setErrorMessage(errorMessage);
			if (usage != null) {
				execution.setInputTokens((long) usage.inputTokens());
				execution.setOutputTokens((long) usage.outputTokens());
				execution.setThinkingTokens((long) usage.thinkingTokens());
				execution.setCacheReadTokens((long) usage.cacheReadTokens());
				execution.setCacheWriteTokens((long) usage.cacheWriteTokens());
			}
			execution.setTranscript(transcript);
			execution.setEndedAt(new Date());
			AgentSession session = execution.getSession();
			session.setLastUpdated(new Date());
			tx.commit();
		}
		return null;
		});
	}

	/**
	 * Looks up an execution by its run id, or <code>null</code> if none exists.
	 */
	public AgentExecution findExecutionByRunId(String runId) {
		if (runId == null || runId.isEmpty()) {
			return null;
		}
		for (TLObject obj : allInstances(TlAiAgentFactory.getAgentExecutionType())) {
			AgentExecution execution = (AgentExecution) obj;
			if (runId.equals(execution.getRunId())) {
				return execution;
			}
		}
		return null;
	}

	/**
	 * All executions of the given session, oldest first.
	 */
	public List<AgentExecution> executionsOf(AgentSession session) {
		List<AgentExecution> result = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiAgentFactory.getAgentExecutionType())) {
			AgentExecution execution = (AgentExecution) obj;
			if (session.equals(execution.getSession())) {
				result.add(execution);
			}
		}
		result.sort(Comparator.comparing(AgentExecution::getStartedAt,
			Comparator.nullsLast(Comparator.naturalOrder())));
		return result;
	}

	/**
	 * Deletes the given session with everything recorded under it: tasks, the interaction
	 * requests of its executions, the executions themselves, and finally the session.
	 */
	public void deleteSession(AgentSession session) {
		if (session == null) {
			return;
		}
		write(() -> {
		try (Transaction tx = beginTx("Delete agent session")) {
			List<AgentExecution> executions = executionsOf(session);
			for (TLObject obj : allInstances(TlAiAgentFactory.getInteractionRequestType())) {
				InteractionRequest request = (InteractionRequest) obj;
				if (executions.contains(request.getExecution())) {
					request.tDelete();
				}
			}
			for (TLObject obj : allInstances(TlAiAgentFactory.getAgentTaskType())) {
				AgentTask task = (AgentTask) obj;
				if (session.equals(task.getSession())) {
					task.tDelete();
				}
			}
			for (AgentExecution execution : executions) {
				execution.tDelete();
			}
			session.tDelete();
			tx.commit();
		}
		return null;
		});
	}

	/**
	 * Total token usage of the given execution including all (transitive) child executions.
	 *
	 * <p>
	 * This is the "true cost" of a delegating run: the parent's own tokens plus everything its
	 * sub-agents consumed.
	 * </p>
	 */
	public TokenUsage totalUsageWithChildren(AgentExecution root) {
		List<AgentExecution> all = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiAgentFactory.getAgentExecutionType())) {
			all.add((AgentExecution) obj);
		}
		TokenUsage sum = usageOf(root);
		List<AgentExecution> frontier = new ArrayList<>(List.of(root));
		while (!frontier.isEmpty()) {
			List<AgentExecution> next = new ArrayList<>();
			for (AgentExecution execution : all) {
				if (frontier.contains(execution.getParentExecution())) {
					sum = sum.add(usageOf(execution));
					next.add(execution);
				}
			}
			frontier = next;
		}
		return sum;
	}

	// ---- InteractionRequest ----

	/**
	 * Records a pending human-in-the-loop request.
	 *
	 * @param execution
	 *        The execution waiting for the response; must not be <code>null</code>.
	 * @param type
	 *        The interaction kind; must not be <code>null</code>.
	 * @param requestId
	 *        Correlation id used by the surface presenting the request; must not be
	 *        <code>null</code>.
	 * @param header
	 *        Short label (e.g. "Approval"); may be <code>null</code>.
	 * @param prompt
	 *        The question or approval text presented to the responder; may be <code>null</code>.
	 * @param payload
	 *        Structured request data, e.g. the tool name and the actual argument JSON of an
	 *        approval; may be <code>null</code>.
	 * @param responder
	 *        The person expected to respond; may be <code>null</code> if any user may respond.
	 */
	public InteractionRequest createInteraction(AgentExecution execution, InteractionType type,
			String requestId, String header, String prompt, String payload, Person responder) {
		return write(() -> {
		try (Transaction tx = beginTx("Create interaction request " + requestId)) {
			InteractionRequest request = TlAiAgentFactory.getInstance().createInteractionRequest();
			request.setExecution(execution);
			request.setType(interactionTypeClassifier(type));
			request.setState(interactionStateClassifier(InteractionState.PENDING));
			request.setRequestId(requestId);
			request.setHeader(header);
			request.setPrompt(prompt);
			request.setPayload(payload);
			if (responder != null) {
				request.setResponder(responder);
			}
			request.setCreatedAt(new Date());
			tx.commit();
			return request;
		}
		});
	}

	/**
	 * Records the outcome of a human-in-the-loop request.
	 *
	 * @param state
	 *        Terminal state; must not be {@link InteractionState#PENDING}.
	 * @param resolution
	 *        The response data (decision, answer text, selected options); may be
	 *        <code>null</code>.
	 */
	public void resolveInteraction(InteractionRequest request, InteractionState state, String resolution) {
		if (state == InteractionState.PENDING) {
			throw new IllegalArgumentException("Resolution state must be terminal, not PENDING.");
		}
		write(() -> {
			try (Transaction tx = beginTx("Resolve interaction request " + request.getRequestId())) {
				request.setState(interactionStateClassifier(state));
				request.setResolution(resolution);
				request.setResolvedAt(new Date());
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * All pending interaction requests addressed to the given person, oldest first.
	 *
	 * <p>
	 * Includes requests without a dedicated responder (any user may respond to those).
	 * </p>
	 */
	public List<InteractionRequest> pendingInteractionsFor(Person responder) {
		TLClassifier pending = interactionStateClassifier(InteractionState.PENDING);
		List<InteractionRequest> result = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiAgentFactory.getInteractionRequestType())) {
			InteractionRequest request = (InteractionRequest) obj;
			if (!pending.equals(request.getState())) {
				continue;
			}
			Person requestResponder = request.getResponder();
			if (requestResponder == null || requestResponder.equals(responder)) {
				result.add(request);
			}
		}
		result.sort(Comparator.comparing(InteractionRequest::getCreatedAt,
			Comparator.nullsLast(Comparator.naturalOrder())));
		return result;
	}

	// ---- AgentTask ----

	/**
	 * Creates a new task in the session's todo plan.
	 */
	public AgentTask createTask(AgentSession session, String taskId, String subject, String content,
			String status, int ordinal, AgentExecution touchedBy) {
		return write(() -> {
		try (Transaction tx = beginTx("Create agent task " + taskId)) {
			AgentTask task = TlAiAgentFactory.getInstance().createAgentTask();
			task.setSession(session);
			task.setTaskId(taskId);
			task.setSubject(subject);
			task.setContent(content);
			task.setStatus(status);
			task.setOrdinal(ordinal);
			if (touchedBy != null) {
				task.setLastTouchedByExecution(touchedBy);
			}
			tx.commit();
			return task;
		}
		});
	}

	/**
	 * Updates a task's mutable fields; <code>null</code> arguments leave the field unchanged.
	 */
	public void updateTask(AgentTask task, String subject, String content, String status,
			Integer ordinal, AgentExecution touchedBy) {
		write(() -> {
		try (Transaction tx = beginTx("Update agent task " + task.getTaskId())) {
			if (subject != null) {
				task.setSubject(subject);
			}
			if (content != null) {
				task.setContent(content);
			}
			if (status != null) {
				task.setStatus(status);
			}
			if (ordinal != null) {
				task.setOrdinal(ordinal);
			}
			if (touchedBy != null) {
				task.setLastTouchedByExecution(touchedBy);
			}
			tx.commit();
		}
		return null;
		});
	}

	/**
	 * Deletes the given task.
	 */
	public void deleteTask(AgentTask task) {
		if (task == null) {
			return;
		}
		write(() -> {
			try (Transaction tx = beginTx("Delete agent task " + task.getTaskId())) {
				task.tDelete();
				tx.commit();
			}
			return null;
		});
	}

	/**
	 * All tasks of the given session, in plan order.
	 */
	public List<AgentTask> tasksOf(AgentSession session) {
		List<AgentTask> result = new ArrayList<>();
		for (TLObject obj : allInstances(TlAiAgentFactory.getAgentTaskType())) {
			AgentTask task = (AgentTask) obj;
			if (session.equals(task.getSession())) {
				result.add(task);
			}
		}
		result.sort(Comparator.comparing(AgentTask::getOrdinal,
			Comparator.nullsLast(Comparator.naturalOrder())));
		return result;
	}

	// ---- Helpers ----

	private static Transaction beginTx(String message) {
		return PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text(message));
	}

	private static List<TLObject> allInstances(com.top_logic.model.TLClass type) {
		return MetaElementUtil.getAllInstancesOf(type, TLObject.class);
	}

	private static TokenUsage usageOf(AgentExecution execution) {
		return new TokenUsage(
			intValue(execution.getInputTokens()),
			intValue(execution.getOutputTokens()),
			intValue(execution.getThinkingTokens()),
			intValue(execution.getCacheReadTokens()),
			intValue(execution.getCacheWriteTokens()));
	}

	private static int intValue(Long value) {
		return value != null ? value.intValue() : 0;
	}

	private static TLClassifier kindClassifier(SessionKind kind) {
		switch (kind) {
			case CHAT:
				return TlAiAgentFactory.getCHATSessionKindClassifier();
			case BACKGROUND:
				return TlAiAgentFactory.getBACKGROUNDSessionKindClassifier();
			case SCHEDULED:
				return TlAiAgentFactory.getSCHEDULEDSessionKindClassifier();
			case SCRIPT:
				return TlAiAgentFactory.getSCRIPTSessionKindClassifier();
		}
		throw new IllegalArgumentException("Unknown session kind: " + kind);
	}

	private static TLClassifier statusClassifier(ExecutionStatus status) {
		switch (status) {
			case RUNNING:
				return TlAiAgentFactory.getRUNNINGExecutionStatusClassifier();
			case COMPLETE:
				return TlAiAgentFactory.getCOMPLETEExecutionStatusClassifier();
			case ERROR:
				return TlAiAgentFactory.getERRORExecutionStatusClassifier();
			case CANCELLED:
				return TlAiAgentFactory.getCANCELLEDExecutionStatusClassifier();
			case REJECTED:
				return TlAiAgentFactory.getREJECTEDExecutionStatusClassifier();
		}
		throw new IllegalArgumentException("Unknown execution status: " + status);
	}

	private static TLClassifier interactionTypeClassifier(InteractionType type) {
		switch (type) {
			case APPROVAL:
				return TlAiAgentFactory.getAPPROVALInteractionTypeClassifier();
			case QUESTION:
				return TlAiAgentFactory.getQUESTIONInteractionTypeClassifier();
			case CHOICE:
				return TlAiAgentFactory.getCHOICEInteractionTypeClassifier();
		}
		throw new IllegalArgumentException("Unknown interaction type: " + type);
	}

	private static TLClassifier interactionStateClassifier(InteractionState state) {
		switch (state) {
			case PENDING:
				return TlAiAgentFactory.getPENDINGInteractionStateClassifier();
			case RESOLVED:
				return TlAiAgentFactory.getRESOLVEDInteractionStateClassifier();
			case TIMED_OUT:
				return TlAiAgentFactory.getTIMEDOUTInteractionStateClassifier();
			case CANCELLED:
				return TlAiAgentFactory.getCANCELLEDInteractionStateClassifier();
		}
		throw new IllegalArgumentException("Unknown interaction state: " + state);
	}

	/**
	 * Module for {@link AgentExecutionStore}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentExecutionStore> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<AgentExecutionStore> getImplementation() {
			return AgentExecutionStore.class;
		}
	}
}

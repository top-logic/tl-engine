/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.ContextBook;
import com.top_logic.ai.a2a.client.RemoteAgents.RemoteAgent;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.basic.Logger;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;
import com.top_logic.util.TLContext;

/**
 * The tools that let a local agent talk to remote A2A agents (tool factory {@code a2a}).
 *
 * <p>
 * Asking and replying send data out of the application, so they are {@link ToolMode#EXTERNAL} (a chat
 * user approves them) in the {@link ToolGroups#EGRESS} group. What a remote agent wrote comes back
 * inside {@link UntrustedEnvelope#A2A_RESULT}.
 * </p>
 */
public class A2AClientTools {

	/** How much of an artifact one fetch returns. */
	static final int MAX_FETCH_CHARS = 30_000;

	/**
	 * Lists the remote agents.
	 */
	@ToolMethod(description = "List the remote agents (other applications' agents, reached over the A2A protocol) "
		+ "you can ask with askAgent: their names, what they do and their skills.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS, marking = AnswerMarking.TOOL)
	public String listAgents() {
		return call(service -> {
			StringBuilder known = new StringBuilder();
			for (RemoteAgent agent : service.remoteAgents().all()) {
				known.append("- name: ").append(agent.name());
				try {
					Map<String, Object> card = service.remoteAgents().card(agent);
					known.append(" (").append(Json.str(card, "name")).append(")\n  description: ")
						.append(Json.str(card, "description"));
					for (Object skill : Json.list(card, "skills")) {
						known.append("\n  skill: ").append(Json.str(skill, "name")).append(" - ")
							.append(Json.str(skill, "description"));
					}
				} catch (IOException | A2AError ex) {
					known.append("\n  (unreachable right now: ").append(ex.getMessage()).append(")");
				}
				known.append('\n');
			}
			return known.length() == 0 ? "No remote agents are configured."
				: "Remote agents (call them with askAgent(agent=<name>, message=...)):\n"
					+ UntrustedEnvelope.A2A_RESULT.wrap("a2a:agent-cards", known.toString());
		});
	}

	/**
	 * Asks a remote agent.
	 */
	@ToolMethod(description = "Ask a remote agent (another application's agent, over the A2A protocol) and wait for "
		+ "its answer. The remote agent works in its own application and may take minutes; you see its progress "
		+ "meanwhile. Results: state COMPLETED with the answer (shortened if long) and REFERENCES to its "
		+ "artifacts - read an artifact, or one section of it, with fetchArtifact; INPUT_REQUIRED when it asks "
		+ "you back (answer with replyAgent); WORKING if it did not finish within the wait (wait with "
		+ "getAgentTask). Write the message so it stands on its own: the remote agent sees nothing of your "
		+ "conversation but what earlier messages in the same remote conversation told it. Hand over long "
		+ "material by reference (refs) instead of pasting it. Never send a second message while a task of "
		+ "that agent is still WORKING: wait for it with getAgentTask.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.EGRESS, marking = AnswerMarking.TOOL, concurrencySafe = true)
	public String askAgent(
			@P("Name of the remote agent, as listAgents shows it.") String agent,
			@P("The message: the question or the assignment, self-contained.") String message,
			@P(value = "Which remote conversation: 'continue' (default: the one you last used with this agent), "
				+ "'new' for a fresh one (give it a topic), or the topic of an open one as "
				+ "listAgentConversations shows it.", required = false) String conversation,
			@P(value = "A short topic for a new conversation.", required = false) String topic,
			@P(value = "References to hand over instead of their content: tl-artifact://... references, "
				+ "separated by commas or as a JSON array. The remote agent reads them with fetchArtifact.",
				required = false) String refs,
			@P(value = "Optional structured data to send along, as a JSON value.", required = false) String data,
			ToolExecutionContext context) {
		return call(service -> RemoteExchange.format(exchange(service).ask(caller(service, context), agent, message,
			conversation, topic, parseData(data), parseRefs(refs), maxWaitMillis(service), progress(context),
			() -> context.cancellationToken().isCancelled()), UntrustedEnvelope.A2A_RESULT::wrap));
	}

	/**
	 * Answers a remote agent's question.
	 */
	@ToolMethod(description = "Answer a question a remote agent asked you (its task is INPUT_REQUIRED), then wait "
		+ "for it again, like askAgent.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.EGRESS, marking = AnswerMarking.TOOL, concurrencySafe = true)
	public String replyAgent(
			@P("The taskId the question came with.") String taskId,
			@P("Your answer.") String message,
			@P(value = "Name of the remote agent; only needed if the task is not known from this run.",
				required = false) String agent,
			ToolExecutionContext context) {
		return call(service -> RemoteExchange.format(exchange(service).reply(caller(service, context), agent, taskId,
			message, maxWaitMillis(service), progress(context), () -> context.cancellationToken().isCancelled()),
			UntrustedEnvelope.A2A_RESULT::wrap));
	}

	/**
	 * Waits for a remote task.
	 */
	@ToolMethod(description = "Wait for a task you started at a remote agent that is still WORKING (askAgent or "
		+ "replyAgent stopped waiting), and get its answer, its question back or its artifacts. Waits until the "
		+ "task ends or asks back, up to the configured limit; waitSeconds=0 only reads the current state. Call "
		+ "it again while the task is still WORKING - do not end your turn and do not send the agent a new "
		+ "message meanwhile.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS, marking = AnswerMarking.TOOL)
	public String getAgentTask(
			@P("The taskId.") String taskId,
			@P(value = "Name of the remote agent; only needed if the task is not known from this application.",
				required = false) String agent,
			@P(value = "0: do not wait, just read the state. Anything else: wait up to the configured limit.",
				required = false) Integer waitSeconds,
			ToolExecutionContext context) {
		return call(service -> RemoteExchange.format(exchange(service).follow(caller(service, context), agent, taskId,
			waitSeconds != null && waitSeconds == 0 ? 0 : maxWaitMillis(service), progress(context),
			() -> context.cancellationToken().isCancelled()), UntrustedEnvelope.A2A_RESULT::wrap));
	}

	/**
	 * The open remote conversations of this conversation.
	 */
	@ToolMethod(description = "List the conversations this conversation has open with remote agents: topic, agent, "
		+ "context id, last task and its state. Continue one with askAgent(conversation=<topic>).",
		mode = ToolMode.READ, group = ToolGroups.EGRESS, marking = AnswerMarking.NONE)
	public String listAgentConversations(ToolExecutionContext context) {
		return call(service -> {
			List<ContextBook.RemoteConversation> conversations =
				service.contexts().conversations(caller(service, context).conversationId());
			if (conversations.isEmpty()) {
				return "No remote conversations yet. askAgent opens one.";
			}
			StringBuilder out = new StringBuilder("Remote conversations, most recent first:");
			for (ContextBook.RemoteConversation conversation : conversations) {
				out.append("\n- \"").append(conversation.label()).append("\" with ").append(conversation.agent())
					.append(": contextId ").append(conversation.contextId())
					.append(", last task ").append(conversation.lastTaskId())
					.append(" (").append(conversation.lastState() == null ? "?" : conversation.lastState().name())
					.append(')');
			}
			return out.toString();
		});
	}

	/**
	 * Reads an artifact by reference.
	 */
	@ToolMethod(description = "Read an artifact by its reference (tl-artifact://<app>/<kind>/<id>), or only one "
		+ "section of it - a heading of a text, a member of a JSON object. Without a section you get the whole "
		+ "artifact (shortened if very long) and the list of its sections; read long artifacts section by "
		+ "section.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS, marking = AnswerMarking.TOOL)
	public String fetchArtifact(
			@P("The reference, tl-artifact://...") String ref,
			@P(value = "A section: a heading of a Markdown text, a member (or path a/b) of a JSON object.",
				required = false) String section) {
		return call(service -> {
			ArtifactRef parsed = ArtifactRef.parse(ref);
			if (parsed == null) {
				return "error: Not a reference: " + ref + ". A reference reads tl-artifact://<app>/<kind>/<id>.";
			}
			Map<String, Object> answer = parsed.app().equals(service.appName())
				? A2AService.artifactResponse(service.resolveArtifact(parsed, TLContext.currentUser().getName()),
					parsed, section)
				: service.remoteAgents().fetchArtifact(parsed, section);
			return formatArtifact(answer, parsed);
		});
	}

	static String formatArtifact(Map<String, Object> answer, ArtifactRef ref) {
		List<?> sections = Json.list(answer, "sections");
		String error = Json.str(answer, "error");
		if (error != null) {
			return "error: " + error + (sections.isEmpty() ? "" : " Sections: " + sections + ".");
		}
		Object content = answer.get("content");
		String text = content instanceof String str ? str : Json.write(content);
		StringBuilder out = new StringBuilder("ref: ").append(ref)
			.append("\nname: ").append(Json.str(answer, "name"))
			.append("\nmediaType: ").append(Json.str(answer, "mediaType"));
		for (String key : List.of("version", "section")) {
			if (Json.str(answer, key) != null) {
				out.append('\n').append(key).append(": ").append(Json.str(answer, key));
			}
		}
		out.append("\nsize: ").append(text.length()).append(" characters");
		if (!sections.isEmpty()) {
			out.append("\nsections: ").append(sections);
		}
		String body = text.length() <= MAX_FETCH_CHARS ? text : text.substring(0, MAX_FETCH_CHARS)
			+ "\n[... cut after " + MAX_FETCH_CHARS + " of " + text.length() + " characters - fetch a section instead]";
		return out + "\n\n" + UntrustedEnvelope.A2A_RESULT.wrap("artifact:" + ref, body);
	}

	static List<String> parseRefs(String refs) {
		List<String> result = new ArrayList<>();
		if (refs == null || refs.isBlank()) {
			return result;
		}
		List<?> candidates;
		try {
			candidates = Json.parse(refs) instanceof List<?> list ? list : List.of(refs.split("[,\\s]+"));
		} catch (IllegalArgumentException ex) {
			candidates = List.of(refs.split("[,\\s]+"));
		}
		for (Object candidate : candidates) {
			if (ArtifactRef.isRef(String.valueOf(candidate))) {
				result.add(String.valueOf(candidate).trim());
			}
		}
		return result;
	}

	private static Object parseData(String data) {
		if (data == null || data.isBlank()) {
			return null;
		}
		try {
			return Json.parse(data);
		} catch (IllegalArgumentException ex) {
			return data;
		}
	}

	/**
	 * A tool call against the running service, its failures as an error answer for the model.
	 */
	private interface Call {
		String run(A2AService service) throws IOException, InterruptedException;
	}

	private static String call(Call call) {
		A2AService service = A2AService.getInstance();
		if (service == null) {
			return "error: The A2A service is not running in this application.";
		}
		try {
			return call.run(service);
		} catch (A2AError ex) {
			return "error: " + ex.getMessage();
		} catch (IOException ex) {
			Logger.info("A2A call failed: " + ex.getMessage(), A2AClientTools.class);
			return "error: The remote application cannot be reached: " + ex.getMessage();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return "error: interrupted.";
		}
	}

	private static RemoteExchange exchange(A2AService service) {
		return new RemoteExchange(service.remoteAgents(), service.contexts());
	}

	/**
	 * How long a call waits for a remote task: always the configured limit. A model allowed a shorter
	 * wait stops waiting, polls once and ends its turn before the remote agent has answered.
	 */
	private static long maxWaitMillis(A2AService service) {
		return service.getConfig().getMaxBlockingSeconds() * 1000L;
	}

	private static Consumer<String> progress(ToolExecutionContext context) {
		return line -> context.progressReporter().progress(line.length() > 300 ? line.substring(0, 299) + "…" : line);
	}

	/**
	 * Who calls: this application, the local agent, its conversation and that conversation's subject.
	 */
	private static RemoteExchange.Caller caller(A2AService service, ToolExecutionContext context) {
		RunInfo info = context.runInfo();
		String conversationId = info == null ? null : info.conversationId();
		TLObject subject = null;
		if (context instanceof ToolContext toolContext) {
			if (conversationId == null) {
				conversationId = toolContext.executionHandle().conversationId();
			}
			subject = subjectOf(toolContext);
		}
		return new RemoteExchange.Caller(service.appName(), service.localDisplayName(info == null ? null : info.agentName()),
			conversationId, subject, subject == null ? null : MetaLabelProvider.INSTANCE.getLabel(subject));
	}

	private static TLObject subjectOf(ToolContext context) {
		try {
			AgentExecution execution = context.executionHandle().get();
			AgentSession session = execution == null ? null : AgentExecutionStore.getInstance().sessionOf(execution);
			return session == null ? null : session.getSubject();
		} catch (RuntimeException ex) {
			Logger.info("Cannot read the subject of the calling conversation: " + ex.getMessage(), A2AClientTools.class);
			return null;
		}
	}
}

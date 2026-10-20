/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.function.Predicate;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolArgumentCoercionException;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.json.JsonTree;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Central catalog of {@link Tool}s.
 *
 * <p>Holds tools by name, produces {@link ToolSchema} lists for the LLM, and
 * implements {@link ToolExecutor} for direct integration with the agent loop.</p>
 *
 * <p>This class is a pure lookup and execution layer. Security checks (coarse
 * permission gating via {@link com.top_logic.tl.ai.core.agent.SecurityContext})
 * are handled by the agent loop before dispatching to this executor.</p>
 *
 * @see Tool
 * @see AnnotatedTools
 */
public class ToolCatalog implements ToolRegistry {

	private final Map<String, Tool> _tools = new LinkedHashMap<>();

	private boolean _frozen;

	/**
	 * Register a single tool.
	 *
	 * <p>Validates the tool's JSON Schema at registration time so that malformed
	 * schemas fail fast rather than producing opaque provider errors at runtime.</p>
	 *
	 * @throws IllegalArgumentException if a tool with the same name is already registered
	 * @throws IllegalArgumentException if the tool's input schema is not valid JSON
	 * @throws IllegalStateException    if this registry has been {@linkplain #freeze() frozen}
	 */
	public void register(Tool tool) {
		checkNotFrozen();
		String name = tool.name();
		if (_tools.containsKey(name)) {
			Tool existing = _tools.get(name);
			throw new IllegalArgumentException(
				"Tool name collision: '" + name + "' is already registered"
				+ " (existing: " + existing.getClass().getSimpleName()
				+ ", new: " + tool.getClass().getSimpleName() + ")."
				+ " Use distinct tool names or check agent tool assignments.");
		}
		validateSchema(tool);
		_tools.put(name, tool);
	}

	private static void validateSchema(Tool tool) {
		String json = tool.schema().inputSchemaJson();
		try {
			parseJson(json);
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Tool '" + tool.name() + "' has invalid input schema JSON: " + e.getMessage(), e);
		}
	}

	/**
	 * Scan the given object for {@link ToolMethod}-annotated methods and register each
	 * as a {@link Tool}.
	 *
	 * @throws IllegalArgumentException if any tool name collides with an existing registration
	 * @throws IllegalStateException    if this registry has been {@linkplain #freeze() frozen}
	 */
	public void register(Object annotatedProvider) {
		checkNotFrozen();
		for (Tool tool : AnnotatedTools.from(annotatedProvider)) {
			register(tool);
		}
	}

	/**
	 * Freeze this catalog, preventing further registrations.
	 *
	 * <p>After freezing, all read operations are safe for concurrent access from
	 * multiple threads. Any attempt to {@link #register} after freezing throws
	 * {@link IllegalStateException}.</p>
	 *
	 * @return this catalog (for chaining)
	 */
	public ToolCatalog freeze() {
		_frozen = true;
		return this;
	}

	/**
	 * Whether this catalog has been {@linkplain #freeze() frozen}.
	 */
	public boolean isFrozen() {
		return _frozen;
	}

	private void checkNotFrozen() {
		if (_frozen) {
			throw new IllegalStateException("Catalog is frozen — no further registrations allowed.");
		}
	}

	/** Looks up a tool by name, or {@code null} if not registered. */
	@Override
	public Tool getTool(String name) {
		return _tools.get(name);
	}

	/** All registered tools. */
	public List<Tool> tools() {
		return List.copyOf(_tools.values());
	}

	/** Registered tools matching the given predicate. */
	public List<Tool> tools(Predicate<Tool> filter) {
		return _tools.values().stream().filter(filter).toList();
	}

	/** Schemas of all registered tools. */
	public List<ToolSchema> toolSchemas() {
		return _tools.values().stream().map(Tool::schema).toList();
	}

	/** Schemas of registered tools matching the given predicate. */
	@Override
	public List<ToolSchema> toolSchemas(Predicate<Tool> filter) {
		return _tools.values().stream().filter(filter).map(Tool::schema).toList();
	}

	@Override
	public ToolResultMessage execute(ToolCall toolCall, ToolContext context) {
		Instant start = Instant.now();

		Tool tool = _tools.get(toolCall.name());
		if (tool == null) {
			return failedResult(toolCall, start, unknownToolMessage(toolCall, this, context));
		}

		return executeTool(tool, toolCall, context);
	}

	/**
	 * The message for a tool call the registry cannot resolve.
	 *
	 * <p>
	 * "No such tool" and "you may not use that tool" are different answers and the model acts on
	 * them differently: the first is corrected by picking a name from the list, the second is final.
	 * The list therefore holds exactly the tools this run's principal may call - the same set the
	 * model was sent schemas for - so that following the hint cannot walk into a denial, and so that
	 * a typo does not disclose the names of capabilities the user does not have.
	 * </p>
	 *
	 * <p>
	 * Package-private: shared between {@link ToolCatalog} and {@link DynamicToolCatalog}.
	 * </p>
	 */
	static String unknownToolMessage(ToolCall toolCall, ToolRegistry registry, ToolContext context) {
		Predicate<Tool> visible = context == null
			? tool -> true
			: ToolPermissionFilter.offeredIn(context);
		List<String> available = registry.toolSchemas(visible).stream().map(ToolSchema::name).toList();
		return absenceReason(toolCall.name(), registry, context) + " Available: " + available;
	}

	/**
	 * Which of the ways a tool can be absent this is, in one sentence.
	 *
	 * <p>
	 * They all look alike from here - {@link ToolRegistry#getTool(String)} returned nothing - and the
	 * model has been observed explaining the wrong one to the user: asked for a script tool that no
	 * framework agent lists, it reported that the agent "lacks the {@code agent.authoring} capability",
	 * which was neither true nor actionable. They are told apart because the answer to each is
	 * different: pick another name, load a skill, ask an administrator, or accept a refusal.
	 * </p>
	 *
	 * <p>
	 * A capability is named only where the application really has the tool and the acting user really
	 * is denied it. A name that resolves nowhere never goes through the permission check, so a typo
	 * still cannot make the answer disclose which capabilities the user lacks.
	 * </p>
	 *
	 * <p>
	 * See {@code doc/security/agent-security-architecture.md} section 3.2.7.
	 * </p>
	 */
	private static String absenceReason(String requested, ToolRegistry registry, ToolContext context) {
		Tool elsewhere = registeredElsewhere(requested);
		if (elsewhere != null && context != null) {
			String denial = context.security().toolDenialReason(elsewhere);
			if (denial != null) {
				return denial;
			}
		}
		if (registry.isReachableBySkill(requested)) {
			return "Tool '" + requested + "' belongs to a skill of yours that is not loaded yet: load"
				+ " that skill, then call it again.";
		}
		if (elsewhere != null) {
			return "Tool '" + requested + "' exists in this application but is not carried by this"
				+ " agent - it is in neither this agent's own tool list nor any skill this agent can"
				+ " load. This is not a permission problem and nothing you do here can reach it: an"
				+ " administrator would have to add it to the agent or to one of its skills.";
		}
		return "Unknown tool: '" + requested + "'.";
	}

	/**
	 * The tool of that name the application has, whether or not this agent carries it.
	 *
	 * <p>
	 * Asked of the service-level registry by the qualified name and by the bare name a model would
	 * use. Absent registry - a unit test, a catalog built by hand - reads as "no such tool", which
	 * leaves the plain unknown-tool answer.
	 * </p>
	 */
	private static Tool registeredElsewhere(String requested) {
		if (!ToolRegistryService.Module.INSTANCE.isActive()) {
			return null;
		}
		ToolRegistryService registry = ToolRegistryService.getInstance();
		Tool tool = registry.getTool(requested);
		if (tool != null) {
			return tool;
		}
		return registry.getTools().stream()
			.filter(candidate -> requested.equals(candidate.name()))
			.findFirst()
			.orElse(null);
	}

	/**
	 * Execute a resolved tool with JSON argument parsing and error wrapping.
	 *
	 * <p>Package-private: shared between {@link ToolCatalog} and {@link DynamicToolCatalog}.</p>
	 */
	static ToolResultMessage executeTool(Tool tool, ToolCall toolCall, ToolContext context) {
		Instant start = Instant.now();

		Map<String, Object> args;
		try {
			args = parseJson(toolCall.arguments());
		} catch (Exception e) {
			// Never reached the tool body — synthetic, must not trigger a rollback.
			return failedResult(toolCall, start,
				"Invalid JSON arguments for tool '" + toolCall.name() + "': " + sanitizeError(e), true);
		}

		try {
			ToolResult toolResult = ToolExecutions.execute(tool, args, context);
			if (toolResult.failed()) {
				// The tool ran and said itself that the call failed. That reaches the model as a failed
				// tool call - the flag every provider carries beside the text - but it is synthetic,
				// because a tool reporting a failure instead of throwing is precisely a tool saying it
				// has nothing for the turn's transaction to take back.
				return failedResult(toolCall, start, toolResult.text(), true);
			}
			Instant end = Instant.now();
			return new ToolResultMessage(
				UUID.randomUUID().toString(), Instant.now(),
				toolCall.id(), toolCall.name(),
				new ToolState.Completed(toolResult.text(), start, end, false,
					toolResult.summary(), toolResult.truncationHint(),
					ToolContents.toContentParts(toolResult.content())));
		} catch (ToolArgumentCoercionException e) {
			// Thrown while coercing arguments, strictly before the tool method is invoked —
			// synthetic, must not trigger a rollback.
			return failedResult(toolCall, start, sanitizeError(e), true);
		} catch (IllegalArgumentException e) {
			// May have been thrown by the tool's own body after producing side effects —
			// a genuine failure, must still trigger a rollback.
			return failedResult(toolCall, start, sanitizeError(e));
		} catch (CancellationException e) {
			return failedResult(toolCall, start, "Tool execution cancelled.");
		} catch (Exception e) {
			return failedResult(toolCall, start,
				"Tool '" + toolCall.name() + "' failed: " + sanitizeError(e));
		}
	}

	static String sanitizeError(Exception e) {
		String message = e.getMessage();
		if (message == null || message.isEmpty()) {
			return e.getClass().getSimpleName();
		}
		return message;
	}

	static ToolResultMessage failedResult(ToolCall toolCall, Instant start, String error) {
		return failedResult(toolCall, start, error, false);
	}

	static ToolResultMessage failedResult(ToolCall toolCall, Instant start, String error, boolean synthetic) {
		Instant end = Instant.now();
		return new ToolResultMessage(
			UUID.randomUUID().toString(), Instant.now(),
			toolCall.id(), toolCall.name(),
			new ToolState.Failed(error, start, end, synthetic));
	}

	/**
	 * Parse a JSON object string into a {@code Map<String, Object>}.
	 */
	public static Map<String, Object> parseJson(String json) throws IOException {
		if (json == null || json.isBlank() || "{}".equals(json.trim())) {
			return Map.of();
		}
		return JsonTree.parseObject(json);
	}

	/**
	 * What kind of state change the given call makes: the tool's {@link Tool#mode()}, or
	 * {@link ToolMode#READ} for a call the tool says changes nothing - a dry run.
	 *
	 * <p>
	 * The one answer the approval policy, the turn's transaction and the conversation log go by, so
	 * that they cannot disagree about a call. A tool may only narrow its mode to {@code READ}; any
	 * other answer is read as its declared mode. Arguments that do not parse leave the declared mode
	 * as well - fail-closed, and the call then fails on them when it runs.
	 * </p>
	 *
	 * @see Tool#mode(Map)
	 */
	public static ToolMode callMode(Tool tool, ToolCall call) {
		Map<String, Object> arguments;
		try {
			arguments = parseJson(call.arguments());
		} catch (IOException | RuntimeException ex) {
			return tool.mode();
		}
		return callMode(tool, arguments);
	}

	/**
	 * {@link #callMode(Tool, ToolCall)} for arguments that are already parsed.
	 */
	public static ToolMode callMode(Tool tool, Map<String, Object> arguments) {
		ToolMode declared = tool.mode();
		try {
			return tool.mode(arguments) == ToolMode.READ ? ToolMode.READ : declared;
		} catch (RuntimeException ex) {
			return declared;
		}
	}
}

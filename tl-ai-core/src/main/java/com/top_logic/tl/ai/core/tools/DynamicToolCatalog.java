/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Layered tool catalog that grows at runtime.
 *
 * <p>Wraps a frozen base {@link ToolCatalog} plus overlay catalogs appended
 * when skills are loaded. Each overlay is itself a frozen {@link ToolCatalog}.
 * Thread-safe for concurrent reads during overlay additions.</p>
 *
 * @see ToolCatalog
 */
public class DynamicToolCatalog implements ToolRegistry {

	private final ToolCatalog _base;

	private final CopyOnWriteArrayList<ToolCatalog> _overlays = new CopyOnWriteArrayList<>();

	private Set<String> _reachableBySkill = Set.of();

	/**
	 * Creates a dynamic catalog backed by the given frozen base catalog.
	 */
	public DynamicToolCatalog(ToolCatalog base) {
		if (!base.isFrozen()) {
			throw new IllegalArgumentException("Base catalog must be frozen.");
		}
		_base = base;
	}

	/**
	 * Declares which tools an unloaded skill of this agent would bring.
	 *
	 * <p>
	 * Not used for lookup - a skill's tools only become callable when it is loaded. It is used to
	 * tell the model <em>why</em> a tool is absent: a name in this set means "load the skill", a name
	 * outside it means this agent does not carry the tool at all.
	 * </p>
	 *
	 * @param names
	 *        Every tool name declared by any skill this agent offers.
	 */
	public void setReachableBySkill(Set<String> names) {
		_reachableBySkill = Set.copyOf(names);
	}

	@Override
	public boolean isReachableBySkill(String name) {
		return _reachableBySkill.contains(name);
	}

	/**
	 * Appends a frozen overlay catalog (typically the tools from one loaded skill).
	 */
	public void addOverlay(ToolCatalog overlay) {
		if (!overlay.isFrozen()) {
			throw new IllegalArgumentException("Overlay catalog must be frozen.");
		}
		_overlays.add(overlay);
	}

	@Override
	public Tool getTool(String name) {
		Tool tool = _base.getTool(name);
		if (tool != null) {
			return tool;
		}
		for (ToolCatalog overlay : _overlays) {
			tool = overlay.getTool(name);
			if (tool != null) {
				return tool;
			}
		}
		return null;
	}

	@Override
	public List<ToolSchema> toolSchemas(Predicate<Tool> filter) {
		Map<String, ToolSchema> merged = new LinkedHashMap<>();
		for (ToolSchema schema : _base.toolSchemas(filter)) {
			merged.putIfAbsent(schema.name(), schema);
		}
		for (ToolCatalog overlay : _overlays) {
			for (ToolSchema schema : overlay.toolSchemas(filter)) {
				merged.putIfAbsent(schema.name(), schema);
			}
		}
		return List.copyOf(merged.values());
	}

	@Override
	public ToolResultMessage execute(ToolCall toolCall, ToolContext context) {
		Tool tool = getTool(toolCall.name());
		if (tool == null) {
			Instant start = Instant.now();
			return ToolCatalog.failedResult(toolCall, start,
				ToolCatalog.unknownToolMessage(toolCall, this, context));
		}
		return ToolCatalog.executeTool(tool, toolCall, context);
	}
}

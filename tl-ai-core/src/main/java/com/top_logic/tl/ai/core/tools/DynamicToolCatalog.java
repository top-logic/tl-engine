/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
				"Unknown tool: '" + toolCall.name() + "'.");
		}
		return ToolCatalog.executeTool(tool, toolCall, context);
	}
}

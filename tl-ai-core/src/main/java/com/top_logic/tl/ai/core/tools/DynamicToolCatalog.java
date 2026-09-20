/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Layered tool catalog that grows at runtime.
 *
 * <p>
 * Three layers, always advertised in this order: the frozen base catalog of the agent's own tools,
 * the framework overlays every agent gets ({@code loadSkill}, {@code listTools}), and the skill
 * overlays a run loaded. The first two are the catalog's <em>stable prefix</em>: they are fixed when
 * the agent is built and never change afterwards, whatever a run loads.
 * </p>
 *
 * <p>
 * That order is a prompt-cache contract, not a detail (Gitea #346). Anthropic serialises a request
 * as tools, then system prompt, then messages, and re-uses the cached prefix up to an explicit
 * breakpoint; {@code OpenAI}-compatible endpoints re-use the longest common prefix automatically.
 * Under both rules a tool <em>appended</em> behind everything that was there before leaves that
 * prefix byte-identical, while a tool inserted into it - or the same tools sent in another order -
 * throws the whole prefix away. Hence: skill overlays are appended, in load order, and a skill restored
 * from a previous run of the same session lands exactly where loading it would have put it.
 * </p>
 *
 * <p>
 * Skill overlays are bounded. A session's catalog outlives its runs, so an append-only catalog let
 * an agent grow from its three advertised tools to everything its skills could reach and never
 * shrink again. At most {@link #DEFAULT_MAX_SKILL_OVERLAYS} skills keep their tools loaded; loading
 * one more drops the overlay whose tools were used longest ago. The model is told, and a call for an
 * evicted tool is answered with "load that skill" rather than "no such tool", because
 * {@link #isReachableBySkill(String)} still knows it.
 * </p>
 *
 * <p>
 * Thread-safe for concurrent reads during overlay additions.
 * </p>
 *
 * @see ToolCatalog
 */
public class DynamicToolCatalog implements ToolRegistry {

	/**
	 * How many skills may keep their tools in the catalog at once.
	 *
	 * <p>
	 * A ceiling on what every request of a long session pays for, not a judgement about how many
	 * skills a task needs: a run that needs a ninth skill gets it, and pays for it by giving up the
	 * one it has not used for longest. Eight is well above what the shipped agents' tasks reach and
	 * far below the point at which the tool array crowds out the conversation.
	 * </p>
	 */
	public static final int DEFAULT_MAX_SKILL_OVERLAYS = 8;

	/** One loaded skill and the tools it brought. */
	private record SkillOverlay(String skillId, ToolCatalog tools) {
		// Nothing to add.
	}

	private final ToolCatalog _base;

	/** The framework's own overlays. Part of the stable prefix, so kept apart from the skills'. */
	private final CopyOnWriteArrayList<ToolCatalog> _framework = new CopyOnWriteArrayList<>();

	private final int _maxSkillOverlays;

	/** Guards {@link #_skills}; readers use {@link #_skillSnapshot} instead. */
	private final Object _skillLock = new Object();

	/** Loaded skills in load order. Guarded by {@link #_skillLock}. */
	private final Map<String, SkillOverlay> _skills = new LinkedHashMap<>();

	/** Immutable publication of {@link #_skills}, so a tool call never takes the lock. */
	private volatile List<SkillOverlay> _skillSnapshot = List.of();

	/** When each loaded skill's tools were last called, by the ticks of {@link #_clock}. */
	private final Map<String, Long> _lastUse = new LinkedHashMap<>();

	private final AtomicLong _clock = new AtomicLong();

	private Set<String> _reachableBySkill = Set.of();

	/** Run when the conversation this catalog serves was compacted. */
	private final CopyOnWriteArrayList<Runnable> _compactionListeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a dynamic catalog backed by the given frozen base catalog.
	 */
	public DynamicToolCatalog(ToolCatalog base) {
		this(base, DEFAULT_MAX_SKILL_OVERLAYS);
	}

	/**
	 * Creates a dynamic catalog with an explicit ceiling on loaded skill overlays.
	 *
	 * @param maxSkillOverlays
	 *        How many skills may keep their tools loaded at once; must be positive.
	 */
	public DynamicToolCatalog(ToolCatalog base, int maxSkillOverlays) {
		if (!base.isFrozen()) {
			throw new IllegalArgumentException("Base catalog must be frozen.");
		}
		if (maxSkillOverlays <= 0) {
			throw new IllegalArgumentException("At least one skill overlay must fit.");
		}
		_base = base;
		_maxSkillOverlays = maxSkillOverlays;
	}

	/**
	 * Declares which tools an unloaded skill of this agent would bring.
	 *
	 * <p>
	 * Not used for lookup - a skill's tools only become callable when it is loaded. It is used to
	 * tell the model <em>why</em> a tool is absent: a name in this set means "load the skill", a name
	 * outside it means this agent does not carry the tool at all. It answers the same for a skill
	 * that was evicted, which is what makes eviction recoverable.
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
	 * Appends a frozen framework overlay - a tool every agent carries, such as {@code loadSkill}.
	 *
	 * <p>
	 * Part of the stable prefix, so it goes in front of every skill overlay however late it is
	 * added. {@code AgentFactory} builds these after the {@code SkillTool} whose state they report,
	 * by which time a restored skill may already be loaded.
	 * </p>
	 */
	public void addOverlay(ToolCatalog overlay) {
		if (!overlay.isFrozen()) {
			throw new IllegalArgumentException("Overlay catalog must be frozen.");
		}
		_framework.add(overlay);
	}

	/**
	 * Appends the tools of one loaded skill, evicting the least recently used skills if that puts
	 * the catalog over its ceiling.
	 *
	 * @param skillId
	 *        The skill these tools came with. Loading the same id twice replaces nothing and
	 *        evicts nothing.
	 * @param overlay
	 *        The skill's tools, frozen.
	 * @return The skills evicted to make room, in the order they were dropped. Empty in the normal
	 *         case; the caller has to tell the model about a non-empty one.
	 */
	public List<String> addSkillOverlay(String skillId, ToolCatalog overlay) {
		if (!overlay.isFrozen()) {
			throw new IllegalArgumentException("Overlay catalog must be frozen.");
		}
		List<String> evicted = new ArrayList<>();
		synchronized (_skillLock) {
			if (_skills.containsKey(skillId)) {
				return List.of();
			}
			_skills.put(skillId, new SkillOverlay(skillId, overlay));
			_lastUse.put(skillId, _clock.incrementAndGet());
			while (_skills.size() > _maxSkillOverlays) {
				String victim = leastRecentlyUsed(skillId);
				if (victim == null) {
					break;
				}
				_skills.remove(victim);
				_lastUse.remove(victim);
				evicted.add(victim);
			}
			publish();
		}
		if (!evicted.isEmpty()) {
			Logger.info("Skill overlay ceiling of " + _maxSkillOverlays + " reached loading '"
				+ skillId + "'; unloaded " + evicted, DynamicToolCatalog.class);
		}
		return evicted;
	}

	/**
	 * Registers a callback for {@link #historyCompacted()}.
	 *
	 * <p>
	 * The one thing a compaction changes for the catalog: the tools stay, but the instructions that
	 * came with them may have been summarized away. {@code SkillTool} listens so that a reload
	 * delivers the text again.
	 * </p>
	 */
	public void onHistoryCompacted(Runnable listener) {
		_compactionListeners.add(listener);
	}

	@Override
	public void historyCompacted() {
		for (Runnable listener : _compactionListeners) {
			listener.run();
		}
	}

	/**
	 * The skills whose tools this catalog currently advertises, in the order they were loaded.
	 */
	public List<String> loadedSkills() {
		List<String> ids = new ArrayList<>();
		for (SkillOverlay overlay : _skillSnapshot) {
			ids.add(overlay.skillId());
		}
		return List.copyOf(ids);
	}

	@Override
	public Tool getTool(String name) {
		Tool tool = _base.getTool(name);
		if (tool != null) {
			return tool;
		}
		for (ToolCatalog overlay : _framework) {
			tool = overlay.getTool(name);
			if (tool != null) {
				return tool;
			}
		}
		for (SkillOverlay overlay : _skillSnapshot) {
			tool = overlay.tools().getTool(name);
			if (tool != null) {
				return tool;
			}
		}
		return null;
	}

	@Override
	public List<ToolSchema> toolSchemas(Predicate<Tool> filter) {
		Map<String, ToolSchema> merged = stablePrefix(filter);
		for (SkillOverlay overlay : _skillSnapshot) {
			for (ToolSchema schema : overlay.tools().toolSchemas(filter)) {
				merged.putIfAbsent(schema.name(), schema);
			}
		}
		return List.copyOf(merged.values());
	}

	@Override
	public int stableToolCount(Predicate<Tool> filter) {
		return stablePrefix(filter).size();
	}

	/** The schemas that are fixed for the agent's lifetime, keyed by name in advertising order. */
	private Map<String, ToolSchema> stablePrefix(Predicate<Tool> filter) {
		Map<String, ToolSchema> merged = new LinkedHashMap<>();
		for (ToolSchema schema : _base.toolSchemas(filter)) {
			merged.putIfAbsent(schema.name(), schema);
		}
		for (ToolCatalog overlay : _framework) {
			for (ToolSchema schema : overlay.toolSchemas(filter)) {
				merged.putIfAbsent(schema.name(), schema);
			}
		}
		return merged;
	}

	@Override
	public ToolResultMessage execute(ToolCall toolCall, ToolContext context) {
		Tool tool = getTool(toolCall.name());
		if (tool == null) {
			Instant start = Instant.now();
			return ToolCatalog.failedResult(toolCall, start,
				ToolCatalog.unknownToolMessage(toolCall, this, context));
		}
		touchOwner(toolCall.name());
		return ToolCatalog.executeTool(tool, toolCall, context);
	}

	/**
	 * Records that the skill owning the named tool is still in use.
	 *
	 * <p>
	 * A skill counts as used when one of its tools is <em>called</em>, not when it merely sits in
	 * the catalog: the point of the ceiling is to drop what the run has stopped working with.
	 * </p>
	 */
	private void touchOwner(String toolName) {
		for (SkillOverlay overlay : _skillSnapshot) {
			if (overlay.tools().getTool(toolName) != null) {
				synchronized (_skillLock) {
					if (_skills.containsKey(overlay.skillId())) {
						_lastUse.put(overlay.skillId(), _clock.incrementAndGet());
					}
				}
				return;
			}
		}
	}

	/** The loaded skill used longest ago, never the one just loaded. Call under the lock. */
	private String leastRecentlyUsed(String justLoaded) {
		String victim = null;
		long oldest = Long.MAX_VALUE;
		for (String skillId : _skills.keySet()) {
			if (skillId.equals(justLoaded)) {
				continue;
			}
			long used = _lastUse.getOrDefault(skillId, 0L);
			if (used < oldest) {
				oldest = used;
				victim = skillId;
			}
		}
		return victim;
	}

	/** Republishes the read-side snapshot. Call under the lock. */
	private void publish() {
		_skillSnapshot = List.copyOf(_skills.values());
	}
}

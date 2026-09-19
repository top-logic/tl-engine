/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool that allows agents to load skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent's skill set. The agent sees a catalog of skill names
 * and descriptions in its system prompt and calls {@code loadSkill} to retrieve the full content.
 * When a skill declares tools, they are dynamically added to the agent's tool catalog.
 * </p>
 *
 * <p>
 * Tools the run's principal may not use are withheld: they are neither added to the catalog nor
 * named in the load message. Announcing them would put an authoritative tool result in the
 * conversation that the next turn's schema filter immediately contradicts, and a model that took it
 * at its word would earn a denial. Which skills are offered here at all is decided one level up, in
 * {@code AgentFactory}.
 * </p>
 *
 * <p>
 * A skill that decision withheld is <em>explained</em> rather than merely refused - see
 * {@code AgentCapabilities.skillAbsenceReason}. "Not available for this agent" covered a withheld
 * skill, a skill this agent never declared and a misspelled id alike, and the model filled the gap
 * with a capability of its own choosing.
 * </p>
 *
 * <p>
 * Loading is bounded (Gitea #346). A chat session's catalog outlives its runs, so an agent that
 * could only ever load grew from the handful of tools it advertises to everything its skills can
 * reach and stayed there for the rest of the session. The {@link DynamicToolCatalog} drops the
 * overlay used longest ago when a load would take it over its ceiling, and this tool says so in the
 * answer it gives the model - an eviction it kept quiet about would look like a tool that stopped
 * existing.
 * </p>
 */
public class SkillTool {

	private final Map<String, Skill> _skills;

	private final DynamicToolCatalog _dynamicCatalog;

	private final Function<String, Tool> _toolResolver;

	private final Predicate<Tool> _permitted;

	private final Function<String, String> _absenceReason;

	/**
	 * Which skills this run has loaded, in the order it loaded them.
	 *
	 * <p>
	 * Synchronized because concurrency-safe tools of one turn run on parallel threads and both
	 * {@code loadSkill} and {@code listTools} are such tools: one adding to this set while the other
	 * reads it is an ordinary turn, not an edge case. Ordered because the order is what the next run
	 * of the session restores the catalog in, and a catalog restored in another order is a prompt
	 * cache thrown away for nothing.
	 * </p>
	 */
	private final Set<String> _loadedSkills = Collections.synchronizedSet(new LinkedHashSet<>());

	/**
	 * Which skills' instructions are in the conversation the model reads.
	 *
	 * <p>
	 * Kept apart from {@link #_loadedSkills} because the two are lost for different reasons. Tools
	 * leave when the catalog evicts them; instructions leave when a compaction summarizes the turn
	 * that carried them away. A skill whose instructions are gone must be loadable again even though
	 * its tools never left, or the model follows a skill it can no longer read.
	 * </p>
	 */
	private final Set<String> _deliveredContent = Collections.synchronizedSet(new LinkedHashSet<>());

	/**
	 * Creates a skill tool with dynamic tool loading, refusing an absent skill without explaining it.
	 *
	 * @see #SkillTool(Map, DynamicToolCatalog, Function, Predicate, Set, Function)
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver, Predicate<Tool> permitted,
			Set<String> preloadedSkillIds) {
		this(skills, dynamicCatalog, toolResolver, permitted, preloadedSkillIds, null);
	}

	/**
	 * Creates a skill tool with dynamic tool loading.
	 *
	 * @param skills
	 *        Map of skill id to {@link Skill}.
	 * @param dynamicCatalog
	 *        the dynamic catalog to add tool overlays to, or {@code null} for legacy behavior
	 * @param toolResolver
	 *        resolves qualified tool names to {@link Tool} instances, or {@code null} for legacy
	 *        behavior
	 * @param permitted
	 *        whether the run's principal may use a given tool; tools it rejects are neither
	 *        registered nor announced. May be {@code null}, which permits everything.
	 * @param preloadedSkillIds
	 *        skill IDs to restore from a previous session; their tools are added to the catalog
	 *        immediately. May be {@code null} or empty.
	 * @param absenceReason
	 *        why a skill that is not in {@code skills} is not there, for an id the caller can explain -
	 *        typically {@code AgentCapabilities.skillAbsenceReason}. May be {@code null} or answer
	 *        {@code null}, which leaves the plain "no such skill" message.
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver, Predicate<Tool> permitted,
			Set<String> preloadedSkillIds, Function<String, String> absenceReason) {
		_skills = skills;
		_dynamicCatalog = dynamicCatalog;
		_toolResolver = toolResolver;
		_permitted = permitted != null ? permitted : tool -> true;
		_absenceReason = absenceReason != null ? absenceReason : skillId -> null;

		if (dynamicCatalog != null) {
			dynamicCatalog.onHistoryCompacted(this::forgetDeliveredContent);
		}

		if (preloadedSkillIds != null) {
			for (String skillId : preloadedSkillIds) {
				Skill skill = _skills.get(skillId);
				if (skill != null) {
					addSkillTools(skill);
					// The session continues the conversation the previous run loaded this skill in,
					// so its instructions are still in it. A compaction takes this mark away again.
					_deliveredContent.add(skillId);
				} else {
					Logger.info("Skipping restore of removed skill: " + skillId, SkillTool.class);
				}
			}
		}
	}

	/**
	 * Creates a skill tool with dynamic tool loading and no preloaded skills.
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver) {
		this(skills, dynamicCatalog, toolResolver, null, null);
	}

	/**
	 * Legacy constructor without dynamic tool loading.
	 */
	public SkillTool(Map<String, Skill> skills) {
		this(skills, null, null);
	}

	/**
	 * Every tool name any of these skills declares, loaded or not.
	 *
	 * <p>
	 * What the agent could reach by loading a skill. Handed to the {@link DynamicToolCatalog} so that
	 * a call for an absent tool can say "load the skill that has it" rather than "no such tool".
	 * </p>
	 */
	public Set<String> declaredToolNames() {
		Set<String> names = new HashSet<>();
		for (Skill skill : _skills.values()) {
			for (String toolName : skill.tools()) {
				names.add(toolName);
				Tool tool = _toolResolver == null ? null : _toolResolver.apply(toolName);
				if (tool != null) {
					// The model calls a tool by its bare name, not by the qualified name a skill
					// lists it under.
					names.add(tool.name());
				}
			}
		}
		return names;
	}

	/**
	 * The ids of the skills this agent can actually hand over.
	 *
	 * <p>
	 * Already filtered for the run's principal, so it is a subset of what the agent declares: a skill
	 * every tool of which the principal is denied is not in it. For {@code listTools}, which lists the
	 * declared skills and has to mark each one - a declared skill missing from this set is one the model
	 * must not be told to load.
	 * </p>
	 */
	public Set<String> offeredSkills() {
		return Set.copyOf(_skills.keySet());
	}

	/**
	 * The ids of the skills this run has already loaded.
	 *
	 * <p>
	 * Read per call, so a {@code listTools} in a later turn reflects everything loaded since, for
	 * {@code listTools}: "already in your tool list" and "call {@code loadSkill} first" are different answers,
	 * and getting them the wrong way round costs a wasted turn.
	 * </p>
	 */
	public Set<String> loadedSkills() {
		synchronized (_loadedSkills) {
			return Collections.unmodifiableSet(new LinkedHashSet<>(_loadedSkills));
		}
	}

	/**
	 * Loads a skill by its id, returning its full content.
	 * If the skill declares tools and this is the first load, those tools are added
	 * to the agent's active tool catalog.
	 *
	 * @param skillId
	 *        The skill id from the catalog.
	 * @return The full skill content with a summary of added tools, or an error message.
	 */
	@ToolMethod(
		description = "Loads a skill by its id. Returns specialized instructions you MUST follow for the related task. When a skill provides tools, they become available immediately. Check the Available Skills section in your system instructions for skill ids and when to use them.",
		mode = ToolMode.READ,
		concurrencySafe = true,
		truncationHint = Integer.MAX_VALUE, group = ToolGroups.INTERACTION)
	public String loadSkill(@P("Skill id from the catalog") String skillId) {
		Skill skill = _skills.get(skillId);
		if (skill == null) {
			return absenceMessage(skillId);
		}

		// Return short message if the instructions are still in the conversation (avoid dumping
		// the content twice).
		if (_deliveredContent.contains(skill.id())) {
			return "Skill \"" + skillId + "\" is already loaded. Its tools are available. "
				+ "Do not call loadSkill for this skill again.";
		}

		Load load = addSkillTools(skill);
		_deliveredContent.add(skill.id());

		StringBuilder result = new StringBuilder();
		result.append("Skill \"").append(skillId).append("\" loaded.\n\n");

		if (!load.addedTools().isEmpty()) {
			result.append("The following tools are now available:\n");
			for (String toolName : load.addedTools()) {
				Tool tool = _dynamicCatalog.getTool(toolName);
				if (tool != null) {
					result.append("- ").append(toolName).append(": ")
						.append(tool.description()).append("\n");
				}
			}
			result.append("\n");
		}

		if (!load.evictedSkills().isEmpty()) {
			// Said plainly, because the model has read those skills' instructions earlier in this very
			// conversation and would otherwise call a tool that has quietly gone away.
			result.append("To make room, these skills were unloaded and their tools are gone: ")
				.append(String.join(", ", load.evictedSkills()))
				.append(". Load one again if you need it.\n\n");
		}

		result.append(skill.content());
		return result.toString();
	}

	/**
	 * The answer to a {@code loadSkill} for a skill this agent cannot hand over.
	 *
	 * <p>
	 * The list is the skills this agent really can load, so that following it cannot walk into a second
	 * refusal - the same rule the unknown-tool message follows. An id nobody can explain keeps the
	 * plain sentence it always had: a typo must disclose nothing about capabilities.
	 * </p>
	 */
	private String absenceMessage(String skillId) {
		String reason = _absenceReason.apply(skillId);
		if (reason == null) {
			return "Skill '" + skillId + "' is not available for this agent. Available: "
				+ _skills.keySet();
		}
		return reason + " Available: " + _skills.keySet();
	}

	/**
	 * What one load did: the tools it brought, and the skills it cost.
	 *
	 * @param addedTools
	 *        Names of the tools now callable that were not callable before.
	 * @param evictedSkills
	 *        Ids of the skills whose tools were dropped to stay under the catalog's ceiling.
	 */
	private record Load(List<String> addedTools, List<String> evictedSkills) {

		static final Load NOTHING = new Load(List.of(), List.of());
	}

	/**
	 * Registers a skill's tools and records the skill as loaded.
	 *
	 * <p>
	 * A skill without tools is recorded too, although it registers nothing: it is loaded in every
	 * sense the model cares about, and a skill that never counted as loaded had its whole content
	 * repeated into the conversation on every {@code loadSkill} of it.
	 * </p>
	 */
	private Load addSkillTools(Skill skill) {
		if (_loadedSkills.contains(skill.id())) {
			return Load.NOTHING;
		}
		if (_dynamicCatalog == null || _toolResolver == null) {
			// Legacy mode: no catalog to grow, so nothing to remember either.
			return Load.NOTHING;
		}
		markLoaded(skill.id());
		if (skill.tools().isEmpty()) {
			return Load.NOTHING;
		}

		ToolCatalog overlay = new ToolCatalog();
		List<String> added = new ArrayList<>();
		List<String> withheld = new ArrayList<>();

		for (String toolName : skill.tools()) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool == null) {
				Logger.warn("Skill '" + skill.id() + "' references unknown tool: " + toolName,
					SkillTool.class);
				continue;
			}
			// A tool the principal may not use is neither registered nor reported as available:
			// the per-turn schema filter would remove it again anyway, and an authoritative tool
			// result naming a tool the model cannot call is worse than not mentioning it.
			if (!_permitted.test(tool)) {
				withheld.add(tool.name());
				continue;
			}
			// Skip if already in the catalog (from another skill)
			if (_dynamicCatalog.getTool(tool.name()) != null) {
				continue;
			}
			overlay.register(tool);
			added.add(tool.name());
		}

		if (!withheld.isEmpty()) {
			Logger.info("Skill '" + skill.id() + "' loaded without " + withheld.size()
				+ " tools the current user may not use: " + withheld, SkillTool.class);
		}

		if (overlay.tools().isEmpty()) {
			return Load.NOTHING;
		}
		overlay.freeze();
		List<String> evicted = _dynamicCatalog.addSkillOverlay(skill.id(), overlay);
		for (String evictedSkill : evicted) {
			forgetLoaded(evictedSkill);
		}
		return new Load(added, evicted);
	}

	/** Records a skill as loaded, here and in the session state the next run restores from. */
	private void markLoaded(String skillId) {
		_loadedSkills.add(skillId);
		LoadedSkillsBinding.addSkill(skillId);
	}

	/**
	 * Forgets an evicted skill, so that loading it again works and the next run does not restore it.
	 *
	 * <p>
	 * The instructions are forgotten with the tools although they are still in the conversation: a
	 * reload has to bring the tools back, and the short "already loaded" answer would not.
	 * </p>
	 */
	private void forgetLoaded(String skillId) {
		_loadedSkills.remove(skillId);
		_deliveredContent.remove(skillId);
		LoadedSkillsBinding.removeSkill(skillId);
	}

	/**
	 * Answers a compaction: the instructions may have been summarized away, the tools have not moved.
	 *
	 * <p>
	 * Dropping the tools here instead would take a capability away from the model for a reason it
	 * cannot see, in the middle of the task it loaded the skill for. What a compaction really costs
	 * is the text, so that is what is re-delivered on the next {@code loadSkill} - at most one
	 * repetition per skill per compaction, and only if the model asks.
	 * </p>
	 */
	private void forgetDeliveredContent() {
		_deliveredContent.clear();
	}
}

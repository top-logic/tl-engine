/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.ToolAccessExplanation;

/**
 * One row of the Effektive-Rechte table.
 *
 * <p>
 * Two levels, because that is how a policy is written and read: an administrator grants a capability
 * group, and the tools are what the group turns out to contain. The unclassified tools are the
 * exception that proves the rule - they have no group to sit under, so they are rows of their own.
 * </p>
 */
public sealed interface EffectiveAccessRow {

	/** Whether the subject reaches what this row is about. */
	boolean allowed();

	/**
	 * Which rule decided, in the administrator's language.
	 *
	 * <p>
	 * Two policies answer here - the capability policy for a tool, the run policy for an agent, a
	 * model or an MCP server - and they explain themselves differently, which is why the sentence is
	 * asked of the row rather than built from one explanation type.
	 * </p>
	 */
	String provenance();

	/**
	 * A capability group, with the decision for the group as a whole.
	 *
	 * @param group
	 *        The technical group name.
	 * @param explanation
	 *        The decision for the group.
	 * @param toolCount
	 *        How many registered tools the group holds. Zero for a group the policy knows and no tool
	 *        belongs to - which is worth seeing, because a grant of it reaches nothing.
	 * @param scriptCommitNote
	 *        The extra sentence the {@code script} group needs, or <code>null</code> for every other
	 *        group. A capability that is consumed by a tool <em>argument</em> does not fit the row
	 *        model and has to be said in words.
	 */
	record Capability(String group, ToolAccessExplanation explanation, int toolCount,
			String scriptCommitNote) implements EffectiveAccessRow {

		@Override
		public boolean allowed() {
			return explanation().allowed();
		}

		@Override
		public String provenance() {
			String sentence = ToolAccessLabels.sentence(explanation());
			return scriptCommitNote() == null ? sentence : sentence + " · " + scriptCommitNote();
		}
	}

	/**
	 * One tool, under its capability group or on its own if it has none.
	 *
	 * @param qualifiedName
	 *        The name the policy knows the tool by.
	 * @param explanation
	 *        The decision for this tool, which can differ from its group's: a per-tool grant or a
	 *        per-tool deny reaches exactly one row.
	 */
	record ToolRow(String qualifiedName, ToolAccessExplanation explanation) implements EffectiveAccessRow {

		@Override
		public boolean allowed() {
			return explanation().allowed();
		}

		@Override
		public String provenance() {
			return ToolAccessLabels.sentence(explanation());
		}
	}

	/**
	 * One of the three sections that decide what a run may <em>be</em>, as a heading row.
	 *
	 * @param dimension
	 *        Which section.
	 * @param allowedCount
	 *        How many of the application's agents, models or servers the subject may use.
	 * @param totalCount
	 *        How many there are. The pair is the answer an administrator is looking for; the rows
	 *        below say which ones.
	 */
	record RunSection(Dimension dimension, int allowedCount, int totalCount)
			implements EffectiveAccessRow {

		/**
		 * A heading, not a decision.
		 *
		 * <p>
		 * Its rows carry the verdicts; the section itself would have to read "granted" for one agent
		 * out of six, which is the wrong headline for a dimension that is five sixths denied. The
		 * accessor leaves the verdict cell of a section empty - see
		 * {@link EffectiveAccessAccessor}.
		 * </p>
		 */
		@Override
		public boolean allowed() {
			return allowedCount() > 0;
		}

		@Override
		public String provenance() {
			return ToolAccessLabels.sectionSummary(this);
		}
	}

	/**
	 * One agent, model or MCP server, under the section it belongs to.
	 *
	 * @param dimension
	 *        Which section.
	 * @param name
	 *        The name the policy knows it by.
	 * @param decision
	 *        Whether the subject may use it, and what decided.
	 * @param explanation
	 *        What to say instead of the sentence {@code decision} would give, or <code>null</code> to
	 *        say that one. For the agent a run cannot use because of what it uses <em>itself</em>: the
	 *        section that stops it is another one, and a row that quoted its sentence would talk about
	 *        a model under the heading "agents".
	 */
	record RunName(Dimension dimension, String name, Decision decision, String explanation)
			implements EffectiveAccessRow {

		/** A row whose own section decided it. */
		RunName(Dimension dimension, String name, Decision decision) {
			this(dimension, name, decision, null);
		}

		@Override
		public boolean allowed() {
			return decision() instanceof Decision.Granted;
		}

		@Override
		public String provenance() {
			return explanation() != null ? explanation() : ToolAccessLabels.sentence(decision());
		}
	}

}

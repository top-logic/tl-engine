/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.tl.ai.tool.api.ToolGroups;

/*
 * Deliberately free of resources: the sentence an administrator reads is assembled in the view
 * (com.top_logic.tl.ai.core.security.layout.ToolAccessRuleFormat), so that a test can assert the
 * decision and its provenance as data rather than by matching translated prose.
 */

/**
 * One rule of a {@link ToolAccessPolicy} that participated in a decision.
 *
 * <p>
 * The <em>why</em> of a {@link ToolAccessExplanation}: which line of the policy - or which unwritten
 * default - produced the answer. A rule names at most one role, at most one capability group and at
 * most one tool, because that is all any single entry of the policy can name.
 * </p>
 *
 * @param kind
 *        Which of the policy's mechanisms this rule is. Never <code>null</code>.
 * @param roleName
 *        The role whose entry decided, or <code>null</code> for a rule that is not a role's.
 * @param group
 *        The capability group the rule is about, or <code>null</code> for a rule that names no group -
 *        an entry that granted a single tool, or a tool that belongs to no group at all.
 * @param namedTool
 *        The tool the entry named explicitly, or <code>null</code> if the entry matched through the
 *        tool's group. Non-<code>null</code> is what distinguishes {@code <grant tools="...">} from
 *        {@code <grant group="...">}.
 * @see ToolAccessExplanation
 */
public record ToolAccessRule(ToolAccessRule.Kind kind, String roleName, String group, String namedTool) {

	/**
	 * The mechanisms of a {@link ToolAccessPolicy} a decision can come from.
	 *
	 * <p>
	 * One constant per sentence an administrator has to be able to read, which is why the three
	 * baseline states and the two "nothing reached it" cases are told apart rather than collapsed:
	 * "the shipped default applies", "your baseline lists it" and "nobody classified this tool" send
	 * an administrator to three different places.
	 * </p>
	 */
	public enum Kind {

		/**
		 * {@link RoleToolAccess#IMPLICIT_BASELINE} - granted to everyone, in every run mode, without
		 * being configured.
		 */
		IMPLICIT_BASELINE,

		/**
		 * {@link RoleToolAccess#DEFAULT_BASELINE} - the read-only default that applies because the
		 * policy declares no {@code <baseline>} at all.
		 */
		DEFAULT_BASELINE,

		/** An entry of the policy's declared {@code <baseline>}, which reaches every account. */
		BASELINE,

		/** A {@code <grant>} of a role the account holds. */
		ROLE_GRANT,

		/** A {@code <deny>} of a role the account holds. Wins over every grant. */
		ROLE_DENY,

		/**
		 * The {@code <background-allowlist>} ceiling of a run that has nobody to answer an approval
		 * prompt. Binds administrators too.
		 */
		BACKGROUND_CEILING,

		/** The account is an administrator and therefore bypasses this layer entirely. */
		ADMIN_BYPASS,

		/**
		 * The tool belongs to no capability group, so no group grant can reach it and only an explicit
		 * per-tool grant could.
		 */
		UNCLASSIFIED,

		/** Nothing in the policy reaches the capability: no baseline entry and no role of the account. */
		NOT_GRANTED;
	}

	/** The implicit grant of {@link ToolGroups#INTERACTION} / {@link ToolGroups#INTROSPECTION}. */
	static ToolAccessRule implicitBaseline(String group) {
		return new ToolAccessRule(Kind.IMPLICIT_BASELINE, null, group, null);
	}

	/** The shipped read-only baseline, applying because the policy declares none. */
	static ToolAccessRule defaultBaseline(String group) {
		return new ToolAccessRule(Kind.DEFAULT_BASELINE, null, group, null);
	}

	/** An entry of the declared {@code <baseline>}. */
	static ToolAccessRule baseline(String group, String namedTool) {
		return new ToolAccessRule(Kind.BASELINE, null, group, namedTool);
	}

	/** A {@code <grant>} of the named role. */
	static ToolAccessRule roleGrant(String roleName, String group, String namedTool) {
		return new ToolAccessRule(Kind.ROLE_GRANT, roleName, group, namedTool);
	}

	/** A {@code <deny>} of the named role. */
	static ToolAccessRule roleDeny(String roleName, String group, String namedTool) {
		return new ToolAccessRule(Kind.ROLE_DENY, roleName, group, namedTool);
	}

	/** The background allowlist as a ceiling. */
	static ToolAccessRule backgroundCeiling(String group) {
		return new ToolAccessRule(Kind.BACKGROUND_CEILING, null, group, null);
	}

	/** The administrator bypass. */
	static ToolAccessRule adminBypass() {
		return new ToolAccessRule(Kind.ADMIN_BYPASS, null, null, null);
	}

	/**
	 * A tool that belongs to no group.
	 *
	 * <p>
	 * Carries no tool name: the caller asked about a tool and knows which one. What would go in
	 * {@link #namedTool()} is the name a policy <em>entry</em> spelled out, and the point of this rule
	 * is that no entry did.
	 * </p>
	 */
	static ToolAccessRule unclassified() {
		return new ToolAccessRule(Kind.UNCLASSIFIED, null, null, null);
	}

	/** Nothing reached the given capability. */
	static ToolAccessRule notGranted(String group) {
		return new ToolAccessRule(Kind.NOT_GRANTED, null, group, null);
	}

	/**
	 * Whether the policy entry that decided named one tool rather than a capability group.
	 */
	public boolean isPerTool() {
		return namedTool != null;
	}

}

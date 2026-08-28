/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.tl.ai.tool.api.ToolGroups;

/*
 * Deliberately free of resources: the sentence an administrator reads is assembled in the view
 * (com.top_logic.tl.ai.core.security.layout.ToolAccessLabels), so that a test can assert the decision
 * and its provenance as data rather than by matching translated prose.
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

	/*
	 * The JavaDoc of this enum and of its constants is exported to META-INF/messages_*.properties by
	 * the build's doclet, so it is written for an administrator: short, and free of XML element names,
	 * which the editor's safe-HTML renderer rejects as unknown tags. TestSecurityConfigPresentation
	 * enforces both.
	 *
	 * One constant per sentence an administrator has to be able to read, which is why the three
	 * baseline states and the two "nothing reached it" cases are told apart rather than collapsed:
	 * "the shipped default applies", "your baseline lists it" and "nobody classified this tool" send an
	 * administrator to three different places.
	 */

	/**
	 * The mechanisms of a tool access policy a decision can come from.
	 */
	public enum Kind {

		/**
		 * Granted to everyone, in every run mode, without being configured.
		 *
		 * @see RoleToolAccess#IMPLICIT_BASELINE
		 */
		IMPLICIT_BASELINE,

		/**
		 * The read-only default that applies because the policy states no baseline of its own.
		 *
		 * @see RoleToolAccess#DEFAULT_BASELINE
		 */
		DEFAULT_BASELINE,

		/** An entry of the policy's own baseline, which reaches every account. */
		BASELINE,

		/** A permission the account holds through one of its roles. */
		ROLE_GRANT,

		/** A prohibition of one of the account's roles. Wins over every permission. */
		ROLE_DENY,

		/**
		 * The ceiling for runs that have nobody to answer a confirmation prompt. Binds administrators
		 * too.
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

	/** The implicit grant of {@link ToolGroups#INTERACTION} and {@link ToolGroups#INTROSPECTION}. */
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

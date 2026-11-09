/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Collection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.ToolAccessExplanation;
import com.top_logic.tl.ai.core.security.ToolAccessRule;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.util.Resources;

/**
 * How a tool-access decision and its provenance are put into words.
 *
 * <p>
 * The one place the sentences of the "Agent-Sicherheit" view are assembled, so that the same rule
 * reads the same way in the catalog and in the effective-permissions view. The decision itself is
 * data - {@link ToolAccessExplanation} - and stays free of resources, so that a test can assert what
 * was decided rather than how it was translated.
 * </p>
 *
 * <p>
 * A provenance sentence names both sides whenever two rules disagreed. "Denied" alone sends nobody
 * anywhere. "Capped by the background allowlist. Role {@code projectPartner} grants it, but a run nobody
 * watches may not use it." names the control to change and the grant that is already there.
 * </p>
 */
public class ToolAccessLabels {

	/** Separates a rule from the sentence about what it overrode. */
	private static final String CLAUSE_SEPARATOR = ". ";

	private ToolAccessLabels() {
		// Static utility.
	}

	/**
	 * The full provenance sentence of a decision, including what it overrode.
	 *
	 * @param explanation
	 *        The decision to describe, may be <code>null</code>.
	 * @return The sentence in the acting user's language, or the empty string for no decision.
	 */
	public static String sentence(ToolAccessExplanation explanation) {
		if (explanation == null) {
			return "";
		}
		String decided = resources().getString(ruleKey(explanation.decidedBy()));
		String overridden = overriddenClause(explanation);
		return overridden.isEmpty() ? decided : decided + CLAUSE_SEPARATOR + overridden;
	}

	/**
	 * The clause naming the rule that lost, phrased for the rule that won.
	 *
	 * @return The clause, or the empty string if nothing was overridden.
	 */
	private static String overriddenClause(ToolAccessExplanation explanation) {
		ToolAccessRule overridden = explanation.overridden();
		if (overridden == null) {
			return "";
		}
		switch (explanation.kind()) {
			case BACKGROUND_CEILING:
				return resources().getString(
					I18NConstants.CAPPED_ALTHOUGH_GRANTED__SUBJECT.fill(subject(overridden)));
			case ROLE_DENY:
				return resources().getString(
					I18NConstants.DENIED_ALTHOUGH_GRANTED__SUBJECT.fill(subject(overridden)));
			case ADMIN_BYPASS:
				return resources().getString(I18NConstants.ALLOWED_ALTHOUGH_POLICY__RULE
					.fill(resources().getString(ruleKey(overridden))));
			default:
				// Nothing else produces a loser, and inventing a phrasing for a case that cannot
				// happen would be a sentence nobody ever reviewed.
				return "";
		}
	}

	/**
	 * The label of one rule, as a sentence of its own.
	 *
	 * @param rule
	 *        The rule to name. Never <code>null</code>.
	 */
	public static ResKey ruleKey(ToolAccessRule rule) {
		switch (rule.kind()) {
			case IMPLICIT_BASELINE:
				return I18NConstants.RULE_IMPLICIT_BASELINE;
			case DEFAULT_BASELINE:
				return I18NConstants.RULE_DEFAULT_BASELINE;
			case BASELINE:
				return I18NConstants.RULE_BASELINE;
			case ROLE_GRANT:
				return (rule.isPerTool()
					? I18NConstants.RULE_ROLE_GRANT_TOOL__ROLE
					: I18NConstants.RULE_ROLE_GRANT__ROLE).fill(rule.roleName());
			case ROLE_DENY:
				return I18NConstants.RULE_ROLE_DENY__ROLE.fill(rule.roleName());
			case BACKGROUND_CEILING:
				return I18NConstants.RULE_BACKGROUND_CEILING;
			case ADMIN_BYPASS:
				return I18NConstants.RULE_ADMIN_BYPASS;
			case UNCLASSIFIED:
				return I18NConstants.RULE_UNCLASSIFIED;
			default:
				return I18NConstants.RULE_NOT_GRANTED;
		}
	}

	/**
	 * How a rule is named <em>inside</em> a sentence about another rule.
	 *
	 * <p>
	 * "role {@code projectPartner} grants it" rather than "Grant of role {@code projectPartner} grants it".
	 * </p>
	 */
	private static String subject(ToolAccessRule rule) {
		switch (rule.kind()) {
			case IMPLICIT_BASELINE:
				return resources().getString(I18NConstants.SUBJECT_IMPLICIT_BASELINE);
			case DEFAULT_BASELINE:
				return resources().getString(I18NConstants.SUBJECT_DEFAULT_BASELINE);
			case BASELINE:
				return resources().getString(I18NConstants.SUBJECT_BASELINE);
			case ROLE_GRANT:
				return resources().getString(I18NConstants.SUBJECT_ROLE__ROLE.fill(rule.roleName()));
			case ADMIN_BYPASS:
				return resources().getString(I18NConstants.SUBJECT_ADMIN);
			default:
				return resources().getString(ruleKey(rule));
		}
	}

	/**
	 * The verdict word of a decision, for a cell that has no room for an icon.
	 *
	 * @param verdict
	 *        The decision to name. Never <code>null</code>.
	 */
	public static String verdict(ToolAccessVerdict verdict) {
		return resources().getString(verdict == ToolAccessVerdict.ALLOWED
			? I18NConstants.VERDICT_ALLOWED : I18NConstants.VERDICT_DENIED);
	}

	/**
	 * What one of the three run sections is about, as a heading.
	 *
	 * @param dimension
	 *        The section to name. Never <code>null</code>.
	 */
	public static String dimensionLabel(Dimension dimension) {
		switch (dimension) {
			case MODEL:
				return resources().getString(I18NConstants.SECTION_MODEL_ACCESS);
			case MCP_SERVER:
				return resources().getString(I18NConstants.SECTION_MCP_ACCESS);
			default:
				return resources().getString(I18NConstants.SECTION_AGENT_ACCESS);
		}
	}

	/**
	 * How much of one run section the subject reaches, as the sentence of its heading row.
	 *
	 * @param section
	 *        The heading row. Never <code>null</code>.
	 */
	public static String sectionSummary(EffectiveAccessRow.RunSection section) {
		if (section.totalCount() == 0) {
			// Nothing was refused: the application configures none of them. "0 of 0 reachable" over a
			// "denied" verdict reads as a restriction that nobody wrote.
			return resources().getString(I18NConstants.SECTION_SUMMARY_NONE_CONFIGURED);
		}
		return resources().getString(I18NConstants.SECTION_SUMMARY__ALLOWED_TOTAL
			.fill(Integer.valueOf(section.allowedCount()), Integer.valueOf(section.totalCount())));
	}

	/**
	 * Why an agent, a model or an MCP server is reachable or not, as a sentence.
	 *
	 * @param decision
	 *        The decision to explain. Never <code>null</code>.
	 */
	public static String sentence(Decision decision) {
		if (decision instanceof Decision.Granted granted) {
			return byRoleSentence(granted.byRole());
		}
		Decision.Denied denied = (Decision.Denied) decision;
		return resources().getString(denied.reason());
	}

	/**
	 * Which rule let a name through: a role of the subject, the section default, or the agent itself.
	 */
	private static String byRoleSentence(String byRole) {
		if (RoleAccessPolicy.DEFAULT_ROLE.equals(byRole)) {
			return resources().getString(I18NConstants.ALLOWED_BY_SECTION_DEFAULT);
		}
		if (RoleAccessPolicy.AGENT_ROLE.equals(byRole)) {
			return resources().getString(I18NConstants.ALLOWED_BY_THE_AGENT);
		}
		return resources().getString(I18NConstants.ALLOWED_BY_ROLE__ROLE.fill(byRole));
	}

	/**
	 * A capability group as "display name (technical name)", or the technical name alone for a group
	 * the framework does not ship.
	 *
	 * @param group
	 *        The technical group name, may be <code>null</code>.
	 * @return The label, or the empty string for no group.
	 */
	public static String groupLabel(String group) {
		if (group == null) {
			return "";
		}
		ResKey label = ToolGroups.labelKey(group);
		return label == null ? group : resources().getString(label) + " (" + group + ")";
	}

	/**
	 * The one sentence saying what granting the given group lets an agent do.
	 *
	 * @param group
	 *        The technical group name, may be <code>null</code>.
	 * @return The explanation, or <code>null</code> for a group no resource describes.
	 */
	public static String groupTooltip(String group) {
		ResKey label = ToolGroups.labelKey(group);
		return label == null ? null : resources().getString(label.tooltipOptional());
	}

	/**
	 * The risk marker of a capability group, or the empty string for a group that carries none.
	 *
	 * @param group
	 *        The technical group name, may be <code>null</code>.
	 */
	public static String risk(String group) {
		return risk(CapabilityRisk.of(group));
	}

	/**
	 * The risk marker in words, or the empty string for {@link CapabilityRisk#NONE}.
	 *
	 * @param risk
	 *        The marker to name. Never <code>null</code>.
	 */
	public static String risk(CapabilityRisk risk) {
		switch (risk) {
			case PRIVILEGED:
				return resources().getString(I18NConstants.RISK_PRIVILEGED);
			case EGRESS:
				return resources().getString(I18NConstants.RISK_EGRESS);
			case IMPLICIT:
				return resources().getString(I18NConstants.RISK_IMPLICIT);
			default:
				// A group with nothing special about it says nothing rather than "nothing special":
				// a column of "none" reads as information and is not.
				return "";
		}
	}

	/**
	 * Who reaches a tool, in words.
	 *
	 * @param reachability
	 *        The state to name. Never <code>null</code>.
	 * @param roleNames
	 *        The roles to list for {@link ToolReachability#NAMED_ROLES}, in the order to show them.
	 */
	public static String reachability(ToolReachability reachability, Collection<String> roleNames) {
		switch (reachability) {
			case EVERYONE:
				return resources().getString(I18NConstants.REACHABLE_EVERYONE);
			case NOBODY:
				return resources().getString(I18NConstants.REACHABLE_NOBODY);
			default:
				return String.join(", ", roleNames);
		}
	}

	/**
	 * The name of one of the three baseline states.
	 *
	 * @param choice
	 *        The state to name. Never <code>null</code>.
	 */
	public static ResKey baselineChoice(PolicyDraft.BaselineChoice choice) {
		switch (choice) {
			case DEFAULT:
				return I18NConstants.BASELINE_DEFAULT;
			case NOTHING:
				return I18NConstants.BASELINE_NOTHING;
			default:
				return I18NConstants.BASELINE_CUSTOM;
		}
	}

	/**
	 * The name of one of the three states of the background limit.
	 *
	 * @param choice
	 *        The state to name. Never <code>null</code>.
	 */
	public static ResKey backgroundChoice(PolicyDraft.BackgroundChoice choice) {
		switch (choice) {
			case UNDECLARED:
				return I18NConstants.BACKGROUND_UNDECLARED;
			case ALL:
				return I18NConstants.BACKGROUND_ALL;
			default:
				return I18NConstants.BACKGROUND_CUSTOM;
		}
	}

	/**
	 * What kind of role a name refers to, as the picker groups them.
	 *
	 * @param kind
	 *        The kind to name. Never <code>null</code>.
	 */
	public static ResKey roleKind(SecurityRoleKind kind) {
		switch (kind) {
			case FRAMEWORK:
				return I18NConstants.ROLE_KIND_FRAMEWORK;
			case INTERNAL:
				return I18NConstants.ROLE_KIND_INTERNAL;
			default:
				return I18NConstants.ROLE_KIND_APPLICATION;
		}
	}

	/**
	 * Where the group of a tool comes from, in words.
	 *
	 * @param source
	 *        The source. Never <code>null</code>.
	 */
	public static String groupSource(RoleToolAccess.GroupSource source) {
		switch (source) {
			case DECLARED:
				return resources().getString(I18NConstants.GROUP_SOURCE_DECLARED);
			case CONFIGURED:
				return resources().getString(I18NConstants.GROUP_SOURCE_CONFIGURED);
			default:
				return resources().getString(I18NConstants.GROUP_SOURCE_NONE);
		}
	}

	private static Resources resources() {
		return Resources.getInstance();
	}

}

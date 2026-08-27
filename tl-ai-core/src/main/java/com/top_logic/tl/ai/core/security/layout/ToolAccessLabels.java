/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
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
 * anywhere; "capped by the background allowlist - role projectPartner grants it, but a run nobody
 * watches may not" names the control to change and the grant that is already there.
 * </p>
 */
public class ToolAccessLabels {

	/** Separates a rule from what it overrode. */
	private static final String CLAUSE_SEPARATOR = " - ";

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
	 * "role projectPartner grants it" rather than "Grant of role projectPartner grants it".
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
	 */
	public static String verdict(boolean allowed) {
		return resources().getString(allowed ? I18NConstants.VERDICT_ALLOWED
			: I18NConstants.VERDICT_DENIED);
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
	 * <p>
	 * Three markers, because three different things about a group make an administrator want to look
	 * twice: it can express every other capability, it can send data out of the application, or it is
	 * held by everyone whether the policy says so or not.
	 * </p>
	 *
	 * @param group
	 *        The technical group name, may be <code>null</code>.
	 */
	public static String risk(String group) {
		if (group == null) {
			return "";
		}
		if (RoleToolAccess.IMPLICIT_BASELINE.contains(group)) {
			return resources().getString(I18NConstants.RISK_IMPLICIT);
		}
		if (ToolGroups.EGRESS.equals(group)) {
			return resources().getString(I18NConstants.RISK_EGRESS);
		}
		if (PRIVILEGED_GROUPS.contains(group)) {
			return resources().getString(I18NConstants.RISK_PRIVILEGED);
		}
		return "";
	}

	/**
	 * The groups whose JavaDoc in {@link ToolGroups} says "grant with care", plus the one that can
	 * grant every other.
	 *
	 * @see ToolGroups#SCRIPT
	 * @see ToolGroups#MODEL_STRUCTURE
	 * @see ToolGroups#AGENT_AUTHORING
	 */
	private static final Set<String> PRIVILEGED_GROUPS = Set.of(
		ToolGroups.SCRIPT, ToolGroups.MODEL_STRUCTURE, ToolGroups.AGENT_AUTHORING);

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

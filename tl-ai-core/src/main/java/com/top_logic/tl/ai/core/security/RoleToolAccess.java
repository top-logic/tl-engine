/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Decides whether the roles a user holds grant them a given tool.
 *
 * <p>
 * Evaluates a {@link ToolAccessPolicy} against a set of held role names. This is the role-based
 * half of the authorization; the other half - whether the user may touch the data the tool reaches
 * - is {@link ModelSecurity}. A tool call is permitted only when both allow.
 * </p>
 *
 * <p>
 * The decision, in order:
 * </p>
 * <ol>
 * <li>An explicit deny from <em>any</em> held role rejects the tool outright.</li>
 * <li>Every capability in {@link #IMPLICIT_BASELINE} is granted to every user without being
 * configured.</li>
 * <li>The baseline grants apply to every user - the configured ones, or {@link #DEFAULT_BASELINE}
 * when the policy declares no {@code <baseline>} at all.</li>
 * <li>The grants of all held roles are unioned.</li>
 * <li>Anything not reached by the above is denied.</li>
 * </ol>
 *
 * <p>
 * This class is pure policy evaluation: it neither reads the {@code KnowledgeBase} nor resolves
 * roles. The caller supplies the role names, which keeps the decision unit-testable and keeps the
 * question "which roles does this person hold on the security root" in one place.
 * </p>
 *
 * @see ToolAccessPolicy
 * @see ToolGroups
 */
public class RoleToolAccess {

	/**
	 * Resolves the group a tool declared for itself.
	 *
	 * <p>
	 * Java tools declare their group on {@code @ToolMethod}; this abstraction keeps
	 * {@link RoleToolAccess} independent of how that declaration is read, and lets tests supply a
	 * plain map. Policy-configured group membership ({@link ToolAccessPolicy#getToolGroups()}) takes
	 * precedence over the declared group.
	 * </p>
	 */
	@FunctionalInterface
	public interface DeclaredGroups {

		/** Never resolves a group; every tool relies on the policy's explicit assignment. */
		DeclaredGroups NONE = toolName -> null;

		/**
		 * The group the given tool declares for itself.
		 *
		 * @param qualifiedToolName
		 *        The qualified tool name.
		 * @return The declared group, or <code>null</code> if the tool declares none.
		 */
		String groupOf(String qualifiedToolName);
	}

	/**
	 * The outcome of a {@link RoleToolAccess#check(Collection, String) check}.
	 */
	public sealed interface Decision {

		/**
		 * The tool is granted.
		 *
		 * @param byRole
		 *        The role that granted it, or {@code "(baseline)"} for a baseline grant.
		 * @param viaGroup
		 *        The group through which it was granted, or <code>null</code> for a per-tool grant.
		 */
		record Granted(String byRole, String viaGroup) implements Decision {
			// Intentionally empty.
		}

		/**
		 * The tool is denied.
		 *
		 * @param reason
		 *        A message safe to feed back to the model: it names the missing capability, never
		 *        the data that was withheld.
		 */
		record Denied(String reason) implements Decision {
			// Intentionally empty.
		}
	}

	private static final String BASELINE_ROLE = "(baseline)";

	/**
	 * The capabilities every policy grants, whether or not it says so.
	 *
	 * <p>
	 * Two groups no administrator has to configure, for the same kind of reason: an agent that cannot
	 * do these is not restricted but broken, and a forgotten {@code <baseline>} must not be able to
	 * produce that state. They therefore reach every role - including a role that appears with an empty
	 * grant list, and a user with no role at all - and they reach unattended runs too, so that the same
	 * omission cannot silence a background run either.
	 * </p>
	 *
	 * <ul>
	 * <li>{@link ToolGroups#INTERACTION} - an agent that cannot ask a question is broken.</li>
	 * <li>{@link ToolGroups#INTROSPECTION} - an agent that cannot say what it carries answers a
	 * question about a named tool by guessing, and was observed blaming a capability that had nothing
	 * to do with it. Withholding that answer is what produced the wrong answer, so the answer must not
	 * be withholdable.</li>
	 * </ul>
	 *
	 * <p>
	 * An explicit deny still wins, because denies are evaluated first: an administrator who takes a
	 * capability away from a role has said so out loud, which is a different thing from never having
	 * mentioned it. Declaring either in {@code <baseline>} stays legal and is a no-op.
	 * </p>
	 */
	public static final Set<String> IMPLICIT_BASELINE =
		Set.of(ToolGroups.INTERACTION, ToolGroups.INTROSPECTION);

	/**
	 * The capabilities every user gets when the policy declares no {@code <baseline>} at all.
	 *
	 * <p>
	 * The point of this default is that an application which has configured nothing should look
	 * <em>unconfigured</em> rather than broken. Without it, the only capability left after #288 removed
	 * the permissive security mode is {@link #IMPLICIT_BASELINE} - the agent boots, answers questions,
	 * can say what it carries and can do nothing useful, which reads as a defect.
	 * </p>
	 *
	 * <p>
	 * Exactly two groups, and the list must stay exactly these two:
	 * </p>
	 * <ul>
	 * <li>{@link ToolGroups#MODEL_READ} - browse the meta-model: modules, types, attributes. No
	 * business data.</li>
	 * <li>{@link ToolGroups#DATA_READ} - read business instances.</li>
	 * </ul>
	 *
	 * <p>
	 * An application that writes a {@code <baseline>} replaces this entirely, and
	 * {@code <baseline/>} says "nothing beyond {@link #IMPLICIT_BASELINE}".
	 * </p>
	 *
	 * @see ToolAccessPolicy#getBaseline()
	 */
	/*
	 * Issue #293. What is excluded, and why, so that nobody widens this casually:
	 *
	 *   data.write, model.structure, agent.authoring, agent.meta, workspace
	 *       All of them mutate something. A default must not.
	 *   egress
	 *       Web fetch and the browser tools send data outward. A default that can exfiltrate is not a
	 *       read-only default.
	 *   script
	 *       TL-Script reaches every type; its enumeration results were the leak #289 closed; and
	 *       changeData is a path to a persisted write. Never a default.
	 *
	 * A word on why data.read is defensible here. The justification is that L1 grants the *ability to
	 * ask* and L2 decides per object what is actually visible - so an application with no
	 * <security-config> is answered "nothing" by the engine's deny-all rights, and one with a
	 * <security-config> gets exactly what it configured.
	 *
	 * That argument holds since #289/#295. The data tools do consult L2: every TLObject they render
	 * passes ReadFilter, which cannot be constructed without the run's ModelSecurity, and TidResolver
	 * is itself the read check - so InstanceBrowserTool's identities, counts and attribute values and
	 * a TL-Script result are all filtered per object and per attribute. What this default still does
	 * not promise is the two disclosures no output-boundary filter can reach: a cardinality TL-Script
	 * reduced to a number before the filter saw it (#291) and a label reached through string coercion
	 * (#296). Both are stated in doc/security/agent-security-architecture.md 3.3.2 and asserted in
	 * SecDemoReadFilterTest, and nothing in the documentation may claim more than that.
	 */
	public static final List<String> DEFAULT_BASELINE =
		List.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ);

	private final ToolAccessPolicy _policy;

	private final DeclaredGroups _declaredGroups;

	/** Policy-configured group membership: qualified tool name to group. */
	private final Map<String, String> _configuredGroups;

	/**
	 * Creates a {@link RoleToolAccess} for the given policy.
	 *
	 * @param policy
	 *        The administrator-authored policy to evaluate.
	 * @param declaredGroups
	 *        Resolves the group a tool declares for itself; use {@link DeclaredGroups#NONE} when
	 *        only the policy assigns groups.
	 */
	public RoleToolAccess(ToolAccessPolicy policy, DeclaredGroups declaredGroups) {
		_policy = policy;
		_declaredGroups = declaredGroups;
		_configuredGroups = indexConfiguredGroups(policy);
	}

	private static Map<String, String> indexConfiguredGroups(ToolAccessPolicy policy) {
		Map<String, String> result = new HashMap<>();
		for (Map.Entry<String, GroupMembership> entry : policy.getToolGroups().entrySet()) {
			for (String toolName : entry.getValue().getTools()) {
				result.put(toolName, entry.getKey());
			}
		}
		return result;
	}

	/**
	 * The group the given tool belongs to.
	 *
	 * <p>
	 * The policy's explicit assignment wins over the tool's own declaration, so an administrator can
	 * reclassify a tool (typically into {@link ToolGroups#EGRESS}) without touching its code.
	 * </p>
	 *
	 * @param qualifiedToolName
	 *        The qualified tool name.
	 * @return The group name, or <code>null</code> if the tool is ungrouped - in which case only an
	 *         explicit per-tool grant can reach it.
	 */
	public String groupOf(String qualifiedToolName) {
		return groupOf(new ToolIdentity(qualifiedToolName, null,
			_declaredGroups.groupOf(qualifiedToolName)));
	}

	/**
	 * The group the given tool belongs to.
	 *
	 * <p>
	 * The policy's explicit assignment wins over the tool's own declaration, so an administrator can
	 * reclassify a tool (typically into {@link ToolGroups#EGRESS}) without touching its code.
	 * </p>
	 *
	 * @param tool
	 *        The tool to classify.
	 * @return The group name, or <code>null</code> if the tool is ungrouped - in which case only an
	 *         explicit per-tool grant can reach it.
	 */
	public String groupOf(ToolIdentity tool) {
		for (String name : tool.names()) {
			String configured = _configuredGroups.get(name);
			if (configured != null) {
				return configured;
			}
		}
		return tool.declaredGroup();
	}

	/**
	 * Whether the roles held by a user grant them the given tool.
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>;
	 *        may be empty, in which case only the baseline applies.
	 * @param qualifiedToolName
	 *        The qualified name of the tool being checked; its group is resolved through the
	 *        {@link DeclaredGroups} this instance was created with.
	 * @return The decision, carrying the granting role or the denial reason.
	 */
	public Decision check(Collection<String> heldRoleNames, String qualifiedToolName) {
		return check(heldRoleNames, new ToolIdentity(qualifiedToolName, null,
			_declaredGroups.groupOf(qualifiedToolName)));
	}

	/**
	 * Whether the roles held by a user grant them the given tool.
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>;
	 *        may be empty, in which case only the baseline applies.
	 * @param tool
	 *        The tool being checked.
	 * @return The decision, carrying the granting role or the denial reason.
	 */
	public Decision check(Collection<String> heldRoleNames, ToolIdentity tool) {
		String group = groupOf(tool);
		String label = tool.displayName();
		ToolAccessRule rule = explain(heldRoleNames, tool).decidedBy();

		switch (rule.kind()) {
			case ROLE_DENY:
				return new Decision.Denied("Permission denied: tool '" + label
					+ "' is explicitly denied to your role '" + rule.roleName() + "'.");
			case IMPLICIT_BASELINE:
			case DEFAULT_BASELINE:
			case BASELINE:
				return new Decision.Granted(BASELINE_ROLE, group);
			case ROLE_GRANT:
				return new Decision.Granted(rule.roleName(), group);
			case UNCLASSIFIED:
				return new Decision.Denied("Permission denied: tool '" + label
					+ "' belongs to no tool group and has no explicit grant, so it is denied to everyone."
					+ " An administrator must assign it a group or grant it by name.");
			default:
				return new Decision.Denied("Permission denied: tool '" + label
					+ "' requires the '" + group + "' capability, which none of your roles grants.");
		}
	}

	/**
	 * The same decision as {@link #check(Collection, ToolIdentity)}, with the rule that made it.
	 *
	 * <p>
	 * The query behind the effective-permissions view: no side effect, no message, and the rule the
	 * administrator has to change if the answer is wrong. This is the implementation the enforcing
	 * {@link #check(Collection, ToolIdentity)} delegates to, so the two cannot drift apart - which is
	 * the whole reason the explanation is not a second evaluation written alongside the first.
	 * </p>
	 *
	 * <p>
	 * Answers about the policy alone. The administrator bypass and the ceiling an unattended run is
	 * under are properties of the <em>run</em>, not of the policy, and are composed on top by
	 * {@link EffectiveToolAccess}.
	 * </p>
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>; may
	 *        be empty, in which case only the baseline applies.
	 * @param tool
	 *        The tool being checked.
	 * @return The decision and its provenance, never <code>null</code>.
	 */
	public ToolAccessExplanation explain(Collection<String> heldRoleNames, ToolIdentity tool) {
		String group = groupOf(tool);
		List<String> roleNames = stableOrder(heldRoleNames);

		for (String roleName : roleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role == null) {
				continue;
			}
			Match deny = firstMatch(role.getDenies(), tool, group);
			if (deny != null) {
				// A deny is decided before anything else, so what it took away has to be searched for
				// afterwards: without it the sentence reads "denied" where an administrator needs
				// "denied although this grant is there".
				return ToolAccessExplanation.deniedBy(
					ToolAccessRule.roleDeny(roleName, deny.group(), deny.namedTool()),
					grantingRule(roleNames, tool, group));
			}
		}

		ToolAccessRule granting = grantingRule(roleNames, tool, group);
		if (granting != null) {
			return ToolAccessExplanation.allowedBy(granting);
		}
		if (group == null) {
			return ToolAccessExplanation.deniedBy(ToolAccessRule.unclassified());
		}
		return ToolAccessExplanation.deniedBy(ToolAccessRule.notGranted(group));
	}

	/**
	 * The rule that grants the given tool, ignoring every {@code <deny>}.
	 *
	 * <p>
	 * One of the two places the {@code <baseline>} element's three states are read, together with
	 * {@link #grantingCapabilityRule(List, String)}: a declared list is evaluated as written, and an
	 * absent one means {@link #DEFAULT_BASELINE}. An explicitly empty list is a declared list, so it
	 * grants nothing - which is what makes "no extra grants" sayable at all. The default names groups
	 * only, so an ungrouped tool is not reached by it - deliberately: a tool nobody classified must
	 * not become callable because nobody configured a baseline either.
	 * </p>
	 *
	 * @return The granting rule in the order the decision applies them - implicit baseline, baseline,
	 *         role grants - or <code>null</code> if nothing grants the tool.
	 */
	private ToolAccessRule grantingRule(List<String> roleNames, ToolIdentity tool, String group) {
		if (isImplicitBaseline(group)) {
			return ToolAccessRule.implicitBaseline(group);
		}
		Baseline declared = _policy.getBaseline();
		if (declared != null) {
			Match baseline = firstMatch(declared.getGrants(), tool, group);
			if (baseline != null) {
				return ToolAccessRule.baseline(baseline.group(), baseline.namedTool());
			}
		} else if (group != null && DEFAULT_BASELINE.contains(group)) {
			return ToolAccessRule.defaultBaseline(group);
		}
		for (String roleName : roleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role == null) {
				continue;
			}
			Match grant = firstMatch(role.getGrants(), tool, group);
			if (grant != null) {
				return ToolAccessRule.roleGrant(roleName, grant.group(), grant.namedTool());
			}
		}
		return null;
	}

	/**
	 * The same decision as {@link #checkCapability(Collection, String, String)}, with the rule that
	 * made it.
	 *
	 * <p>
	 * The capability-level counterpart of {@link #explain(Collection, ToolIdentity)}, and the
	 * implementation {@link #checkCapability(Collection, String, String)} delegates to. Like that
	 * method it reads {@code <grant group="...">} entries only, and like
	 * {@link #explain(Collection, ToolIdentity)} it says nothing about the run - including nothing
	 * about {@link #IMPLICIT_BASELINE}, which a policy grants per tool group and not as an operation's
	 * price. {@link EffectiveToolAccess#explainCapability(String)} is the one that answers for a whole
	 * group of tools.
	 * </p>
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root.
	 * @param capability
	 *        The name of the required capability (tool group).
	 * @return The decision and its provenance, never <code>null</code>.
	 */
	public ToolAccessExplanation explainCapability(Collection<String> heldRoleNames, String capability) {
		List<String> roleNames = stableOrder(heldRoleNames);

		for (String roleName : roleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role != null && grantsGroup(role.getDenies(), capability)) {
				return ToolAccessExplanation.deniedBy(
					ToolAccessRule.roleDeny(roleName, capability, null),
					grantingCapabilityRule(roleNames, capability));
			}
		}

		ToolAccessRule granting = grantingCapabilityRule(roleNames, capability);
		if (granting != null) {
			return ToolAccessExplanation.allowedBy(granting);
		}
		return ToolAccessExplanation.deniedBy(ToolAccessRule.notGranted(capability));
	}

	/**
	 * The rule that grants the given capability as such, ignoring every {@code <deny>}.
	 */
	private ToolAccessRule grantingCapabilityRule(List<String> roleNames, String capability) {
		Baseline declared = _policy.getBaseline();
		if (declared != null) {
			if (grantsGroup(declared.getGrants(), capability)) {
				return ToolAccessRule.baseline(capability, null);
			}
		} else if (DEFAULT_BASELINE.contains(capability)) {
			return ToolAccessRule.defaultBaseline(capability);
		}
		for (String roleName : roleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role != null && grantsGroup(role.getGrants(), capability)) {
				return ToolAccessRule.roleGrant(roleName, capability, null);
			}
		}
		return null;
	}

	/*
	 * The JavaDoc of this enum and of its constants is exported to META-INF/messages_*.properties by
	 * the build's doclet and shown to an administrator, so it stays short and free of XML element
	 * names, which the editor's safe-HTML renderer rejects as unknown tags.
	 *
	 * Cheap to compute and worth showing: without it a catalog cannot explain why removing one
	 * <tool-groups> entry de-classifies one tool and not the one next to it.
	 */

	/**
	 * Where the capability group of a tool comes from.
	 *
	 * @see #groupOf(ToolIdentity)
	 */
	public enum GroupSource {

		/** The tool states its own group, in its code. */
		DECLARED,

		/**
		 * A group assignment of the policy puts the tool into its group. Either the tool states none,
		 * or an administrator reclassified it.
		 */
		CONFIGURED,

		/** Neither: the tool belongs to no group and is therefore denied to everyone. */
		NONE;
	}

	/**
	 * Where the {@link #groupOf(ToolIdentity) group} of the given tool comes from.
	 *
	 * @param tool
	 *        The tool to classify.
	 * @return The source, never <code>null</code>.
	 */
	public GroupSource groupSourceOf(ToolIdentity tool) {
		for (String name : tool.names()) {
			if (_configuredGroups.containsKey(name)) {
				return GroupSource.CONFIGURED;
			}
		}
		return tool.declaredGroup() != null ? GroupSource.DECLARED : GroupSource.NONE;
	}

	/**
	 * The held roles in a defined order, so that two roles disagreeing produce the same sentence
	 * twice.
	 *
	 * <p>
	 * The roles arrive as a {@link java.util.Set} whose iteration order is a hash order, and a
	 * decision reports the <em>first</em> rule it finds. Sorting does not change which decisions are
	 * made - a deny wins whichever role wrote it - it only stops the explanation from naming a
	 * different role on the next request.
	 * </p>
	 */
	private static List<String> stableOrder(Collection<String> heldRoleNames) {
		return new ArrayList<>(new TreeSet<>(heldRoleNames));
	}

	/**
	 * Whether the roles held by a user grant them a capability as such, independently of any tool.
	 *
	 * <p>
	 * For an operation that is not a tool of its own but costs a capability anyway: a call of
	 * {@code changeData} that is no dry run persists arbitrary writes, so it requires
	 * {@link ToolGroups#DATA_WRITE} in addition to the {@link ToolGroups#SCRIPT} the tool itself is
	 * classified as. The argument that decides is known only inside the tool, which is why this
	 * question is asked separately from {@link #check(Collection, ToolIdentity)} and answered by the
	 * same rules.
	 * </p>
	 *
	 * <p>
	 * Only {@code <grant group="...">} entries are consulted, never per-tool grants: the question is
	 * about the capability itself, and a grant of one tool says nothing about a second capability that
	 * tool can reach through an argument.
	 * </p>
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>;
	 *        may be empty, in which case only the baseline applies.
	 * @param capability
	 *        The name of the required capability (tool group).
	 * @param operation
	 *        What the caller is about to do, for the denial message - e.g.
	 *        {@code "changing data with changeData"}.
	 * @return The decision, carrying the granting role or the denial reason.
	 */
	public Decision checkCapability(Collection<String> heldRoleNames, String capability, String operation) {
		ToolAccessRule rule = explainCapability(heldRoleNames, capability).decidedBy();

		switch (rule.kind()) {
			case ROLE_DENY:
				return new Decision.Denied("Permission denied: " + operation + " requires the '"
					+ capability + "' capability, which is explicitly denied to your role '"
					+ rule.roleName() + "'.");
			case DEFAULT_BASELINE:
			case BASELINE:
				return new Decision.Granted(BASELINE_ROLE, capability);
			case ROLE_GRANT:
				return new Decision.Granted(rule.roleName(), capability);
			default:
				return new Decision.Denied("Permission denied: " + operation + " requires the '"
					+ capability + "' capability, which none of your roles grants.");
		}
	}

	/**
	 * Whether a capability as such is reachable in a run that has no human to answer approval
	 * prompts.
	 *
	 * <p>
	 * The counterpart of {@link #allowedInBackground(ToolIdentity)} for
	 * {@link #checkCapability(Collection, String, String)}, and applied in addition to it.
	 * </p>
	 *
	 * @param capability
	 *        The name of the required capability (tool group).
	 * @return Whether the background allowlist admits the capability.
	 */
	public boolean capabilityAllowedInBackground(String capability) {
		BackgroundAllowlist allowlist = _policy.getBackgroundAllowlist();
		if (allowlist == null) {
			// An absent declaration admits nothing - see allowedInBackground(ToolIdentity).
			return false;
		}
		if (allowlist.isAll()) {
			return true;
		}
		return grantsGroup(allowlist.getGrants(), capability);
	}

	/**
	 * The capability names this policy could decide about.
	 *
	 * <p>
	 * The groups the framework ships ({@link ToolGroups#ALL}) plus the ones the policy itself assigns
	 * tools to. Group names are an open string space, so this is a list of what is <em>known</em>
	 * rather than of what is legal - the same reading {@link AllToolGroups} takes. A group a tool
	 * declares for itself and no policy mentions is not in it, because {@link DeclaredGroups} answers
	 * per tool name and cannot be enumerated.
	 * </p>
	 *
	 * <p>
	 * Used to answer "what does this ceiling withhold?" - a question that needs the candidate set,
	 * unlike every decision above, which is asked about one named group.
	 * </p>
	 *
	 * @return The known capability names, in a stable order.
	 */
	public Set<String> knownCapabilities() {
		Set<String> result = new TreeSet<>(ToolGroups.ALL);
		result.addAll(_policy.getToolGroups().keySet());
		return result;
	}

	/**
	 * Whether any entry of {@code entries} names the given group.
	 */
	private static boolean grantsGroup(Collection<Grant> entries, String group) {
		for (Grant entry : entries) {
			if (!isBlank(entry.getGroup()) && entry.getGroup().equals(group)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given tool is reachable in a run that has no human to answer approval prompts.
	 *
	 * <p>
	 * Applied <em>in addition</em> to {@link #check(Collection, ToolIdentity)}: a background run gets
	 * the intersection of the user's grants and the allowlist.
	 * </p>
	 *
	 * @param qualifiedToolName
	 *        The qualified name of the tool being checked.
	 * @return Whether the background allowlist admits the tool.
	 */
	public boolean allowedInBackground(String qualifiedToolName) {
		return allowedInBackground(new ToolIdentity(qualifiedToolName, null,
			_declaredGroups.groupOf(qualifiedToolName)));
	}

	/**
	 * Whether the given tool is reachable in a run that has no human to answer approval prompts.
	 *
	 * <p>
	 * Applied <em>in addition</em> to {@link #check(Collection, ToolIdentity)}: a background run gets
	 * the intersection of the user's grants and the allowlist.
	 * </p>
	 *
	 * <p>
	 * <b>An absent declaration admits nothing beyond {@link #IMPLICIT_BASELINE}.</b> The allowlist is
	 * the compensating control for the approval prompt an unattended run cannot show, so "the
	 * administrator said nothing" must not be the state that allows everything - a policy that enables
	 * enforcement and forgets the allowlist would otherwise hand unattended runs the user's full grant
	 * set, auto-approved. {@link AiToolAccessService} refuses such a policy at boot; this method is the
	 * same rule at the decision point, for the policies that never pass through the service.
	 * </p>
	 *
	 * @param tool
	 *        The tool being checked.
	 * @return Whether the background allowlist admits the tool.
	 */
	public boolean allowedInBackground(ToolIdentity tool) {
		String group = groupOf(tool);
		if (isImplicitBaseline(group)) {
			// The implicit grant is not a capability an allowlist rations: it exists so that no
			// omission can leave a run unable to talk to anyone, and an unattended run is exactly
			// the kind of run an omission is easy to make about.
			return true;
		}
		BackgroundAllowlist allowlist = _policy.getBackgroundAllowlist();
		if (allowlist == null) {
			return false;
		}
		if (allowlist.isAll()) {
			// The explicit opt-out: unattended runs are bounded only by the user's own grants.
			return true;
		}
		return matchesAny(allowlist.getGrants(), tool, group);
	}

	/**
	 * Whether the given group is granted to everyone without being configured.
	 *
	 * @param group
	 *        The group a tool belongs to, may be <code>null</code>.
	 * @return Whether {@link #IMPLICIT_BASELINE} covers it.
	 */
	private static boolean isImplicitBaseline(String group) {
		return group != null && IMPLICIT_BASELINE.contains(group);
	}

	/**
	 * The capabilities the baseline grants every user under this policy, for a boot log or a
	 * diagnostic.
	 *
	 * @return {@link #DEFAULT_BASELINE} when the policy declares no {@code <baseline>}, otherwise the
	 *         groups it lists; never <code>null</code>.
	 */
	public List<String> baselineGroups() {
		Baseline declared = _policy.getBaseline();
		if (declared == null) {
			return DEFAULT_BASELINE;
		}
		List<String> groups = new ArrayList<>();
		for (Grant grant : declared.getGrants()) {
			String group = grant.getGroup();
			if (group != null && !group.isBlank()) {
				groups.add(group);
			}
		}
		return groups;
	}

	/**
	 * Whether the policy leaves the baseline to {@link #DEFAULT_BASELINE}.
	 *
	 * <p>
	 * Told apart from a declared baseline so that the boot log can say which of the two an
	 * administrator is looking at - {@code baseline=default} and {@code baseline=0} mean very different
	 * things and used to be spelled the same.
	 * </p>
	 */
	public boolean usesDefaultBaseline() {
		return _policy.getBaseline() == null;
	}

	/**
	 * Whether any entry of {@code entries} covers the given tool.
	 *
	 * @see #firstMatch(Collection, ToolIdentity, String)
	 */
	private static boolean matchesAny(Collection<Grant> entries, ToolIdentity tool, String group) {
		return firstMatch(entries, tool, group) != null;
	}

	/**
	 * How the first entry of {@code entries} that covers the given tool covers it.
	 *
	 * <p>
	 * A per-tool entry has to name the tool, by any one of {@link ToolIdentity#names() its names}.
	 * There is no pattern syntax: a tool of any origin - a new Java tool, a TL-Script tool authored in
	 * the running application, a tool a connected MCP server grew - is denied until an administrator
	 * says otherwise.
	 * </p>
	 *
	 * @return The match, or <code>null</code> if no entry covers the tool.
	 */
	private static Match firstMatch(Collection<Grant> entries, ToolIdentity tool, String group) {
		List<String> names = tool.names();
		for (Grant entry : entries) {
			if (!isBlank(entry.getGroup()) && entry.getGroup().equals(group)) {
				return new Match(group, null);
			}
			for (String granted : entry.getTools()) {
				if (names.contains(granted)) {
					return new Match(null, granted);
				}
			}
		}
		return null;
	}

	/**
	 * What an entry of the policy named to cover a tool: exactly one of the two, which is the rule
	 * {@code AiToolAccessService.underspecifiedEntries} enforces.
	 *
	 * @param group
	 *        The capability group the entry named, or <code>null</code> if it named the tool.
	 * @param namedTool
	 *        The tool name the entry spelled out, or <code>null</code> if it named the group.
	 */
	private record Match(String group, String namedTool) {
		// Intentionally empty.
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

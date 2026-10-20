/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.RolePolicy;
import com.top_logic.util.TLContext;

/*
 * Issue #347. The resolution seam, deliberately shaped as "account in, policy out" so that #381's
 * RunPrincipal (account plus entry point) can be handed to it unchanged once that lands: an entry
 * point is not a dimension of this policy, and adding one later needs no caller to change.
 *
 * Why there is no administrator bypass here, although the role-based tool access of #392 has one.
 * That layer answers "is this account trusted to use this capability", and an administrator already
 * bypasses the engine's own rights, so a tool layer that disagreed would produce an unexplainable
 * half-denial. This layer answers a different question - "may this installation send data to that
 * origin" - which is a statement about the destination, not about the account. The browser tool's
 * older allowed-domains/blocked-domains bind an administrator too, and a governance layer an
 * administrator's session silently escapes would be worth very little.
 */

/**
 * The browsing policies an application has configured, and the resolution of an account to the one
 * that binds its runs.
 *
 * <p>
 * Built once at service start from {@link BrowsingPolicyConfig} and then immutable. An application
 * that configures nothing gets {@link #unrestricted()}, whose every answer is "permitted", so the
 * layer costs a null check and changes no behaviour.
 * </p>
 *
 * @see BrowsingPolicy
 * @see WebToolsService#browsingPolicies()
 */
public final class BrowsingPolicies {

	/** The policies of an application that configured none. */
	private static final BrowsingPolicies UNRESTRICTED =
		new BrowsingPolicies(Map.of(), Map.of(), BrowsingPolicy.unrestricted());

	private final Map<String, BrowsingPolicy> _byName;

	private final Map<String, String> _policyByRole;

	private final BrowsingPolicy _fallback;

	private BrowsingPolicies(Map<String, BrowsingPolicy> byName, Map<String, String> policyByRole,
			BrowsingPolicy fallback) {
		_byName = byName;
		_policyByRole = policyByRole;
		_fallback = fallback;
	}

	/** The policies of an application that configured none: every origin the SSRF check permits. */
	public static BrowsingPolicies unrestricted() {
		return UNRESTRICTED;
	}

	/**
	 * Resolves a configured section into the immutable policies it describes.
	 *
	 * @param config
	 *        The configured section, may be {@code null}.
	 * @return The resolved policies; never {@code null}.
	 * @throws IllegalArgumentException
	 *         If the configuration names a policy that does not exist. Callers turn that into the
	 *         boot error it is, rather than starting with a policy nobody wrote.
	 */
	public static BrowsingPolicies of(BrowsingPolicyConfig config) {
		if (config == null) {
			return UNRESTRICTED;
		}
		List<String> problems = problems(config);
		if (!problems.isEmpty()) {
			throw new IllegalArgumentException(String.join(" ", problems));
		}
		Map<String, BrowsingPolicy> byName = new LinkedHashMap<>();
		for (Map.Entry<String, BrowsingPolicyConfig.Policy> entry : config.getPolicies().entrySet()) {
			byName.put(entry.getKey(), BrowsingPolicy.of(entry.getValue()));
		}
		Map<String, String> policyByRole = new LinkedHashMap<>();
		for (Map.Entry<String, RolePolicy> entry : config.getRoles().entrySet()) {
			policyByRole.put(entry.getKey(), entry.getValue().getPolicy());
		}
		if (byName.isEmpty() && policyByRole.isEmpty() && config.getDefault() == null) {
			return UNRESTRICTED;
		}
		String fallbackName = config.getDefault();
		BrowsingPolicy fallback =
			fallbackName == null ? BrowsingPolicy.unrestricted() : byName.get(fallbackName);
		return new BrowsingPolicies(Map.copyOf(byName), Map.copyOf(policyByRole), fallback);
	}

	/**
	 * What is wrong with {@code config}, as sentences naming the offending entry.
	 *
	 * <p>
	 * A policy name that does not exist is the mistake that matters: silently falling back to
	 * "unrestricted" would hand out the opposite of what was written, so it is refused at boot.
	 * </p>
	 *
	 * @param config
	 *        The configured section, may be {@code null}.
	 * @return The problems, empty when the configuration is sound.
	 */
	public static List<String> problems(BrowsingPolicyConfig config) {
		List<String> problems = new ArrayList<>();
		if (config == null) {
			return problems;
		}
		Set<String> known = config.getPolicies().keySet();
		String fallback = config.getDefault();
		if (fallback != null && !known.contains(fallback)) {
			problems.add("The default browsing policy '" + fallback + "' is not declared; declared are "
				+ describe(known) + ".");
		}
		for (Map.Entry<String, RolePolicy> entry : config.getRoles().entrySet()) {
			String name = entry.getValue().getPolicy();
			if (!known.contains(name)) {
				problems.add("The role '" + entry.getKey() + "' browses under the policy '" + name
					+ "', which is not declared; declared are " + describe(known) + ".");
			}
		}
		for (Map.Entry<String, BrowsingPolicyConfig.Policy> entry : config.getPolicies().entrySet()) {
			for (BrowsingPolicyConfig.OriginRule origin : entry.getValue().getOrigins()) {
				String host = UrlSafety.normalizeHost(origin.getHost());
				if (host == null || host.isEmpty() || host.equals("*.")) {
					problems.add("The browsing policy '" + entry.getKey()
						+ "' has an origin rule that names no host.");
				}
			}
		}
		return problems;
	}

	private static String describe(Collection<String> names) {
		return names.isEmpty() ? "none" : String.join(", ", new TreeSet<>(names));
	}

	/** Whether nothing is configured, so no origin check has to be made at all. */
	public boolean isUnrestricted() {
		return _byName.isEmpty() && _fallback.isUnrestricted();
	}

	/**
	 * The policy binding a run of the given account.
	 *
	 * @param account
	 *        The acting account, may be {@code null} for a run with no user, which is bound by the
	 *        default policy.
	 * @return The policy; never {@code null}.
	 */
	public BrowsingPolicy forAccount(Person account) {
		if (isUnrestricted()) {
			return BrowsingPolicy.unrestricted();
		}
		return forRoles(AiToolAccessService.heldRoleNames(account));
	}

	/**
	 * The policy binding a run whose account holds the given roles.
	 *
	 * <p>
	 * Every role that names a policy contributes it, and all of them bind the run at once, so one
	 * more role can only ever narrow what it reaches. A run whose roles name none is bound by the
	 * default policy.
	 * </p>
	 *
	 * @param roleNames
	 *        The roles the account holds on the security root; may be empty.
	 * @return The policy; never {@code null}.
	 */
	public BrowsingPolicy forRoles(Collection<String> roleNames) {
		if (isUnrestricted()) {
			return BrowsingPolicy.unrestricted();
		}
		// Collected by POLICY name and sorted, not by role: two accounts bound to the same policies
		// must resolve to the same BrowsingPolicy#name(), because that name is what the browser's
		// filtering proxy is keyed by. A name that varied with the iteration order of a role set would
		// start one proxy per account instead of one per policy.
		Set<String> policyNames = new TreeSet<>();
		for (String roleName : roleNames) {
			String policyName = _policyByRole.get(roleName);
			if (policyName != null) {
				policyNames.add(policyName);
			}
		}
		if (policyNames.isEmpty()) {
			return _fallback;
		}
		List<BrowsingPolicy> parts = new ArrayList<>();
		for (String policyName : policyNames) {
			BrowsingPolicy policy = _byName.get(policyName);
			if (policy != null) {
				parts.add(policy);
			}
		}
		return BrowsingPolicy.all(parts);
	}

	/**
	 * The policy of the given name, or {@code null} when none is declared under it.
	 *
	 * @param name
	 *        The configured policy name.
	 */
	public BrowsingPolicy byName(String name) {
		return _byName.get(name);
	}

	/** What the boot log says about this configuration. */
	public String describe() {
		if (isUnrestricted()) {
			return "no browsing policy configured; every origin that passes the SSRF check is reachable";
		}
		StringBuilder out = new StringBuilder();
		out.append(_byName.size()).append(" browsing policy/policies (");
		out.append(describe(_byName.keySet()));
		out.append("), ").append(_policyByRole.size()).append(" role(s) bound, default: ");
		out.append(_fallback.describeAllowed());
		return out.toString();
	}

	/**
	 * Records a refused origin where an operator can find it, then returns the reason unchanged.
	 *
	 * <p>
	 * At INFO rather than WARN: an agent asked to research a topic will walk into an off-policy link
	 * in the ordinary course of its work, and a governance layer whose normal operation fills the
	 * warning log teaches operators to stop reading it. The line names the account, the roles it
	 * holds, the policy and the origin - which is what answers "who was refused where?" - and never
	 * the page or the query.
	 * </p>
	 *
	 * @param policy
	 *        The policy that refused.
	 * @param origin
	 *        The host that was refused.
	 * @param reason
	 *        The refusal as the model is told it.
	 * @return {@code reason}, unchanged.
	 */
	public static String audit(BrowsingPolicy policy, String origin, String reason) {
		Person account = currentAccountQuietly();
		Set<String> roles = AiToolAccessService.heldRoleNames(account);
		Logger.info("Browsing refused: origin='" + origin + "'"
			+ " user=" + (account == null ? "(none)" : account.getName())
			+ " roles=" + new TreeSet<>(roles)
			+ " policy=" + (policy == null ? "(none)" : policy.name())
			+ " reason=" + reason, BrowsingPolicies.class);
		return reason;
	}

	/**
	 * The account of the running thread, or {@code null} when there is none or it cannot be read.
	 *
	 * <p>
	 * Quiet on purpose: this is reached from a refusal path, and an audit line that threw would turn
	 * a refusal into an error.
	 * </p>
	 */
	public static Person currentAccountQuietly() {
		try {
			TLContext context = TLContext.getContext();
			return context == null ? null : context.getPerson();
		} catch (RuntimeException ex) {
			return null;
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.Access;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.DownloadAccess;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.OriginRule;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.Policy;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.RolePolicy;

/**
 * Tests for the browsing governance model of issue #347: the configured origin policy, how a role
 * resolves to one, and what it answers about a host.
 *
 * <p>
 * Pure: no browser, no network, no knowledge base. The decision this layer makes is a function of
 * the configuration and a host name, and it is the decision - not the machinery that carries it -
 * that has to be pinned, because an origin policy that is wrong about one host is wrong everywhere
 * that host appears.
 * </p>
 */
public class TestBrowsingPolicy extends TestCase {

	/**
	 * The typed configuration items resolve nested config types through the {@code TypeIndex}, which
	 * needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestBrowsingPolicy.class)));
	}

	/** An application that configures nothing browses exactly as it did before this layer existed. */
	public void testNothingConfiguredChangesNothing() {
		BrowsingPolicies policies = BrowsingPolicies.of(newConfig());

		assertTrue(policies.isUnrestricted());
		BrowsingPolicy policy = policies.forRoles(Set.of("AnyRole"));
		assertTrue(policy.isUnrestricted());
		assertNull(policy.refuseAccess("example.com"));
		assertNull(policy.refuseDownload("example.com"));
		assertFalse(policy.bounded());
	}

	/** A missing service is the same as an unconfigured one, so a tool never has to special-case it. */
	public void testNoServiceIsUnrestricted() {
		assertTrue(WebToolsService.policyFor(null).isUnrestricted());
		assertTrue(WebToolsService.policyForRoles(Set.of("AnyRole")).isUnrestricted());
	}

	/** A deny-by-default policy reaches the origins it names, and nothing else. */
	public void testDenyByDefaultReachesOnlyTheNamedOrigins() {
		BrowsingPolicy policy = BrowsingPolicy.of(
			policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));

		assertNull(policy.refuseAccess("docs.oracle.com"));
		assertNotNull(policy.refuseAccess("oracle.com"));
		assertNotNull(policy.refuseAccess("evil.example.com"));
		assertTrue(policy.bounded());
		assertEquals(List.of("docs.oracle.com"), policy.allowedSearchDomains());
	}

	/** An allow-by-default policy reaches everything but the origins it rules out. */
	public void testAllowByDefaultRefusesOnlyTheNamedOrigins() {
		BrowsingPolicy policy = BrowsingPolicy.of(
			policy("open", Access.ALLOW, origin("pastebin.com", Access.DENY)));

		assertNull(policy.refuseAccess("example.com"));
		assertNotNull(policy.refuseAccess("pastebin.com"));
		assertFalse(policy.bounded());
		assertEquals(List.of(), policy.allowedSearchDomains());
		assertEquals(List.of("pastebin.com"), policy.blockedSearchDomains());
	}

	/**
	 * An exact host is that host and nothing else; a wildcard is the sub-domains and not the apex.
	 *
	 * <p>
	 * Deliberately different from the browser tool's older {@code allowed-domains}, where an entry
	 * always covers the sub-domains too. A policy written to carve narrow exceptions out of a
	 * deny-by-default needs a form that means "this host only", and a syntax with one form has none.
	 * </p>
	 */
	public void testHostPatterns() {
		BrowsingPolicy exact = BrowsingPolicy.of(
			policy("exact", Access.DENY, origin("example.com", Access.ALLOW)));
		assertNull(exact.refuseAccess("example.com"));
		assertNotNull("a sub-domain is not the host itself", exact.refuseAccess("www.example.com"));

		BrowsingPolicy wildcard = BrowsingPolicy.of(
			policy("wildcard", Access.DENY, origin("*.example.com", Access.ALLOW)));
		assertNull(wildcard.refuseAccess("www.example.com"));
		assertNull(wildcard.refuseAccess("a.b.example.com"));
		assertNotNull("the apex is not a sub-domain of itself", wildcard.refuseAccess("example.com"));
		assertNotNull("a suffix that is not a label boundary never matches",
			wildcard.refuseAccess("notexample.com"));
	}

	/**
	 * The spellings that slip past a naive host comparison are judged as the host they are.
	 *
	 * @see UrlSafety#normalizeHost(String)
	 */
	public void testHostSpellingsAreNormalised() {
		BrowsingPolicy policy = BrowsingPolicy.of(
			policy("blocklist", Access.ALLOW, origin("Blocked.Example.COM", Access.DENY)));

		assertNotNull(policy.refuseAccess("blocked.example.com"));
		assertNotNull("the fully qualified spelling is the same host",
			policy.refuseAccess("blocked.example.com."));
		assertNotNull("case is not a way past a rule", policy.refuseAccess("BLOCKED.EXAMPLE.COM"));
	}

	/** Deny beats allow, whatever order the rules are written in. */
	public void testDenyBeatsAllow() {
		BrowsingPolicy allowFirst = BrowsingPolicy.of(policy("p", Access.DENY,
			origin("*.example.com", Access.ALLOW), origin("secrets.example.com", Access.DENY)));
		BrowsingPolicy denyFirst = BrowsingPolicy.of(policy("p", Access.DENY,
			origin("secrets.example.com", Access.DENY), origin("*.example.com", Access.ALLOW)));

		assertNotNull(allowFirst.refuseAccess("secrets.example.com"));
		assertNotNull(denyFirst.refuseAccess("secrets.example.com"));
		assertNull(allowFirst.refuseAccess("docs.example.com"));
		assertNull(denyFirst.refuseAccess("docs.example.com"));
	}

	/** Downloads are the second axis: an origin may be readable without being allowed to hand over files. */
	public void testDownloadsAreASecondAxis() {
		OriginRule readOnly = origin("docs.example.com", Access.ALLOW);
		readOnly.setDownloads(DownloadAccess.DENY);
		BrowsingPolicy policy = BrowsingPolicy.of(policy("p", Access.DENY, readOnly));

		assertNull("the page is readable", policy.refuseAccess("docs.example.com"));
		assertNotNull("but it may not hand over a file", policy.refuseDownload("docs.example.com"));
	}

	/** An origin that may not be reached may not download either, whatever the download axis says. */
	public void testARefusedOriginNeverDownloads() {
		OriginRule contradictory = origin("evil.example.com", Access.DENY);
		contradictory.setDownloads(DownloadAccess.ALLOW);
		BrowsingPolicy policy = BrowsingPolicy.of(policy("p", Access.ALLOW, contradictory));

		assertNotNull(policy.refuseAccess("evil.example.com"));
		assertNotNull(policy.refuseDownload("evil.example.com"));
	}

	/** Left out, the download axis follows the access axis at both levels. */
	public void testDownloadsFollowAccessWhenUnset() {
		BrowsingPolicy denyAll = BrowsingPolicy.of(policy("deny-all", Access.DENY));
		assertNotNull(denyAll.refuseDownload("example.com"));

		BrowsingPolicy allowAll = BrowsingPolicy.of(policy("allow-all", Access.ALLOW));
		assertNull(allowAll.refuseDownload("example.com"));
	}

	/** A policy may forbid downloads everywhere while still letting its origins be read. */
	public void testAPolicyCanReadEverywhereAndDownloadNowhere() {
		Policy config = policy("read-only", Access.ALLOW);
		config.setDownloads(DownloadAccess.DENY);
		BrowsingPolicy policy = BrowsingPolicy.of(config);

		assertNull(policy.refuseAccess("example.com"));
		assertNotNull(policy.refuseDownload("example.com"));
	}

	/** A role browses under the policy it names; a role that names none browses under the default. */
	public void testRoleResolution() {
		BrowsingPolicyConfig config = newConfig();
		config.getPolicies().put("open", policy("open", Access.ALLOW));
		config.getPolicies().put("research", policy("research", Access.DENY,
			origin("docs.oracle.com", Access.ALLOW)));
		config.setDefault("research");
		config.getRoles().put("SalesUser", role("SalesUser", "open"));

		BrowsingPolicies policies = BrowsingPolicies.of(config);
		assertFalse(policies.isUnrestricted());

		assertNull("the named role browses under its own policy",
			policies.forRoles(Set.of("SalesUser")).refuseAccess("example.com"));
		assertNotNull("everybody else is bound by the default",
			policies.forRoles(Set.of("OtherRole")).refuseAccess("example.com"));
		assertNotNull("a run with no account at all is bound by the default",
			policies.forRoles(Set.of()).refuseAccess("example.com"));
	}

	/** Holding one more role can only narrow what a run reaches, never widen it. */
	public void testSeveralRolesBindAtOnce() {
		BrowsingPolicyConfig config = newConfig();
		config.getPolicies().put("open", policy("open", Access.ALLOW));
		config.getPolicies().put("research",
			policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));
		config.getRoles().put("SalesUser", role("SalesUser", "open"));
		config.getRoles().put("Researcher", role("Researcher", "research"));

		BrowsingPolicy both = BrowsingPolicies.of(config).forRoles(Set.of("SalesUser", "Researcher"));

		assertNotNull("the narrower of the two still binds", both.refuseAccess("example.com"));
		assertNull("what both permit stays reachable", both.refuseAccess("docs.oracle.com"));
	}

	/** The refusal says whose policy refused and where the run may go instead. */
	public void testTheRefusalIsActionable() {
		BrowsingPolicy policy = BrowsingPolicy.of(
			policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));

		String refusal = policy.refuseAccess("evil.example.com");
		assertTrue(refusal, refusal.contains("evil.example.com"));
		assertTrue(refusal, refusal.contains("not allowed for your role"));
		assertTrue("it names where the run MAY go", refusal.contains("docs.oracle.com"));
		assertTrue("it names the policy an operator has to look at", refusal.contains("research"));
	}

	/** A policy name nobody declared is a boot error, not a silent fall back to "everything". */
	public void testAnUndeclaredPolicyNameIsRefused() {
		BrowsingPolicyConfig config = newConfig();
		config.getPolicies().put("open", policy("open", Access.ALLOW));
		config.getRoles().put("SalesUser", role("SalesUser", "opne"));

		List<String> problems = BrowsingPolicies.problems(config);
		assertEquals(problems.toString(), 1, problems.size());
		assertTrue(problems.get(0), problems.get(0).contains("opne"));
		try {
			BrowsingPolicies.of(config);
			fail("A role bound to a policy that does not exist must not start.");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("opne"));
		}
	}

	/** An undeclared default is refused for the same reason. */
	public void testAnUndeclaredDefaultIsRefused() {
		BrowsingPolicyConfig config = newConfig();
		config.getPolicies().put("open", policy("open", Access.ALLOW));
		config.setDefault("closed");

		assertEquals(1, BrowsingPolicies.problems(config).size());
	}

	/**
	 * The XML an administrator writes parses into the policy this class evaluates.
	 *
	 * <p>
	 * The shape is part of the contract - it is what the documentation shows - so it is asserted
	 * rather than only exercised by a running application. It also pins that a {@code downloads}
	 * attribute left out means "follow access" rather than a value of its own.
	 * </p>
	 */
	public void testConfigurationSyntax() throws Exception {
		String xml = "<browsing config:interface='" + BrowsingPolicyConfig.class.getName() + "'"
			+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " default='research'>"
			+ "  <policies>"
			+ "    <policy name='open'/>"
			+ "    <policy name='research' access='deny'>"
			+ "      <origins>"
			+ "        <origin host='docs.oracle.com'/>"
			+ "        <origin host='*.top-logic.com' downloads='deny'/>"
			+ "      </origins>"
			+ "    </policy>"
			+ "  </policies>"
			+ "  <roles>"
			+ "    <role name='SalesAssistantUser' policy='open'/>"
			+ "  </roles>"
			+ "</browsing>";

		ConfigurationItem parsed = TypedConfiguration.parse(CharacterContents.newContent(xml));
		BrowsingPolicyConfig config = (BrowsingPolicyConfig) parsed;

		assertEquals("research", config.getDefault());
		assertEquals(Access.ALLOW, config.getPolicies().get("open").getAccess());
		assertEquals(Access.DENY, config.getPolicies().get("research").getAccess());
		assertEquals("an unstated downloads axis follows access", DownloadAccess.AS_ACCESS,
			config.getPolicies().get("research").getDownloads());
		assertEquals(2, config.getPolicies().get("research").getOrigins().size());

		BrowsingPolicies policies = BrowsingPolicies.of(config);
		BrowsingPolicy sales = policies.forRoles(Set.of("SalesAssistantUser"));
		assertNull(sales.refuseAccess("example.com"));

		BrowsingPolicy others = policies.forRoles(Set.of());
		assertNull(others.refuseAccess("docs.oracle.com"));
		assertNull(others.refuseDownload("docs.oracle.com"));
		assertNull(others.refuseAccess("www.top-logic.com"));
		assertNotNull("the wildcard rule reads but does not download",
			others.refuseDownload("www.top-logic.com"));
		assertNotNull(others.refuseAccess("example.com"));
	}

	private static BrowsingPolicyConfig newConfig() {
		return TypedConfiguration.newConfigItem(BrowsingPolicyConfig.class);
	}

	/** A policy with the given default and rules. Package-visible: the enforcement tests share it. */
	static Policy policy(String name, Access access, OriginRule... origins) {
		Policy policy = TypedConfiguration.newConfigItem(Policy.class);
		policy.setName(name);
		policy.setAccess(access);
		for (OriginRule origin : origins) {
			policy.getOrigins().add(origin);
		}
		return policy;
	}

	/** One origin rule. Package-visible: the enforcement tests share it. */
	static OriginRule origin(String host, Access access) {
		OriginRule origin = TypedConfiguration.newConfigItem(OriginRule.class);
		origin.setHost(host);
		origin.setAccess(access);
		return origin;
	}

	private static RolePolicy role(String name, String policyName) {
		RolePolicy role = TypedConfiguration.newConfigItem(RolePolicy.class);
		role.setName(name);
		role.setPolicy(policyName);
		return role;
	}
}

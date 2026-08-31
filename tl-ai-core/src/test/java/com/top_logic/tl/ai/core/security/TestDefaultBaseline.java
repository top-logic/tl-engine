/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.RoleToolAccess.Decision;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Tests the read-only default baseline an application gets when it configures nothing (issue #293).
 *
 * <p>
 * The three states of the {@code <baseline>} element are what this is about, because they are easy to
 * conflate and only two of them are reachable by accident:
 * </p>
 * <ul>
 * <li><b>absent</b> - {@link RoleToolAccess#DEFAULT_BASELINE}, so an unconfigured application has a
 * usable, read-only agent rather than one that can do nothing.</li>
 * <li><b>empty</b> - nothing beyond {@link RoleToolAccess#IMPLICIT_BASELINE}. An explicit "grant
 * nobody anything", which the {@code secdemo} fixture depends on.</li>
 * <li><b>declared</b> - exactly what is listed, replacing the default entirely.</li>
 * </ul>
 *
 * <p>
 * What this deliberately does <b>not</b> assert is that a default {@code data.read} is safe. It is
 * not, yet: the data tools do not consult the model-access layer, so per-object read filtering arrives
 * with issue #289. {@link #testTheDefaultIsReadOnly()} pins the one property that does hold today -
 * that nothing in the default can change or export anything.
 * </p>
 */
public class TestDefaultBaseline extends TestCase {

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestDefaultBaseline.class)));
	}

	/** The default is exactly the two read groups - a change here is a change of security posture. */
	public void testTheDefaultIsExactlyTwoReadGroups() {
		assertEquals(List.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ),
			RoleToolAccess.DEFAULT_BASELINE);
	}

	/**
	 * Nothing in the default mutates, exports or reaches every type.
	 *
	 * <p>
	 * The predicate a reviewer of a future widening has to break: {@code data.write},
	 * {@code model.structure}, {@code agent.authoring}, {@code agent.meta} and {@code workspace} all
	 * change something; {@code egress} sends data outward; {@code script} reaches every type and can
	 * persist a write through its own argument.
	 * </p>
	 */
	public void testTheDefaultIsReadOnly() {
		for (String group : List.of(ToolGroups.DATA_WRITE, ToolGroups.MODEL_STRUCTURE,
			ToolGroups.AGENT_AUTHORING, ToolGroups.AGENT_META, ToolGroups.WORKSPACE,
			ToolGroups.EGRESS, ToolGroups.SCRIPT)) {
			assertFalse("The default baseline must not carry '" + group + "'.",
				RoleToolAccess.DEFAULT_BASELINE.contains(group));
		}
	}

	/** An application that configures nothing can read the model and the data, and nothing else. */
	public void testUnconfiguredApplicationCanRead() {
		RoleToolAccess access = access(unconfigured());

		assertGranted(access, "model-browser.listModules", ToolGroups.MODEL_READ);
		assertGranted(access, "instance-browser.listInstances", ToolGroups.DATA_READ);
		assertDenied(access, "instance-browser.createInstance", ToolGroups.DATA_WRITE);
		assertDenied(access, "script.executeTLScript", ToolGroups.SCRIPT);
		assertDenied(access, "web.webFetch", ToolGroups.EGRESS);
		assertDenied(access, "model-editor.createClass", ToolGroups.MODEL_STRUCTURE);
	}

	/** The grant is attributed to the baseline, so a log line says where it came from. */
	public void testTheDefaultGrantIsAttributedToTheBaseline() {
		Decision decision = access(unconfigured())
			.check(Set.of(), new ToolIdentity("listModules", "model-browser.listModules",
				ToolGroups.MODEL_READ));

		assertTrue(String.valueOf(decision), decision instanceof Decision.Granted);
		assertEquals("(baseline)", ((Decision.Granted) decision).byRole());
	}

	/** It reaches a user who holds no role at all - that is the case it exists for. */
	public void testTheDefaultReachesAUserWithoutRoles() {
		ToolAccessPolicy policy = unconfigured();
		policy.getToolAccess().put("Clerk", role("Clerk"));

		assertGranted(access(policy), "instance-browser.listInstances", ToolGroups.DATA_READ);
	}

	/** An explicit {@code <baseline/>} grants nothing beyond asking a question. */
	public void testAnEmptyDeclarationGrantsNothing() {
		ToolAccessPolicy policy = unconfigured();
		policy.setBaseline(TypedConfiguration.newConfigItem(Baseline.class));
		RoleToolAccess access = access(policy);

		assertDenied(access, "model-browser.listModules", ToolGroups.MODEL_READ);
		assertDenied(access, "instance-browser.listInstances", ToolGroups.DATA_READ);
		assertGranted(access, "user-interaction.askUser", ToolGroups.INTERACTION);
	}

	/**
	 * A declared baseline replaces the default rather than adding to it.
	 *
	 * <p>
	 * The property the whole design rests on: an application asking for one capability must not
	 * silently also get the two the default carries.
	 * </p>
	 */
	public void testADeclaredBaselineReplacesTheDefault() {
		ToolAccessPolicy policy = unconfigured();
		policy.setBaseline(baseline(group(ToolGroups.WORKSPACE)));
		RoleToolAccess access = access(policy);

		assertGranted(access, "task.createTask", ToolGroups.WORKSPACE);
		assertDenied(access, "model-browser.listModules", ToolGroups.MODEL_READ);
		assertDenied(access, "instance-browser.listInstances", ToolGroups.DATA_READ);
	}

	/** A role {@code <deny>} still wins over the default, as it does over a configured baseline. */
	public void testADenyWinsOverTheDefault() {
		ToolAccessPolicy policy = unconfigured();
		RoleAccess clerk = role("Clerk");
		clerk.getDenies().add(group(ToolGroups.DATA_READ));
		policy.getToolAccess().put("Clerk", clerk);

		Decision decision = access(policy).check(Set.of("Clerk"),
			new ToolIdentity("listInstances", "instance-browser.listInstances", ToolGroups.DATA_READ));

		assertTrue(String.valueOf(decision), decision instanceof Decision.Denied);
	}

	/**
	 * The default does not widen an unattended run.
	 *
	 * <p>
	 * An absent {@code <background-allowlist>} admits nothing beyond
	 * {@link RoleToolAccess#IMPLICIT_BASELINE}, and the background ceiling is applied on top of the
	 * baseline rather than instead of it - so a run with nobody to approve anything reaches neither
	 * read capability in an application that configured nothing.
	 * </p>
	 */
	public void testTheDefaultDoesNotReachAnUnattendedRun() {
		RoleToolAccess access = access(unconfigured());

		assertFalse(access.allowedInBackground("model-browser.listModules"));
		assertFalse(access.allowedInBackground("instance-browser.listInstances"));
		assertTrue("Asking a question survives, as it always does.",
			access.allowedInBackground(new ToolIdentity("askUser", "user-interaction.askUser",
				ToolGroups.INTERACTION)));
	}

	/**
	 * The default costs a run nothing it would not otherwise be denied: it grants the capability, and
	 * the capability check agrees with the tool check.
	 */
	public void testTheCapabilityCheckAgreesWithTheToolCheck() {
		RoleToolAccess access = access(unconfigured());

		assertTrue(access.checkCapability(Set.of(), ToolGroups.DATA_READ,
			"reading") instanceof Decision.Granted);
		assertTrue(access.checkCapability(Set.of(), ToolGroups.DATA_WRITE,
			"committing") instanceof Decision.Denied);
	}

	/** The boot log tells the default apart from a configured baseline. */
	public void testTheBootLogTellsTheTwoApart() {
		RoleToolAccess byDefault = access(unconfigured());
		assertTrue(byDefault.usesDefaultBaseline());
		assertEquals(RoleToolAccess.DEFAULT_BASELINE, byDefault.baselineGroups());

		ToolAccessPolicy declared = unconfigured();
		declared.setBaseline(baseline(group(ToolGroups.WORKSPACE)));
		RoleToolAccess configured = access(declared);

		assertFalse(configured.usesDefaultBaseline());
		assertEquals(List.of(ToolGroups.WORKSPACE), configured.baselineGroups());
	}

	/**
	 * An application that configures nothing still boots.
	 *
	 * <p>
	 * The default baseline is not a grant table, so it does not trip the rule that a policy naming
	 * roles must also declare what unattended runs may reach.
	 * </p>
	 */
	public void testAnUnconfiguredPolicyStillBoots() {
		assertFalse(AiToolAccessService.needsBackgroundAllowlist(unconfigured()));
	}

	private static void assertGranted(RoleToolAccess access, String qualifiedName, String group) {
		Decision decision = access.check(Set.of(), identity(qualifiedName, group));
		assertTrue(qualifiedName + ": " + decision, decision instanceof Decision.Granted);
	}

	private static void assertDenied(RoleToolAccess access, String qualifiedName, String group) {
		Decision decision = access.check(Set.of(), identity(qualifiedName, group));
		assertTrue(qualifiedName + ": " + decision, decision instanceof Decision.Denied);
	}

	private static ToolIdentity identity(String qualifiedName, String group) {
		String simpleName = qualifiedName.substring(qualifiedName.indexOf('.') + 1);
		return new ToolIdentity(simpleName, qualifiedName, group);
	}

	private static RoleToolAccess access(ToolAccessPolicy policy) {
		return new RoleToolAccess(policy, RoleToolAccess.DeclaredGroups.NONE);
	}

	/** A policy an application that configured nothing has: no roles, no baseline, no allowlist. */
	private static ToolAccessPolicy unconfigured() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		assertNull("An unconfigured policy must arrive with no baseline at all, not an empty one.",
			policy.getBaseline());
		assertNull(policy.getBackgroundAllowlist());
		return policy;
	}

	private static RoleAccess role(String name) {
		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName(name);
		return role;
	}

	/** A declared baseline carrying the given entries. */
	private static Baseline baseline(Grant... entries) {
		Baseline baseline = TypedConfiguration.newConfigItem(Baseline.class);
		baseline.getGrants().addAll(List.of(entries));
		return baseline;
	}

	private static Grant group(String group) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setGroup(group);
		return grant;
	}
}

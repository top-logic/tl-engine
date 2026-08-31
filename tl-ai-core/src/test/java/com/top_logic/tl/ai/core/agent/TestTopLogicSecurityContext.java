/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests what {@link TopLogicSecurityContext} decides for a run.
 *
 * <p>
 * The point of these tests is that there is no way <em>out</em>: a run without a principal is refused
 * where the context is built, and a run with one is authorized against a real policy. There is no
 * mode to configure and no default that checks nothing.
 * </p>
 *
 * <p>
 * The policy-taking {@code forRun} overload is the test seam these use, so that a policy can be
 * exercised without booting {@code AiToolAccessService}. What it pairs with
 * {@link ModelSecurity#UNCHECKED} - the model layer - needs a booted
 * {@code SecurityConfigurationService} and is covered in {@code SecDemoModelSecurityTest}.
 * </p>
 */
public class TestTopLogicSecurityContext extends TestCase {

	private static final String CLERK = "Clerk";

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestTopLogicSecurityContext.class)));
	}

	/**
	 * A run with no authenticated user is refused up front rather than at the first tool call.
	 *
	 * <p>
	 * The single production entry point, and the whole posture in one assertion: a run that cannot
	 * name its principal cannot be authorized, so it does not start. Failing later would waste a
	 * provider round trip and produce a confusing half-run.
	 * </p>
	 */
	public void testRunWithoutAUserIsRefused() {
		try {
			TopLogicSecurityContext.forRun((Person) null, false);
			fail("A run without a principal must be refused.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("authenticated user"));
			assertTrue("The message must say where a principal comes from: " + ex.getMessage(),
				ex.getMessage().contains("service account"));
		}
	}

	/** An unattended run without a user is refused too - being unattended is not an excuse. */
	public void testUnattendedRunWithoutAUserIsRefusedAsWell() {
		try {
			TopLogicSecurityContext.forRun((Person) null, true);
			fail("An unattended run without a principal must be refused as well.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("authenticated user"));
		}
	}

	/**
	 * An application that authored no policy grants no tool - it has not opted out of the check.
	 *
	 * <p>
	 * The inversion this change makes: an empty {@code <tool-access>} used to mean "check nothing",
	 * and now means what it says. The only capabilities left are the implicit baseline ones, which are
	 * covered by {@code TestRoleToolAccess}.
	 * </p>
	 */
	public void testEmptyPolicyGrantsNoPrivilegedTool() {
		TopLogicSecurityContext context =
			TopLogicSecurityContext.forRun(policy(), null, Set.of(CLERK));
		Tool privileged = tool("executeTLScript", ToolGroups.SCRIPT);

		assertFalse("An empty policy is deny-all, not check-nothing.",
			context.hasToolPermission(privileged));
		assertNotNull("...and it says which capability is missing.",
			context.toolDenialReason(privileged));
		assertNotNull("Committing through TL-Script is capped by the same policy.",
			context.capabilityDenialReason(ToolGroups.DATA_WRITE,
				"executing TL-Script with withCommit=true"));
	}

	/** What a role is granted, it may use; what it is not, it may not. */
	public void testAGrantedCapabilityIsUsableAndAnUngrantedOneIsNot() {
		ToolAccessPolicy policy = policy();
		policy.getToolAccess().put(CLERK, roleGranting(CLERK, ToolGroups.DATA_READ));
		TopLogicSecurityContext context =
			TopLogicSecurityContext.forRun(policy, null, Set.of(CLERK));

		assertTrue(context.hasToolPermission(tool("readInstances", ToolGroups.DATA_READ)));
		assertFalse(context.hasToolPermission(tool("createInstance", ToolGroups.DATA_WRITE)));
	}

	/** A user holding none of the policy's roles reaches nothing the policy grants. */
	public void testAUserWithoutTheRoleReachesNothing() {
		ToolAccessPolicy policy = policy();
		policy.getToolAccess().put(CLERK, roleGranting(CLERK, ToolGroups.DATA_READ));
		TopLogicSecurityContext context = TopLogicSecurityContext.forRun(policy, null, Set.of());

		assertFalse(context.hasToolPermission(tool("readInstances", ToolGroups.DATA_READ)));
	}

	/**
	 * The unrestricted context refuses nothing and is what tests build on.
	 *
	 * <p>
	 * Pinned here so that the one remaining permissive context stays a recognizable test seam rather
	 * than something a production path could drift into.
	 * </p>
	 */
	public void testTheUnrestrictedContextIsPermissive() {
		Tool privileged = tool("executeTLScript", ToolGroups.SCRIPT);

		assertTrue(SecurityContext.UNRESTRICTED.hasToolPermission(privileged));
		assertSame(ModelSecurity.UNCHECKED, SecurityContext.UNRESTRICTED.modelSecurity());
		assertNull(SecurityContext.UNRESTRICTED.capabilityDenialReason(ToolGroups.DATA_WRITE,
			"executing TL-Script with withCommit=true"));
	}

	/** A minimal {@link Tool} carrying a name and a declared group. */
	private static Tool tool(String name, String group) {
		return new Tool() {

			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return name;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, name, "{\"type\":\"object\"}");
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public String group() {
				return group;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				throw new UnsupportedOperationException("Authorization is decided before execution.");
			}
		};
	}

	/**
	 * An empty policy, which grants nothing beyond the implicit baseline.
	 *
	 * <p>
	 * The {@code <baseline>} is declared and left empty on purpose. Leaving it out would mean
	 * {@link RoleToolAccess#DEFAULT_BASELINE} - the read-only default of #293, tested by
	 * {@code TestDefaultBaseline} - and these tests are about what a role grant reaches.
	 * </p>
	 */
	private static ToolAccessPolicy policy() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		policy.setBaseline(TypedConfiguration.newConfigItem(Baseline.class));
		return policy;
	}

	/** A role entry granting the given capability groups. */
	private static RoleAccess roleGranting(String name, String... groups) {
		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName(name);
		for (String group : groups) {
			Grant grant = TypedConfiguration.newConfigItem(Grant.class);
			grant.setGroup(group);
			role.getGrants().add(grant);
		}
		return role;
	}
}

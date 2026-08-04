/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests how {@link TopLogicSecurityContext#forRun} resolves the security mode of a run.
 *
 * <p>
 * The point of these tests is the <b>default</b>: an application that configures no policy must come
 * out unenforced, because the engine's model security is deny-by-default and enforcing it without a
 * {@code <security-config>} would leave the agent unable to read anything.
 * </p>
 */
public class TestTopLogicSecurityContext extends TestCase {

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestTopLogicSecurityContext.class)));
	}

	/** No configuration at all: unenforced, so behaviour is what it was before the modes existed. */
	public void testUnconfiguredApplicationIsUnenforced() {
		TopLogicSecurityContext context = TopLogicSecurityContext.forRun((ToolAccessPolicy) null, null);

		assertEquals(AiSecurityMode.LEGACY, context.securityMode());
		assertSame(ModelSecurity.UNCHECKED, context.modelSecurity());
		assertFalse(context.securityMode().enforcesModelAccess());
		assertAllowsEveryTool(context);
	}

	/** The plain constructor - what every existing caller uses - is unenforced too. */
	public void testDefaultConstructorIsUnenforced() {
		TopLogicSecurityContext context = new TopLogicSecurityContext();

		assertEquals(AiSecurityMode.LEGACY, context.securityMode());
		assertSame(ModelSecurity.UNCHECKED, context.modelSecurity());
		assertAllowsEveryTool(context);
	}

	/**
	 * An unenforced context grants every tool and says nothing about why.
	 *
	 * <p>
	 * This is the promise the whole change rests on: until an application writes a
	 * {@code <tool-access>} policy, the tool permission gate is the {@code true} it always was.
	 * </p>
	 */
	private static void assertAllowsEveryTool(TopLogicSecurityContext context) {
		Tool privileged = tool("executeTLScript", ToolGroups.SCRIPT);

		assertTrue("A tool in a privileged group must pass while unenforced.",
			context.hasToolPermission(privileged));
		assertTrue("An ungrouped tool must pass while unenforced.",
			context.hasToolPermission(tool("mcp__jira__createIssue", "")));
		assertTrue("A tool known only by name must pass while unenforced.",
			context.hasToolPermission("anything.at.all"));
		assertNull(context.toolDenialReason(privileged));
		assertNull(context.toolDenialReason("anything.at.all"));

		assertNull("The generic filter must find nothing to complain about either.",
			new ToolPermissionFilter(context).denialReason(privileged));

		assertNull("Committing through TL-Script must not be capped while unenforced either.",
			context.capabilityDenialReason(ToolGroups.DATA_WRITE,
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

	/** A policy that does not name a mode is still an unmigrated application. */
	public void testPolicyWithoutModeIsUnenforced() {
		assertEquals(AiSecurityMode.LEGACY,
			TopLogicSecurityContext.forRun(policy(null), null).securityMode());
	}

	/** The mode the administrator configured is the mode the run carries. */
	public void testConfiguredModeIsCarried() {
		assertEquals(AiSecurityMode.SYSTEM,
			TopLogicSecurityContext.forRun(policy(AiSecurityMode.SYSTEM), null).securityMode());
	}

	/**
	 * {@link AiSecurityMode#ENFORCED} refuses a run with no principal up front rather than at the
	 * first tool call.
	 */
	public void testEnforcedRefusesRunWithoutUser() {
		try {
			TopLogicSecurityContext.forRun(policy(AiSecurityMode.ENFORCED), null);
			fail("An enforced run without a principal must be refused.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("authenticated user"));
			assertTrue(ex.getMessage(), ex.getMessage().contains(AiSecurityMode.ENFORCED.name()));
		}
	}

	/** An agent that declares nothing leaves the deployment's mode alone. */
	public void testAbsentFloorLeavesTheDeploymentAlone() {
		assertEquals(AiSecurityMode.LEGACY,
			TopLogicSecurityContext.forRun(policy(null), null, null).securityMode());
		assertEquals(AiSecurityMode.SYSTEM,
			TopLogicSecurityContext.forRun(policy(AiSecurityMode.SYSTEM), null, null).securityMode());
	}

	/** A floor stricter than the deployment is what the run carries. */
	public void testStricterFloorIsCarried() {
		assertEquals(AiSecurityMode.SYSTEM,
			TopLogicSecurityContext.forRun(policy(null), null, AiSecurityMode.SYSTEM).securityMode());
		assertEquals(AiSecurityMode.SYSTEM,
			TopLogicSecurityContext.forRun(policy(AiSecurityMode.LEGACY), null, AiSecurityMode.SYSTEM)
				.securityMode());
	}

	/** A floor laxer than the deployment is ignored: the deployment's mode stands. */
	public void testLaxerFloorIsIgnored() {
		assertEquals(AiSecurityMode.SYSTEM,
			TopLogicSecurityContext.forRun(policy(AiSecurityMode.SYSTEM), null, AiSecurityMode.LEGACY)
				.securityMode());
	}

	/**
	 * An agent demanding {@link AiSecurityMode#ENFORCED} in an unmigrated deployment really enforces -
	 * observable here as the up-front refusal of a run with no principal.
	 *
	 * <p>
	 * A {@link AiSecurityMode#LEGACY} deployment happily runs without a user (the case above). That
	 * the very same policy now refuses is the floor taking effect, and it is deterministic without a
	 * booted application.
	 * </p>
	 */
	public void testEnforcedFloorInALegacyDeploymentReallyEnforces() {
		try {
			TopLogicSecurityContext.forRun(policy(null), null, AiSecurityMode.ENFORCED);
			fail("An agent floor of ENFORCED must refuse a run without a principal, even though the"
				+ " deployment would have allowed one.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("authenticated user"));
			assertTrue(expected.getMessage(),
				expected.getMessage().contains(AiSecurityMode.ENFORCED.name()));
		}
	}

	/**
	 * A child run of an agent that declares a laxer mode than its parent's stays at the parent's.
	 *
	 * <p>
	 * A delegated child inherits its parent's context and the child's loop can only narrow it, so
	 * this is the composition the {@link AgentLoop} performs for a child - the same call, with the
	 * child agent's declaration.
	 * </p>
	 */
	public void testChildIsNeverLaxerThanItsParent() {
		SecurityContext parent = TopLogicSecurityContext.forRun(policy(AiSecurityMode.SYSTEM), null);

		assertSame("A child declaring a laxer mode must not get a different context at all.",
			parent, parent.restrictedTo(AiSecurityMode.LEGACY));
		assertSame("A child declaring nothing must not get a different context either.",
			parent, parent.restrictedTo(null));
		assertEquals(AiSecurityMode.SYSTEM, parent.restrictedTo(AiSecurityMode.LEGACY).securityMode());
	}

	/** A child agent declaring a stricter mode than its parent's keeps it. */
	public void testChildKeepsItsOwnStricterFloor() {
		SecurityContext parent = TopLogicSecurityContext.forRun(policy(null), null);
		assertEquals(AiSecurityMode.LEGACY, parent.securityMode());

		SecurityContext child = parent.restrictedTo(AiSecurityMode.SYSTEM);

		assertEquals(AiSecurityMode.SYSTEM, child.securityMode());
		assertEquals("The parent's own context is untouched - the mode is resolved per run.",
			AiSecurityMode.LEGACY, parent.securityMode());
	}

	/**
	 * A context with no principal refuses to be tightened rather than dropping the floor silently.
	 *
	 * <p>
	 * {@link SecurityContext#UNRESTRICTED} is the context a TL-Script entry point falls back to while
	 * nothing is enforced, and it has no principal to authorize against. Answering itself would turn
	 * an agent that asked to be checked into one that is not, so it refuses - which is why the entry
	 * points resolve the floor <em>before</em> they choose that fallback.
	 * </p>
	 */
	public void testAContextWithoutAPrincipalRefusesToBeTightened() {
		assertSame("Nothing to do without a floor.",
			SecurityContext.UNRESTRICTED, SecurityContext.UNRESTRICTED.restrictedTo(null));
		assertSame("SYSTEM is what this context already is.",
			SecurityContext.UNRESTRICTED, SecurityContext.UNRESTRICTED.restrictedTo(AiSecurityMode.SYSTEM));

		try {
			SecurityContext.UNRESTRICTED.restrictedTo(AiSecurityMode.ENFORCED);
			fail("An unrestricted context must not silently drop an ENFORCED floor.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("no principal"));
		}
	}

	/** The unrestricted context is honest about acting as the system rather than claiming LEGACY. */
	public void testUnrestrictedActsAsSystem() {
		assertEquals(AiSecurityMode.SYSTEM, SecurityContext.UNRESTRICTED.securityMode());
		assertSame(ModelSecurity.UNCHECKED, SecurityContext.UNRESTRICTED.modelSecurity());
		assertNull("It restricts no capability either - the TL-Script entry points outside an agent"
			+ " loop run against this context.",
			SecurityContext.UNRESTRICTED.capabilityDenialReason(ToolGroups.DATA_WRITE,
				"executing TL-Script with withCommit=true"));
	}

	private static ToolAccessPolicy policy(AiSecurityMode mode) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		if (mode != null) {
			policy.setSecurityMode(mode);
		}
		return policy;
	}
}

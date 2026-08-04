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

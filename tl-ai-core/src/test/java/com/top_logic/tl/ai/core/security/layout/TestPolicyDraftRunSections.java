/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;

import java.util.Set;

/**
 * That the policy editor can write, and can count, what the policy can express.
 *
 * <p>
 * A role entry with no names is how "this role may use none of them" is said. Two views of one
 * policy disagreeing about whether such a role is restricted - the editor said no, the boot log said
 * yes - is the defect this covers, together with the half that made the shape unreachable from the
 * editor at all.
 * </p>
 */
public class TestPolicyDraftRunSections extends TestCase {

	private static final String ROLE = "ticketReader";

	/** The draft copies configuration items, which resolve their types through the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestPolicyDraftRunSections.class)));
	}

	/** A role the section does not name is not restricted by it. */
	public void testAnUnnamedRoleIsNotRestricted() {
		assertFalse(draft().isRunRestricted(Dimension.MCP_SERVER, ROLE));
	}

	/**
	 * Taking a role into a section with nothing named is what "may use none of them" is, and has to
	 * be writable.
	 */
	public void testTakingARoleInWritesAnEmptyEntry() {
		PolicyDraft draft = draft();
		draft.setRunRestricted(Dimension.MCP_SERVER, ROLE, true);

		assertTrue("The section now names the role.", draft.isRunRestricted(Dimension.MCP_SERVER, ROLE));
		assertEquals("It names nothing else.", List.of(), draft.runNames(Dimension.MCP_SERVER, ROLE, false));
		assertTrue("The draft differs from what it was created from.", draft.isModified());

		// And that is what the policy then enforces: everything but what the agent itself declares.
		RoleAccessPolicy mcp = RoleAccessPolicy.mcpServers(draft.config());
		assertTrue(mcp.check(Set.of(ROLE), "jira", Set.of()) instanceof Decision.Denied);
	}

	/** Taking it out again returns its holders to the section default. */
	public void testTakingARoleOutRemovesTheEntry() {
		PolicyDraft draft = draft();
		draft.setRunRestricted(Dimension.AGENT, ROLE, true);
		draft.setRunRestricted(Dimension.AGENT, ROLE, false);

		assertFalse(draft.isRunRestricted(Dimension.AGENT, ROLE));
		assertTrue("An agent is permitted again by the default.",
			RoleAccessPolicy.agents(draft.config()).check(Set.of(ROLE), "TL_SingleAgent", Set.of())
				instanceof Decision.Granted);
	}

	/** Writing names takes the role in, whether or not the switch was used first. */
	public void testWritingNamesTakesTheRoleIn() {
		PolicyDraft draft = draft();
		draft.setRunNames(Dimension.MODEL, ROLE, false, List.of("claude-haiku-4-5"));

		assertTrue(draft.isRunRestricted(Dimension.MODEL, ROLE));
		assertEquals(List.of("claude-haiku-4-5"), draft.runNames(Dimension.MODEL, ROLE, false));
	}

	/**
	 * Writing what is already stored changes nothing.
	 *
	 * <p>
	 * The round-trip guarantee: opening the editor on a hand-written policy and saving it again must
	 * not reorder or rewrite its entries.
	 * </p>
	 */
	public void testWritingWhatIsStoredIsANoOp() {
		PolicyDraft draft = draft();
		draft.setRunRestricted(Dimension.MODEL, ROLE, true);
		draft.setRunNames(Dimension.MODEL, ROLE, false, List.of("claude-haiku-4-5"));
		AiToolAccessService.Config<?> before = TypedConfiguration.copy(draft.config());

		draft.setRunNames(Dimension.MODEL, ROLE, false, List.of("claude-haiku-4-5"));
		draft.setRunRestricted(Dimension.MODEL, ROLE, true);

		assertEquals(TypedConfiguration.toString(before), TypedConfiguration.toString(draft.config()));
	}

	private static PolicyDraft draft() {
		return new PolicyDraft(TypedConfiguration.newConfigItem(AiToolAccessService.Config.class));
	}
}

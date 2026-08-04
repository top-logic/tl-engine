/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.RoleToolAccess.DeclaredGroups;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * The ceiling a fire-and-forget subagent run is bounded by, and the reach a synchronous delegation
 * keeps.
 *
 * <p>
 * A detached subagent keeps working on a pool thread after the parent turn that spawned it has ended,
 * so from that moment nobody is there to answer an approval prompt. That is the design's own
 * definition of an unattended run, so the {@code <background-allowlist>} applies to it -
 * {@link ToolContext#forDetachedSubagent} is where that is decided. A synchronous delegation is the
 * opposite case and must not be touched: it completes <em>inside</em> the parent turn, where the human
 * who was there before it started still is.
 * </p>
 *
 * <p>
 * The fixture makes the difference observable rather than vacuous: the role holds
 * {@link ToolGroups#DATA_WRITE} and the allowlist deliberately does not carry it, while
 * {@link ToolGroups#DATA_READ} is on both.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.security.ToolAuthorization#asBackground()
 */
public class TestDetachedSubagentCeiling extends TestCase {

	/** A tool of a capability the role holds and the allowlist does not carry. */
	private static final Tool WRITE_TOOL = tool("addNote", ToolGroups.DATA_WRITE, ToolMode.DATA_WRITE);

	/** A tool of a capability the role holds and the allowlist does carry. */
	private static final Tool READ_TOOL = tool("listInstances", ToolGroups.DATA_READ, ToolMode.READ);

	/** A tool of a capability no role in the fixture holds. */
	private static final Tool STRUCTURE_TOOL =
		tool("createClass", ToolGroups.MODEL_STRUCTURE, ToolMode.STRUCTURAL);

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestDetachedSubagentCeiling.class)));
	}

	/**
	 * The whole change in one pair: a capability the acting user holds is out of reach in a detached
	 * subagent, and the refusal blames the allowlist rather than the role.
	 */
	public void testDetachedSubagentIsABackgroundRun() {
		ToolContext parent = enforcedParent();
		assertFalse("The parent turn has a human in front of it.", parent.security().isBackgroundRun());
		assertTrue("Precondition: the role holds data.write interactively.",
			parent.security().hasToolPermission(WRITE_TOOL));

		ToolContext child = detachedChild(parent);

		assertTrue("A detached subagent has nobody to approve its calls.",
			child.security().isBackgroundRun());
		assertFalse("...so it must not reach a capability that is off the allowlist.",
			child.security().hasToolPermission(WRITE_TOOL));

		String reason = child.security().toolDenialReason(WRITE_TOOL);
		assertNotNull("A denial must state a reason.", reason);
		assertTrue("The denial must blame the allowlist - the role does hold the capability: " + reason,
			reason.contains("background allowlist"));
		assertFalse("Blaming the role would send an operator to the wrong configuration: " + reason,
			reason.contains("none of your roles grants"));
	}

	/**
	 * A synchronous delegation keeps the parent's classification and therefore the parent's reach.
	 *
	 * <p>
	 * It runs to completion inside the parent turn, so the human who could answer an approval prompt
	 * before it started still can. Marking every sub-agent context as unattended would take a
	 * capability away from a run that never lost the control the ceiling compensates for.
	 * </p>
	 */
	public void testSynchronousDelegationKeepsTheParentsReach() {
		ToolContext parent = enforcedParent();

		ToolContext child = parent.forDelegation(TransactionScope.NOOP);

		assertFalse(child.security().isBackgroundRun());
		assertTrue("A synchronous delegate is attended and keeps the parent's reach.",
			child.security().hasToolPermission(WRITE_TOOL));
		assertNull(child.security().toolDenialReason(WRITE_TOOL));
		assertEquals("Nothing about the withheld set to report for an attended run.",
			Set.of(), child.security().capabilitiesWithheldInBackground());
	}

	/** The ceiling narrows and is not a blanket refusal: what the allowlist admits still works. */
	public void testAllowlistedCapabilityStillWorksInADetachedSubagent() {
		ToolContext child = detachedChild(enforcedParent());

		assertTrue("data.read is on the allowlist and the role holds it.",
			child.security().hasToolPermission(READ_TOOL));
		assertNull(child.security().toolDenialReason(READ_TOOL));
	}

	/**
	 * The regression of issue #266: under {@code AUTO_APPROVE} a detached subagent used to
	 * self-approve every write tool after the parent turn had ended.
	 *
	 * <p>
	 * The approval callback is carried into the child unchanged and would still say yes - that is
	 * asserted here rather than assumed. What stops the call is the authorization gate, which
	 * {@code AgentLoop#executeWithSecurityCheck} consults <em>before</em> approval, so the callback
	 * never gets asked. Keeping the callback is deliberate: in an {@code ALWAYS_ASK} session an open
	 * browser window can still answer the child's prompts.
	 * </p>
	 */
	public void testAutoApproveDoesNotLetADetachedSubagentWrite() {
		ToolContext child = detachedChild(enforcedParent());

		assertTrue("The child carries a callback that approves everything...",
			child.approval().requestApproval(approvalOf(WRITE_TOOL)) instanceof ApprovalResult.Approved);
		assertFalse("...and is refused the tool anyway, because authorization decides first.",
			child.security().hasToolPermission(WRITE_TOOL));
		assertTrue(child.security().toolDenialReason(WRITE_TOOL).contains("background allowlist"));
	}

	/**
	 * An application that has configured no {@code <tool-access>} policy sees no change at all.
	 *
	 * <p>
	 * The most important test in this class: {@link com.top_logic.tl.ai.core.security.AiSecurityMode#LEGACY}
	 * is what every unmigrated application runs in, and a detached subagent there must keep behaving
	 * exactly as it did before this ceiling existed.
	 * </p>
	 */
	public void testLegacyRunIsUnaffected() {
		ToolContext parent = new ToolContext(new TopLogicSecurityContext(),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);

		ToolContext child = detachedChild(parent);

		assertSame("Nothing to derive, so not even a new context.", parent.security(), child.security());
		assertFalse(child.security().isBackgroundRun());
		assertTrue(child.security().hasToolPermission(WRITE_TOOL));
		assertEquals(Set.of(), child.security().capabilitiesWithheldInBackground());
	}

	/** The unrestricted context of a test or a trusted internal run is not narrowed either. */
	public void testUnrestrictedRunIsUnaffected() {
		ToolContext child = detachedChild(ToolContext.EMPTY);

		assertSame(ToolContext.EMPTY.security(), child.security());
		assertTrue(child.security().hasToolPermission(WRITE_TOOL));
	}

	/**
	 * What the ceiling costs is named, so the loss is legible rather than silent - both to an operator
	 * reading the log and to the child agent's own prompt.
	 */
	public void testTheWithheldCapabilitiesAreNamed() {
		SecurityContext child = detachedChild(enforcedParent()).security();

		assertEquals("The capability the role holds and the allowlist does not carry.",
			Set.of(ToolGroups.DATA_WRITE), child.capabilitiesWithheldInBackground());

		String notice = new AgentCapabilities(name -> resolve(name), child)
			.notice(List.of(WRITE_TOOL.name(), READ_TOOL.name()), List.of());

		assertTrue("The notice must say the run is unattended: " + notice,
			notice.contains("unattended"));
		assertTrue("...and name the capability it lost: " + notice,
			notice.contains(ToolGroups.DATA_WRITE));
		assertFalse("It must not claim the user lacks a capability they hold: " + notice,
			notice.contains("This user does **not** hold"));
	}

	/** An attended run's notice is unchanged - it reports what the user does not hold, as before. */
	public void testAnAttendedRunsNoticeIsUnchanged() {
		String notice = new AgentCapabilities(name -> resolve(name), enforcedParent().security())
			.notice(List.of(WRITE_TOOL.name(), READ_TOOL.name(), STRUCTURE_TOOL.name()), List.of());

		assertTrue("model.structure is held by nobody here: " + notice,
			notice.contains("This user does **not** hold"));
		assertTrue(notice, notice.contains(ToolGroups.MODEL_STRUCTURE));
		assertFalse("An attended run must not be described as unattended: " + notice,
			notice.contains("unattended"));
	}

	/** The detached child of the given parent turn, exactly as {@code SubagentSpawner} builds it. */
	private static ToolContext detachedChild(ToolContext parent) {
		return parent.forDetachedSubagent(TransactionScope.NOOP, new CancellationToken());
	}

	/**
	 * The context of an attended turn under the fixture policy, auto-approving like a chat session
	 * whose user chose {@code AUTO_APPROVE}.
	 */
	private static ToolContext enforcedParent() {
		// No principal: the administrator bypass needs a Person, which cannot be created without a
		// KnowledgeBase. It is covered where it can be, in SecDemoBackgroundAccessTest.
		ToolAuthorization authorization = new ToolAuthorization(
			new RoleToolAccess(fixturePolicy(), DeclaredGroups.NONE), null, Set.of("Clerk"), false);
		return new ToolContext(TopLogicSecurityContext.enforcedRun(authorization),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/**
	 * A role holding {@code data.read} and {@code data.write}, with only {@code data.read} allowlisted
	 * for unattended runs.
	 */
	private static ToolAccessPolicy fixturePolicy() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		RoleAccess clerk = TypedConfiguration.newConfigItem(RoleAccess.class);
		clerk.setName("Clerk");
		clerk.getGrants().add(group(ToolGroups.DATA_READ));
		clerk.getGrants().add(group(ToolGroups.DATA_WRITE));
		policy.getToolAccess().put(clerk.getName(), clerk);

		BackgroundAllowlist allowlist = TypedConfiguration.newConfigItem(BackgroundAllowlist.class);
		allowlist.getGrants().add(group(ToolGroups.DATA_READ));
		policy.setBackgroundAllowlist(allowlist);
		return policy;
	}

	private static Grant group(String name) {
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setGroup(name);
		return grant;
	}

	/** Resolves the three fixture tools by name, like the tool registry would. */
	private static Tool resolve(String name) {
		for (Tool tool : List.of(WRITE_TOOL, READ_TOOL, STRUCTURE_TOOL)) {
			if (tool.name().equals(name)) {
				return tool;
			}
		}
		return null;
	}

	private static ApprovalRequest approvalOf(Tool tool) {
		return new ApprovalRequest("approval-1", tool.name(), "call-1", Map.of(), tool.mode(),
			"Modifies shared application data.", Duration.ofSeconds(1));
	}

	/** A minimal {@link Tool} that carries a name, a declared group and a mode. */
	private static Tool tool(String name, String group, ToolMode mode) {
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
			public String group() {
				return group;
			}

			@Override
			public ToolMode mode() {
				return mode;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, name, "{\"type\":\"object\"}");
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				throw new UnsupportedOperationException("Not executed in this test.");
			}
		};
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelDefault;
import com.top_logic.tl.ai.core.security.layout.DraftAccess.Mark;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tl.ai.core.security.layout.PolicyMatrix.RowState;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The overlay and the cell tooltips of the policy matrix: what one role on its own may use under the
 * draft (#404).
 */
public class TestDraftAccess extends TestCase {

	private static final String ROLE = "customerSupportLevel2";

	private static final String EDITOR = "TL_ModelEditor";

	private static final String SINGLE = "TL_SingleAgent";

	private static final String IMPROVER = "TL_PromptImprover";

	private static final String OPUS = "claude-opus";

	private static final String SONNET = "claude-sonnet";

	private static final String HAIKU = "claude-haiku";

	/** Three agents: two personas with their own models, and one the application runs for users. */
	private static final AgentFacts FACTS = new AgentFacts() {

		private final Map<String, String> _models = Map.of(EDITOR, OPUS, SINGLE, SONNET, IMPROVER, HAIKU);

		@Override
		public String assignedModel(String agentName) {
			return _models.get(agentName);
		}

		@Override
		public Collection<String> declaredServers(String agentName) {
			return EDITOR.equals(agentName) ? List.of("jira") : List.of();
		}

		@Override
		public boolean isInfrastructure(String agentName) {
			return IMPROVER.equals(agentName);
		}
	};

	/** The draft copies configuration items, which resolve their types through the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestDraftAccess.class)));
	}

	/**
	 * An agent the agent cell grants is refused when the role may not use the agent's own model - the
	 * ⊘ of the overlay.
	 */
	public void testTheAgentsOwnModelDeniedRefusesTheAgent() {
		PolicyDraft draft = draft();
		PolicyMatrix agents = new PolicyMatrix(draft, MatrixDimension.AGENTS);
		agents.setRowState(ROLE, RowState.NAMED);
		agents.setCell(ROLE, EDITOR, CellState.GRANTED);
		PolicyMatrix models = new PolicyMatrix(draft, MatrixDimension.MODELS);
		models.setRowState(ROLE, RowState.NAMED);
		models.setCell(ROLE, OPUS, CellState.DENIED);

		DraftAccess.RunVerdict verdict = access(draft).run(ROLE, Dimension.AGENT, EDITOR);

		assertFalse(verdict.allowed());
		assertEquals(Mark.REFUSED, verdict.mark());
		assertEquals(Cause.AGENTS_OWN_DENIED, ((Decision.Denied) verdict.decision()).cause());
		assertEquals(List.of(), access(draft).runnableAgents(ROLE));
	}

	/** The infrastructure agent is refused in every row, whatever the section grants. */
	public void testTheInfrastructureAgentIsRefusedEverywhere() {
		PolicyDraft draft = draft();
		for (String role : List.of(ROLE, "anyOtherRole")) {
			DraftAccess.RunVerdict verdict = access(draft).run(role, Dimension.AGENT, IMPROVER);
			assertEquals(Mark.REFUSED, verdict.mark());
			assertEquals(Cause.INFRASTRUCTURE_AGENT, ((Decision.Denied) verdict.decision()).cause());
		}
	}

	/** A model no cell grants is reachable as the model of an agent the role may run - the A. */
	public void testAModelOnlyTheAgentUsesIsMarked() {
		PolicyDraft draft = draft();
		PolicyMatrix agents = new PolicyMatrix(draft, MatrixDimension.AGENTS);
		agents.setRowState(ROLE, RowState.NAMED);
		agents.setCell(ROLE, SINGLE, CellState.GRANTED);
		PolicyMatrix models = new PolicyMatrix(draft, MatrixDimension.MODELS);
		models.setRowState(ROLE, RowState.NAMED);
		models.setCell(ROLE, HAIKU, CellState.GRANTED);

		DraftAccess access = access(draft);
		assertEquals(Mark.AGENTS_OWN, access.run(ROLE, Dimension.MODEL, SONNET).mark());
		assertTrue(access.run(ROLE, Dimension.MODEL, SONNET).allowed());
		assertEquals(List.of(SINGLE), access.agentsUsing(ROLE, Dimension.MODEL, SONNET));
		assertEquals("The model the cell grants carries no mark.", Mark.NONE,
			access.run(ROLE, Dimension.MODEL, HAIKU).mark());
		assertFalse("Nobody's agent uses Opus.", access.run(ROLE, Dimension.MODEL, OPUS).allowed());
		assertFalse("The agent the role runs declares no server.",
			access.run(ROLE, Dimension.MCP_SERVER, "jira").allowed());
	}

	/** The answer is the draft's: an edit the service has never seen is what it shows. */
	public void testTheVerdictsAreTheDrafts() {
		PolicyDraft draft = draft();
		assertFalse("Only the infrastructure agent uses Haiku, and nobody converses with it.",
			access(draft).run(ROLE, Dimension.MODEL, HAIKU).allowed());

		draft.setSectionDefault(Dimension.MODEL, ModelDefault.ALL);

		assertTrue("A fresh evaluation of the edited draft.",
			access(draft).run(ROLE, Dimension.MODEL, HAIKU).allowed());
	}

	/** A capability the role grants is refused in an unattended run the limit does not admit. */
	public void testTheBackgroundLimitCapsACapability() {
		PolicyDraft draft = draft();
		new PolicyMatrix(draft, MatrixDimension.CAPABILITIES).setCell(ROLE, ToolGroups.DATA_WRITE,
			CellState.GRANTED);
		draft.setBackgroundChoice(BackgroundChoice.CUSTOM);
		draft.setBackgroundGroups(List.of(ToolGroups.DATA_READ));

		DraftAccess.CapabilityVerdict verdict = access(draft).capability(ROLE, ToolGroups.DATA_WRITE);

		assertTrue(verdict.allowed(false));
		assertFalse(verdict.allowed(true));
	}

	private static DraftAccess access(PolicyDraft draft) {
		return new DraftAccess(draft, FACTS, dimension -> {
			switch (dimension) {
				case MODEL:
					return List.of(HAIKU, OPUS, SONNET);
				case MCP_SERVER:
					return List.of("jira");
				default:
					return List.of(EDITOR, IMPROVER, SINGLE);
			}
		});
	}

	private static PolicyDraft draft() {
		return new PolicyDraft(TypedConfiguration.newConfigItem(AiToolAccessService.Config.class));
	}
}

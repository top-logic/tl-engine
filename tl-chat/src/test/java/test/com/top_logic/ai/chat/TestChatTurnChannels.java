/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.view.ChatTurnChannels;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.util.model.ModelService;

/**
 * That a write to one of the four turn-setting channels of {@code <chat-conversation/>} is refused
 * exactly where a send naming the same value would be.
 *
 * <p>
 * The sibling of {@link TestChatRunAccess}, which pins the same rule for {@code /chat/send}. The
 * point of doing it twice is that a channel is a second way in: a host that writes a forbidden model
 * would otherwise leave the composer showing a setting the next send is answered 403 for, which is a
 * refusal the user can neither see nor undo.
 * </p>
 *
 * <p>
 * Asserted against the <em>shipped defaults</em> for the same reason {@link TestChatRunAccess} is:
 * the test application configures two models and grants nobody anything, so "a request may not choose
 * a model other than its agent's" is the behaviour of an application that wrote no policy.
 * </p>
 */
public class TestChatTurnChannels extends BasicTestCase {

	/** The model the test application assigns to every agent. */
	private static final String ASSIGNED_MODEL = "test-assigned-model";

	/** A second configured model, which is therefore a choice rather than the agent's own. */
	private static final String OTHER_MODEL = "test-other-model";

	/** An agent this product ships. */
	private static final String AGENT = "TL_ModelExplorer";

	private static Person berta;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (berta == null) {
			berta = TestPerson.createPerson("berta-who-wires-channels");
		}
	}

	/** Precondition: the fixture this test reasons about actually reached the application. */
	public void testFixture() {
		assertTrue("The security service decides here; without it nothing is checked.",
			AiToolAccessService.Module.INSTANCE.isActive());
		assertTrue("Two models must be configured for 'the other one' to mean anything: "
			+ LLMProviderService.getInstance().getConfiguredModelNames(),
			LLMProviderService.getInstance().getConfiguredModelNames()
				.containsAll(List.of(ASSIGNED_MODEL, OTHER_MODEL)));
	}

	/** An agent this product ships may be written onto the {@code agent} channel. */
	public void testAgentTheAccountMayRunIsAllowed() {
		assertTrue(ChatTurnChannels.allowsAgent(berta, AGENT));
	}

	/**
	 * A name no agent has cannot be followed, whoever is asking.
	 *
	 * <p>
	 * Not a policy refusal - nothing forbids it, the name is simply wrong, which is what
	 * {@code /chat/send} answers 404 for. Following it would leave the composer set to an agent that
	 * cannot answer, and the run would die in the background thread instead of refusing.
	 * </p>
	 */
	public void testUnknownAgentIsRefused() {
		assertFalse(ChatTurnChannels.allowsAgent(berta, "NoSuchAgent"));
		assertFalse("Not a policy question, so it holds with no account either.",
			ChatTurnChannels.allowsAgent(null, "NoSuchAgent"));
		assertFalse(ChatTurnChannels.allowsAgent(berta, ""));
	}

	/**
	 * Naming the model the agent is already assigned is not choosing one.
	 *
	 * <p>
	 * The load-bearing case: it is what the composer publishes back on every agent change, so a rule
	 * that called it a choice would refuse the conversation's own write-back.
	 * </p>
	 */
	public void testTheAgentsOwnModelIsAllowed() {
		assertTrue(ChatTurnChannels.allowsModel(berta, AGENT, ASSIGNED_MODEL));
	}

	/** The second configured model is a choice, and under the shipped default nobody is granted it. */
	public void testAForeignModelIsRefused() {
		assertFalse(ChatTurnChannels.allowsModel(berta, AGENT, OTHER_MODEL));
	}

	/** A server the agent does not declare is refused the way {@code mcpOverride} is on a send. */
	public void testAForeignServerIsRefused() {
		assertFalse(ChatTurnChannels.allowsServers(berta, AGENT, List.of("some-server")));
	}

	/** Attaching nothing is not attaching something forbidden. */
	public void testAttachingNoServerIsAllowed() {
		assertTrue(ChatTurnChannels.allowsServers(berta, AGENT, List.of()));
		assertTrue(ChatTurnChannels.allowsServers(berta, AGENT, null));
	}

	/**
	 * Nothing is checked for a request with no account behind it.
	 *
	 * <p>
	 * A pre-check is a courtesy; the run itself still asks. Refusing here instead would leave a
	 * background render unable to follow any channel at all.
	 * </p>
	 */
	public void testNoAccountIsNotChecked() {
		assertTrue(ChatTurnChannels.allowsAgent(null, AGENT));
		assertTrue(ChatTurnChannels.allowsModel(null, AGENT, OTHER_MODEL));
		assertTrue(ChatTurnChannels.allowsServers(null, AGENT, List.of("some-server")));
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatTurnChannels.class),
				ModelService.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}

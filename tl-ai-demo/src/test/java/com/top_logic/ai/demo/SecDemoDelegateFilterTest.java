/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.tools.interaction.ListAgentsTool;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Which sub-agents a persona's agent may hand work to, on the framework agents the deployment really
 * ships.
 *
 * <p>
 * The delegate half of {@link SecDemoCapabilityNoticeTest}, and user experience for the same reason: a
 * child run inherits the parent's security context unchanged, so no delegate could ever have done more
 * than its caller. What the filter buys is that a delegate whose every tool is denied is not offered at
 * all - offering it costs a prompt line, a child run, a provider round trip and a failure report - and
 * that a delegate which can do only part of its job says so instead of being found out by refusal.
 * </p>
 *
 * <p>
 * The agents are the real ones from {@code tl-ai-core.conf.config.xml}, so the assertions are about the
 * deployed wiring:
 * </p>
 * <ul>
 * <li>{@code TL_Orchestrator} declares all three delegations, so it is the agent whose list is
 * compared.</li>
 * <li>{@code TL_ModelExplorer} carries {@code model.read} and {@code data.read} tools only, so it is
 * the delegate that disappears for a persona holding neither and is <em>limited</em> for one holding
 * just the first.</li>
 * <li>{@code TL_TLScriptAgent} carries {@code script}, {@code workspace} and {@code model.read}
 * tools.</li>
 * <li>{@code TL_ModelEditor} carries {@code user-interaction.askUser}, and {@code interaction} is
 * granted implicitly to every role, so it survives for everyone - which is what keeps this test from
 * passing by deleting everything.</li>
 * </ul>
 *
 * @see AgentCapabilities#unusableReason(AgentDefinition)
 */
public class SecDemoDelegateFilterTest extends SecDemoPersonaTest {

	/** The framework agent that delegates. */
	private static final String ORCHESTRATOR = "TL_Orchestrator";

	/** Reads the model and its instances, and nothing else. */
	private static final String EXPLORER = "TL_ModelExplorer";

	/** Runs TL-Script, and keeps tasks. */
	private static final String SCRIPT_AGENT = "TL_TLScriptAgent";

	/** Changes the model - and asks questions, which every persona may do. */
	private static final String EDITOR_AGENT = "TL_ModelEditor";

	/** Precondition: the agents this test reasons about have the shape it assumes. */
	public void testFixtureAgentsHaveTheAssumedShape() {
		assertEquals("TL_Orchestrator is the delegating agent of the shipped wiring.",
			List.of(SCRIPT_AGENT, EDITOR_AGENT, EXPLORER), agent(ORCHESTRATOR).delegations());
		assertEquals("TL_ModelExplorer must reach the two read capabilities and nothing else.",
			Set.of(ToolGroups.MODEL_READ, ToolGroups.DATA_READ), reachableCapabilities(EXPLORER));
		assertTrue("TL_ModelEditor must reach the implicitly granted interaction capability,"
			+ " otherwise no persona would keep any delegate.",
			reachableCapabilities(EDITOR_AGENT).contains(ToolGroups.INTERACTION));
	}

	/**
	 * Every persona of this fixture reaches a tool of every delegate, so every delegate is offered.
	 *
	 * <p>
	 * The load-bearing half of the filter, and the one that is easy to break: a filter that dropped too
	 * much would fail here. The complementary half - a delegate <em>dropped</em> because the persona
	 * can reach none of its tools, with a reason naming the capabilities - needs a role holding nothing
	 * but {@code interaction}, which this fixture deliberately does not have. {@code TestDelegateFilter}
	 * in {@code tl-ai-core} covers it against a hand-built policy.
	 * </p>
	 */
	public void testEveryDelegateIsOfferedToEveryPersona() {
		for (String login : SecDemoSetupService.personaLogins()) {
			assertEquals(login + " holds both read capabilities and script, which reach a tool of each"
				+ " delegate.", List.of(SCRIPT_AGENT, EDITOR_AGENT, EXPLORER),
				offeredDelegatesOf(login));
		}
	}

	/** An unenforced run keeps every declared delegate, whatever its principal may do. */
	public void testUnenforcedRunFiltersNothing() {
		assertEquals(agent(ORCHESTRATOR).delegations(),
			AgentFactory.offeredDelegates(agent(ORCHESTRATOR),
				AgentCapabilities.forRun(SecurityContext.UNRESTRICTED)));
	}

	/**
	 * A delegate only part of whose tools the persona can reach stays offered and is said to be limited;
	 * for a persona holding more, the same delegate is offered without a limit.
	 *
	 * <p>
	 * This is the pair the decision rests on: a persona can usefully ask {@code TL_TLScriptAgent} to run
	 * a script and not to keep a task, and being told so beats discovering it from a child run that
	 * comes back refused.
	 * </p>
	 */
	public void testLimitedDelegateIsOfferedAndSaysWhatItCannotDo() {
		assertEquals("No role holds workspace, so the task tools of TL_TLScriptAgent are out of reach.",
			Set.of(ToolGroups.WORKSPACE),
			capabilitiesOf(BEN).unreachableCapabilities(agent(SCRIPT_AGENT)));
		assertEquals("TL_ModelExplorer needs the two read capabilities, which every persona holds, so"
			+ " the same persona gets it unlimited.",
			Set.of(), capabilitiesOf(BEN).unreachableCapabilities(agent(EXPLORER)));
	}

	/**
	 * A delegate whose every tool is denied is absent from {@code list_agents} too, and a reachable one
	 * is present.
	 *
	 * <p>
	 * The third surface. Filtering the prompt alone would be cosmetic: {@code list_agents} is how an
	 * agent finds a name to spawn, and a name it finds there is a name it will use.
	 * </p>
	 */
	public void testListAgentsCarriesTheSameFilter() {
		String benSees = listAgentsAs(BEN);

		assertTrue("Every delegate this persona reaches must be listed: " + benSees,
			benSees.contains("\"" + EDITOR_AGENT + "\"") && benSees.contains("\"" + EXPLORER + "\"")
				&& benSees.contains("\"" + SCRIPT_AGENT + "\""));
		assertFalse("An agent nothing could come of must not be listed - and the fixture has none, so"
			+ " what this pins is that the listing is filtered at all rather than that it is empty: "
			+ benSees, benSees.contains("\"no-such-agent\""));
	}

	/** {@code list_agents} names what a listed agent cannot use, as the prompt catalog does. */
	public void testListAgentsNamesTheUnreachableCapabilities() {
		String benSees = listAgentsAs(BEN);

		assertTrue("The task tools TL_TLScriptAgent cannot use must be named: " + benSees,
			benSees.contains("\"cannotUse\""));
		assertTrue(benSees, benSees.contains("\"" + ToolGroups.WORKSPACE + "\""));
	}

	/** Outside an enforcing run the listing is what it was before this filter existed. */
	public void testListAgentsIsUnfilteredWithoutARun() {
		String listing = listAgentsAs(SecurityContext.UNRESTRICTED);

		for (AgentDefinition definition : AgentDefinitionQuery.getInstance().getAllAgents()) {
			assertTrue(definition.name() + " must be listed: " + listing,
				listing.contains("\"" + definition.name() + "\""));
		}
		assertFalse("Nothing is limited when nothing is enforced: " + listing,
			listing.contains("\"cannotUse\""));
	}

	/** The delegates the given persona's runs are offered, in declaration order. */
	private List<String> offeredDelegatesOf(String login) {
		return AgentFactory.offeredDelegates(agent(ORCHESTRATOR), capabilitiesOf(login));
	}

	/** The capability view of a run acting as the given persona, over the real tool registry. */
	private AgentCapabilities capabilitiesOf(String login) {
		return AgentCapabilities.forRun(securityFor(login));
	}

	/** The result of the real {@code list_agents} tool in a run acting as the given persona. */
	private String listAgentsAs(String login) {
		return listAgentsAs(securityFor(login));
	}

	/** The result of the real {@code list_agents} tool under the given security context. */
	private static String listAgentsAs(SecurityContext security) {
		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
		return new ListAgentsTool().execute(Map.of(), context).text();
	}

	/** The capabilities the tools of the named agent sit in, for the precondition check. */
	private static Set<String> reachableCapabilities(String agentName) {
		AgentCapabilities capabilities = AgentCapabilities.forRun(SecurityContext.UNRESTRICTED);
		return capabilities.reachableTools(agent(agentName)).stream()
			.map(tool -> tool.group())
			.filter(group -> group != null && !group.isBlank())
			.collect(Collectors.toSet());
	}

	private static AgentDefinition agent(String name) {
		AgentDefinition definition = AgentDefinitionQuery.getInstance().findAgentByName(name);
		assertNotNull("The framework agent '" + name + "' must exist; this test bed assumes the agents"
			+ " shipped in 'tl-ai-core.conf.config.xml'.", definition);
		return definition;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoDelegateFilterTest.class, TopLogicAgentDefinitions.Module.INSTANCE);
	}

}

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
	 * Two independent filters decide it, and the fixture deploys both. The {@code <agent-access>} section
	 * (#392) says which agents the acting account may run at all, and it is asked first; the capability
	 * filter then asks whether an agent the account may run could reach a tool it may call. So a delegate
	 * can be withheld from a persona whose capabilities would have reached every one of its tools, and the
	 * assertions below are therefore <em>per persona</em> rather than one set for all of them (#445).
	 * </p>
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
	 * just the first. It is also the single agent {@code secdemo.config.xml} grants
	 * {@link #RESTRICTED_ROLE}, and therefore the only delegate a persona holding that role keeps.</li>
	 * <li>{@code TL_TLScriptAgent} carries {@code script}, {@code workspace}, {@code model.read},
	 * {@code data.read} and - since the text edit was offered to the agents that write instance data
	 * (#168) - {@code data.write} tools.</li>
	 * <li>{@code TL_ModelEditor} carries {@code user-interaction.askUser}, and {@code interaction} is
	 * granted implicitly to every role, so no capability filter ever drops it - which is what keeps this
	 * test from passing by deleting everything.</li>
	 * </ul>
	 *
	 * @see AgentCapabilities#unusableReason(AgentDefinition)
	 */
public class SecDemoDelegateFilterTest extends SecDemoPersonaTest {

	/** The framework agent that delegates. */
	private static final String ORCHESTRATOR = "TL_Orchestrator";

	/** Reads the model and its instances, and nothing else. */
	private static final String EXPLORER = "TL_ModelExplorer";

	/** Runs TL-Script, keeps tasks, and edits a long text in place. */
	private static final String SCRIPT_AGENT = "TL_TLScriptAgent";

	/** Changes the model - and asks questions, which every persona may do. */
	private static final String EDITOR_AGENT = "TL_ModelEditor";

	/**
	 * The one role the fixture's {@code <agent-access>} section names, and which it grants
	 * {@link #EXPLORER} alone.
	 *
	 * <p>
	 * Written out rather than read back from the policy, so that this test states what
	 * {@code secdemo.config.xml} is meant to configure instead of restating what it happens to say.
	 * Which personas hold it is looked up ({@link SecDemoSetupService#personaRoles()}), because a
	 * persona added to the aspect must be covered without an edit here.
	 * </p>
	 */
	private static final String RESTRICTED_ROLE = "ticketReader";

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
	 * Which delegates a persona is offered is decided by the role it holds, not by the tools it can
	 * reach.
	 *
	 * <p>
	 * The load-bearing half of the filter, and the one that is easy to break in both directions. Every
	 * persona of this fixture reaches a tool of every delegate, so the capability filter alone would
	 * offer all three to all of them - and a filter that dropped one would fail here. What it offers
	 * instead is what {@code <agent-access>} leaves: a persona holding {@link #RESTRICTED_ROLE} keeps
	 * {@link #EXPLORER} and nothing else, and one holding no role that section names keeps the lot.
	 * </p>
	 *
	 * <p>
	 * The contrast is the point of the fixture rather than an accident of it, which is why both halves
	 * are asserted to occur: a change that gave every persona the restricted role would otherwise leave
	 * this test passing while demonstrating half of what it claims.
	 * </p>
	 *
	 * <p>
	 * The complementary case - a delegate <em>dropped</em> because the persona can reach none of its
	 * tools, with a reason naming the capabilities - needs a role holding nothing but
	 * {@code interaction}, which this fixture deliberately does not have. {@code TestDelegateFilter} in
	 * {@code tl-ai-core} covers it against a hand-built policy.
	 * </p>
	 */
	public void testDelegatesAreOfferedAsAgentAccessLeavesThem() {
		int restrictedPersonas = 0;
		for (Map.Entry<String, List<String>> persona : SecDemoSetupService.personaRoles().entrySet()) {
			String login = persona.getKey();
			boolean restricted = persona.getValue().contains(RESTRICTED_ROLE);

			if (restricted) {
				restrictedPersonas++;
				assertEquals(login + " holds '" + RESTRICTED_ROLE + "', which <agent-access> grants"
					+ " one agent, so that is the only delegate left - although its capabilities"
					+ " reach a tool of each of the three.", List.of(EXPLORER),
					offeredDelegatesOf(login));
			} else {
				assertEquals(login + " holds no role <agent-access> names, so the section default"
					+ " applies and every delegate whose tools it reaches is offered.",
					List.of(SCRIPT_AGENT, EDITOR_AGENT, EXPLORER), offeredDelegatesOf(login));
			}
		}

		assertTrue("The fixture must restrict at least one persona, otherwise <agent-access> is not"
			+ " demonstrated at all.", restrictedPersonas > 0);
		assertTrue("The fixture must leave at least one persona unrestricted, otherwise nothing shows"
			+ " that the restriction is what makes the difference.",
			restrictedPersonas < SecDemoSetupService.personaLogins().size());
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
	 *
	 * <p>
	 * The two personas on the same delegate are what says the limit is a statement about the persona:
	 * ben reads tickets and writes none, so the text edit is out of reach for him on top of the task
	 * tools, while anna, who writes, is limited by the task tools alone.
	 * </p>
	 */
	public void testLimitedDelegateIsOfferedAndSaysWhatItCannotDo() {
		assertEquals("No role holds workspace, and ben's ticketReader reads but does not write, so the"
			+ " task tools and the text edit of TL_TLScriptAgent are both out of reach.",
			Set.of(ToolGroups.DATA_WRITE, ToolGroups.WORKSPACE),
			capabilitiesOf(BEN).unreachableCapabilities(agent(SCRIPT_AGENT)));
		assertEquals("anna's projectPartner holds data.write, so the same delegate is limited by the"
			+ " task tools alone - the limit follows the persona, not the agent.",
			Set.of(ToolGroups.WORKSPACE),
			capabilitiesOf(ANNA).unreachableCapabilities(agent(SCRIPT_AGENT)));
		assertEquals("TL_ModelExplorer needs the two read capabilities, which every persona holds, so"
			+ " the same persona gets it unlimited.",
			Set.of(), capabilitiesOf(BEN).unreachableCapabilities(agent(EXPLORER)));
	}

	/**
	 * An agent a persona may not run is absent from {@code list_agents} too, and the ones it may run are
	 * present.
	 *
	 * <p>
	 * The third surface, and the one that matters most: {@code list_agents} is how an agent finds a name
	 * to spawn, and a name it finds there is a name it will use. Filtering the prompt catalog alone
	 * would leave the restriction cosmetic.
	 * </p>
	 *
	 * <p>
	 * Per persona, because that is what the listing is: ben's role is granted {@link #EXPLORER} alone,
	 * so the other two must not appear for him, while anna, whose role {@code <agent-access>} does not
	 * name, sees all three.
	 * </p>
	 */
	public void testListAgentsCarriesTheSameFilter() {
		String annaSees = listAgentsAs(ANNA);
		for (String agentName : List.of(SCRIPT_AGENT, EDITOR_AGENT, EXPLORER)) {
			assertTrue("anna may run '" + agentName + "', so it must be listed to her: " + annaSees,
				isListed(annaSees, agentName));
		}

		String benSees = listAgentsAs(BEN);
		assertTrue("The one agent ben's role is granted must be listed: " + benSees,
			isListed(benSees, EXPLORER));
		for (String agentName : List.of(SCRIPT_AGENT, EDITOR_AGENT)) {
			assertFalse("ben may not run '" + agentName + "', so a listing that named it would hand"
				+ " him a name to spawn: " + benSees, isListed(benSees, agentName));
		}
	}

	/**
	 * {@code list_agents} names what a listed agent cannot use, as the prompt catalog does - and names
	 * nothing where there is nothing to say.
	 *
	 * <p>
	 * Per persona for the same reason as the listing itself, and the pair is what tells the two filters
	 * apart: anna is offered {@link #SCRIPT_AGENT} and told it cannot keep a task, while ben is left one
	 * agent and reaches every tool of it, so his listing carries no limit at all. A limit reported to
	 * him would be about an agent he is not offered.
	 * </p>
	 */
	public void testListAgentsNamesTheUnreachableCapabilities() {
		String annaSees = listAgentsAs(ANNA);

		assertTrue("The task tools TL_TLScriptAgent cannot use must be named: " + annaSees,
			annaSees.contains("\"cannotUse\""));
		assertTrue(annaSees, annaSees.contains("\"" + ToolGroups.WORKSPACE + "\""));

		String benSees = listAgentsAs(BEN);

		assertFalse("ben is left one agent and reaches both of its capabilities, so nothing is"
			+ " limited for him: " + benSees, benSees.contains("\"cannotUse\""));
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

	/** Whether the given {@code list_agents} result names the given agent. */
	private static boolean isListed(String listing, String agentName) {
		return listing.contains("\"name\":\"" + agentName + "\"");
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

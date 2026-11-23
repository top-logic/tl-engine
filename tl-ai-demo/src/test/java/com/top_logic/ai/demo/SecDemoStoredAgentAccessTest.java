/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.ai.chat.api.ChatCapabilities;
import com.top_logic.ai.chat.api.messages.AgentOptionDto;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentCapabilities;
import com.top_logic.ai.core.agent.AgentDefinition;
import com.top_logic.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.ai.core.agent.AgentFactory;
import com.top_logic.ai.core.agent.AgentFactory.RunOverrides;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.TransactionScope;
import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.message.Conversation;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.message.ToolResultMessage;
import com.top_logic.ai.core.message.ToolState;
import com.top_logic.ai.core.model.Agent;
import com.top_logic.ai.core.model.Skill;
import com.top_logic.ai.core.model.TlAiCoreFactory;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.ai.core.security.ModelSecurity;
import com.top_logic.ai.core.tools.ToolRegistry;
import com.top_logic.ai.core.tools.authoring.AgentAuthoringTool;
import com.top_logic.ai.core.tools.interaction.ListAgentsTool;
import com.top_logic.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Stored agents and skills under model rights (Ticket #30493), on the agents and skills of a project.
 *
 * <p>
 * {@code pm:ProjectAgent} and {@code pm:ProjectSkill} specialize the engine's stored agent and skill
 * and are parts of a project, so they follow the project: Anna, the partner of Alpha, knows the
 * agent and the skill of Alpha, Cara (partner of Beta) and Dora (partner of nothing) do not. Dora is
 * a persona whose {@code <agent-access>} permits every agent, so what she does not see is decided by
 * the model rights alone. A stored agent of the base type is visible to every member of the group
 * "user", as before. No language model is called: every assertion is about the lookup, a list, the
 * refusal of a start, or the catalog a run would get.
 * </p>
 */
public class SecDemoStoredAgentAccessTest extends SecDemoPersonaTest {

	/** The agent of project Alpha. */
	private static final String ALPHA_AGENT = "alpha-planner";

	private static final String ALPHA_PROMPT = "Plan the releases of Alpha.";

	/** The skill of project Alpha. */
	private static final String ALPHA_SKILL = "alpha-checklist";

	private static final String ALPHA_SKILL_TEXT = "Check the release branch of Alpha.";

	/** The agent of project Beta, which Anna may not read. */
	private static final String BETA_AGENT = "beta-planner";

	/** A project agent outside any project. */
	private static final String LOOSE_AGENT = "loose-planner";

	/** A stored agent of the base type, which delegates to the agent and the skill of Alpha. */
	private static final String SHARED_AGENT = "secdemo-shared-helper";

	/** A stored skill of the base type. */
	private static final String SHARED_SKILL = "secdemo-shared-skill";

	/** A name that nothing has. */
	private static final String UNKNOWN = "secdemo-no-such-name";

	/** The name of a configured agent in another case. */
	private static final String CONFIGURED_AGENT_OTHER_CASE = "tl_singleagent";

	/** The name of a configured skill in another case. */
	private static final String CONFIGURED_SKILL_OTHER_CASE = "tl_taskmanagement";

	private static final List<String> AGENTS =
		List.of(ALPHA_AGENT, BETA_AGENT, LOOSE_AGENT, SHARED_AGENT, CONFIGURED_AGENT_OTHER_CASE,
			BETA_AGENT.toUpperCase(), "secdemo-authored-agent");

	private static final List<String> SKILLS = List.of(ALPHA_SKILL, SHARED_SKILL, CONFIGURED_SKILL_OTHER_CASE);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		deleteRecords();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Agent alphaAgent = projectAgent(ALPHA_AGENT);
			alphaAgent.setSystemPrompt(ALPHA_PROMPT);
			project(ALPHA).tAddByName("agents", alphaAgent);
			Skill alphaSkill = (Skill) newObject("pm:ProjectSkill");
			alphaSkill.setName(ALPHA_SKILL);
			alphaSkill.setContent(ALPHA_SKILL_TEXT);
			project(ALPHA).tAddByName("skills", alphaSkill);
			project(BETA).tAddByName("agents", projectAgent(BETA_AGENT));
			projectAgent(LOOSE_AGENT);

			Agent shared = TlAiCoreFactory.getInstance().createAgent();
			shared.setName(SHARED_AGENT);
			shared.setDelegations(Set.of(ALPHA_AGENT, UNKNOWN));
			shared.setSkills(Set.of(SHARED_SKILL, ALPHA_SKILL, UNKNOWN));
			Skill sharedSkill = TlAiCoreFactory.getInstance().createSkill();
			sharedSkill.setName(SHARED_SKILL);
			sharedSkill.setContent("Help everybody.");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		deleteRecords();
		super.tearDown();
	}

	/** Without rules of the application, every persona sees a stored agent of the base type. */
	public void testTheGroupUserSeesAStoredAgentOfTheBaseType() {
		for (String login : personaLogins()) {
			assertNotNull(login + " is a member of the group 'user'.",
				query().findAgentByName(SHARED_AGENT, readerOf(login)));
		}
	}

	/** The agents and skills of a project follow the project. */
	public void testOnlyThePartnersOfTheProjectKnowItsAgentsAndSkills() {
		assertNotNull(query().findAgentByName(ALPHA_AGENT, readerOf(ANNA)));
		assertNotNull(query().findSkillByName(ALPHA_SKILL, readerOf(ANNA)));
		for (String outsider : List.of(CARA, DORA)) {
			assertNull(outsider, query().findAgentByName(ALPHA_AGENT, readerOf(outsider)));
			assertNull(outsider, query().findSkillByName(ALPHA_SKILL, readerOf(outsider)));
		}
		assertTrue(names(query().getAllAgents(readerOf(CARA))).contains(BETA_AGENT));
		assertFalse(names(query().getAllAgents(readerOf(CARA))).contains(ALPHA_AGENT));
	}

	/** A specialization starts closed: a project agent outside a project is nobody's. */
	public void testAProjectAgentOutsideAProjectIsClosed() {
		for (String login : personaLogins()) {
			assertNull(login, query().findAgentByName(LOOSE_AGENT, readerOf(login)));
		}
		assertNotNull(query().findAgentByName(LOOSE_AGENT, new ModelAccessRightsSecurity(anyAdministrator())));
	}

	/** The chat offers the agent of Alpha to Anna and not to Dora, whom no policy restricts. */
	public void testTheChatMenuOffersOnlyWhatTheAccountMayRead() {
		assertTrue(menuOf(ANNA).contains(ALPHA_AGENT));
		List<String> dorasMenu = menuOf(DORA);
		assertFalse(dorasMenu.toString(), dorasMenu.contains(ALPHA_AGENT));
		assertTrue(dorasMenu.toString(), dorasMenu.contains(SHARED_AGENT));
	}

	/** list_agents follows the same rule. */
	public void testListAgentsListsOnlyWhatTheRunMayRead() {
		assertTrue(listAgentsAs(ANNA).contains(ALPHA_AGENT));
		assertFalse(listAgentsAs(DORA).contains(ALPHA_AGENT));
	}

	/**
	 * Every start of an agent Dora may not read is answered as the start of a name that nothing has:
	 * a run, a delegation, a subagent, and the pre-check of the chat.
	 */
	public void testAStartOfAnUnreadableAgentIsTheStartOfAnUnknownName() {
		String unknownRun = startFailure(UNKNOWN);
		assertTrue(unknownRun, unknownRun.contains("No agent found"));
		assertSameAnswer(unknownRun, startFailure(ALPHA_AGENT));

		String unknownDelegate = buildFailure(UNKNOWN, false);
		assertTrue(unknownDelegate, unknownDelegate.contains("No agent found"));
		assertSameAnswer(unknownDelegate, buildFailure(ALPHA_AGENT, false));
		assertSameAnswer(buildFailure(UNKNOWN, true), buildFailure(ALPHA_AGENT, true));

		Person dora = actAs(DORA);
		assertEquals(
			AgentFactory.runRefusal(AiToolAccessService.effectiveRunAccessFor(dora), readerOf(DORA), UNKNOWN,
				RunOverrides.NONE),
			AgentFactory.runRefusal(AiToolAccessService.effectiveRunAccessFor(dora), readerOf(DORA), ALPHA_AGENT,
				RunOverrides.NONE));
	}

	/** Anna's run gets past the lookup of the agent of her project. */
	public void testThePartnerStartsTheAgentOfHerProject() {
		String outcome = outcomeFor(ANNA, ALPHA_AGENT);
		assertFalse("Anna knows the agent of Alpha: " + outcome, outcome.contains("No agent found"));
	}

	/** A delegate and a skill of another project do not exist for the run, in the catalog either. */
	public void testDelegatesAndSkillsOfAnotherProjectAreUnknown() {
		assertNotNull(AgentCapabilities.forRun(securityFor(ANNA)).graph().agent(ALPHA_AGENT));
		assertNull(AgentCapabilities.forRun(securityFor(DORA)).graph().agent(ALPHA_AGENT));

		assertTrue(loadSkill(ANNA, ALPHA_SKILL).contains(ALPHA_SKILL_TEXT));
		assertSameAnswer(loadSkill(DORA, UNKNOWN), loadSkill(DORA, ALPHA_SKILL));
	}

	/** TL-Script sees what the reader sees: the shipped rule makes the base type readable. */
	public void testScriptSeesWhatTheAccountMayRead() {
		actAs(ANNA);
		List<String> annas = storedAgentsOfScript();
		assertTrue(annas.toString(), annas.contains(ALPHA_AGENT));
		assertTrue(annas.toString(), annas.contains(SHARED_AGENT));
		actAs(DORA);
		List<String> doras = storedAgentsOfScript();
		assertFalse(doras.toString(), doras.contains(ALPHA_AGENT));
		assertTrue(doras.toString(), doras.contains(SHARED_AGENT));
	}

	/** Only an administrator changes the MCP servers and the infrastructure mark. */
	public void testOnlyAnAdministratorChangesTheMcpServersAndTheInfrastructureMark() {
		Agent alphaAgent = storedAgent(ALPHA_AGENT);
		ModelSecurity anna = readerOf(ANNA);
		assertTrue("Anna writes the agents of her project.", anna.canWrite(alphaAgent));
		assertFalse(anna.canWrite(alphaAgent, TlAiCoreFactory.getMcpServersAgentAttr()));
		assertFalse(anna.canWrite(alphaAgent, TlAiCoreFactory.getInfrastructureAgentAttr()));
		assertTrue(new ModelAccessRightsSecurity(anyAdministrator())
			.canWrite(alphaAgent, TlAiCoreFactory.getMcpServersAgentAttr()));
	}

	/** The authoring tool asks the model rights of the run. */
	public void testTheAuthoringToolFollowsTheModelRights() {
		AgentAuthoringTool tool = new AgentAuthoringTool();

		actAs(ANNA);
		ToolContext anna = runOf(ANNA);
		String created = tool.createAgent("secdemo-authored-agent", null, "You help.", null, null, null, null, anna);
		assertTrue("Nobody but an administrator creates a stored agent of the base type: " + created,
			created.contains("'Create'"));
		assertTrue(tool.getAgent(ALPHA_AGENT, anna).contains(ALPHA_PROMPT));
		assertTrue(tool.listAgents(anna).contains(ALPHA_AGENT));
		String changed = tool.updateAgent(ALPHA_AGENT, null, "Plan the hotfixes.", null, null, null, null, anna);
		assertTrue(changed, changed.contains("\"success\":true"));
		String withServer = tool.updateAgent(ALPHA_AGENT, null, null, null, List.of("mail"), null, null, anna);
		assertTrue("The MCP servers are the administrator's: " + withServer, withServer.contains("mcpServers"));
		assertEquals(Set.of(), storedAgent(ALPHA_AGENT).getMcpServers());

		actAs(DORA);
		ToolContext dora = runOf(DORA);
		assertTrue(tool.getAgent(ALPHA_AGENT, dora).contains("\"exists\":false"));
		assertFalse(tool.listAgents(dora).contains(ALPHA_AGENT));
		String unknown = tool.updateAgent(ALPHA_AGENT, null, "Mine now.", null, null, null, null, dora);
		assertTrue(unknown, unknown.contains("No application agent named"));
		String notDeleted = tool.deleteAgent(ALPHA_AGENT, dora);
		assertTrue(notDeleted, notDeleted.contains("No application agent named"));
	}

	/**
	 * Names are global. Anna cannot take the name of a configured agent or skill, nor the name of
	 * the agent of Beta, which she may not read.
	 */
	public void testTheNameRule() {
		actAs(ANNA);
		assertRefused(() -> addAgentToAlpha(CONFIGURED_AGENT_OTHER_CASE));
		assertRefused(() -> addAgentToAlpha(BETA_AGENT.toUpperCase()));
		assertRefused(() -> {
			Skill skill = (Skill) newObject("pm:ProjectSkill");
			skill.setName(CONFIGURED_SKILL_OTHER_CASE);
			project(ALPHA).tAddByName("skills", skill);
		});
		assertNull(storedAgent(CONFIGURED_AGENT_OTHER_CASE));
	}

	private static void addAgentToAlpha(String name) {
		project(ALPHA).tAddByName("agents", projectAgent(name));
	}

	/** Runs the given change in a transaction of its own and expects the commit to be refused. */
	private static void assertRefused(Runnable change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			change.run();
			tx.commit();
		} catch (RuntimeException expected) {
			return;
		}
		fail("The commit must be refused by the name rule.");
	}

	/** Asserts that the answer about the unreadable record is the answer about a name nothing has. */
	private static void assertSameAnswer(String forUnknownName, String forUnreadableRecord) {
		assertEquals(forUnknownName,
			forUnreadableRecord.replace(ALPHA_AGENT, UNKNOWN).replace(ALPHA_SKILL, UNKNOWN));
	}

	/** The failure of a run of the named agent for Dora, the way the chat, a script and A2A start it. */
	private String startFailure(String agentName) {
		ToolContext context = runOf(DORA);
		try {
			AgentFactory.getInstance().runAgent(agentName, new Conversation(), null, context, event -> {
				// No event is expected: the run is refused before it starts.
			});
			return "started";
		} catch (RuntimeException ex) {
			return ex.getMessage();
		}
	}

	/** The failure of a delegation (or a subagent) to the named agent in Dora's run. */
	private String buildFailure(String agentName, boolean subagent) {
		return outcome(securityFor(DORA), agentName, subagent);
	}

	/** What building the delegation to the named agent does in a run of the given persona. */
	private String outcomeFor(String login, String agentName) {
		return outcome(securityFor(login), agentName, false);
	}

	private static String outcome(SecurityContext security, String agentName, boolean subagent) {
		try {
			if (subagent) {
				AgentFactory.getInstance().buildSubagentLoop(agentName, security);
			} else {
				AgentFactory.getInstance().buildAgentLoop(agentName, security);
			}
			return "built";
		} catch (RuntimeException ex) {
			return String.valueOf(ex.getMessage());
		}
	}

	/** What {@code loadSkill} answers in a run of the shared agent for the given persona. */
	private String loadSkill(String login, String skillName) {
		SecurityContext security = securityFor(login);
		AgentDefinition shared = query().findAgentByName(SHARED_AGENT, security.modelSecurity());
		ToolRegistry catalog = AgentFactory.getInstance().buildToolCatalog(shared, AgentCapabilities.forRun(security));
		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
		ToolResultMessage result = catalog.execute(
			new ToolCall("call-1", "loadSkill", "{\"skillId\": \"" + skillName + "\"}"), context);
		if (result.state() instanceof ToolState.Completed completed) {
			return completed.result();
		}
		return ((ToolState.Failed) result.state()).error();
	}

	private List<String> menuOf(String login) {
		List<String> offered = new ArrayList<>();
		for (AgentOptionDto option : ChatCapabilities.build(actAs(login), null).getAgents()) {
			offered.add(option.getName());
		}
		return offered;
	}

	private String listAgentsAs(String login) {
		return new ListAgentsTool().execute(Map.of(), runOf(login)).text();
	}

	private ToolContext runOf(String login) {
		return new ToolContext(securityFor(login), ApprovalCallback.AUTO_APPROVE);
	}

	/** The model rights of the given persona, as a run of it has them. */
	private ModelSecurity readerOf(String login) {
		return securityFor(login).modelSecurity();
	}

	private static List<String> storedAgentsOfScript() {
		List<String> names = new ArrayList<>();
		for (Object agent : (Collection<?>) TLScriptExecutor.compile("all(`tl.ai.core:Agent`)").execute()) {
			names.add(((Agent) agent).getName());
		}
		return names;
	}

	private static AgentDefinitionQuery query() {
		return AgentDefinitionQuery.getInstance();
	}

	private static List<String> names(Collection<AgentDefinition> agents) {
		List<String> names = new ArrayList<>();
		for (AgentDefinition agent : agents) {
			names.add(agent.name());
		}
		return names;
	}

	private static Agent projectAgent(String name) {
		Agent agent = (Agent) newObject("pm:ProjectAgent");
		agent.setName(name);
		return agent;
	}

	private static TLObject newObject(String typeName) {
		return ModelService.getInstance().getFactory().createObject((TLClass) TLModelUtil.findType(typeName));
	}

	private static Agent storedAgent(String name) {
		for (Object agent : instancesOf("tl.ai.core:Agent")) {
			if (((Agent) agent).tValid() && name.equals(((Agent) agent).getName())) {
				return (Agent) agent;
			}
		}
		return null;
	}

	private static Skill storedSkill(String name) {
		for (Object skill : instancesOf("tl.ai.core:Skill")) {
			if (((Skill) skill).tValid() && name.equals(((Skill) skill).getName())) {
				return (Skill) skill;
			}
		}
		return null;
	}

	private static void deleteRecords() {
		List<TLObject> doomed = new ArrayList<>();
		for (String name : AGENTS) {
			Agent agent = storedAgent(name);
			if (agent != null) {
				doomed.add(agent);
			}
		}
		for (String name : SKILLS) {
			Skill skill = storedSkill(name);
			if (skill != null) {
				doomed.add(skill);
			}
		}
		if (doomed.isEmpty()) {
			return;
		}
		ThreadContext.inSystemContext(SecDemoStoredAgentAccessTest.class, () -> {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				for (TLObject record : doomed) {
					record.tDelete();
				}
				tx.commit();
			}
		});
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoStoredAgentAccessTest.class, TopLogicAgentDefinitions.Module.INSTANCE,
			LLMProviderService.Module.INSTANCE,
			AgentRuntimeService.Module.INSTANCE,
			AgentApplicationService.Module.INSTANCE,
			AgentPolicyService.Module.INSTANCE);
	}

}

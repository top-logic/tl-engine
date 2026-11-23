/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.model.Agent;
import com.top_logic.ai.core.model.Skill;
import com.top_logic.ai.core.model.TlAiCoreFactory;
import com.top_logic.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.ai.core.security.ModelSecurity;
import com.top_logic.ai.core.tools.authoring.AgentAuthoringTool;
import com.top_logic.ai.tool.api.Tool;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The shipped access definition of stored agents and skills ({@code tl-ai-core.agentSecurity.config.xml}),
 * the lookups with a reader, the name rule and the model rights of {@link AgentAuthoringTool}.
 *
 * <p>
 * Nothing is stubbed but the tool policy of a run: the roles come from the group "user" and the role
 * parent rule of the shipped file, the grants from its {@code <security-config>}.
 * </p>
 */
public class TestStoredAgentAccess extends BasicTestCase {

	/** A configured agent of this module. */
	private static final String CONFIGURED_AGENT = "TL_ModelExplorer";

	/** A configured skill of this module. */
	private static final String CONFIGURED_SKILL = "TL_taskManagement";

	private static final String STORED_AGENT = "stored-access-agent";

	private static final String STORED_SKILL = "stored-access-skill";

	/** The name of the configured agent in another case, which the reserved prefix does not catch. */
	private static final String CONFIGURED_AGENT_OTHER_CASE = "tl_modelexplorer";

	private static final String CONFIGURED_SKILL_OTHER_CASE = "tl_taskmanagement";

	private static final String NEW_AGENT = "stored-access-new-agent";

	/** A name with an accent, which a database may compare equal to the name without it. */
	private static final String ACCENTED_AGENT = "stored-access-ch\u00e4t";

	private static final List<String> AGENT_NAMES =
		List.of(STORED_AGENT, CONFIGURED_AGENT_OTHER_CASE, NEW_AGENT, ACCENTED_AGENT, "stored-access-blank ");

	private static final List<String> SKILL_NAMES = List.of(STORED_SKILL, CONFIGURED_SKILL_OTHER_CASE);

	/** A member of the group "user", as every new account is. */
	private static Person member;

	/** An account outside the group "user". */
	private static Person outsider;

	private static Person administrator;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (member == null) {
			member = TestPerson.createPerson("stored-access-member");
			outsider = TestPerson.createPerson("stored-access-outsider");
			administrator = TestPerson.createPerson("stored-access-admin");
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				Group users = Group.getGroupByName("user");
				assertNotNull("The engine declares the group 'user'.", users);
				users.addMember(member);
				users.removeMember(outsider);
				administrator.setAdmin(true);
				tx.commit();
			}
		}
		deleteRecords();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.setName(STORED_AGENT);
			agent.setSystemPrompt("The instructions of a stored agent.");
			Skill skill = TlAiCoreFactory.getInstance().createSkill();
			skill.setName(STORED_SKILL);
			skill.setContent("The text of a stored skill.");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		deleteRecords();
		super.tearDown();
	}

	/**
	 * Runs each test with the security checks on, so that the rights are decided for the account
	 * each question names.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> withSecurityChecks(true, () -> {
			super.runTest();
			return null;
		});
	}

	/** Without rules of the application, the group "user" sees stored agents and skills, as before. */
	public void testTheGroupUserReadsStoredAgentsAndSkills() {
		assertNotNull(query().findAgentByName(STORED_AGENT, rights(member)));
		assertNotNull(query().findSkillByName(STORED_SKILL, rights(member)));
		assertTrue(names(query().getAllAgents(rights(member))).contains(STORED_AGENT));
	}

	/** An account outside the group does not know them, in a lookup and in a list. */
	public void testAnAccountOutsideTheGroupDoesNotKnowThem() {
		assertNull(query().findAgentByName(STORED_AGENT, rights(outsider)));
		assertNull(query().findSkillByName(STORED_SKILL, rights(outsider)));
		assertFalse(names(query().getAllAgents(rights(outsider))).contains(STORED_AGENT));
		assertTrue("A configured agent is no stored record and stays known.",
			names(query().getAllAgents(rights(outsider))).contains(CONFIGURED_AGENT));
		assertNotNull("The lookup for a configuration check still sees it.", query().findAgentByName(STORED_AGENT));
	}

	/** The shipped definition writes nothing: changing a stored agent is the administrator's. */
	public void testTheShippedDefinitionGrantsOnlyRead() {
		Agent agent = storedAgent(STORED_AGENT);
		assertFalse(rights(member).canWrite(agent));
		assertFalse(rights(member).canDelete(agent));
		assertFalse(rights(member).canCreate(TlAiCoreFactory.getAgentType(), null));
		assertTrue(rights(administrator).canWrite(agent, TlAiCoreFactory.getMcpServersAgentAttr()));
	}

	/**
	 * A stored record that the reader may not read and that has the name of a configured agent leaves
	 * the configured agent in its place, while it replaces the configured agent for a reader of it.
	 *
	 * <p>
	 * Asked through the list, which merges in Java: what the database finds for a name depends on
	 * its collation, and the list does not.
	 * </p>
	 */
	public void testAnUnreadableRecordLeavesTheConfiguredAgent() {
		ThreadContext.inSystemContext(TestStoredAgentAccess.class, () -> createAgent(CONFIGURED_AGENT_OTHER_CASE));

		assertEquals(AgentSource.KB_OVERRIDE, sourceOf(CONFIGURED_AGENT, query().getAllAgents(rights(member))));
		assertEquals(AgentSource.CONFIG, sourceOf(CONFIGURED_AGENT, query().getAllAgents(rights(outsider))));
		assertEquals(AgentSource.CONFIG, query().findAgentByName(CONFIGURED_AGENT, rights(outsider)).source());
	}

	/** A person who is no administrator cannot take the name of a configured agent or skill. */
	public void testOnlyAnAdministratorOrTheSystemTakesAConfiguredName() {
		assertRefused(I18NConstants.DUPLICATE_NAME_ERROR__NAME,
			() -> asPerson(member, () -> createAgent(CONFIGURED_AGENT_OTHER_CASE)));
		assertNull(storedAgent(CONFIGURED_AGENT_OTHER_CASE));
		assertRefused(I18NConstants.DUPLICATE_NAME_ERROR__NAME,
			() -> asPerson(member, () -> createSkill(CONFIGURED_SKILL_OTHER_CASE)));

		asPerson(administrator, () -> createAgent(CONFIGURED_AGENT_OTHER_CASE));
		assertNotNull(storedAgent(CONFIGURED_AGENT_OTHER_CASE));
		deleteRecords();

		ThreadContext.inSystemContext(TestStoredAgentAccess.class, () -> createSkill(CONFIGURED_SKILL_OTHER_CASE));
		assertNotNull(storedSkill(CONFIGURED_SKILL_OTHER_CASE));
	}

	/** A name with a blank at its start or end is refused, for everybody. */
	public void testANameWithAnOuterBlankIsRefused() {
		assertRefused(I18NConstants.OUTER_BLANK_ERROR__NAME,
			() -> ThreadContext.inSystemContext(TestStoredAgentAccess.class, () -> createAgent("stored-access-blank ")));
		assertNull(storedAgent("stored-access-blank "));
	}

	/**
	 * The lookup by name compares like the name rule, whatever the database answers: a record whose
	 * name differs by more than the case is not the record of that name.
	 */
	public void testTheLookupComparesLikeTheNameRule() {
		ThreadContext.inSystemContext(TestStoredAgentAccess.class, () -> createAgent(ACCENTED_AGENT));
		Agent accented = storedAgent(ACCENTED_AGENT);

		assertNull(AgentDefinitionQuery.named(List.of(accented), "stored-access-chat"));
		assertNull(AgentDefinitionQuery.named(List.of(accented), ACCENTED_AGENT + " "));
		assertSame(accented, AgentDefinitionQuery.named(List.of(accented), ACCENTED_AGENT.toUpperCase()));
		assertNull(query().findStoredAgent("stored-access-chat"));
		assertSame(accented, query().findStoredAgent(ACCENTED_AGENT));
	}

	/** A form keeps the names a record holds, also those its editor may not know. */
	public void testTheOptionsKeepTheHeldNames() {
		assertEquals(List.of("known", "held"),
			AllAgentNamesGenerator.withHeld(new ArrayList<>(List.of("known")), List.of("known", "held")));
		assertEquals(List.of("known"), AllAgentNamesGenerator.withHeld(new ArrayList<>(List.of("known")), null));
	}

	/** The authoring tool asks the model rights of the run for every record. */
	public void testTheAuthoringToolChecksTheModelRightsOfTheRun() {
		AgentAuthoringTool tool = new AgentAuthoringTool();

		String created = answerAs(member, () -> tool.createAgent(NEW_AGENT, null, "You help.", null, null, null,
			null, runOf(member)));
		assertTrue("No Create without a grant: " + created, created.contains("'Create'"));
		assertNull(storedAgent(NEW_AGENT));

		assertTrue(answerAs(member, () -> tool.getAgent(STORED_AGENT, runOf(member))).contains("\"exists\":true"));
		assertTrue("A record the run may not read does not exist for it.",
			answerAs(outsider, () -> tool.getAgent(STORED_AGENT, runOf(outsider))).contains("\"exists\":false"));
		assertFalse(answerAs(outsider, () -> tool.listAgents(runOf(outsider))).contains(STORED_AGENT));

		String changed = answerAs(member,
			() -> tool.updateAgent(STORED_AGENT, null, "Changed.", null, null, null, null, runOf(member)));
		assertTrue("No Write without a grant: " + changed, changed.contains("'Write'"));
		String unknown = answerAs(outsider,
			() -> tool.updateAgent(STORED_AGENT, null, "Changed.", null, null, null, null, runOf(outsider)));
		assertTrue("Unknown for the outsider: " + unknown, unknown.contains("No application agent named"));
		String deleted = answerAs(member, () -> tool.deleteAgent(STORED_AGENT, runOf(member)));
		assertTrue("No Delete without a grant: " + deleted, deleted.contains("'Delete'"));

		String byAdministrator = answerAs(administrator, () -> tool.createAgent(NEW_AGENT, null, "You help.", null,
			List.of("some-server"), null, null, runOf(administrator)));
		assertTrue(byAdministrator, byAdministrator.contains("\"success\":true"));
	}

	/** Who may create an agent still may not set its MCP servers: they are the administrator's. */
	public void testTheMcpServersOfANewAgentAreTheAdministrators() {
		AgentAuthoringTool tool = new AgentAuthoringTool();
		ModelSecurity mayCreate = new Narrowed(rights(member), true, null);

		String withServer = answerAs(member, () -> tool.createAgent(NEW_AGENT, null, "You help.", null,
			List.of("some-server"), null, null, runOf(mayCreate)));
		assertTrue("Refused for the attribute: " + withServer, withServer.contains("mcpServers"));
		assertNull(storedAgent(NEW_AGENT));

		String withoutServer = answerAs(member,
			() -> tool.createAgent(NEW_AGENT, null, "You help.", null, null, null, null, runOf(mayCreate)));
		assertTrue(withoutServer, withoutServer.contains("\"success\":true"));
	}

	/** The tool hands out an attribute only where the run may read it. */
	public void testTheToolAnswersOnlyTheReadableAttributes() {
		AgentAuthoringTool tool = new AgentAuthoringTool();
		ModelSecurity withoutInstructions =
			new Narrowed(rights(member), false, TlAiCoreFactory.getSystemPromptAgentAttr());

		String detail = answerAs(member, () -> tool.getAgent(STORED_AGENT, runOf(withoutInstructions)));
		assertTrue(detail, detail.contains("\"exists\":true"));
		assertFalse(detail, detail.contains("The instructions of a stored agent."));
		assertTrue(answerAs(member, () -> tool.getAgent(STORED_AGENT, runOf(member)))
			.contains("The instructions of a stored agent."));
	}

	/** The tool changes nothing without a user account. */
	public void testTheToolRefusesASystemContext() {
		AgentAuthoringTool tool = new AgentAuthoringTool();
		String created = ThreadContext.inSystemContext(TestStoredAgentAccess.class,
			() -> tool.createAgent(NEW_AGENT, null, "You help.", null, null, null, null, runOf(administrator)));
		assertTrue(created, created.contains("without a user account"));
		assertNull(storedAgent(NEW_AGENT));
	}

	/**
	 * Asserts that the given commit is refused by a rule with the given message, in the chain of the
	 * exception or in the arguments of a message there.
	 */
	private static void assertRefused(ResKey1 rule, Runnable commit) {
		String expected = rule.fill("").getKey();
		try {
			commit.run();
		} catch (RuntimeException ex) {
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				if (cause instanceof I18NFailure failure && mentions(failure.getErrorKey(), expected)) {
					return;
				}
			}
			throw new AssertionError("Refused for another reason than " + expected + ".", ex);
		}
		fail("The commit must be refused by the rule " + expected + ".");
	}

	private static boolean mentions(ResKey key, String expected) {
		if (key == null) {
			return false;
		}
		if (key.hasKey() && expected.equals(key.getKey())) {
			return true;
		}
		for (Object argument : key.arguments()) {
			if (argument instanceof ResKey nested && mentions(nested, expected)) {
				return true;
			}
		}
		return false;
	}

	private static AgentSource sourceOf(String name, Iterable<AgentDefinition> agents) {
		for (AgentDefinition agent : agents) {
			if (agent.name().equalsIgnoreCase(name)) {
				return agent.source();
			}
		}
		return null;
	}

	/** A run of the given account whose tool policy permits everything. */
	private static ToolContext runOf(Person account) {
		return runOf(rights(account));
	}

	/** A run with the given model rights whose tool policy permits everything. */
	private static ToolContext runOf(ModelSecurity rights) {
		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return rights;
			}
		};
		return new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
	}

	/** What the given job answers, run for the given account with the security checks on. */
	private static <T> T answerAs(Person account, Supplier<T> job) {
		return TLContextManager.<T, RuntimeException, RuntimeException> inPersonContext(account, job::get);
	}

	/** Runs the given job for the given account with the security checks on. */
	private static void asPerson(Person account, Runnable job) {
		TLContextManager.<Void, RuntimeException, RuntimeException> inPersonContext(account, () -> {
			job.run();
			return null;
		});
	}

	private static void createAgent(String name) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TlAiCoreFactory.getInstance().createAgent().setName(name);
			tx.commit();
		}
	}

	private static void createSkill(String name) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TlAiCoreFactory.getInstance().createSkill().setName(name);
			tx.commit();
		}
	}

	private static ModelSecurity rights(Person account) {
		return new ModelAccessRightsSecurity(account);
	}

	private static AgentDefinitionQuery query() {
		return AgentDefinitionQuery.getInstance();
	}

	private static List<String> names(Iterable<AgentDefinition> agents) {
		List<String> names = new ArrayList<>();
		for (AgentDefinition agent : agents) {
			names.add(agent.name());
		}
		return names;
	}

	private static Agent storedAgent(String name) {
		for (Agent agent : query().getAllKBAgents()) {
			if (agent.tValid() && name.equals(agent.getName())) {
				return agent;
			}
		}
		return null;
	}

	private static Skill storedSkill(String name) {
		for (Skill skill : query().getAllKBSkills()) {
			if (skill.tValid() && name.equals(skill.getName())) {
				return skill;
			}
		}
		return null;
	}

	private static void deleteRecords() {
		List<TLObject> doomed = new ArrayList<>();
		for (String name : AGENT_NAMES) {
			Agent agent = storedAgent(name);
			if (agent != null) {
				doomed.add(agent);
			}
		}
		for (String name : SKILL_NAMES) {
			Skill skill = storedSkill(name);
			if (skill != null) {
				doomed.add(skill);
			}
		}
		if (doomed.isEmpty()) {
			return;
		}
		ThreadContext.inSystemContext(TestStoredAgentAccess.class, () -> {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				for (TLObject record : doomed) {
					record.tDelete();
				}
				tx.commit();
			}
		});
	}

	/**
	 * The model rights of an account, with Create granted on top or the Read of one attribute taken
	 * away: shapes an application configures, which the shipped definition does not have.
	 */
	private static final class Narrowed implements ModelSecurity {

		private final ModelSecurity _base;

		private final boolean _mayCreate;

		private final TLStructuredTypePart _hidden;

		Narrowed(ModelSecurity base, boolean mayCreate, TLStructuredTypePart hidden) {
			_base = base;
			_mayCreate = mayCreate;
			_hidden = hidden;
		}

		@Override
		public boolean canRead(TLObject obj) {
			return _base.canRead(obj);
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return !attribute.equals(_hidden) && _base.canRead(obj, attribute);
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return _base.canWrite(obj);
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return _base.canWrite(obj, attribute);
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return _base.canDelete(obj);
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return _mayCreate || _base.canCreate(type, context);
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
			return _mayCreate || _base.canCreate(parent, compositionAttribute, type);
		}

		@Override
		public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return _base.canSetInitial(draft, attribute);
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return _base.canPerform(obj, operation);
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return _base.filterReadable(objects);
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return _base.accessibleTypes(operation);
		}
	}

	/**
	 * Creates the test suite with the services the definition is made of: the model, the groups
	 * and roles, the access manager that derives the roles, the rights service that grants them, and
	 * the configured agents.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestStoredAgentAccess.class),
				ModelService.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				InitialGroupManager.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE));
	}
}

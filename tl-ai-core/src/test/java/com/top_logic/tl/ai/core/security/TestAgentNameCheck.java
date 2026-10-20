/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.InitialDataSetupService;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.model.ModelService;

/**
 * Tests that an agent the initial data import creates may be named in the agent tool access policy
 * (#663).
 *
 * <p>
 * The agents of an application are its configured ones and its {@code tl.ai.core:Agent} records,
 * and the initial data import creates records from {@code WEB-INF/data/agents.objects.xml}. Nothing
 * orders {@link AiToolAccessService} after that import, so on a fresh database a policy that grants
 * such an agent was refused at start-up whenever the access service happened to start first. The
 * initial data import is not started here, which is that situation: the access service starts
 * before the record exists.
 * </p>
 */
public class TestAgentNameCheck extends BasicTestCase {

	private static final String ROLE = "agentNameCheckRole";

	private static final String IMPORTED_AGENT = "AgentNameCheck_Imported";

	/**
	 * A knowledge base with the agent model and the access service's dependencies, and without the
	 * initial data import.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestAgentNameCheck.class),
				ModelService.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertFalse("This test is about a start before the initial data import.",
			InitialDataSetupService.Module.INSTANCE.isActive());
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (BoundedRole.getRoleByName(kb, ROLE) == null) {
			try (Transaction tx = kb.beginTransaction()) {
				BoundedRole.createBoundedRole(ROLE);
				tx.commit();
			}
		}
	}

	/**
	 * A policy that grants an agent the import creates later starts, and the check after the import
	 * accepts it.
	 */
	public void testAnAgentTheImportCreatesIsAccepted() {
		AiToolAccessService service = start(policyGranting(IMPORTED_AGENT));
		Agent agent = createAgent(IMPORTED_AGENT);
		try {
			service.refuseUnknownAgents();
		} finally {
			delete(agent);
		}
	}

	/**
	 * An agent that is neither configured nor a record after the import refuses the start, naming the
	 * agent.
	 */
	public void testAnAgentNobodyCreatesIsRefusedAfterTheImport() {
		AiToolAccessService service = start(policyGranting("AgentNameCheck_Misspelled"));
		try {
			service.refuseUnknownAgents();
			fail("An agent the application does not have must refuse the policy.");
		} catch (ConfigurationError expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("AgentNameCheck_Misspelled"));
		}
	}

	/** A configured agent is accepted before and after the import. */
	public void testAConfiguredAgentIsAccepted() {
		start(policyGranting("TL_ModelExplorer")).refuseUnknownAgents();
	}

	private static AiToolAccessService start(AiToolAccessService.Config<?> config) {
		AiToolAccessService service =
			new AiToolAccessService(new DefaultInstantiationContext(TestAgentNameCheck.class), config);
		service.startUp();
		return service;
	}

	/** A policy whose only role may run the given agent. */
	@SuppressWarnings("unchecked")
	private static AiToolAccessService.Config<?> policyGranting(String agentName) {
		AiToolAccessService.Config<?> policy =
			TypedConfiguration.newConfigItem(AiToolAccessService.Config.class);
		ToolAccessPolicy.AgentRoleAccess role =
			TypedConfiguration.newConfigItem(ToolAccessPolicy.AgentRoleAccess.class);
		role.setName(ROLE);
		ToolAccessPolicy.AgentGrant grant =
			TypedConfiguration.newConfigItem(ToolAccessPolicy.AgentGrant.class);
		grant.setAgents(List.of(agentName));
		role.getGrants().add(grant);
		policy.getAgentAccess().getRoles().put(role.getName(), role);
		return policy;
	}

	private static Agent createAgent(String name) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
			tx.commit();
			return agent;
		}
	}

	private static void delete(Agent agent) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			agent.tDelete();
			tx.commit();
		}
	}
}

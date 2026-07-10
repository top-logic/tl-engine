/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link NameBasedInstanceResolver}.
 */
public class TestNameBasedInstanceResolver extends BasicTestCase {

	private static final String AGENT_KIND = "tl.ai.core:Agent";

	/**
	 * Reproduces the re-import failure: the resolver is invoked once per imported object within a
	 * single transaction. After the first call deletes a matching instance, a later call must not
	 * trip over that already-deleted instance while scanning all instances of the type.
	 *
	 * <p>
	 * Before the fix, the second {@link NameBasedInstanceResolver#resolve} call threw
	 * {@code DeletedObjectAccess} when reading the {@code name} of the instance deleted by the
	 * first call.
	 * </p>
	 */
	public void testResolveSkipsInstancesDeletedByEarlierCall() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			createAgent(factory, "A");
			createAgent(factory, "B");
			tx.commit();
		}

		NameBasedInstanceResolver resolver = new NameBasedInstanceResolver();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			// Importing object "A": its pre-existing instance is deleted, null signals "create new".
			assertNull(resolver.resolve(null, null, AGENT_KIND, "A"));
			// Importing object "B": the scan must skip the now-deleted "A" instead of reading it.
			assertNull(resolver.resolve(null, null, AGENT_KIND, "B"));
			tx.commit();
		}

		assertNull("Existing agent 'A' must have been deleted by the resolver.", findAgent("A"));
		assertNull("Existing agent 'B' must have been deleted by the resolver.", findAgent("B"));
	}

	private static Agent createAgent(TlAiCoreFactory factory, String name) {
		Agent agent = factory.createAgent();
		agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
		return agent;
	}

	private static TLObject findAgent(String name) {
		TLClass type = TlAiCoreFactory.getAgentType();
		List<TLObject> instances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);
		for (TLObject instance : instances) {
			if (name.equals(instance.tValueByName(Agent.NAME_ATTR))) {
				return instance;
			}
		}
		return null;
	}

	/**
	 * Creates the test suite with the required service dependencies (KB + {@link ModelService}).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestNameBasedInstanceResolver.class),
				ModelService.Module.INSTANCE));
	}
}

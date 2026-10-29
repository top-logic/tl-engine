/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.secdemo;

import java.util.List;

import junit.framework.Test;

import com.top_logic.ai.demo.SecDemoPersonaTest;
import com.top_logic.ai.demo.secdemo.SecDemoSetup.PersonaConfig;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;

/**
 * Tests that the start of the security test bed checks its persona matrix only on committed data
 * (#626).
 *
 * <p>
 * The startup hook runs the setup inside its own transaction, so the setup's own commit is nested.
 * Checked right after that commit, every persona and project created by this run would be reported
 * as missing. Here the setup runs inside an enclosing transaction of the test, which is that
 * situation, and creates one new persona.
 * </p>
 */
public class SecDemoFixtureStartTest extends SecDemoPersonaTest {

	/**
	 * A start inside an enclosing transaction checks once that transaction has committed, and then
	 * finds the new persona's role.
	 */
	public void testAStartInsideATransactionChecksAfterItsCommit() {
		SecDemoSetup setup = new SecDemoSetup(
			new DefaultInstantiationContext(SecDemoFixtureStartTest.class), withPersona("fred"));
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		// One system interaction around the setup, as the startup hook has: the setup joins it, and
		// with it the enclosing transaction.
		ThreadContext.inSystemContext(SecDemoFixtureStartTest.class, () -> {
			try (Transaction startup = kb.beginTransaction()) {
				setup.perform();
				assertNull("Nothing the start created is committed yet, so nothing may be judged: "
					+ setup.lastProblems(), setup.lastProblems());
				startup.commit();
			}
		});

		assertEquals("Checked once the start-up transaction committed, the new persona holds its role.",
			List.of(), setup.lastProblems());
	}

	/** The shipped configuration of the fixture, with one more partner persona. */
	private static SecDemoSetup.Config<?> withPersona(String login) {
		SecDemoSetup.Config<?> config = TypedConfiguration.copy(secDemoConfig());
		PersonaConfig persona = TypedConfiguration.newConfigItem(PersonaConfig.class);
		TypedConfigUtil.setProperty(persona, PersonaConfig.LOGIN, login);
		TypedConfigUtil.setProperty(persona, PersonaConfig.GROUPS, List.of("projectPartners"));
		TypedConfigUtil.setProperty(persona, PersonaConfig.ROLES, List.of("projectPartner"));
		config.getPersonas().add(persona);
		return config;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoFixtureStartTest.class);
	}
}

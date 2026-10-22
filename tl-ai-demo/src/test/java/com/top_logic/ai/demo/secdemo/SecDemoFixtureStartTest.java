/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.secdemo;

import java.util.List;

import junit.framework.Test;

import com.top_logic.ai.demo.SecDemoPersonaTest;
import com.top_logic.ai.demo.secdemo.SecDemoSetupService.PersonaConfig;
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
 * The application starts every service inside one start-up transaction. On the first boot of a
 * fresh database the fixture's own commit is therefore nested, and the check that followed it
 * reported every persona and project as missing, while the second boot on the same database was
 * silent. Here the start runs inside an enclosing transaction of the test, which is that situation,
 * and creates one new persona.
 * </p>
 */
public class SecDemoFixtureStartTest extends SecDemoPersonaTest {

	/**
	 * A start inside an enclosing transaction checks once that transaction has committed, and then
	 * finds the new persona's role.
	 */
	public void testAStartInsideATransactionChecksAfterItsCommit() {
		SecDemoSetupService service = new SecDemoSetupService(
			new DefaultInstantiationContext(SecDemoFixtureStartTest.class), withPersona("fred"));
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		// One system interaction around the start, as the application's start has: the fixture's
		// setup joins it, and with it the enclosing transaction.
		ThreadContext.inSystemContext(SecDemoFixtureStartTest.class, () -> {
			try (Transaction startup = kb.beginTransaction()) {
				service.startUp();
				assertNull("Nothing the start created is committed yet, so nothing may be judged: "
					+ service.lastProblems(), service.lastProblems());
				startup.commit();
			}
		});

		assertEquals("Checked once the start-up transaction committed, the new persona holds its role.",
			List.of(), service.lastProblems());
	}

	/** The shipped configuration of the fixture, with one more partner persona. */
	private static SecDemoSetupService.Config<?> withPersona(String login) {
		SecDemoSetupService.Config<?> config = TypedConfiguration.copy(SecDemoSetupService.getInstance().getConfig());
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

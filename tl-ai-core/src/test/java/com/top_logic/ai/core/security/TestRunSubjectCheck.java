/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * {@link RunSecurity#contextFor(RunPrincipal)} keeps the subject of a run only if the run may read
 * it, decided by the model access rights of the account the run acts as.
 *
 * <p>
 * The subject is a conversation of one account. Under the access policy that this module ships for
 * conversations, only its owner may read it. No policy is written for the test: the default factory
 * and the shipped rights decide.
 * </p>
 */
public class TestRunSubjectCheck extends BasicTestCase {

	/** May read {@link #subject}. */
	private static Person owner;

	/** May not read {@link #subject}. */
	private static Person stranger;

	private static AgentSession subject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("run-subject-owner");
		stranger = TestPerson.createPerson("run-subject-stranger");
		subject = AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, owner, "Website relaunch");
		HistoryUtils.updateSessionRevision();
	}

	/** An account that may read the subject keeps it, and everything else the entry point stated. */
	public void testReadableSubjectIsKept() {
		RunPrincipal stated = new RunPrincipal(owner, RunEntryPoint.SCRIPT, true, "conv-1", subject);

		assertEquals(stated, RunSecurity.contextFor(stated).principal());
	}

	/**
	 * An account that may not read the subject gets a run without one, and everything else the entry
	 * point stated.
	 */
	public void testUnreadableSubjectIsDropped() {
		RunPrincipal stated = new RunPrincipal(stranger, RunEntryPoint.SCRIPT, true, "conv-2", subject);

		assertEquals(new RunPrincipal(stranger, RunEntryPoint.SCRIPT, true, "conv-2", null),
			RunSecurity.contextFor(stated).principal());
	}

	/** A run that states no subject has none. */
	public void testRunWithoutSubjectHasNone() {
		RunPrincipal stated = new RunPrincipal(owner, RunEntryPoint.CHAT, false);

		assertEquals(stated, RunSecurity.contextFor(stated).principal());
	}

	/**
	 * The services of the conversations and of their access policy, and the tool access the default
	 * context of a run is built with.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunSubjectCheck.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link ConversationBackendService#featuresFor(TLStructuredType)}: what an empty chat
 * about a subject of a type offers.
 */
public class TestConversationBackendService extends BasicTestCase {

	private static final BackendFeatures ATTACHMENTS_ONLY =
		new BackendFeatures(false, false, false, false, false, false, true, false);

	private static final String CLAIMED_TYPE = "tl.ai.execution:AgentSession";

	private static final String UNCLAIMED_TYPE = "tl.accounts:Person";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ScriptedBackend.install(subject -> null,
			subjectType -> subjectType.equals(type(CLAIMED_TYPE)) ? ATTACHMENTS_ONLY : null);
	}

	@Override
	protected void tearDown() throws Exception {
		ScriptedBackend.reset();
		super.tearDown();
	}

	/** The features of a type a resolver answers are the ones the resolver names. */
	public void testResolverAnswersItsType() {
		assertEquals(ATTACHMENTS_ONLY, ConversationBackendService.featuresFor(type(CLAIMED_TYPE)));
	}

	/** A type no resolver answers offers what the framework's own agent offers. */
	public void testUnclaimedTypeIsAnsweredInternally() {
		assertEquals(BackendFeatures.INTERNAL, ConversationBackendService.featuresFor(type(UNCLAIMED_TYPE)));
	}

	/** A conversation about nothing offers what the framework's own agent offers. */
	public void testNoTypeIsAnsweredInternally() {
		assertEquals(BackendFeatures.INTERNAL, ConversationBackendService.featuresFor(null));
	}

	private static TLStructuredType type(String name) {
		return (TLStructuredType) TLModelUtil.findType(name);
	}

	/** The suite, on the model and the resolver configuration of this module's tests. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestConversationBackendService.class),
				ModelService.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}

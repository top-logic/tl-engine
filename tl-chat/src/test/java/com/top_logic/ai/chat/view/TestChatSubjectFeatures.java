/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.ConversationFeaturesDto;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.view.ChatCommandDriver.Conversation;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * A {@code <chat-conversation>} with a {@code subject} tells the browser what a conversation about
 * the subject in its channel offers ({@link ChatConversationControl#STATE_SUBJECT_FEATURES}), so that
 * an empty chat shows only the controls the subject's backend supports.
 */
public class TestChatSubjectFeatures extends SecurityCheckedTestCase {

	private static final String SUBJECT = "chatSubject";

	private static final String WITH_SUBJECT = "subject='" + SUBJECT + "'";

	private static final BackendFeatures ATTACHMENTS_ONLY =
		new BackendFeatures(false, false, false, false, false, false, true, false);

	private static Person anna;

	private ChatCommandDriver _driver;

	private ViewChannel _subject;

	/** An object of the type the installed resolver answers. */
	private TLObject _claimed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-subject-features");
		}
		_claimed = ChatHistoryService.createSession("about something " + getName(), anna);
		HistoryUtils.updateSessionRevision();
		TLStructuredType claimedType = _claimed.tType();
		ScriptedBackend.install(subject -> null,
			subjectType -> subjectType.equals(claimedType) ? ATTACHMENTS_ONLY : null);

		_driver = new ChatCommandDriver("subjectFeaturesWindow", "subject-features-" + getName());
		_subject = new DefaultViewChannel(SUBJECT);
		_driver.registerChannel(SUBJECT, _subject);
	}

	@Override
	protected void tearDown() throws Exception {
		ScriptedBackend.reset();
		_driver.close();
		super.tearDown();
	}

	/** A subject of a type whose backend offers only attachments: the chat offers only attachments. */
	public void testSubjectOfAClaimedType() throws Exception {
		_subject.set(_claimed);
		Conversation conversation = _driver.conversation(anna, WITH_SUBJECT, "");
		try {
			assertFeatures(ATTACHMENTS_ONLY, conversation);
		} finally {
			conversation.cleanupTree();
		}
	}

	/** The subject changes to one of a type no resolver answers: the chat offers everything. */
	public void testSwitchToAnUnclaimedType() throws Exception {
		_subject.set(_claimed);
		Conversation conversation = _driver.conversation(anna, WITH_SUBJECT, "");
		try {
			assertFeatures(ATTACHMENTS_ONLY, conversation);

			_subject.set(anna);
			assertFeatures(BackendFeatures.INTERNAL, conversation);

			_subject.set(_claimed);
			assertFeatures(ATTACHMENTS_ONLY, conversation);
		} finally {
			conversation.cleanupTree();
		}
	}

	/** An empty subject channel: the chat offers what the framework's own agent offers. */
	public void testEmptyChannel() throws Exception {
		Conversation conversation = _driver.conversation(anna, WITH_SUBJECT, "");
		try {
			assertFeatures(BackendFeatures.INTERNAL, conversation);

			_subject.set(_claimed);
			assertFeatures(ATTACHMENTS_ONLY, conversation);

			_subject.set(null);
			assertFeatures(BackendFeatures.INTERNAL, conversation);
		} finally {
			conversation.cleanupTree();
		}
	}

	/** An element without a subject offers what the framework's own agent offers. */
	public void testNoSubjectConfigured() throws Exception {
		_subject.set(_claimed);
		Conversation conversation = _driver.conversation(anna);
		try {
			assertFeatures(BackendFeatures.INTERNAL, conversation);
		} finally {
			conversation.cleanupTree();
		}
	}

	private static void assertFeatures(BackendFeatures expected, Conversation conversation) {
		ConversationFeaturesDto actual =
			(ConversationFeaturesDto) conversation.state(ChatConversationControl.STATE_SUBJECT_FEATURES);
		assertNotNull("No features were written.", actual);
		assertEquals(expected, new BackendFeatures(actual.isRegenerate(), actual.isEditMessages(),
			actual.isAgentPicker(), actual.isModelPicker(), actual.isEffortPicker(), actual.isMcpPicker(),
			actual.isAttachments(), actual.isApprovals()));
	}

	/** The suite, on the services a chat element is rendered with. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSubjectFeatures.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}

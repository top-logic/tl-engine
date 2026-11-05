/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.DraftAttachmentDto;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * What the chat controls hold for the message being written, before it is sent: the files attached
 * to it ({@link ChatControl#handleUpload}, {@link ChatControl#CMD_REMOVE_ATTACHMENT}) and what the
 * page states about the situation ({@link ChatViewBinding#facts()}).
 */
public class TestChatDraftCommands extends BasicTestCase {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G' };

	private static Person anna;

	private ChatCommandDriver _driver;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-writing-a-message");
		}
		_driver = new ChatCommandDriver("draftWindow", "draft-" + getName());
	}

	@Override
	protected void tearDown() throws Exception {
		_driver.close();
		super.tearDown();
	}

	/** An uploaded file is attached, and the composer is told about it through the state. */
	public void testAnUploadedFileIsAttached() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna);
		try {
			Map<String, Object> reply = _driver.upload(conversation, "bild.png", "image/png", PNG);

			assertNull("Nothing was refused: " + reply, reply.get(ChatControl.FIELD_REASON));
			List<?> shown = (List<?>) conversation.state(ChatControl.STATE_DRAFT_ATTACHMENTS);
			assertEquals(1, shown.size());
			DraftAttachmentDto file = (DraftAttachmentDto) shown.get(0);
			assertEquals("bild.png", file.getFileName());
			assertEquals("image/png", file.getMimeType());
			assertEquals(PNG.length, file.getSize());
			assertFalse(file.getId().isEmpty());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A file of a type a message cannot carry is not attached, and the answer says why. */
	public void testAFileOfAnotherTypeIsRefused() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna);
		try {
			Map<String, Object> reply = _driver.upload(conversation, "notiz.txt", "text/plain", PNG);

			assertEquals(List.of(Resources.getInstance().getString(
				I18NConstants.ERROR_ATTACHMENT_TYPE__FILE.fill("notiz.txt"))), reply.get(ChatControl.FIELD_REASON));
			assertEquals(List.of(), ChatCommandDriver.attachments(conversation).entries());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A message carries at most five files; the sixth is refused with a sentence of its own. */
	public void testTheSixthFileIsRefused() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna);
		try {
			for (int n = 1; n <= ChatAttachments.MAX_ATTACHMENTS; n++) {
				assertNull(_driver.upload(conversation, "bild" + n + ".png", "image/png", PNG).get(ChatControl.FIELD_REASON));
			}
			Map<String, Object> reply = _driver.upload(conversation, "zuviel.png", "image/png", PNG);

			assertEquals(List.of(Resources.getInstance().getString(
				I18NConstants.ERROR_TOO_MANY_ATTACHMENTS__FILE_MAX.fill("zuviel.png",
					Integer.valueOf(ChatAttachments.MAX_ATTACHMENTS)))),
				reply.get(ChatControl.FIELD_REASON));
			assertEquals(ChatAttachments.MAX_ATTACHMENTS, ChatCommandDriver.attachments(conversation).entries().size());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A file larger than a message may carry is refused before it is held. */
	public void testTheUploadLimitIsTheAttachmentLimit() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna);
		try {
			assertTrue(conversation.maxUploadSize() > 0);
			assertTrue(conversation.maxUploadSize() <= ChatAttachments.MAX_ATTACHMENT_SIZE);
		} finally {
			conversation.cleanupTree();
		}
	}

	/** The command takes the file it names off the message, and the state follows. */
	public void testAFileIsTakenOffAgain() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna);
		try {
			_driver.upload(conversation, "eins.png", "image/png", PNG);
			_driver.upload(conversation, "zwei.png", "image/png", PNG);
			String first = ChatCommandDriver.attachments(conversation).entries().get(0).id();

			_driver.command(conversation, ChatControl.CMD_REMOVE_ATTACHMENT,
				Map.of(ChatControl.ARG_ATTACHMENT_ID, first));

			List<?> shown = (List<?>) conversation.state(ChatControl.STATE_DRAFT_ATTACHMENTS);
			assertEquals(1, shown.size());
			assertEquals("zwei.png", ((DraftAttachmentDto) shown.get(0)).getFileName());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A send without text and without files sends nothing, and creates no chat. */
	public void testAnEmptySendSendsNothing() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna);
		try {
			Map<String, Object> reply = _driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "   "));

			assertEquals(ChatControl.STATUS_ERROR, reply.get(ChatControl.FIELD_STATUS));
			assertNull(reply.get(ChatControl.FIELD_SESSION_ID));
			assertNull(ChatCommandDriver.tracker(conversation).getActiveSessionId());
		} finally {
			conversation.cleanupTree();
		}
	}

	/**
	 * What the page states is evaluated by the server when a message is sent, for as long as the
	 * conversation is displayed; the browser carries none of it.
	 */
	public void testTheFactsAreThePagesWhileItIsDisplayed() throws Exception {
		ChatCommandDriver.Conversation conversation = _driver.conversation(anna,
			"<context><fact name='Application' stable='true'><value>\"Demo\"</value></fact></context>");
		ChatViewBinding binding = conversation.binding();
		try {
			assertEquals(List.of(new EnvironmentFact("Application", "Demo", true)), binding.facts());
			assertNull("The browser is not handed the facts to carry back.", conversation.state("contextFacts"));
		} finally {
			conversation.cleanupTree();
		}
		assertEquals(List.of(), binding.facts());
	}

	/** The model, a login and the services a chat control reads. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatDraftCommands.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}

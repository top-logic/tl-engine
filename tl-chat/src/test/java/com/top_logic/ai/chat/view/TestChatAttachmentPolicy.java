/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.backend.AttachmentPolicy;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.react.control.upload.UploadSupport;

/**
 * Which files {@link ChatAttachments} takes under an {@link AttachmentPolicy}, when a file is
 * attached and when the message is sent.
 */
public class TestChatAttachmentPolicy extends BasicTestCase {

	private static final AttachmentPolicy CSV_AND_ZIP = new AttachmentPolicy("text/csv, *.zip", 2, 100, 150);

	/** A policy allows the files of its types, up to its limits. */
	public void testAcceptsItsTypes() {
		ChatAttachments attachments = new ChatAttachments();

		assertNull(attachments.add("daten.csv", "text/csv", bytes(50), CSV_AND_ZIP));
		assertNull(attachments.add("archiv.zip", "application/zip", bytes(50), CSV_AND_ZIP));

		assertEquals(List.of("daten.csv", "archiv.zip"), names(attachments));
	}

	/** A file of another type is refused, naming the types that are allowed. */
	public void testRefusesAnotherType() {
		ChatAttachments attachments = new ChatAttachments();

		assertEquals(I18NConstants.ERROR_ATTACHMENT_TYPE__FILE_TYPES.fill("bild.png", "text/csv, *.zip"),
			attachments.add("bild.png", "image/png", bytes(10), CSV_AND_ZIP));
		assertEquals(List.of(), names(attachments));
	}

	/** A file larger than the policy's limit of one file is refused, naming the limit. */
	public void testRefusesATooLargeFile() {
		ChatAttachments attachments = new ChatAttachments();

		assertEquals(I18NConstants.ERROR_ATTACHMENT_TOO_LARGE__FILE_MAX.fill("gross.csv", UploadSupport.sizeLabel(100)),
			attachments.add("gross.csv", "text/csv", bytes(101), CSV_AND_ZIP));
		assertEquals(List.of(), names(attachments));
	}

	/** One file more than the policy allows is refused. */
	public void testRefusesOneFileTooMany() {
		ChatAttachments attachments = new ChatAttachments();
		attachments.add("eins.csv", "text/csv", bytes(10), CSV_AND_ZIP);
		attachments.add("zwei.csv", "text/csv", bytes(10), CSV_AND_ZIP);

		assertEquals(I18NConstants.ERROR_TOO_MANY_ATTACHMENTS__FILE_MAX.fill("drei.csv", Integer.valueOf(2)),
			attachments.add("drei.csv", "text/csv", bytes(10), CSV_AND_ZIP));
		assertEquals(List.of("eins.csv", "zwei.csv"), names(attachments));
	}

	/** A file that would make the message larger than the policy allows is refused. */
	public void testRefusesAMessageTooLarge() {
		ChatAttachments attachments = new ChatAttachments();
		attachments.add("eins.csv", "text/csv", bytes(90), CSV_AND_ZIP);

		assertEquals(
			I18NConstants.ERROR_MESSAGE_TOO_LARGE__FILE_MAX.fill("zwei.zip", UploadSupport.sizeLabel(150)),
			attachments.add("zwei.zip", "application/zip", bytes(90), CSV_AND_ZIP));
		assertEquals(List.of("eins.csv"), names(attachments));
	}

	/** {@link AttachmentPolicy#NONE} takes no file at all. */
	public void testNoneRefusesEverything() {
		ChatAttachments attachments = new ChatAttachments();

		assertFalse(AttachmentPolicy.NONE.allowsAny());
		assertEquals(com.top_logic.ai.chat.backend.I18NConstants.ATTACHMENTS_NOT_SUPPORTED,
			attachments.add("bild.png", "image/png", bytes(10), AttachmentPolicy.NONE));
	}

	/** A pattern accepting every file accepts a file of any type. */
	public void testAllTypes() {
		AttachmentPolicy all = new AttachmentPolicy("*/*", 5, 0, 0);
		ChatAttachments attachments = new ChatAttachments();

		assertNull(attachments.add("notiz.txt", "text/plain", bytes(10), all));
		assertNull(attachments.add("programm.exe", "", bytes(10), all));
	}

	/** The files the language model reads: images and PDF, nothing else. */
	public void testModelReadable() {
		ChatAttachments attachments = new ChatAttachments();

		assertNull(attachments.add("bild.png", "image/png", bytes(10), AttachmentPolicy.MODEL_READABLE));
		assertNull(attachments.add("foto.jpg", "image/jpeg", bytes(10), AttachmentPolicy.MODEL_READABLE));
		assertNull(attachments.add("bericht.pdf", "application/pdf", bytes(10), AttachmentPolicy.MODEL_READABLE));
		assertNull(attachments.add("grafik.webp", "image/webp", bytes(10), AttachmentPolicy.MODEL_READABLE));
		assertNull(attachments.add("animation.gif", "image/gif", bytes(10), AttachmentPolicy.MODEL_READABLE));
		assertEquals(
			I18NConstants.ERROR_ATTACHMENT_TYPE__FILE_TYPES.fill("daten.csv",
				AttachmentPolicy.MODEL_READABLE.typesLabel()),
			attachments.add("daten.csv", "text/csv", bytes(10), AttachmentPolicy.MODEL_READABLE));
		assertEquals(List.of("bild.png", "foto.jpg", "bericht.pdf", "grafik.webp", "animation.gif"),
			names(attachments));
	}

	/** A configuration without settings describes {@link AttachmentPolicy#MODEL_READABLE}. */
	public void testConfigurationDefault() throws Exception {
		assertEquals(AttachmentPolicy.MODEL_READABLE,
			AttachmentPolicy.of(TypedConfiguration.newConfigItem(AttachmentPolicy.Config.class)));
		assertEquals(AttachmentPolicy.MODEL_READABLE,
			AttachmentPolicy.of(serviceConfig("<service/>").getInternalAttachments()));
	}

	/** An application narrows the files of its own agent to PNG images: a PDF is refused. */
	public void testConfiguredInternalAttachments() throws Exception {
		ConversationBackendService.Config<?> config = serviceConfig(
			"<service><" + ConversationBackendService.Config.INTERNAL_ATTACHMENTS + " "
				+ AttachmentPolicy.Config.ACCEPTED_TYPES_NAME + "=\"image/png\" "
				+ AttachmentPolicy.Config.MAX_MESSAGE_SIZE + "=\"1m\"/></service>");
		AttachmentPolicy policy = AttachmentPolicy.of(config.getInternalAttachments());
		ChatAttachments attachments = new ChatAttachments();

		assertEquals(new AttachmentPolicy("image/png", AttachmentPolicy.MODEL_READABLE_MAX_FILES,
			AttachmentPolicy.MODEL_READABLE_MAX_FILE_SIZE, 1024 * 1024), policy);
		assertNull(attachments.add("bild.png", "image/png", bytes(10), policy));
		assertEquals(I18NConstants.ERROR_ATTACHMENT_TYPE__FILE_TYPES.fill("bericht.pdf", "image/png"),
			attachments.add("bericht.pdf", "application/pdf", bytes(10), policy));
	}

	/**
	 * The files attached under one policy are checked again under the policy of the chat the
	 * message is sent to.
	 */
	public void testRefusalOnSend() {
		ChatAttachments attachments = new ChatAttachments();
		attachments.add("bild.png", "image/png", bytes(10), AttachmentPolicy.MODEL_READABLE);
		attachments.add("bericht.pdf", "application/pdf", bytes(10), AttachmentPolicy.MODEL_READABLE);
		List<ChatAttachments.Entry> entries = attachments.entries();

		assertNull(ChatAttachments.refusal(entries, AttachmentPolicy.MODEL_READABLE));
		assertNull(ChatAttachments.refusal(List.of(), AttachmentPolicy.NONE));
		assertEquals(
			I18NConstants.ERROR_ATTACHMENT_TYPE__FILE_TYPES.fill("bericht.pdf", "image/png"),
			ChatAttachments.refusal(entries, new AttachmentPolicy("image/png", 5, 0, 0)));
		assertEquals(I18NConstants.ERROR_TOO_MANY_ATTACHMENTS__FILE_MAX.fill("bericht.pdf", Integer.valueOf(1)),
			ChatAttachments.refusal(entries, new AttachmentPolicy("*/*", 1, 0, 0)));
	}

	private static ConversationBackendService.Config<?> serviceConfig(String xml) throws Exception {
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("service",
				TypedConfiguration.getConfigurationDescriptor(ConversationBackendService.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem config = reader.read();
		return (ConversationBackendService.Config<?>) config;
	}

	private static List<String> names(ChatAttachments attachments) {
		return attachments.entries().stream().map(entry -> entry.attachment().getFileName()).toList();
	}

	private static byte[] bytes(int size) {
		return new byte[size];
	}

	/** The suite, with the MIME types a file's type is read from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestChatAttachmentPolicy.class, MimeTypes.Module.INSTANCE));
	}
}

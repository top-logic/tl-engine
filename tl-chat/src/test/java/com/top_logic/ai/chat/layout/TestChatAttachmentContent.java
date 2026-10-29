/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;

/**
 * Tests that a chat attachment becomes a payload the providers can actually carry.
 *
 * <p>
 * The upload check in {@code ChatServlet} bounds what may arrive, but the conversion is what builds
 * the payload, so it decides for itself rather than trusting a check that ran elsewhere.
 * </p>
 */
public class TestChatAttachmentContent extends TestCase {

	private static final String BASE64 = "iVBORw0KGgoAAAANSUhEUg==";

	/** Every accepted image type becomes an {@link ImageContent} data URI stating that type. */
	public void testAcceptedImageTypesBecomeImageContent() {
		for (String mediaType : ImageContent.ACCEPTED_MEDIA_TYPES) {
			ImageContent image = (ImageContent) ChatProcessor.toContentPart(attachment("shot", mediaType));

			assertEquals(mediaType, image.mediaType());
			assertEquals("data:" + mediaType + ";base64," + BASE64, image.url());
		}
	}

	/** An image type no provider accepts becomes the unsupported-attachment marker, naming it. */
	public void testUnacceptedImageTypeBecomesTheUnsupportedMarker() {
		ContentPart part = ChatProcessor.toContentPart(attachment("scan.bmp", "image/bmp"));

		String text = ((TextContent) part).text();
		assertTrue("The file must be named: " + text, text.contains("scan.bmp"));
		assertTrue("The type must be named: " + text, text.contains("image/bmp"));
		assertTrue("The accepted types must be stated: " + text,
			text.contains("image/png, image/jpeg, image/gif, image/webp"));
	}

	/** {@code image/jpg} is a spelling no provider takes, so the payload states the canonical one. */
	public void testJpgSpellingIsNormalized() {
		ImageContent image = (ImageContent) ChatProcessor.toContentPart(attachment("photo", "image/jpg"));

		assertEquals("image/jpeg", image.mediaType());
		assertEquals("data:image/jpeg;base64," + BASE64, image.url());
	}

	/** A PDF is unaffected by the image narrowing. */
	public void testPdfStaysAPdf() {
		PdfContent pdf = (PdfContent) ChatProcessor.toContentPart(attachment("invoice.pdf", "application/pdf"));

		assertEquals("application/pdf", pdf.mediaType());
		assertEquals(BASE64, pdf.data());
	}

	/** An attachment with no declared type is not guessed at. */
	public void testMissingMimeTypeBecomesTheUnsupportedMarker() {
		ContentPart part = ChatProcessor.toContentPart(attachment("mystery", null));

		assertTrue(((TextContent) part).text().contains("mystery"));
	}

	private static AttachmentDto attachment(String fileName, String mimeType) {
		return AttachmentDto.create().setFileName(fileName).setMimeType(mimeType).setBase64Data(BASE64);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Tests how {@link AIScriptFunctions} turns TL-Script message content into
 * {@link ContentPart}s - the path every {@code aiChat}, {@code aiAgentChat} and
 * {@code aiAgentChatAsync} call takes.
 *
 * <p>
 * The point of most of these is that content survives the trip: a caller that built a
 * {@link PdfContent} has said what the model must perceive, and the providers can all send it, so
 * the construction path must not flatten it into text on the way.
 * </p>
 */
public class TestAIScriptMessages extends TestCase {

	private static final byte[] PDF_BYTES = "%PDF-1.7\nfake pdf body".getBytes(StandardCharsets.UTF_8);

	private static final byte[] PNG_BYTES = { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3 };

	/**
	 * A {@link ContentPart} in the message list reaches the {@link UserMessage} unchanged. Before
	 * this worked, a caller-built PDF was stringified via {@code toString()} and the model received
	 * base64 as plain text instead of a document it can read.
	 */
	public void testContentPartInListSurvivesUnchanged() {
		TextContent text = new TextContent("Extract the price.");
		PdfContent pdf = new PdfContent("JVBERi0xLjcK", "application/pdf");

		UserMessage message = AIScriptFunctions.createUserMessage(List.of(text, pdf));

		assertEquals("Exactly the two given parts are expected.", 2, message.content().size());
		assertSame(text, message.content().get(0));
		assertSame("The PDF must not be stringified into text.", pdf, message.content().get(1));
	}

	/** A single {@link ContentPart} passed without a list is kept as well. */
	public void testTopLevelContentPartSurvivesUnchanged() {
		PdfContent pdf = new PdfContent("JVBERi0xLjcK", "application/pdf");

		UserMessage message = AIScriptFunctions.createUserMessage(pdf);

		assertEquals(1, message.content().size());
		assertSame(pdf, message.content().get(0));
	}

	/** A PDF {@link BinaryData} becomes a {@link PdfContent} carrying its base64 bytes. */
	public void testPdfBinaryBecomesPdfContent() {
		BinaryData binary = BinaryDataFactory.createBinaryData(PDF_BYTES, "application/pdf", "invoice.pdf");

		ContentPart part = AIScriptFunctions.createBinaryContent(binary);

		PdfContent pdf = (PdfContent) part;
		assertEquals(Base64.getEncoder().encodeToString(PDF_BYTES), pdf.data());
		assertEquals("application/pdf", pdf.mediaType());
	}

	/**
	 * A content type carrying parameters still yields the bare media type: providers accept exactly
	 * {@code application/pdf}.
	 */
	public void testPdfContentTypeWithParametersIsNormalized() {
		BinaryData binary =
			BinaryDataFactory.createBinaryData(PDF_BYTES, "application/pdf; version=1.7", "invoice.pdf");

		PdfContent pdf = (PdfContent) AIScriptFunctions.createBinaryContent(binary);

		assertEquals("application/pdf", pdf.mediaType());
	}

	/**
	 * An empty payload is no document: it must not fail the script call, it is described like any
	 * other content nothing can be shown for.
	 */
	public void testEmptyPdfBinaryStaysDescribedAsText() {
		BinaryData binary = BinaryDataFactory.createBinaryData(new byte[0], "application/pdf", "empty.pdf");

		ContentPart part = AIScriptFunctions.createBinaryContent(binary);

		assertTrue("Expected a description, got: " + part, part instanceof TextContent);
		assertTrue(((TextContent) part).text().contains("application/pdf"));
	}

	/** Regression guard: an image binary still becomes an {@link ImageContent} data URI. */
	public void testImageBinaryBecomesImageContent() {
		BinaryData binary = BinaryDataFactory.createBinaryData(PNG_BYTES, "image/png", "shot.png");

		ImageContent image = (ImageContent) AIScriptFunctions.createBinaryContent(binary);

		assertEquals("image/png", image.mediaType());
		assertEquals("data:image/png;base64," + Base64.getEncoder().encodeToString(PNG_BYTES),
			image.url());
	}

	/** Regression guard: a text binary is inlined with its charset. */
	public void testTextBinaryBecomesTextContent() {
		BinaryData binary = BinaryDataFactory.createBinaryData(
			"Hallo Wörld".getBytes(StandardCharsets.UTF_8), "text/plain; charset=UTF-8", "notes.txt");

		TextContent text = (TextContent) AIScriptFunctions.createBinaryContent(binary);

		assertEquals("Hallo Wörld", text.text());
	}

	/**
	 * Regression guard: an unsupported binary type keeps its base64 description - the fallback is
	 * deliberately unchanged.
	 */
	public void testUnsupportedBinaryStaysDescribedAsText() {
		BinaryData binary = BinaryDataFactory.createBinaryData(PNG_BYTES, "application/zip", "data.zip");

		TextContent text = (TextContent) AIScriptFunctions.createBinaryContent(binary);

		assertTrue("Type and base64 are expected: " + text.text(),
			text.text().contains("application/zip")
				&& text.text().contains(Base64.getEncoder().encodeToString(PNG_BYTES)));
	}

	/** Regression guard: a plain String is a single text part. */
	public void testStringBecomesTextContent() {
		UserMessage message = AIScriptFunctions.createUserMessage("What is TopLogic?");

		assertEquals(1, message.content().size());
		assertEquals(new TextContent("What is TopLogic?"), message.content().get(0));
	}

	/** A mixed list keeps binaries and strings in order. */
	public void testMixedListKeepsOrder() {
		BinaryData binary = BinaryDataFactory.createBinaryData(PDF_BYTES, "application/pdf", "invoice.pdf");

		UserMessage message =
			AIScriptFunctions.createUserMessage(List.of("Read this invoice:", binary, 42));

		assertEquals(3, message.content().size());
		assertEquals(new TextContent("Read this invoice:"), message.content().get(0));
		assertTrue(message.content().get(1) instanceof PdfContent);
		assertEquals(new TextContent("42"), message.content().get(2));
	}

	/** The transcript records the text as sent and names an attachment instead of inlining it. */
	public void testTranscriptNamesAttachmentsInsteadOfInliningThem() {
		UserMessage message = AIScriptFunctions.createUserMessage(List.of(
			new TextContent("Extract the price."),
			new PdfContent("JVBERi0xLjcK", "application/pdf")));

		assertEquals("Extract the price.\n[PDF document: application/pdf]",
			AIScriptFunctions.transcriptText(message));
	}

	/**
	 * A part kind the transcript does not know is still announced, and contributes no empty line:
	 * any {@link ContentPart} a caller builds reaches the message unchanged, so silence would leave
	 * a transcript that does not say anything was sent.
	 */
	public void testTranscriptNamesAnUnrecognisedPart() {
		UserMessage message = AIScriptFunctions.createUserMessage(List.of(
			new TextContent("Before."),
			new ThinkingContent("reasoning", null),
			new TextContent("After.")));

		assertEquals("Before.\n[Attachment: ThinkingContent]\nAfter.",
			AIScriptFunctions.transcriptText(message));
	}

	/**
	 * Assistant history stays text, but a {@link TextContent} contributes its text rather than its
	 * record representation.
	 */
	public void testAssistantMessageUnwrapsTextContent() {
		AssistantMessage message = AIScriptFunctions.createAssistantMessage(
			List.of(new TextContent("Line one."), "Line two."));

		assertEquals(1, message.content().size());
		assertEquals(new TextContent("Line one.\nLine two."), message.content().get(0));
	}
}

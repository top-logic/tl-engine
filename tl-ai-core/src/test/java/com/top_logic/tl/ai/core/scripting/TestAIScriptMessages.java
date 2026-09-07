/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicRequestBuilder;
import com.top_logic.tl.ai.core.session.persist.MessageSerializer;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonUtil;
import de.haumacher.msgbuf.json.JsonWriter;

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

	/** The PNG file signature, so a payload declared only as {@code octet-stream} is recognisable. */
	private static final byte[] PNG_BYTES =
		{ (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3 };

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
	 * A PDF whose source reports no usable content type is still a PDF.
	 *
	 * <p>
	 * This is the case issue #373 is really about after the declared-{@code application/pdf} branch
	 * landed: {@code application/octet-stream} is what TopLogic documents as the content type of a
	 * {@link com.top_logic.basic.io.binary.BinaryDataSource} whose real one is unknown, which is what
	 * a stored or uploaded document carries when nothing recorded it. Before the fix this produced
	 * {@code TextContent[text=[Binary content: application/octet-stream, base64: JVBERi0...]]}.
	 * </p>
	 */
	public void testGenericContentTypePdfBecomesPdfContent() {
		BinaryData binary =
			BinaryDataFactory.createBinaryData(PDF_BYTES, "application/octet-stream", "invoice.pdf");

		ContentPart part = AIScriptFunctions.createBinaryContent(binary);

		assertTrue("Expected a PdfContent, got: " + part, part instanceof PdfContent);
		assertEquals(Base64.getEncoder().encodeToString(PDF_BYTES), ((PdfContent) part).data());
		assertEquals("application/pdf", ((PdfContent) part).mediaType());
	}

	/** The same recovery works for an image whose content type says nothing. */
	public void testGenericContentTypeImageBecomesImageContent() {
		BinaryData binary =
			BinaryDataFactory.createBinaryData(PNG_BYTES, "application/octet-stream", "shot.png");

		ImageContent image = (ImageContent) AIScriptFunctions.createBinaryContent(binary);

		assertEquals("image/png", image.mediaType());
	}

	/**
	 * A declared content type is trusted rather than second-guessed: only a generic one is sniffed.
	 */
	public void testDeclaredContentTypeWinsOverTheMagicBytes() {
		BinaryData binary = BinaryDataFactory.createBinaryData(PDF_BYTES, "text/plain", "invoice.txt");

		TextContent text = (TextContent) AIScriptFunctions.createBinaryContent(binary);

		assertEquals(new String(PDF_BYTES, StandardCharsets.UTF_8), text.text());
	}

	/**
	 * An empty payload is no document: it is refused by name, rather than reaching
	 * {@link PdfContent}, which rejects empty data with a message that does not say which file.
	 */
	public void testEmptyPdfBinaryIsRefused() {
		BinaryData binary = BinaryDataFactory.createBinaryData(new byte[0], "application/pdf", "empty.pdf");

		try {
			AIScriptFunctions.createBinaryContent(binary);
			fail("An empty PDF must be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue("The file must be named: " + ex.getMessage(), ex.getMessage().contains("empty.pdf"));
			assertTrue("The reason must be stated: " + ex.getMessage(), ex.getMessage().contains("no content"));
		}
	}

	/** An empty text file is legitimately empty text and stays one. */
	public void testEmptyTextBinaryStaysEmptyText() {
		BinaryData binary = BinaryDataFactory.createBinaryData(new byte[0], "text/plain", "empty.txt");

		assertEquals(new TextContent(""), AIScriptFunctions.createBinaryContent(binary));
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
	 * A content type no provider can carry is refused, naming it.
	 *
	 * <p>
	 * The base64-in-a-text-block fallback it replaces reached the model as an unreadable blob whose
	 * text was a third longer than the file, and failed far from its cause. Nothing downstream can do
	 * anything with such a part: every one of the four request builders renders exactly text, images
	 * and PDFs.
	 * </p>
	 */
	public void testUnsupportedBinaryIsRefused() {
		BinaryData binary = BinaryDataFactory.createBinaryData(PNG_BYTES, "application/zip", "data.zip");

		try {
			AIScriptFunctions.createBinaryContent(binary);
			fail("An unsupported content type must be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue("The content type must be named: " + ex.getMessage(),
				ex.getMessage().contains("application/zip"));
			assertTrue("The file must be named: " + ex.getMessage(), ex.getMessage().contains("data.zip"));
			assertFalse("The payload must not be inlined: " + ex.getMessage(),
				ex.getMessage().contains(Base64.getEncoder().encodeToString(PNG_BYTES)));
		}
	}

	/**
	 * Unrecognised bytes with no usable content type are refused as well, rather than sent as a
	 * base64 blob nothing can read.
	 */
	public void testUnrecognisedBinaryWithoutContentTypeIsRefused() {
		BinaryData binary =
			BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3, 4 }, "application/octet-stream", "blob.bin");

		try {
			AIScriptFunctions.createBinaryContent(binary);
			fail("An unrecognised payload must be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("application/octet-stream"));
		}
	}

	/**
	 * A structured text type is inlined like {@code text/*}, matching what the tool-side
	 * {@link com.top_logic.tl.ai.core.tools.perception.BinaryPerception} counts as text.
	 */
	public void testJsonBinaryIsInlinedAsText() {
		BinaryData binary = BinaryDataFactory.createBinaryData(
			"{\"price\": 42}".getBytes(StandardCharsets.UTF_8), "application/json", "quote.json");

		TextContent text = (TextContent) AIScriptFunctions.createBinaryContent(binary);

		assertEquals("{\"price\": 42}", text.text());
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

	/**
	 * The PDF this path produces is shaped the way a provider expects, asserted against what the
	 * Anthropic request builder actually emits rather than against the record's fields.
	 *
	 * <p>
	 * This is where the two conventions could silently disagree: {@link ImageContent} carries a
	 * {@code data:} URL, {@link PdfContent} carries bare base64, and a PDF built with a {@code data:}
	 * prefix would produce a request every provider rejects.
	 * </p>
	 */
	public void testPdfFromScriptReachesTheProviderAsADocumentBlock() throws Exception {
		BinaryData binary = BinaryDataFactory.createBinaryData(PDF_BYTES, "application/pdf", "invoice.pdf");
		UserMessage message = AIScriptFunctions.createUserMessage(List.of("Extract the price.", binary));

		String json = AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
			"claude-sonnet-4-20250514", "You are helpful.", List.<Message> of(message), List.of(), 256));

		List<?> blocks = (List<?>) asMap(((List<?>) asMap(JsonUtil.parseJsonValue(json)).get("messages"))
			.get(0)).get("content");
		assertEquals(2, blocks.size());
		assertEquals("text", asMap(blocks.get(0)).get("type"));

		Map<String, Object> document = asMap(blocks.get(1));
		assertEquals("document", document.get("type"));
		Map<String, Object> source = asMap(document.get("source"));
		assertEquals("base64", source.get("type"));
		assertEquals("application/pdf", source.get("media_type"));
		assertEquals("The bare base64 is expected, not a data: URL.",
			Base64.getEncoder().encodeToString(PDF_BYTES), source.get("data"));
	}

	/** A PDF built on this path survives persistence and comes back as the same document. */
	public void testPdfFromScriptRoundTripsThroughTheMessageSerializer() throws Exception {
		BinaryData binary = BinaryDataFactory.createBinaryData(PDF_BYTES, "application/pdf", "invoice.pdf");
		UserMessage message = AIScriptFunctions.createUserMessage(List.of("Extract the price.", binary));

		StringW buffer = new StringW();
		try (JsonWriter out = new JsonWriter(buffer)) {
			MessageSerializer.toDto(message).writeTo(out);
		}
		PersistedMessage dto;
		try (JsonReader in = new JsonReader(new StringR(buffer.toString()))) {
			dto = PersistedMessage.readPersistedMessage(in);
		}
		UserMessage restored = (UserMessage) MessageSerializer.fromDto(dto);

		assertEquals(2, restored.content().size());
		assertEquals(new TextContent("Extract the price."), restored.content().get(0));
		PdfContent pdf = (PdfContent) restored.content().get(1);
		assertEquals(Base64.getEncoder().encodeToString(PDF_BYTES), pdf.data());
		assertEquals("application/pdf", pdf.mediaType());
	}

	/**
	 * A PDF is charged the flat document figure, so the estimate no longer scales with the payload.
	 *
	 * <p>
	 * The base64-text fallback this replaces was charged as text, by the character, so a one megabyte
	 * PDF alone estimated past every context window and drove compaction on a conversation that had
	 * barely started.
	 * </p>
	 */
	public void testPdfFromScriptIsEstimatedAsADocument() {
		byte[] large = new byte[512 * 1024];
		System.arraycopy(PDF_BYTES, 0, large, 0, PDF_BYTES.length);

		ByteBasedTokenEstimator estimator = new ByteBasedTokenEstimator();
		int small = estimator.estimateTokens(AIScriptFunctions.createUserMessage(
			BinaryDataFactory.createBinaryData(PDF_BYTES, "application/pdf", "small.pdf")));
		int big = estimator.estimateTokens(AIScriptFunctions.createUserMessage(
			BinaryDataFactory.createBinaryData(large, "application/pdf", "big.pdf")));
		int asBase64Text = estimator.estimateTokens(AIScriptFunctions.createUserMessage(
			"[Binary content: application/pdf, base64: " + Base64.getEncoder().encodeToString(large) + "]"));

		assertEquals("The document figure is flat, whatever the PDF weighs.", small, big);
		assertTrue("The old fallback charged the base64 by the character: " + asBase64Text
			+ " against " + big, asBase64Text > 10 * big);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		assertTrue("Expected a JSON object, got: " + value, value instanceof Map);
		return (Map<String, Object>) value;
	}
}

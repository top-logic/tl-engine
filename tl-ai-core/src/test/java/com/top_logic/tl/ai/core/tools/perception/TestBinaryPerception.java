/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCapabilities;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelModalities;
import com.top_logic.tl.ai.tool.api.ToolContent;

/**
 * Tests the content-type dispatch and capability gate of {@link BinaryPerception}.
 */
public class TestBinaryPerception extends TestCase {

	private static final byte[] PDF_BYTES = "%PDF-1.7\nfake pdf body".getBytes(StandardCharsets.UTF_8);

	/** An image is downscaled and attached as base64 {@link ToolContent.Image}. */
	public void testImageBecomesImageContent() throws IOException {
		byte[] png = TestImageDownscaler.png(2000, 1000);

		BinaryPerception.Plan plan = BinaryPerception.plan("shot.png", png, "image/png", vision());

		assertTrue("An image must be attached for a vision model.", plan.attaches());
		ToolContent.Image image = (ToolContent.Image) plan.attachment();
		assertEquals("image/png", image.mediaType());
		assertFalse("The attachment carries raw base64, not a data URI.",
			image.data().startsWith("data:"));
		assertTrue("The caption must name the source.", plan.message().contains("shot.png"));
		assertTrue("The result must say the image was downscaled.", plan.message().contains("downscaled"));
		assertTrue("The result must report the sent resolution.", plan.message().contains("1568x784"));
	}

	/** A PDF is attached as base64 {@link ToolContent.Document}. */
	public void testPdfBecomesPdfContent() {
		BinaryPerception.Plan plan =
			BinaryPerception.plan("invoice.pdf", PDF_BYTES, "application/pdf", vision());

		assertTrue(plan.attaches());
		ToolContent.Document pdf = (ToolContent.Document) plan.attachment();
		assertEquals("application/pdf", pdf.mediaType());
		assertEquals(Base64.getEncoder().encodeToString(PDF_BYTES), pdf.data());
	}

	/** Text is returned inline, never injected. */
	public void testTextIsReturnedInline() {
		byte[] data = "Hallo Wörld".getBytes(StandardCharsets.UTF_8);

		BinaryPerception.Plan plan =
			BinaryPerception.plan("notes.txt", data, "text/plain; charset=UTF-8", vision());

		assertFalse("Text needs no attachment.", plan.attaches());
		assertTrue(plan.message().contains("Hallo Wörld"));
	}

	/** JSON counts as text even though its content type is not {@code text/*}. */
	public void testJsonIsTreatedAsText() {
		byte[] data = "{\"a\":1}".getBytes(StandardCharsets.UTF_8);

		BinaryPerception.Plan plan =
			BinaryPerception.plan("data.json", data, "application/json", vision());

		assertFalse(plan.attaches());
		assertTrue(plan.message().contains("{\"a\":1}"));
	}

	/** Long text is truncated rather than flooding the context. */
	public void testLongTextIsTruncated() {
		byte[] data = "x".repeat(BinaryPerception.MAX_TEXT_CHARS + 500).getBytes(StandardCharsets.UTF_8);

		BinaryPerception.Plan plan = BinaryPerception.plan("big.txt", data, "text/plain", vision());

		assertFalse(plan.attaches());
		assertTrue(plan.message().contains("truncated"));
		assertTrue(plan.message().contains("500 more characters"));
	}

	/** An unsupported content type is reported, not injected and not crashed on. */
	public void testUnsupportedContentType() {
		BinaryPerception.Plan plan = BinaryPerception.plan("archive.zip",
			new byte[] { 'P', 'K', 3, 4 }, "application/zip", vision());

		assertFalse(plan.attaches());
		assertTrue(plan.message().contains("application/zip"));
		assertTrue(plan.message().contains("not something a model can perceive"));
	}

	/** */
	public void testEmptyContent() {
		BinaryPerception.Plan plan = BinaryPerception.plan("empty.png", new byte[0], "image/png", vision());

		assertFalse(plan.attaches());
		assertTrue(plan.message().contains("empty"));
	}

	/** A text-only model gets a description of the image instead of the image. */
	public void testImageBlockedForTextOnlyModel() throws IOException {
		BinaryPerception.Plan plan = BinaryPerception.plan("shot.png",
			TestImageDownscaler.png(100, 100), "image/png", textOnly());

		assertFalse("A text-only model must not receive an image.", plan.attaches());
		assertTrue(plan.message().contains("cannot view images"));
		assertTrue("The model must still learn what the object is.", plan.message().contains("shot.png"));
		assertTrue(plan.message().contains("image/png"));
	}

	/** A model whose catalogue lists images but not PDFs is blocked for PDFs only. */
	public void testPdfBlockedForImageOnlyModel() throws IOException {
		ModelPerception imageOnly = context(modalities(List.of("text", "image"), false));

		assertTrue(BinaryPerception.plan("shot.png", TestImageDownscaler.png(50, 50), "image/png",
			imageOnly).attaches());

		BinaryPerception.Plan pdfPlan =
			BinaryPerception.plan("invoice.pdf", PDF_BYTES, "application/pdf", imageOnly);
		assertFalse(pdfPlan.attaches());
		assertTrue(pdfPlan.message().contains("cannot view PDF documents"));
	}

	/** Without declared modalities the coarse attachment flag decides. */
	public void testFallsBackToAttachmentFlag() throws IOException {
		ModelPerception attachments = context(capabilities(null, true));
		ModelPerception noAttachments = context(capabilities(null, false));

		assertEquals(PerceptionSupport.SUPPORTED, attachments.support(Modality.IMAGE));
		assertEquals(PerceptionSupport.UNSUPPORTED, noAttachments.support(Modality.IMAGE));
		assertTrue(BinaryPerception.plan("shot.png", TestImageDownscaler.png(50, 50), "image/png",
			attachments).attaches());
		assertFalse(BinaryPerception.plan("shot.png", TestImageDownscaler.png(50, 50), "image/png",
			noAttachments).attaches());
	}

	/** An unknown model is given the benefit of the doubt, with the uncertainty spelled out. */
	public void testUnknownModelInjectsWithACaveat() throws IOException {
		ModelPerception unknown = new ModelPerception("some-local-model", null);

		assertEquals(PerceptionSupport.UNKNOWN, unknown.support(Modality.IMAGE));

		BinaryPerception.Plan plan = BinaryPerception.plan("shot.png",
			TestImageDownscaler.png(50, 50), "image/png", unknown);

		assertTrue(plan.attaches());
		assertTrue(plan.message().contains("unverified"));
		assertTrue(plan.message().contains("some-local-model"));
	}

	/** Outside an agent loop there is no model to gate on, so the content is attached anyway. */
	public void testWithoutModelTheImageIsStillAttached() throws IOException {
		BinaryPerception.Plan plan =
			BinaryPerception.plan("shot.png", TestImageDownscaler.png(50, 50), "image/png", null);

		assertTrue(plan.attaches());
	}

	/** A missing or generic content type is recovered from the payload's magic bytes. */
	public void testSniffsContentType() throws IOException {
		byte[] png = TestImageDownscaler.png(40, 40);

		assertEquals("image/png", BinaryPerception.resolveContentType(png, null));
		assertEquals("image/png", BinaryPerception.resolveContentType(png, "application/octet-stream"));
		assertEquals("application/pdf", BinaryPerception.resolveContentType(PDF_BYTES, null));
		assertEquals("image/jpeg",
			BinaryPerception.sniff(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0 }));
		assertNull(BinaryPerception.sniff(new byte[] { 1, 2, 3, 4 }));
		assertEquals("A declared type wins over sniffing.", "text/csv",
			BinaryPerception.resolveContentType(png, "text/csv"));
	}

	/** A PNG stored without a content type still reaches the model as an image. */
	public void testSniffedImageIsAttached() throws IOException {
		BinaryPerception.Plan plan =
			BinaryPerception.plan("unnamed", TestImageDownscaler.png(60, 60), null, vision());

		assertTrue(plan.attaches());
		assertEquals("image/png", ((ToolContent.Image) plan.attachment()).mediaType());
	}

	/** An image too large even after downscaling is refused with a clear reason. */
	public void testOversizedImageIsRefused() {
		// Undecodable bytes skip downscaling, so the raw size decides.
		byte[] huge = new byte[BinaryPerception.MAX_IMAGE_BYTES + 1];
		huge[0] = 'x';

		BinaryPerception.Plan plan = BinaryPerception.plan("huge.webp", huge, "image/webp", vision());

		assertFalse(plan.attaches());
		assertTrue(plan.message().contains("above the"));
	}

	/** */
	public void testOversizedPdfIsRefused() {
		byte[] huge = new byte[BinaryPerception.MAX_PDF_BYTES + 1];

		BinaryPerception.Plan plan =
			BinaryPerception.plan("huge.pdf", huge, "application/pdf", vision());

		assertFalse(plan.attaches());
		assertTrue(plan.message().contains("above the"));
	}

	static ModelPerception vision() {
		return context(modalities(List.of("text", "image", "pdf"), true));
	}

	private static ModelPerception textOnly() {
		return context(modalities(List.of("text"), false));
	}

	private static ModelPerception context(ModelCapabilities capabilities) {
		return new ModelPerception("test-model",
			new ModelInfo("test-model", null, null, capabilities, null, null, false, null));
	}

	private static ModelCapabilities modalities(List<String> input, boolean attachment) {
		return capabilities(new ModelModalities(input, List.of("text")), attachment);
	}

	private static ModelCapabilities capabilities(ModelModalities modalities, boolean attachment) {
		return new ModelCapabilities(false, true, true, attachment, false, modalities, null);
	}
}

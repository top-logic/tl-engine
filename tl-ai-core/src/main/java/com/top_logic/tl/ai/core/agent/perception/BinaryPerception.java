/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.perception;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.Locale;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;

/**
 * Turns stored bytes into something a model can actually perceive.
 *
 * <p>
 * Dispatches on content type: images and PDFs become multimodal {@link ContentPart}s that the agent
 * loop injects into the conversation, text is returned inline as the tool's own result, and
 * anything else produces a plain explanation. Nothing here touches the knowledge base or the agent
 * loop, which keeps the whole decision testable in isolation.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.tools.model.ViewDocumentTool
 */
public final class BinaryPerception {

	/**
	 * Maximum encoded image size accepted for injection. Providers reject larger base64 image
	 * payloads outright; the {@link ImageDownscaler} normally keeps real screenshots far below it.
	 */
	public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

	/** Maximum PDF size accepted for injection. */
	public static final int MAX_PDF_BYTES = 20 * 1024 * 1024;

	/** Maximum number of characters returned for a text document. */
	public static final int MAX_TEXT_CHARS = 100_000;

	private BinaryPerception() {
		// Static utility.
	}

	/**
	 * What should happen with a binary payload.
	 *
	 * @param message
	 *        The text the calling tool returns to the model. Always present: even when nothing can
	 *        be shown, the model learns what the object is.
	 * @param perceived
	 *        The captioned content to place into the model's context, or {@code null} when there
	 *        is nothing to inject (text, unsupported type, model cannot see it).
	 */
	public record Plan(String message, PerceivedContent perceived) {

		/** Whether this plan wants content placed into the model's context. */
		public boolean injects() {
			return perceived != null;
		}

		/** The content part to inject, or {@code null}. */
		public ContentPart content() {
			return perceived == null ? null : perceived.content();
		}
	}

	/**
	 * Decides how the given payload can be brought to the model's attention.
	 *
	 * @param label
	 *        Human-readable name of the source object, used in every message.
	 * @param data
	 *        The stored bytes; must not be {@code null}.
	 * @param declaredContentType
	 *        The content type recorded with the data. May be {@code null} or generic, in which
	 *        case it is sniffed from the payload.
	 * @param context
	 *        The running model's perception context, or {@code null} when the caller is not inside
	 *        an agent loop (then nothing can be injected).
	 * @return The plan, never {@code null}.
	 */
	public static Plan plan(String label, byte[] data, String declaredContentType,
			PerceptionContext context) {
		if (data == null || data.length == 0) {
			return new Plan("The content of " + label + " is empty.", null);
		}
		String contentType = resolveContentType(data, declaredContentType);
		String normalized = contentType.toLowerCase(Locale.ROOT);

		if (normalized.startsWith("text/") || isTextLike(normalized)) {
			return planText(label, data, contentType);
		}
		if (normalized.startsWith("image/")) {
			return planImage(label, data, contentType, context);
		}
		if (normalized.startsWith("application/pdf")) {
			return planPdf(label, data, contentType, context);
		}
		return new Plan("Cannot show " + label + ": content type " + contentType
			+ " is not something a model can perceive. Only images, PDFs and text are supported.",
			null);
	}

	private static Plan planText(String label, byte[] data, String contentType) {
		String text = new String(data, charsetOf(contentType));
		if (text.length() > MAX_TEXT_CHARS) {
			text = text.substring(0, MAX_TEXT_CHARS)
				+ "\n[... truncated, " + (text.length() - MAX_TEXT_CHARS) + " more characters ...]";
		}
		return new Plan("Content of " + label + " (" + contentType + ", " + data.length
			+ " bytes):\n\n" + text, null);
	}

	private static Plan planImage(String label, byte[] data, String contentType,
			PerceptionContext context) {
		PerceptionSupport support = support(context, Modality.IMAGE);
		if (!support.permitted()) {
			return new Plan(cannotPerceive("images", context)
				+ " " + label + " is a " + contentType + " image of " + data.length + " bytes.", null);
		}

		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(data, contentType);
		if (scaled.data().length > MAX_IMAGE_BYTES) {
			return new Plan("Cannot show " + label + ": the image is " + scaled.data().length
				+ " bytes even after downscaling, above the " + MAX_IMAGE_BYTES
				+ " byte limit for images sent to a model.", null);
		}

		String base64 = Base64.getEncoder().encodeToString(scaled.data());
		ImageContent image =
			new ImageContent("data:" + scaled.contentType() + ";base64," + base64, scaled.contentType());
		String dimensions = scaled.hasDimensions()
			? " " + scaled.width() + "x" + scaled.height() + " pixels"
				+ (scaled.resized() ? " (downscaled)" : "")
			: "";
		String caption = "Image content of " + label + " (" + scaled.contentType() + dimensions + ")";
		return new Plan(caption + " has been added to your context; it is shown in the next message."
			+ unknownSupportHint(support, context, "images"),
			new PerceivedContent(caption + ":", image));
	}

	private static Plan planPdf(String label, byte[] data, String contentType,
			PerceptionContext context) {
		PerceptionSupport support = support(context, Modality.PDF);
		if (!support.permitted()) {
			return new Plan(cannotPerceive("PDF documents", context)
				+ " " + label + " is a PDF of " + data.length + " bytes.", null);
		}
		if (data.length > MAX_PDF_BYTES) {
			return new Plan("Cannot show " + label + ": the PDF is " + data.length
				+ " bytes, above the " + MAX_PDF_BYTES + " byte limit for documents sent to a model.",
				null);
		}
		PdfContent pdf = new PdfContent(Base64.getEncoder().encodeToString(data), "application/pdf");
		String caption = "PDF content of " + label + " (" + data.length + " bytes)";
		return new Plan(caption + " has been added to your context; it is shown in the next message."
			+ unknownSupportHint(support, context, "PDF documents"),
			new PerceivedContent(caption + ":", pdf));
	}

	private static PerceptionSupport support(PerceptionContext context, Modality modality) {
		return context == null ? PerceptionSupport.UNKNOWN : context.support(modality);
	}

	/**
	 * Only reachable for {@link PerceptionSupport#UNSUPPORTED}, which requires a catalogue entry
	 * and therefore a non-{@code null} context.
	 */
	private static String cannotPerceive(String what, PerceptionContext context) {
		return "The '" + context.model() + "' model cannot view " + what
			+ ", so it was not added to your context.";
	}

	private static String unknownSupportHint(PerceptionSupport support, PerceptionContext context,
			String what) {
		if (support != PerceptionSupport.UNKNOWN || context == null) {
			return "";
		}
		return " (The model catalogue has no entry for '" + context.model()
			+ "', so whether it can read " + what + " is unverified.)";
	}

	/**
	 * The content type to dispatch on: the declared one, unless it is missing or the catch-all
	 * {@code application/octet-stream}, in which case the payload's magic bytes decide.
	 */
	public static String resolveContentType(byte[] data, String declared) {
		String normalized = declared == null ? "" : stripParameters(declared).toLowerCase(Locale.ROOT);
		if (!normalized.isEmpty() && !"application/octet-stream".equals(normalized)) {
			return declared.trim();
		}
		String sniffed = sniff(data);
		return sniffed != null ? sniffed : "application/octet-stream";
	}

	/**
	 * Recognizes the handful of formats this tool can do something with, by magic bytes.
	 *
	 * @return The detected content type, or {@code null} when the payload is not recognized.
	 */
	public static String sniff(byte[] data) {
		if (data == null) {
			return null;
		}
		if (startsWith(data, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
			return "image/png";
		}
		if (startsWith(data, 0xFF, 0xD8, 0xFF)) {
			return "image/jpeg";
		}
		if (startsWith(data, 'G', 'I', 'F', '8')) {
			return "image/gif";
		}
		if (startsWith(data, 'B', 'M')) {
			return "image/bmp";
		}
		if (startsWith(data, 'R', 'I', 'F', 'F') && data.length > 11
				&& data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
			return "image/webp";
		}
		if (startsWith(data, '%', 'P', 'D', 'F', '-')) {
			return "application/pdf";
		}
		return null;
	}

	private static boolean startsWith(byte[] data, int... signature) {
		if (data.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if ((data[i] & 0xFF) != (signature[i] & 0xFF)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether a non-{@code text/*} content type still carries human-readable text.
	 */
	private static boolean isTextLike(String normalized) {
		String base = stripParameters(normalized);
		return base.equals("application/json")
			|| base.equals("application/xml")
			|| base.equals("application/yaml")
			|| base.endsWith("+json")
			|| base.endsWith("+xml");
	}

	private static String stripParameters(String contentType) {
		int semicolon = contentType.indexOf(';');
		return (semicolon < 0 ? contentType : contentType.substring(0, semicolon)).trim();
	}

	private static Charset charsetOf(String contentType) {
		int semicolon = contentType.indexOf(';');
		if (semicolon < 0) {
			return StandardCharsets.UTF_8;
		}
		for (String parameter : contentType.substring(semicolon + 1).split(";")) {
			String[] pair = parameter.split("=", 2);
			if (pair.length == 2 && pair[0].trim().equalsIgnoreCase("charset")) {
				String name = pair[1].trim().replace("\"", "");
				try {
					return Charset.forName(name);
				} catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
					return StandardCharsets.UTF_8;
				}
			}
		}
		return StandardCharsets.UTF_8;
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.Locale;

import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.tools.util.UntrustedEnvelope;
import com.top_logic.tl.ai.tool.api.ToolContent;

/**
 * Turns stored bytes into something a model can actually perceive.
 *
 * <p>
 * Dispatches on content type: images and PDFs become {@link ToolContent} attachments the calling
 * tool hands back inside its own result, text is returned inline inside the
 * {@link UntrustedEnvelope untrusted-content envelope}, and anything else produces a plain
 * explanation. Nothing here touches the knowledge base or the agent loop, which keeps the whole
 * decision testable in isolation.
 * </p>
 *
 * <p>
 * Every path is bounded, and the bounds are the point rather than housekeeping: the payload is named
 * by the model and written by whoever supplied the document, so its size, its declared pixel count and
 * its text are all adversarial inputs. See {@link #MAX_SOURCE_BYTES}, {@link ImageDownscaler#MAX_PIXELS},
 * {@link #MAX_IMAGE_BYTES} and {@link #MAX_PDF_BYTES} - the latter two stated on the base64-encoded
 * size, because that is what is sent.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.tools.model.ViewDocumentTool
 */
public final class BinaryPerception {

	/**
	 * Maximum <b>base64-encoded</b> image size accepted as an attachment. Providers reject larger
	 * payloads outright; the {@link ImageDownscaler} normally keeps real screenshots far below it.
	 *
	 * <p>
	 * Compared against the encoded size rather than the raw one, because that is what is actually sent:
	 * base64 inflates by 4/3, so an image just under this limit raw is a megabyte over it on the wire -
	 * which used to pass here and be refused by the provider instead, with a message nobody could act
	 * on.
	 * </p>
	 */
	public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

	/** Maximum number of characters returned for a text document. */
	public static final int MAX_TEXT_CHARS = 100_000;

	/**
	 * Largest stored payload a tool may read into memory to bring it here at all.
	 *
	 * <p>
	 * The caps around it bound what leaves for the provider; this one bounds the {@code byte[]} on the
	 * way in, which is the allocation a caller controls least: the object to read from is named by the
	 * model, so without a bound a single tool call can be pointed at an arbitrarily large stored binary.
	 * It is the largest <b>raw</b> payload any of the three paths can still do something with - a PDF,
	 * whose cap {@link #MAX_PDF_BYTES} is this same limit expressed after base64 encoding - so nothing
	 * that could have been shown is refused by it.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.ViewDocumentTool
	 */
	public static final int MAX_SOURCE_BYTES = 20 * 1024 * 1024;

	/**
	 * Maximum <b>base64-encoded</b> PDF size accepted as an attachment.
	 *
	 * <p>
	 * Compared against the encoded size rather than the raw one for the same reason as
	 * {@link #MAX_IMAGE_BYTES}: the encoded payload is what is actually sent, so a limit stated on the
	 * raw size is a limit nobody can check against what the provider sees.
	 * </p>
	 *
	 * <p>
	 * The number is therefore not a round one: it is exactly the encoded length of
	 * {@link #MAX_SOURCE_BYTES}, the 20 MB raw payload this has always accepted. Switching the
	 * comparison to the encoded size keeps every document that passed before passing now, instead of
	 * silently tightening the limit by a quarter.
	 * </p>
	 */
	public static final int MAX_PDF_BYTES = (int) base64Length(MAX_SOURCE_BYTES);

	private BinaryPerception() {
		// Static utility.
	}

	/**
	 * What should happen with a binary payload.
	 *
	 * @param message
	 *        The text the calling tool returns to the model. Always present: even when nothing can
	 *        be shown, the model learns what the object is.
	 * @param attachment
	 *        The content to attach to the tool result, or {@code null} when there is nothing to
	 *        attach (text, unsupported type, model cannot see it).
	 */
	public record Plan(String message, ToolContent attachment) {

		/** Whether this plan carries content the model has to perceive. */
		public boolean attaches() {
			return attachment != null;
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
	 * @param model
	 *        What the receiving model can perceive, or {@code null} when the model is unknown (then
	 *        the content is attached and the provider decides).
	 * @return The plan, never {@code null}.
	 */
	public static Plan plan(String label, byte[] data, String declaredContentType,
			ModelPerception model) {
		if (data == null || data.length == 0) {
			return new Plan("The content of " + label + " is empty.", null);
		}
		String contentType = resolveContentType(data, declaredContentType);
		String normalized = contentType.toLowerCase(Locale.ROOT);

		if (normalized.startsWith("text/") || isTextLike(normalized)) {
			return planText(label, data, contentType);
		}
		if (normalized.startsWith("image/")) {
			return planImage(label, data, contentType, model);
		}
		if (normalized.startsWith("application/pdf")) {
			return planPdf(label, data, model);
		}
		return new Plan("Cannot show " + label + ": content type " + contentType
			+ " is not something a model can perceive. Only images, PDFs and text are supported.",
			null);
	}

	/**
	 * Returns the document's text inline, inside the untrusted-content envelope.
	 *
	 * <p>
	 * A stored document is as attacker-controlled as a web page: an upload, a saved download or a
	 * mail attachment is written by whoever supplied it, and it lands in the model's context verbatim.
	 * It therefore gets the same barrier as every browser verb, from the same
	 * {@link UntrustedEnvelope} - the content cannot close it, so instructions hidden in a document
	 * cannot escape into the model's instruction channel.
	 * </p>
	 */
	private static Plan planText(String label, byte[] data, String contentType) {
		String text = new String(data, charsetOf(contentType));
		String body = UntrustedEnvelope.DOCUMENT_CONTENT.wrap(label, text, MAX_TEXT_CHARS);
		return new Plan("Content of " + label + " (" + contentType + ", " + data.length
			+ " bytes):\n\n" + body, null);
	}

	private static Plan planImage(String label, byte[] data, String contentType,
			ModelPerception model) {
		PerceptionSupport support = support(model, Modality.IMAGE);
		if (!support.permitted()) {
			return new Plan(cannotPerceive("images", model)
				+ " " + label + " is a " + contentType + " image of " + data.length + " bytes.", null);
		}

		String mediaType = ImageContent.normalizeMediaType(contentType);
		if (!ImageContent.isAccepted(mediaType)) {
			// Refused before anything decodes the payload: an image type no provider takes cannot
			// become sendable further down, and a format nobody accepts is not one to hand to an
			// image decoder either.
			return new Plan("Cannot show " + label + ": " + mediaType + " images are not accepted by "
				+ "any model provider. Accepted image types are " + ImageContent.acceptedMediaTypes()
				+ ". Convert the file to one of them.", null);
		}

		if (ImageDownscaler.exceedsPixelBudget(data)) {
			// A decompression bomb: a small file declaring enormous dimensions. Refused from its header,
			// before anything allocates a raster from those dimensions and dies of it.
			return new Plan("Cannot show " + label + ": the image declares "
				+ ImageDownscaler.headerPixelCount(data) + " pixels, above the "
				+ ImageDownscaler.MAX_PIXELS + " pixel limit for images that may be decoded. It was not "
				+ "read.", null);
		}

		ImageDownscaler.Scaled scaled = ImageDownscaler.downscale(data, mediaType);
		long encodedBytes = base64Length(scaled.data().length);
		if (encodedBytes > MAX_IMAGE_BYTES) {
			return new Plan("Cannot show " + label + ": the image is " + scaled.data().length
				+ " bytes even after downscaling (" + encodedBytes + " base64-encoded), above the "
				+ MAX_IMAGE_BYTES + " byte limit for images sent to a model.", null);
		}

		ToolContent.Image image = new ToolContent.Image(
			Base64.getEncoder().encodeToString(scaled.data()), scaled.contentType());
		String dimensions = scaled.hasDimensions()
			? " " + scaled.width() + "x" + scaled.height() + " pixels"
				+ (scaled.resized() ? " (downscaled)" : "")
			: "";
		String caption = "Image content of " + label + " (" + scaled.contentType() + dimensions + ")";
		return new Plan(caption + ", shown to you with this result."
			+ unknownSupportHint(support, model, "images"), image);
	}

	private static Plan planPdf(String label, byte[] data, ModelPerception model) {
		PerceptionSupport support = support(model, Modality.PDF);
		if (!support.permitted()) {
			return new Plan(cannotPerceive("PDF documents", model)
				+ " " + label + " is a PDF of " + data.length + " bytes.", null);
		}
		long encodedBytes = base64Length(data.length);
		if (encodedBytes > MAX_PDF_BYTES) {
			return new Plan("Cannot show " + label + ": the PDF is " + data.length
				+ " bytes (" + encodedBytes + " base64-encoded), above the " + MAX_PDF_BYTES
				+ " byte limit for documents sent to a model.", null);
		}
		ToolContent.Document pdf = new ToolContent.Document(
			Base64.getEncoder().encodeToString(data), "application/pdf");
		String caption = "PDF content of " + label + " (" + data.length + " bytes)";
		return new Plan(caption + ", shown to you with this result."
			+ unknownSupportHint(support, model, "PDF documents"), pdf);
	}

	/**
	 * The length base64 encoding turns {@code rawBytes} into: four output bytes per three input ones.
	 *
	 * @param rawBytes
	 *        Size of the unencoded payload.
	 * @return The size of its base64 encoding, which is what the caps on attachments are stated in.
	 */
	public static long base64Length(int rawBytes) {
		return 4L * ((rawBytes + 2L) / 3L);
	}

	private static PerceptionSupport support(ModelPerception model, Modality modality) {
		return model == null ? PerceptionSupport.UNKNOWN : model.support(modality);
	}

	/**
	 * Only reachable for {@link PerceptionSupport#UNSUPPORTED}, which requires a catalogue entry
	 * and therefore a non-{@code null} model.
	 */
	private static String cannotPerceive(String what, ModelPerception model) {
		return "The '" + model.model() + "' model cannot view " + what
			+ ", so it is not shown to you.";
	}

	private static String unknownSupportHint(PerceptionSupport support, ModelPerception model,
			String what) {
		if (support != PerceptionSupport.UNKNOWN || model == null) {
			return "";
		}
		return " (The model catalogue has no entry for '" + model.model()
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
	 *
	 * @param normalized
	 *        The content type, lower-cased; parameters may be present and are ignored.
	 */
	public static boolean isTextLike(String normalized) {
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

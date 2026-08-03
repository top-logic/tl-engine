/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.perception.BinaryPerception;
import com.top_logic.tl.ai.core.tools.perception.ModelPerception;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Lets an agent perceive the content stored on a model object.
 *
 * <p>
 * Given the TID of any object that holds a {@code tl.core:Binary} attribute, the tool reads the
 * stored bytes and brings them to the model in the right modality: images and PDFs ride along in the
 * tool result as multimodal content (so a vision-capable model actually <em>sees</em> them), text is
 * returned inline, and anything else produces a plain explanation. It is not tied to any particular
 * type - a browser screenshot, an uploaded invoice PDF and an attached CSV all go through the same
 * path.
 * </p>
 *
 * <p>
 * Read access is checked through the run's {@link SecurityContext#modelSecurity() model security},
 * which consults the engine's model access rights when the run's mode enforces them and grants
 * everything otherwise.
 * </p>
 *
 * @see BinaryPerception
 * @see SecurityContext#modelSecurity()
 */
public class ViewDocumentTool {

	/** Qualified name of the model type used for binary attributes. */
	private static final String BINARY_TYPE = "tl.core:Binary";

	/**
	 * Configuration for {@link ViewDocumentTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ViewDocumentTool> {
		// No additional properties.
	}

	/**
	 * Creates a new {@link ViewDocumentTool}.
	 */
	public ViewDocumentTool() {
		// Stateless tool.
	}

	/**
	 * Creates a new {@link ViewDocumentTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ViewDocumentTool(InstantiationContext context, Config config) {
		// No-op for stateless tools.
	}

	/**
	 * Loads the content stored on the given object into the agent's context.
	 *
	 * <p>
	 * The stored payload is an adversarial input, not merely a large one: which object is read is
	 * decided by a model-supplied TID, and {@link SecurityContext#modelSecurity()} grants everything
	 * in a run whose mode does not enforce model access - which is the default. Its size is therefore
	 * bounded before anything is buffered ({@link BinaryPerception#MAX_SOURCE_BYTES}), and its content
	 * - stored text included - is treated as untrusted by {@link BinaryPerception}.
	 * </p>
	 *
	 * @param tid
	 *        The identifier of the object holding the content, in the format {@code "TypeName:id"}.
	 * @param attribute
	 *        Name of the binary attribute to read. Only needed when the object has more than one;
	 *        the tool then reports the available names.
	 * @param context
	 *        Injected execution context, supplying the read-access check and the running model.
	 * @return A description of what was loaded, carrying the content itself when the model can
	 *         perceive it, or an explanation why it could not be shown.
	 */
	@ToolMethod(
		description = "Load the content stored on a model object into your context so you can "
			+ "actually perceive it. Images and PDFs are shown to you directly as part of this "
			+ "result, text documents are returned inline. Use it to see a saved screenshot, a "
			+ "stored image or an attached document. Pass the object's TID; pass 'attribute' only "
			+ "when the object stores more than one binary and the tool asks for it.",
		mode = ToolMode.READ,
		group = ToolGroups.DATA_READ)
	public ToolResult viewDocument(
			@P("The TopLogic identifier (TID) of the object holding the content, "
				+ "format 'TypeName:id'.") String tid,
			@P(value = "Name of the binary attribute to read; only needed when the object has "
				+ "several.", required = false) String attribute,
			ToolExecutionContext context) {
		TLObject object;
		try {
			object = TidResolver.resolve(tid);
		} catch (IllegalArgumentException ex) {
			return ToolResult.of("Error: " + ex.getMessage() + ".");
		} catch (RuntimeException ex) {
			Logger.error("viewDocument() could not resolve TID '" + tid + "': " + ex.getMessage(), ex,
				ViewDocumentTool.class);
			return ToolResult.of("Error: cannot resolve TID '" + tid + "': " + ex.getMessage());
		}

		// Single access-control seam for this tool: the run's model security, which delegates to the
		// engine's model access rights when the run's mode enforces them and grants everything in a
		// LEGACY run.
		if (!readAccess(context).modelSecurity().canRead(object)) {
			return ToolResult.of("Error: you do not have read access to " + tid + ".");
		}

		List<TLStructuredTypePart> binaries = binaryAttributes(object);
		if (binaries.isEmpty()) {
			return ToolResult.of("Error: " + tid + " stores no binary content.");
		}

		TLStructuredTypePart part = select(binaries, attribute);
		if (part == null) {
			return ToolResult.of(attribute == null || attribute.isBlank()
				? "Error: " + tid + " has several binary attributes (" + names(binaries)
					+ "). Call viewDocument again with the 'attribute' parameter set to the one you want."
				: "Error: " + tid + " has no binary attribute named '" + attribute + "'. Available: "
					+ names(binaries) + ".");
		}

		Object value = object.tValue(part);
		if (!(value instanceof BinaryDataSource binary)) {
			return ToolResult.of("Error: attribute '" + part.getName() + "' of " + tid + " is empty.");
		}

		long declaredSize = binary.getSize();
		if (declaredSize > BinaryPerception.MAX_SOURCE_BYTES) {
			return ToolResult.of(tooLargeMessage(tid, part, declaredSize));
		}

		byte[] data;
		try {
			data = readBytes(binary, BinaryPerception.MAX_SOURCE_BYTES);
		} catch (ContentTooLargeException ex) {
			// The declared size was wrong or unknown (-1); the read stopped at the bound either way.
			return ToolResult.of(tooLargeMessage(tid, part, -1));
		} catch (IOException | RuntimeException ex) {
			Logger.error("viewDocument() could not read '" + part.getName() + "' of " + tid + ": "
				+ ex.getMessage(), ex, ViewDocumentTool.class);
			return ToolResult.of("Error: cannot read the content of " + tid + ": " + ex.getMessage());
		}

		String label = describe(tid, part, binary);
		BinaryPerception.Plan plan =
			BinaryPerception.plan(label, data, binary.getContentType(), perception(context));
		return plan.attaches()
			? ToolResult.multimodal(plan.message(), plan.attachment())
			: ToolResult.of(plan.message());
	}

	/**
	 * What the model of the running agent can perceive, or {@code null} when the tool runs outside
	 * an agent loop (scripting, tests) and there is no model to gate on.
	 */
	private static ModelPerception perception(ToolExecutionContext context) {
		return context instanceof ToolContext toolContext
			? ModelPerception.forModel(toolContext.model())
			: null;
	}

	/**
	 * The security context used for the read check, falling back to
	 * {@link SecurityContext#UNRESTRICTED} when the tool runs outside an agent loop (scripting,
	 * tests).
	 */
	private static SecurityContext readAccess(ToolExecutionContext context) {
		return context instanceof ToolContext toolContext
			? toolContext.security()
			: SecurityContext.UNRESTRICTED;
	}

	/**
	 * All attributes of the object's type declared as {@code tl.core:Binary}.
	 */
	private static List<TLStructuredTypePart> binaryAttributes(TLObject object) {
		TLStructuredType type = object.tType();
		if (type == null) {
			return List.of();
		}
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part.getType() != null && BINARY_TYPE.equals(TLModelUtil.qualifiedName(part.getType()))) {
				result.add(part);
			}
		}
		return result;
	}

	/**
	 * Picks the attribute to read: the requested one, or the only one there is.
	 *
	 * @return {@code null} when the choice is ambiguous or the requested name does not exist.
	 */
	private static TLStructuredTypePart select(List<TLStructuredTypePart> binaries, String attribute) {
		if (attribute == null || attribute.isBlank()) {
			return binaries.size() == 1 ? binaries.get(0) : null;
		}
		String wanted = attribute.trim();
		for (TLStructuredTypePart part : binaries) {
			if (part.getName().equals(wanted)) {
				return part;
			}
		}
		return null;
	}

	private static String names(List<TLStructuredTypePart> binaries) {
		return binaries.stream().map(TLStructuredTypePart::getName).sorted().reduce(
			(left, right) -> left + ", " + right).orElse("");
	}

	/**
	 * How the loaded content is referred to in the tool result and in the injected caption.
	 */
	private static String describe(String tid, TLStructuredTypePart part, BinaryDataSource binary) {
		String fileName = binary.getName();
		String origin = tid + "." + part.getName();
		return fileName == null || fileName.isBlank() ? origin : "\"" + fileName + "\" (" + origin + ")";
	}

	/**
	 * The message for a payload this tool will not read.
	 *
	 * @param declaredSize
	 *        The size the stored data claims, or {@code -1} when it did not claim one and the bound was
	 *        hit while reading.
	 */
	private static String tooLargeMessage(String tid, TLStructuredTypePart part, long declaredSize) {
		return "Error: the content of attribute '" + part.getName() + "' of " + tid + " is "
			+ (declaredSize >= 0 ? declaredSize + " bytes" : "larger than " + BinaryPerception.MAX_SOURCE_BYTES
				+ " bytes")
			+ ", above the " + BinaryPerception.MAX_SOURCE_BYTES + " byte limit for content this tool "
			+ "loads into your context. It was not read. Nothing that large can be shown to a model "
			+ "anyway; work from an extract or a smaller rendition of it.";
	}

	/**
	 * Reads the stored bytes, refusing to buffer more than {@code maxBytes} of them.
	 *
	 * <p>
	 * The bound is enforced <em>while</em> reading rather than from
	 * {@link BinaryDataSource#getSize()} alone: that size is documented as {@code -1} when unknown, and
	 * the object to read is named by the model, so a size-less binary of any length would otherwise be
	 * materialised whole.
	 * </p>
	 *
	 * <p>
	 * Package-visible so the bound can be asserted without a knowledge base.
	 * </p>
	 *
	 * @throws ContentTooLargeException
	 *         As soon as the source delivers more than {@code maxBytes}.
	 */
	static byte[] readBytes(BinaryDataSource source, int maxBytes) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		source.deliverTo(new BoundedOutputStream(buffer, maxBytes));
		return buffer.toByteArray();
	}

	/** Signals that the stored content exceeds the size this tool is prepared to buffer. */
	private static final class ContentTooLargeException extends IOException {

		ContentTooLargeException(int maxBytes) {
			super("The content exceeds the " + maxBytes + " byte limit for a viewable document.");
		}
	}

	/**
	 * An {@link OutputStream} that passes at most {@code maxBytes} through and then refuses the rest,
	 * so a producer-driven {@link BinaryDataSource#deliverTo(OutputStream)} can be bounded from the
	 * consuming side.
	 */
	private static final class BoundedOutputStream extends OutputStream {

		private final OutputStream _out;

		private final int _maxBytes;

		private long _written;

		BoundedOutputStream(OutputStream out, int maxBytes) {
			_out = out;
			_maxBytes = maxBytes;
		}

		@Override
		public void write(int b) throws IOException {
			checkRoomFor(1);
			_out.write(b);
			_written++;
		}

		@Override
		public void write(byte[] bytes, int offset, int length) throws IOException {
			checkRoomFor(length);
			_out.write(bytes, offset, length);
			_written += length;
		}

		private void checkRoomFor(int length) throws IOException {
			if (_written + length > _maxBytes) {
				throw new ContentTooLargeException(_maxBytes);
			}
		}
	}
}

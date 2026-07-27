/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import com.top_logic.tl.ai.core.agent.perception.BinaryPerception;
import com.top_logic.tl.ai.core.agent.perception.PerceptionBinding;
import com.top_logic.tl.ai.core.agent.perception.PerceptionContext;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Lets an agent perceive the content stored on a model object.
 *
 * <p>
 * Given the TID of any object that holds a {@code tl.core:Binary} attribute, the tool reads the
 * stored bytes and brings them to the model in the right modality: images and PDFs are placed into
 * the model's context as multimodal content (so a vision-capable model actually <em>sees</em>
 * them), text is returned inline, and anything else produces a plain explanation. It is not tied to
 * any particular type — a browser screenshot, an uploaded invoice PDF and an attached CSV all go
 * through the same path.
 * </p>
 *
 * <h2>Proof of concept</h2>
 * <p>
 * Injection reuses the attachment plumbing: the content is queued on the run's
 * {@link PerceptionContext} and appended by the agent loop as a user message before the next
 * provider call, because tool results are text-only in every provider's wire format. Read access is
 * checked through the security context, whose read check is still a placeholder that returns
 * {@code true} for everyone.
 * </p>
 *
 * @see BinaryPerception
 * @see SecurityContext#hasReadAccess(Object)
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
	 * @param tid
	 *        The identifier of the object holding the content, in the format {@code "TypeName:id"}.
	 * @param attribute
	 *        Name of the binary attribute to read. Only needed when the object has more than one;
	 *        the tool then reports the available names.
	 * @param context
	 *        Injected execution context, supplying the read-access check.
	 * @return A description of what was loaded, or an explanation why it could not be.
	 */
	@ToolMethod(
		description = "Load the content stored on a model object into your context so you can "
			+ "actually perceive it. Images and PDFs are shown to you directly (they appear as a "
			+ "message right after this result), text documents are returned inline. Use it to see "
			+ "a saved screenshot, a stored image or an attached document. Pass the object's TID; "
			+ "pass 'attribute' only when the object stores more than one binary and the tool asks "
			+ "for it.",
		mode = ToolMode.READ)
	public String viewDocument(
			@P("The TopLogic identifier (TID) of the object holding the content, "
				+ "format 'TypeName:id'.") String tid,
			@P(value = "Name of the binary attribute to read; only needed when the object has "
				+ "several.", required = false) String attribute,
			ToolExecutionContext context) {
		TLObject object;
		try {
			object = TidResolver.resolve(tid);
		} catch (IllegalArgumentException ex) {
			return "Error: " + ex.getMessage() + ".";
		} catch (RuntimeException ex) {
			Logger.error("viewDocument() could not resolve TID '" + tid + "': " + ex.getMessage(), ex,
				ViewDocumentTool.class);
			return "Error: cannot resolve TID '" + tid + "': " + ex.getMessage();
		}

		// Single access-control seam for this tool. Today SecurityContext.hasReadAccess is a
		// framework-wide placeholder returning true; when it becomes real, this call enforces it.
		if (!readAccess(context).hasReadAccess(object)) {
			return "Error: you do not have read access to " + tid + ".";
		}

		List<TLStructuredTypePart> binaries = binaryAttributes(object);
		if (binaries.isEmpty()) {
			return "Error: " + tid + " stores no binary content.";
		}

		TLStructuredTypePart part = select(binaries, attribute);
		if (part == null) {
			return attribute == null || attribute.isBlank()
				? "Error: " + tid + " has several binary attributes (" + names(binaries)
					+ "). Call viewDocument again with the 'attribute' parameter set to the one you want."
				: "Error: " + tid + " has no binary attribute named '" + attribute + "'. Available: "
					+ names(binaries) + ".";
		}

		Object value = object.tValue(part);
		if (!(value instanceof BinaryDataSource binary)) {
			return "Error: attribute '" + part.getName() + "' of " + tid + " is empty.";
		}

		byte[] data;
		try {
			data = readBytes(binary);
		} catch (IOException | RuntimeException ex) {
			Logger.error("viewDocument() could not read '" + part.getName() + "' of " + tid + ": "
				+ ex.getMessage(), ex, ViewDocumentTool.class);
			return "Error: cannot read the content of " + tid + ": " + ex.getMessage();
		}

		String label = describe(tid, part, binary);
		PerceptionContext perception = PerceptionBinding.getContext();
		BinaryPerception.Plan plan =
			BinaryPerception.plan(label, data, binary.getContentType(), perception);
		if (!plan.injects()) {
			return plan.message();
		}
		if (perception == null) {
			return "Cannot show " + label + ": this run has no agent loop to deliver content to.";
		}
		perception.offer(plan.perceived());
		return plan.message();
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

	private static byte[] readBytes(BinaryDataSource source) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		source.deliverTo(buffer);
		return buffer.toByteArray();
	}
}

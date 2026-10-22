/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.Part;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.form.values.edit.editor.AcceptedTypesChecker;
import com.top_logic.layout.provider.label.FileSizeLabelProvider;
import com.top_logic.layout.react.UploadHandler;

/**
 * Helpers shared by {@link UploadHandler} controls for turning multipart upload {@link Part}s into
 * {@link BinaryData}.
 */
public class UploadSupport {

	/** The multipart field name used for uploaded files. */
	public static final String FILE_PART = "file";

	/**
	 * State key under which an {@link UploadHandler} control publishes its
	 * {@link UploadHandler#maxUploadSize() size limit of one file} to its client component, which
	 * refuses a selection holding a larger file before transmitting it.
	 *
	 * <p>
	 * A value of <code>0</code> means that there is no limit.
	 * </p>
	 */
	public static final String MAX_UPLOAD_SIZE = "maxUploadSize";

	/**
	 * State key under which an {@link UploadHandler} control publishes the message of its last
	 * failed upload to its client component, <code>null</code> after a successful upload.
	 */
	public static final String ERROR = "error";

	/**
	 * The {@link UploadConfig#getMaxUploadSize() configured} maximum size of each uploaded file in
	 * bytes, <code>0</code> if uploads are unlimited.
	 */
	public static long maxUploadSize() {
		UploadConfig config = ApplicationConfig.getInstance().getConfig(UploadConfig.class);
		if (config == null) {
			config = TypedConfiguration.newConfigItem(UploadConfig.class);
		}
		return config.getMaxUploadSize();
	}

	/**
	 * The size limit of one uploaded file for a field with the given limit of its own, capped by the
	 * application-wide {@link #maxUploadSize()}.
	 *
	 * <p>
	 * A limit of <code>0</code> or less means no limit, so where only one of both limits is set it
	 * is the one that applies, and where both are set the smaller one does.
	 * </p>
	 *
	 * @param fieldLimit
	 *        The maximum size in bytes of one file the field accepts, e.g. from the
	 *        {@link com.top_logic.model.annotate.TLMaxUploadSize} annotation of its attribute;
	 *        <code>0</code> for none.
	 * @return The maximum size in bytes of one file the field accepts, <code>0</code> if it is
	 *         unlimited.
	 */
	public static long effectiveMaxUploadSize(long fieldLimit) {
		return effectiveMaxUploadSize(fieldLimit, maxUploadSize());
	}

	/**
	 * The smaller of two size limits, where a limit of <code>0</code> or less means no limit.
	 *
	 * @param fieldLimit
	 *        The limit of the field, <code>0</code> for none.
	 * @param globalLimit
	 *        The application-wide limit, <code>0</code> for none.
	 * @return The limit that applies, <code>0</code> if neither limits the size.
	 *
	 * @see #effectiveMaxUploadSize(long)
	 */
	public static long effectiveMaxUploadSize(long fieldLimit, long globalLimit) {
		if (fieldLimit <= 0) {
			return Math.max(globalLimit, 0);
		}
		if (globalLimit <= 0) {
			return fieldLimit;
		}
		return Math.min(fieldLimit, globalLimit);
	}

	/**
	 * State key under which an {@link UploadHandler} control publishes the file types its file
	 * picker offers, in the form of {@link #acceptAttribute(String)}.
	 */
	public static final String ACCEPT = "accept";

	/** File pattern accepting every file. */
	private static final String ACCEPT_ALL_FILES = "*.*";

	/** MIME type accepting every file. */
	private static final String ACCEPT_ALL_TYPES = "*/*";

	/**
	 * The value of the HTML <code>accept</code> attribute of a file input for the given accepted
	 * file types.
	 *
	 * <p>
	 * The accepted types are given as a comma separated list of MIME types like
	 * <code>image/*</code> or <code>image/jpeg</code> and file patterns like <code>*.xml</code>, the
	 * form the <code>accepted-types</code> annotation of a binary attribute uses. A file input
	 * expects a file extension as <code>.xml</code>, so a pattern loses its wildcard. A pattern
	 * accepting every file (<code>*.*</code> or <code>*&#47;*</code>) filters nothing. The MIME
	 * types come first, as the {@link AcceptedTypesChecker} of an upload field lists them.
	 * </p>
	 *
	 * @param acceptedTypes
	 *        The accepted file types, or <code>null</code> to accept every file.
	 * @return The <code>accept</code> attribute value, <code>null</code> if every file is accepted.
	 *
	 * @see AcceptedTypesChecker
	 */
	public static String acceptAttribute(String acceptedTypes) {
		if (acceptedTypes == null) {
			return null;
		}
		Set<String> mimeTypes = new LinkedHashSet<>();
		Set<String> extensions = new LinkedHashSet<>();
		for (String entry : acceptedTypes.split(",")) {
			String type = entry.trim();
			if (type.isEmpty()) {
				continue;
			}
			if (ACCEPT_ALL_FILES.equals(type) || ACCEPT_ALL_TYPES.equals(type)) {
				return null;
			}
			if (type.indexOf('/') >= 0) {
				mimeTypes.add(type);
			} else {
				extensions.add(type.replace("*", ""));
			}
		}
		List<String> result = new ArrayList<>(mimeTypes);
		result.addAll(extensions);
		return result.isEmpty() ? null : String.join(",", result);
	}

	/**
	 * A human-readable rendering of the given number of bytes, e.g. <code>"50 MB"</code>.
	 */
	public static String sizeLabel(long size) {
		return FileSizeLabelProvider.INSTANCE.getLabel(Long.valueOf(size));
	}

	/**
	 * Reads a single uploaded {@link Part} into an in-memory {@link BinaryData}, defaulting the
	 * content type and file name when the client did not provide them.
	 */
	public static BinaryData toBinaryData(Part part) throws IOException {
		byte[] fileData = part.getInputStream().readAllBytes();
		String contentType = part.getContentType();
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		String fileName = part.getSubmittedFileName();
		if (fileName == null) {
			fileName = "upload.bin";
		}
		return BinaryDataFactory.createBinaryData(fileData, contentType, fileName);
	}

	/**
	 * Reads every {@value #FILE_PART} part of a multipart upload into {@link BinaryData}, preserving
	 * the client's order.
	 */
	public static List<BinaryData> toBinaryData(Collection<Part> parts) throws IOException {
		List<BinaryData> result = new ArrayList<>();
		for (Part part : parts) {
			if (FILE_PART.equals(part.getName())) {
				result.add(toBinaryData(part));
			}
		}
		return result;
	}
}

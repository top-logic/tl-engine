/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.Part;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
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
	 * State key under which an {@link UploadHandler} control publishes {@link #maxUploadSize()} to
	 * its client component, which refuses a larger selection before transmitting it.
	 *
	 * <p>
	 * A value of <code>0</code> means that there is no limit.
	 * </p>
	 */
	public static final String MAX_UPLOAD_SIZE = "maxUploadSize";

	/**
	 * The {@link UploadConfig#getMaxUploadSize() configured} maximum size of an upload in bytes,
	 * <code>0</code> if uploads are unlimited.
	 */
	public static long maxUploadSize() {
		UploadConfig config = ApplicationConfig.getInstance().getConfig(UploadConfig.class);
		if (config == null) {
			config = TypedConfiguration.newConfigItem(UploadConfig.class);
		}
		return config.getMaxUploadSize();
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

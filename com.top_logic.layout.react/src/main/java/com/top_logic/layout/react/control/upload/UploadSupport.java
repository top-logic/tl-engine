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

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.scan.UploadSecurityService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.util.Resources;

/**
 * Helpers shared by {@link UploadHandler} controls for turning multipart upload {@link Part}s into
 * {@link BinaryData}.
 */
public class UploadSupport {

	/** The multipart field name used for uploaded files. */
	public static final String FILE_PART = "file";

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
	 * Checks uploaded content against the {@link UploadSecurityService} (e.g. a virus scanner).
	 *
	 * @param data
	 *        The uploaded content to inspect.
	 *
	 * @return The localized error message if the content is rejected, or <code>null</code> if it is
	 *         accepted.
	 */
	public static String checkContent(BinaryData data) {
		ResKey error = UploadSecurityService.checkUpload(data);
		if (error == null) {
			return null;
		}
		return Resources.getInstance().getString(error);
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

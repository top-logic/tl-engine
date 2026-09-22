/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary.scan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataPart;
import com.top_logic.basic.util.ResKey;

/**
 * Request wrapper checking the content of an uploaded multipart request with the
 * {@link UploadSecurityService}.
 *
 * <p>
 * The check happens once per request for all uploaded parts, no matter which servlet or control
 * consumes them. Since the servlet API does not guarantee that the content of a {@link Part}
 * delivered by the container can be read more than once, each part is read exactly once into a
 * {@link BinaryData} and handed on as a {@link BinaryDataPart}, which can be read any number of
 * times.
 * </p>
 *
 * <p>
 * A part that carries neither a {@link Part#getSubmittedFileName() file name} nor a
 * {@link Part#getContentType() content type} is a plain form field and is passed on without being
 * checked. When a checker rejects an uploaded file, {@link #getParts()} and
 * {@link #getPart(String)} throw an {@link UploadRejectedException} carrying the message of the
 * rejecting {@link UploadContentChecker}, now and for every later access, so that all consumers of
 * the request see the same outcome.
 * </p>
 *
 * @see #guard(HttpServletRequest)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UploadGuardRequest extends HttpServletRequestWrapper {

	/**
	 * Prefix of the {@link HttpServletRequest#getContentType() content type} of a request
	 * transmitting uploaded files.
	 */
	public static final String MULTIPART_CONTENT_TYPE_PREFIX = "multipart/";

	private List<Part> _parts;

	private UploadRejectedException _rejection;

	/**
	 * Wraps the given request into an {@link UploadGuardRequest}, if its uploads must be checked.
	 *
	 * @param request
	 *        The request received by a servlet.
	 *
	 * @return The given request itself, if it transmits no uploads, or the
	 *         {@link UploadSecurityService} is not active, an {@link UploadGuardRequest} wrapping
	 *         the given request otherwise.
	 */
	public static HttpServletRequest guard(HttpServletRequest request) {
		if (!isMultipart(request)) {
			return request;
		}
		if (!UploadSecurityService.Module.INSTANCE.isActive()) {
			return request;
		}
		return new UploadGuardRequest(request);
	}

	private static boolean isMultipart(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType == null) {
			return false;
		}
		return contentType.regionMatches(true, 0, MULTIPART_CONTENT_TYPE_PREFIX, 0,
			MULTIPART_CONTENT_TYPE_PREFIX.length());
	}

	/**
	 * Creates a {@link UploadGuardRequest}.
	 *
	 * @param request
	 *        The request whose uploaded parts are checked.
	 *
	 * @see #guard(HttpServletRequest)
	 */
	public UploadGuardRequest(HttpServletRequest request) {
		super(request);
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return checkedParts();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		for (Part part : checkedParts()) {
			if (part.getName().equals(name)) {
				return part;
			}
		}
		return null;
	}

	private List<Part> checkedParts() throws IOException, ServletException {
		if (_rejection != null) {
			throw _rejection;
		}
		if (_parts == null) {
			_parts = Collections.unmodifiableList(checkParts(super.getParts()));
		}
		return _parts;
	}

	private List<Part> checkParts(Collection<Part> parts) throws IOException {
		List<Part> result = new ArrayList<>(parts.size());
		for (Part part : parts) {
			BinaryData data = BinaryDataFactory.createUploadData(part);
			if (hasContent(part)) {
				ResKey error = UploadSecurityService.checkUpload(data);
				if (error != null) {
					_rejection = new UploadRejectedException(error);
					throw _rejection;
				}
			}
			result.add(new BinaryDataPart(part, data));
		}
		return result;
	}

	/**
	 * Whether the given part transports uploaded content, as opposed to the value of a plain form
	 * field.
	 */
	private static boolean hasContent(Part part) {
		return part.getSubmittedFileName() != null || part.getContentType() != null;
	}

}

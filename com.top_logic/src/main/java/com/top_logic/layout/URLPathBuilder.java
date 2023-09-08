/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.common.net.UrlEscapers;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * The class {@link URLPathBuilder} is an {@link URLBuilder} which encodes the
 * hierarchy of resources as {@link URLPathParser#PATH_SEPARATOR} separated
 * string of resources.
 * 
 * @see URLPathParser
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class URLPathBuilder implements URLBuilder {

	/**
	 * that variable contains the characters of the URL
	 */
	private final StringBuilder url;

	private boolean hasParameter = false;

	private URLPathBuilder(StringBuilder url, boolean hasParameter) {
		this.url = url;
		this.hasParameter = hasParameter;
	}

	/**
	 * Appends the given content the URL without any checks.
	 * 
	 * @param content
	 *        The content to append.
	 */
	public void appendRaw(String content) {
		url.append(content);
	}

	@Override
	public URLBuilder copy() {
		return new URLPathBuilder(new StringBuilder(url), hasParameter);
	}

	@Override
	public String getURL() {
		return url.toString();
	}

	@Override
	public void addResource(CharSequence resource) {
		String pathSegment = String.valueOf(resource);
		/* Do not allow '/' in resource because URLPathParser can not decide whether '/' is path of
		 * a segment and separator between segments. */
		assert pathSegment.indexOf(URLPathParser.PATH_SEPARATOR_CHAR) == -1 : "'/' is the separator of resources, so it must not be contained in resource names";
		if (hasParameter) {
			throw new IllegalStateException("A URL cannot be extended after the first parameter has been added.");
		}

		url.append(URLPathParser.PATH_SEPARATOR_CHAR);
		url.append(escapeUrlPathSegment(pathSegment));

	}

	/**
	 * Escapes special characters in path segments.
	 * 
	 * @param pathSegment
	 *        The path segment to encode.
	 * @return The encodes path segment
	 */
	private String escapeUrlPathSegment(String pathSegment) {
		String escapedString = UrlEscapers.urlPathSegmentEscaper().escape(pathSegment);
		escapedString = escapedString.replace(";", "%3B");
		return escapedString;
	}

	@Override
	public URLBuilder appendParameter(String name, CharSequence value) {
		appendParameterStart(name);
		url.append(encode(value.toString()));
		hasParameter = true;
		return this;
	}

	@Override
	public URLBuilder appendParameter(String name, int value) {
		appendParameterStart(name);
		try {
			StringServices.append(url, value);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		hasParameter = true;
		return this;
	}

	private void appendParameterStart(String name) {
		url.append(hasParameter ? '&' : '?');
		url.append(encode(name));
		url.append('=');
	}

	private String encode(String name) {
		try {
			return URLEncoder.encode(name, LayoutConstants.UTF_8);
		} catch (UnsupportedEncodingException ex) {
			throw (AssertionError) new AssertionError("Encoding not supported: " + LayoutConstants.UTF_8).initCause(ex);
		}
	}

	/**
	 * returns the current build URL.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getURL();
	}

	/**
	 * Creates a new {@link URLPathParser} which initially contains the
	 * {@link DisplayContext#getContextPath() context path} and the default servlet path (
	 * {@link com.top_logic.base.accesscontrol.ApplicationPages.Config#getLayoutServletPath()}).
	 * 
	 * @param context
	 *        The context to get the context path from.
	 */
	public static URLPathBuilder newLayoutServletURLBuilder(DisplayContext context) {
		StringBuilder url = new StringBuilder();
		url.append(context.getContextPath());
		url.append(ApplicationPages.getInstance().getLayoutServletPath());
		return new URLPathBuilder(url, false);
	}

	/**
	 * Creates a new {@link URLPathParser} which initially contains the
	 * {@link DisplayContext#getContextPath() context path}.
	 * 
	 * @param context
	 *        The context to get the context path from.
	 */
	public static URLPathBuilder newBuilder(DisplayContext context) {
		StringBuilder url = new StringBuilder();
		url.append(context.getContextPath());
		return new URLPathBuilder(url, false);
	}

	/**
	 * Creates an empty builder.
	 */
	public static URLPathBuilder newEmptyBuilder() {
		return new URLPathBuilder(new StringBuilder(), false);
	}
}

/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.uri;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;

/**
 * Builder for the source of an {@link URI}.
 * 
 * <p>
 * An {@link UriBuilder} is used by {@link CallBuilder}s in their
 * {@link CallBuilder#buildUrl(UriBuilder, Call)} method.
 * </p>
 * 
 * @see CallBuilder#buildUrl(UriBuilder, Call)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UriBuilder {

	private boolean _parameterSeen;

	private String _baseUrl;

	private final StringBuffer _path = new StringBuffer();

	private final StringBuffer _query = new StringBuffer();

	/**
	 * Creates a {@link UriBuilder}.
	 */
	public UriBuilder(String baseUrl) {
		_baseUrl = baseUrl;
	}

	/**
	 * Appends a part to the URI path (before the query string).
	 *
	 * @param part
	 *        The unescaped path part to add.
	 */
	public void appendPathElement(String part) {
		appendRawPathElement(encode(part));
	}

	private String encode(String part) {
		String encoded;
		try {
			encoded = URLEncoder.encode(part, "utf-8");

			// URLEncoder encodes spaces with '+'. This is problematic, because '+' is not
			// automatically decoded when serving the request. When constructing a path with a path
			// element "foo+bar and bazz" this is encoded to "foo%2Bbar+and+bazz". The receiver gets
			// the path info "foo+bar+and+bazz" for this path and cannot decide which '+' was a
			// space and which was a plus. Therefore, its better to encode this path element as
			// "foo%2Bbar%20and%20bazz".
			encoded = encoded.replace("+", "%20");
		} catch (UnsupportedEncodingException ex) {
			throw new UnsupportedOperationException("Cannot encode path part.", ex);
		}
		return encoded;
	}

	/**
	 * Directly appends a part to the URI path without encoding.
	 *
	 * @param encodedPart
	 *        The already encoded path part to add.
	 */
	private void appendRawPathElement(String encodedPart) {
		if (encodedPart.isEmpty()) {
			return;
		}
		
		int pathLength = _path.length();
		boolean endsWithSlash = pathLength > 0 ? _path.charAt(pathLength - 1) == '/' : _baseUrl.endsWith("/");
		if (!endsWithSlash) {
			_path.append("/");
		}

		_path.append(encodedPart);

		if (endsWithSlash) {
			_path.append("/");
		}
	}

	/**
	 * Adds a query parameter.
	 *
	 * @param name
	 *        The parameter name.
	 * @param value
	 *        The parameter value.
	 */
	public void addParameter(String name, String value) {
		if (_parameterSeen) {
			_path.append('&');
		} else {
			_path.append('?');
			_parameterSeen = true;
		}
		_path.append(name);
		_path.append('=');
		try {
			_path.append(URLEncoder.encode(value, "utf-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}

	/**
	 * Builds the final {@link URI}.
	 */
	public URI build() throws URISyntaxException {
		return new URI(uriString());
	}

	private String uriString() {
		return _baseUrl + _path + _query;
	}

	@Override
	public String toString() {
		return uriString();
	}

}

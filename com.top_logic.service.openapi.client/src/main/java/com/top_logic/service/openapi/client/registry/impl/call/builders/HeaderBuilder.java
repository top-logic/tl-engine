/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.builders;

import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.basic.StringServices;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducer;

/**
 * {@link CallBuilder} adding a HTTP header value to the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HeaderBuilder implements CallBuilder {

	/** {@link ValueProducer} creating the actual header value. */
	private final ValueProducer _valueProducer;

	/** Name of the header to set. */
	private final String _name;

	/**
	 * Whether the header is {@link ClassicHttpRequest#setHeader(String, Object) set} or
	 * {@link ClassicHttpRequest#addHeader(String, Object) added}.
	 */
	private final boolean _additiveHeader;
	
	/**
	 * Creates a new {@link HeaderBuilder} which sets the header with the given name.
	 * 
	 * @param name
	 *        Name of the header to set.
	 * @param valueProducer
	 *        {@link ValueProducer} creating the actual header value.
	 */
	public HeaderBuilder(String name, ValueProducer valueProducer) {
		this(name, valueProducer, false);
	}

	/**
	 * Creates a new {@link HeaderBuilder} which sets or add the header with the given name.
	 * 
	 * @param name
	 *        Name of the header to set.
	 * @param valueProducer
	 *        {@link ValueProducer} creating the actual header value.
	 * @param additiveHeader
	 *        Whether the header is {@link ClassicHttpRequest#setHeader(String, Object) set} or
	 *        {@link ClassicHttpRequest#addHeader(String, Object) added}.
	 */
	public HeaderBuilder(String name, ValueProducer valueProducer, boolean additiveHeader) {
		_valueProducer = valueProducer;
		_name = name;
		_additiveHeader = additiveHeader;
	}

	@Override
	public void buildRequest(ClassicHttpRequest request, Call call) {
		Object value = _valueProducer.getValue(call);
		if (value != null) {
			if (_additiveHeader) {
				request.addHeader(_name, value.toString());
			} else {
				request.setHeader(_name, value.toString());
			}
		}
	}

	private static HeaderBuilder internalCookieBuilder(String headerName, String cookieName,
			ValueProducer cookieValue) {
		ValueProducer headerValue = new ValueProducer() {

			@Override
			public Object getValue(Call call) {
				if (StringServices.isEmpty(cookieName)) {
					return null;
				}
				Object cookie = cookieValue.getValue(call);
				if (cookie == null) {
					return null;
				}
				return cookieName + "=" + cookie;
			}
		};
		return new HeaderBuilder(headerName, headerValue, true);
	}

	/**
	 * {@link HeaderBuilder} adding a new "Cookie" value.
	 * 
	 * @param cookieName
	 *        Name of the cookie to add.
	 * @param cookieValue
	 *        Builder for the cookie value.
	 */
	public static HeaderBuilder cookieBuilder(String cookieName, ValueProducer cookieValue) {
		return internalCookieBuilder("Cookie", cookieName, cookieValue);
	}

	/**
	 * {@link HeaderBuilder} adding a new "Set-Cookie" value.
	 * 
	 * @param cookieName
	 *        Name of the cookie to add.
	 * @param cookieValue
	 *        Builder for the cookie value.
	 */
	public static HeaderBuilder setCookieBuilder(String cookieName, ValueProducer cookieValue) {
		return internalCookieBuilder("Set-Cookie", cookieName, cookieValue);
	}
}

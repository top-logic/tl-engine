/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Definition of the location where the parameter is delivered to the API.
 * 
 * @see ParameterObject
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum ParameterLocation implements ExternallyNamed {

	/**
	 * Used together with Path Templating, where the parameter value is actually part of the
	 * operation's URL. This does not include the host or base path of the API. For example, in
	 * <i>/items/{itemId}</i>, the path parameter is <i>itemId</i>.
	 */
	PATH("path"),

	/**
	 * Parameters that are appended to the URL. For example, in <i>/items?id=###</i>, the
	 * query parameter is <i>id</i>.
	 */
	QUERY("query"),

	/**
	 * Custom headers that are expected as part of the request. Note that [RFC7230] states header
	 * names are case insensitive.
	 */
	HEADER("header"),

	/** Used to pass a specific cookie value to the API. */
	COOKIE("cookie"),

	;

	private final String _externalName;

	/**
	 * Creates a new {@link ParameterLocation}.
	 */
	ParameterLocation(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}


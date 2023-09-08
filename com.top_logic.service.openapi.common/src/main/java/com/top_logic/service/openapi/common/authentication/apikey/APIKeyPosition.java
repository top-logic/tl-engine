/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.apikey;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Possible position for the API key in the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum APIKeyPosition implements ExternallyNamed {

	/** The API key is stores as parameter in the request. */
	QUERY("query"),

	/** The API key is contained in a request header. */
	HEADER("header"),

	/** The API key is contained in a cookie. */
	COOKIE("cookie"),

	;

	private final String _externalName;

	private APIKeyPosition(String name) {
		_externalName = name;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}



}


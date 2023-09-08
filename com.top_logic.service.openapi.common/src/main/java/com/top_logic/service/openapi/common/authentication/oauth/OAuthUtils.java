/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.util.ResKey;

/**
 * Utility methods for OAuth authentication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OAuthUtils {

	/**
	 * Service method to transform the given URL to an URI.
	 * 
	 * @implNote Uses {@link URL#toURI()}.
	 * 
	 * @param url
	 *        The URL to get URI for.
	 * @return Transformed URI.
	 * 
	 * @throws ConfigurationError
	 *         When URI does not confirm to URI syntax.
	 */
	public static URI toURI(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException ex) {
			ResKey errorKey = I18NConstants.ERROR_INVALID_URI__URL.fill(url.toString());
			throw new ConfigurationError(errorKey, ex);
		}
	}
}


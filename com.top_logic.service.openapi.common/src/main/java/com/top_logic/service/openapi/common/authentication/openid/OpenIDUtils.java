/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.openid;

import java.io.IOException;
import java.net.URI;

import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

/**
 * Utility methods for connection to <a href="https://openid.net/">OpenID</a> server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenIDUtils {

	/**
	 * Fetches the <a href="https://openid.net/connect">OpenID Connect</a> provider metadata from
	 * the given <a href="https://openid.net/">OpenID</a> server.
	 * 
	 * @param openIDURI
	 *        {@link URI} for the Open ID server.
	 */
	public static OIDCProviderMetadata openIDMetaData(URI openIDURI) {
		OIDCProviderMetadata openIDMetaData;
		try {
			openIDMetaData = OIDCProviderMetadata.resolve(new Issuer(openIDURI));
		} catch (GeneralException | IOException ex) {
			throw new RuntimeException("Unable to get meta data from " + openIDURI, ex);
		}
		if (openIDMetaData.getGrantTypes() != null
			&& !openIDMetaData.getGrantTypes().contains(GrantType.CLIENT_CREDENTIALS)) {
			throw new RuntimeException(
				"Endpoint '" + openIDURI + "' does not support grant type " + GrantType.CLIENT_CREDENTIALS);
		}
		return openIDMetaData;
	}

}


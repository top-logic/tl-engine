/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * {@link OAuthAuthentication} that checks the tokens that are sent in the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({ TokenBasedAuthentication.URI_PROVIDER })
public interface TokenBasedAuthentication extends OAuthAuthentication {

	/** Configuration name for {@link #getURIProvider()} */
	String URI_PROVIDER = "uri-provider";

	/**
	 * The {@link TokenURIProvider} to get {@link TokenURIProvider#getTokenEndpointURI() token
	 * endpoint URI} from.
	 */
	@ImplementationClassDefault(OpenIDURIProvider.class)
	@ItemDefault
	@NonNullable
	@Name(URI_PROVIDER)
	PolymorphicConfiguration<? extends TokenURIProvider> getURIProvider();

	/**
	 * Setter for {@link #getURIProvider()}.
	 */
	void setURIProvider(PolymorphicConfiguration<? extends TokenURIProvider> value);

}

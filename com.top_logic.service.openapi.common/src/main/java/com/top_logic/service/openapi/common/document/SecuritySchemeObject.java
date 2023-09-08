/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.net.URL;
import java.util.Map;

import com.top_logic.basic.config.URLFormat;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;

/**
 * Defines a security scheme that can be used by the operations. Supported schemes are HTTP
 * authentication, an API key (either as a header, a cookie parameter or as a query parameter),
 * OAuth2's common flows (implicit, password, client credentials and authorization code) as defined
 * in [RFC6749], and <i>OpenID Connect Discovery</i>.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#security-scheme-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	SecuritySchemeObject.SCHEMA_NAME,
	SecuritySchemeObject.TYPE,
	SecuritySchemeObject.DESCRIPTION,
	SecuritySchemeObject.NAME,
	SecuritySchemeObject.IN,
	SecuritySchemeObject.FLOWS,
})
public interface SecuritySchemeObject extends Described {

	/** Configuration name for the value of {@link #getType()}. */
	String TYPE = "type";

	/** Configuration name for the value of {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for the value of {@link #getIn()}. */
	String IN = "in";

	/** Configuration name for the value of {@link #getFlows()}. */
	String FLOWS = "flows";

	/** Configuration name for the value of {@link #getSchemaName()}. */
	String SCHEMA_NAME = "schemaName";

	/** Configuration name for the value of {@link #getScheme()}. */
	String SCHEME = "scheme";

	/** Configuration name for the value of {@link #getOpenIdConnectUrl()}. */
	String OPEN_ID_CONNECT_URL = "openIdConnectUrl";

	/**
	 * Name of the security schema.
	 */
	@Mandatory
	@Name(SCHEMA_NAME)
	String getSchemaName();

	/**
	 * Setter for {@link #getSchemaName()}.
	 */
	void setSchemaName(String value);

	/**
	 * The type of the security scheme.
	 */
	@Name(TYPE)
	@Mandatory
	SecuritySchemeType getType();

	/**
	 * Setter for {@link #getType()}.
	 */
	void setType(SecuritySchemeType type);

	/**
	 * A short description for security scheme.
	 */
	@Override
	String getDescription();

	/**
	 * The name of the header, query or cookie parameter to be used.
	 * 
	 * <p>
	 * Mandatory when {@link #getType()} is {@link SecuritySchemeType#API_KEY}.
	 * </p>
	 */
	@Name(NAME)
	String getName();

	/**
	 * Setter for {@link #getName()}.
	 */
	void setName(String value);

	/**
	 * The location of the API key.
	 * 
	 * <p>
	 * Mandatory when {@link #getType()} is {@link SecuritySchemeType#API_KEY}.
	 * </p>
	 */
	@Name(IN)
	APIKeyPosition getIn();

	/**
	 * Setter for {@link #getIn()}.
	 */
	void setIn(APIKeyPosition value);

	/**
	 * An object containing configuration information for the flow types supported.
	 * 
	 * <p>
	 * Mandatory when {@link #getType()} is {@link SecuritySchemeType#OAUTH2}.
	 * </p>
	 */
	@Key(OAuthFlowObject.FLOW)
	@Name(FLOWS)
	Map<OAuthFlow, OAuthFlowObject> getFlows();

	/**
	 * The name of the HTTP Authorization scheme to be used in the Authorization header as defined
	 * in [RFC7235]. The values used SHOULD be registered in the IANA Authentication Scheme
	 * registry.
	 * 
	 * <p>
	 * Mandatory when {@link #getType()} is {@link SecuritySchemeType#HTTP}.
	 * </p>
	 */
	@Nullable
	@Name(SCHEME)
	String getScheme();

	/**
	 * Setter for {@link #getScheme()}.
	 */
	void setScheme(String value);

	/**
	 * <i>OpenId</i> connect URL to discover <i>OAuth2</i> configuration values.
	 * 
	 * <p>
	 * Mandatory when {@link #getType()} is {@link SecuritySchemeType#OPEN_ID_CONNECT}.
	 * </p>
	 */
	@Format(URLFormat.class)
	@Name(OPEN_ID_CONNECT_URL)
	URL getOpenIdConnectUrl();

	/**
	 * Setter for {@link #getOpenIdConnectUrl()}.
	 */
	void setOpenIdConnectUrl(URL value);

}


/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URL;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.URLFormat;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
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
	SecuritySchemeObject.OPEN_ID_CONNECT_URL,
	SecuritySchemeObject.DESCRIPTION,
	SecuritySchemeObject.NAME,
	SecuritySchemeObject.IN,
	SecuritySchemeObject.FLOWS,
	SecuritySchemeObject.X_TL_IN_USER_CONTEXT,
	SecuritySchemeObject.X_TL_USER_FIELD_NAME,
})
public interface SecuritySchemeObject extends Described {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

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

	/** Configuration name for the value of {@link #isInUserContext()}. */
	String X_TL_IN_USER_CONTEXT = "x-tl-in-user-context";

	/** Configuration name for the value of {@link #getUserFieldName()}. */
	String X_TL_USER_FIELD_NAME = "x-tl-user-field-name";

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

	/**
	 * Whether operations protected by this {@link SecuritySchemeObject} must be executed in user
	 * context.
	 */
	@Name(X_TL_IN_USER_CONTEXT)
	@Label("In user context")
	boolean isInUserContext();

	/**
	 * Optional configuration of the header field, that contains the name of the user in whose
	 * context an operation must be executed.
	 * 
	 * <p>
	 * Must only be set, when the operation is executed {@link #isInUserContext() in user context}.
	 * </p>
	 */
	@Name(X_TL_USER_FIELD_NAME)
	@Nullable
	@Label("User field name")
	String getUserFieldName();

	/**
	 * Marks this {@link SecuritySchemeObject} to be {@link #isInUserContext()}.
	 * 
	 * @param inUserContext
	 *        See {@link #isInUserContext()}.
	 * @param userFieldName
	 *        See {@link #getUserFieldName()}.
	 */
	default void setUserContext(boolean inUserContext, String userFieldName) {
		update(descriptor().getProperty(X_TL_IN_USER_CONTEXT), inUserContext);
		if (inUserContext && !StringServices.isEmpty(userFieldName)) {
			update(descriptor().getProperty(X_TL_USER_FIELD_NAME), userFieldName);
		}
	}

}


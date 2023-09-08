/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.SimpleJsonMapBinding;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Configuration details for a supported {@link OAuthFlow}.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#oauth-flow-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	OAuthFlowObject.FLOW,
	OAuthFlowObject.TOKEN_URL,
	OAuthFlowObject.SCOPES,
})
public interface OAuthFlowObject extends ConfigurationItem {

	/** Configuration name for the value of {@link #getScopes()}. */
	String SCOPES = "scopes";

	/** Configuration name for the value of {@link #getTokenUrl()}. */
	String TOKEN_URL = "tokenUrl";

	/** Configuration name for the value of {@link #getFlow()}. */
	String FLOW = "flow";

	/**
	 * The supported {@link OAuthFlow}.
	 */
	@Name(FLOW)
	OAuthFlow getFlow();

	/**
	 * Setter for {@link #getFlow()}.
	 */
	void setFlow(OAuthFlow value);

	/**
	 * The token URL to be used for this flow. This MUST be in the form of a URL.
	 * 
	 * <p>
	 * The token URL must be set when {@link #getFlow()} is {@link OAuthFlow#PASSWORD},
	 * {@link OAuthFlow#CLIENT_CREDENTIALS}, or {@link OAuthFlow#AUTHORIZATION_CODE}
	 * </p>
	 */
	@Name(TOKEN_URL)
	String getTokenUrl();

	/**
	 * Setter for {@link #getTokenUrl()}.
	 */
	void setTokenUrl(String value);

	/**
	 * The available scopes for the OAuth2 security scheme. A map between the scope name and a short
	 * description for it. The map MAY be empty.
	 */
	@Name(SCOPES)
	@MapBinding
	@JsonBinding(SimpleJsonMapBinding.class)
	Map<String, String> getScopes();

	/**
	 * Setter for {@link #getScopes()}.
	 */
	void setScopes(Map<String, String> value);

}


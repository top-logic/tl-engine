/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

/**
 * Identification of a client in a token or authentication server, e.g. when tokens are requested or
 * checked.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ClientSecret extends ConfigurationItem {

	/** Configuration name for {@link #getClientId()}. */
	String CLIENT_ID = "client-id";

	/** Configuration name for {@link #getPassword()}. */
	String PASSWORD = "password";

	/**
	 * Id of this client known by the token or authentication server.
	 */
	@Name(CLIENT_ID)
	@Mandatory
	String getClientId();

	/**
	 * Setter for {@link #getClientId()}.
	 */
	void setClientId(String value);

	/**
	 * Password (client secret) to authenticate this client at the token or authentication server.
	 *
	 * <p>
	 * The secret is read each time it is sent to the server. An attribute value is an encrypted
	 * secret (or a value with the prefix <code>unencrypted:</code>). A secret from the environment
	 * is referenced by the name of its variable:
	 * </p>
	 *
	 * <pre>
	 * &lt;client-secret client-id="my-app"&gt;
	 *   &lt;password class="com.top_logic.basic.credential.EnvironmentSecretSource" name="MY_APP_CLIENT_SECRET"/&gt;
	 * &lt;/client-secret&gt;
	 * </pre>
	 */
	@Name(PASSWORD)
	@Mandatory
	@Format(SecretSourceFormat.class)
	PolymorphicConfiguration<? extends SecretSource> getPassword();

	/**
	 * Setter for {@link #getPassword()}.
	 */
	void setPassword(PolymorphicConfiguration<? extends SecretSource> value);

}


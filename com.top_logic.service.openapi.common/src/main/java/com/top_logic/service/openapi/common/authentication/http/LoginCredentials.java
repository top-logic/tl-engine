/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.http;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

/**
 * Login data for a user by a given password.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	LoginCredentials.USER,
	LoginCredentials.PASSWORD,
})
public interface LoginCredentials extends EqualityByValue {

	/** Configuration name for the value {@link #getUser()}. */
	String USER = "user";

	/** Configuration name for the value {@link #getPassword()}. */
	String PASSWORD = "password";

	/**
	 * Name of the user to authenticate.
	 */
	@Mandatory
	@Name(USER)
	String getUser();

	/**
	 * Setter for {@link #getUser()}.
	 */
	void setUser(String user);

	/**
	 * Password to authenticate {@link #getUser()}.
	 *
	 * <p>
	 * The password is read each time it is used. An attribute value is an encrypted password (or a
	 * value with the prefix <code>unencrypted:</code>). A password from the environment is
	 * referenced by the name of its variable:
	 * </p>
	 *
	 * <pre>
	 * &lt;login user="my-user"&gt;
	 *   &lt;password class="com.top_logic.basic.credential.EnvironmentSecretSource" name="MY_USER_PASSWORD"/&gt;
	 * &lt;/login&gt;
	 * </pre>
	 */
	@Mandatory
	@Name(PASSWORD)
	@Format(SecretSourceFormat.class)
	PolymorphicConfiguration<? extends SecretSource> getPassword();

	/**
	 * Setter for {@link #getPassword()}.
	 */
	void setPassword(PolymorphicConfiguration<? extends SecretSource> password);

}


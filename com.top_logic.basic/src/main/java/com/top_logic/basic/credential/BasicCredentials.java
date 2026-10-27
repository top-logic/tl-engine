/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link CredentialProvider} sending a user name and password with HTTP basic authentication (RFC
 * 7617).
 *
 * <p>
 * The request gets the header <code>Authorization: Basic &lt;base64(user:password)&gt;</code>,
 * encoded in UTF-8. The password is read from its {@link SecretSource} for each request.
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.basic.credential.BasicCredentials" user="service-user"&gt;
 *   &lt;password class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/service-password"/&gt;
 * &lt;/credentials&gt;
 * </pre>
 */
public class BasicCredentials extends AbstractConfiguredInstance<BasicCredentials.Config>
		implements CredentialProvider {

	/**
	 * Configuration options for {@link BasicCredentials}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<BasicCredentials> {

		/**
		 * Tag name of this configuration in a list of {@link CredentialProvider}s.
		 */
		String TAG_NAME = "basic-credentials";

		/**
		 * @see #getUser()
		 */
		String USER = "user";

		/**
		 * @see #getPassword()
		 */
		String PASSWORD = "password";

		/**
		 * The user name.
		 */
		@Name(USER)
		@Mandatory
		String getUser();

		/**
		 * @see #getUser()
		 */
		void setUser(String value);

		/**
		 * The password of the user.
		 */
		@Name(PASSWORD)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getPassword();

		/**
		 * @see #getPassword()
		 */
		void setPassword(PolymorphicConfiguration<? extends SecretSource> value);

	}

	private final SecretSource _password;

	/**
	 * Creates a {@link BasicCredentials} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public BasicCredentials(InstantiationContext context, Config config) {
		super(context, config);
		_password = context.getInstance(config.getPassword());
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		request.setHeader(AUTHORIZATION, basicAuthorization(getConfig().getUser(), _password.getSecret()));
	}

	/**
	 * The value of an {@link #AUTHORIZATION} header for HTTP basic authentication.
	 *
	 * @param user
	 *        The user name.
	 * @param password
	 *        The password.
	 * @return The header value with the scheme {@link CredentialProvider#BASIC}.
	 */
	public static String basicAuthorization(String user, String password) {
		String userPass = user + ":" + password;
		return BASIC + " " + Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
	}

}

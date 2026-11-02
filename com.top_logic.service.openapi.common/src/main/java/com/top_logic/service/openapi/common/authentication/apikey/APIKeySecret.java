/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.apikey;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.TokenEditor;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;

/**
 * {@link SecretConfiguration} for authentication using an API key.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface APIKeySecret extends SecretConfiguration {

	/** Configuration name for {@link #getAPIKey()}. */
	String API_KEY = "api-key";

	/** Configuration name for {@link #getDescription()}. */
	String DESCRIPTION = "description";

	/**
	 * The API key to authenticate.
	 *
	 * <p>
	 * The key is read each time it is used. An attribute value is an encrypted key (or a value
	 * with the prefix <code>unencrypted:</code>). A key in the secret storage of the installation is
	 * referenced by its name; since the property defaults to a key given in the configuration, the
	 * reference names its class:
	 * </p>
	 *
	 * <pre>
	 * &lt;api-key-server-secret domain="my-api"&gt;
	 *   &lt;api-key class="com.top_logic.basic.credential.NamedSecretSource" name="my_api_key"/&gt;
	 * &lt;/api-key-server-secret&gt;
	 * </pre>
	 */
	@Mandatory
	@Name(API_KEY)
	@Format(SecretSourceFormat.class)
	@ImplementationClassDefault(LiteralSecretSource.class)
	@PropertyEditor(TokenEditor.class)
	PolymorphicConfiguration<? extends SecretSource> getAPIKey();

	/**
	 * Setter for {@link #getAPIKey()}.
	 */
	void setAPIKey(PolymorphicConfiguration<? extends SecretSource> value);

	/**
	 * Optional description about the usage of the API key.
	 */
	@Name(DESCRIPTION)
	@Nullable
	@MultiLine
	String getDescription();

}


/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.credential.CredentialProvider;

/**
 * A kind of git hosting service the control application works with, e.g. Gitea: it creates the
 * {@link GitHostingProvider} of a git connection to a server of its kind.
 *
 * <p>
 * A module supporting a kind of hosting service registers its type under its {@link #getName()
 * name} in the {@link GitHostingService.Config#getProviderTypes() provider types} of the
 * {@link GitHostingService}. A git connection names the type of its server, see
 * {@link GitConnections}.
 * </p>
 */
public interface GitProviderType {

	/**
	 * Configuration of a {@link GitProviderType}.
	 */
	interface Config<I extends GitProviderType> extends PolymorphicConfiguration<I>, NamedConfigMandatory {
		// The name is the name of the type.
	}

	/**
	 * The name the connections name the type by, e.g. {@code gitea}.
	 */
	String getName();

	/**
	 * Creates the provider of a connection to a server of this type.
	 *
	 * <p>
	 * Creating the provider does not contact the server.
	 * </p>
	 *
	 * @param settings
	 *        The server, the owner of the repositories and the credentials of the connection.
	 * @return The provider.
	 * @throws com.top_logic.util.error.TopLogicException
	 *         If the settings are invalid for this type, e.g. the URL is no URL of a server.
	 */
	GitHostingProvider createProvider(GitConnectionSettings settings);

	/**
	 * The credentials sending the access token kept in the named secret of the given name, the way
	 * a server of this type expects an access token.
	 *
	 * @param secretName
	 *        The name of the secret, see {@link com.top_logic.basic.credential.SecretStoreService}.
	 */
	CredentialProvider tokenCredentials(String secretName);

}

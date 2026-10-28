/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.util.Objects;

import com.top_logic.basic.credential.CredentialProvider;

/**
 * What a {@link GitProviderType} creates the {@link GitHostingProvider} of a git connection from.
 *
 * @param url
 *        The URL of the server, e.g. {@code https://git.example.com/}.
 * @param owner
 *        The organization or user the repositories are created in, see
 *        {@link GitHostingProvider#getOwner()}.
 * @param credentials
 *        How the requests to the server are authenticated.
 * @param createOwner
 *        Whether the owner is an organization the provider creates on first use, owned by the user
 *        of the credentials; otherwise the owner must exist.
 */
public record GitConnectionSettings(String url, String owner, CredentialProvider credentials, boolean createOwner) {

	/**
	 * Creates {@link GitConnectionSettings}.
	 */
	public GitConnectionSettings {
		Objects.requireNonNull(url, "url");
		Objects.requireNonNull(owner, "owner");
		Objects.requireNonNull(credentials, "credentials");
	}

	@Override
	public String toString() {
		// The credentials are not shown.
		return "GitConnectionSettings[" + url + ", " + owner + (createOwner ? ", created" : "") + "]";
	}

}

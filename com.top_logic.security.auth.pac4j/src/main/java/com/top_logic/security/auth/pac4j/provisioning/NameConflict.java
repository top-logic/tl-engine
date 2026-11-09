/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * What {@link OidcProvisioningUserMapping} does when the login name of an account to create is
 * already taken by an account the identity is not linked to.
 *
 * <p>
 * Such an account is never linked to the identity: it may belong to someone else.
 * </p>
 */
public enum NameConflict implements ExternallyNamed {

	/**
	 * The login is refused with a message naming the conflict.
	 */
	REFUSE("refuse"),

	/**
	 * The account is created under the name <code>&lt;name&gt;@&lt;client&gt;</code>, if that is
	 * taken as well under <code>&lt;name&gt;@&lt;client&gt;-2</code>,
	 * <code>&lt;name&gt;@&lt;client&gt;-3</code>, and so on.
	 */
	RENAME("rename"),

	;

	private final String _externalName;

	NameConflict(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How an access parent restricted to some {@link OperationRestriction#getOperations() operations}
 * decides Discover on the delegating object.
 *
 * @see OperationRestriction#getDiscover()
 */
public enum DiscoverDelegation implements ExternallyNamed {

	/**
	 * Discover is delegated together with the listed operations: a user may discover the object if
	 * the user may discover its access parent.
	 */
	DELEGATED("delegated"),

	/**
	 * Discover is not delegated as an operation of its own: through the access parent, a user may
	 * discover the object only if the access parent allows one of the listed operations to the
	 * user. Being allowed to discover the access parent alone does not make the object
	 * discoverable.
	 */
	IMPLIED("implied");

	private final String _externalName;

	private DiscoverDelegation(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}

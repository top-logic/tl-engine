/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.accesstoken;

import com.top_logic.element.accesstoken.AccessTokenPermissions.PermissionConfig;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.util.Resources;

/**
 * Shows an {@link PermissionConfig access token permission} by its label, e.g. among the options of
 * the permissions of a new token.
 */
public class AccessTokenPermissionResourceProvider extends DefaultResourceProvider {

	/** Singleton {@link AccessTokenPermissionResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final AccessTokenPermissionResourceProvider INSTANCE = new AccessTokenPermissionResourceProvider();

	/**
	 * Creates a {@link AccessTokenPermissionResourceProvider}.
	 */
	protected AccessTokenPermissionResourceProvider() {
		super();
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof PermissionConfig permission) {
			return Resources.getInstance().getString(permission.getLabel());
		}
		return super.getLabel(object);
	}

}

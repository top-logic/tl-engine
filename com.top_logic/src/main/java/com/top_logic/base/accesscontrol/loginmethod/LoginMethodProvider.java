/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol.loginmethod;

import java.util.List;

/**
 * Provider of {@link LoginMethod}s contributed by a module.
 *
 * <p>
 * Implementations are registered application-wide through {@link LoginMethodConfig} and enumerated by
 * login UIs via {@link LoginMethods#all()}. For example, the pac4j module contributes a provider that
 * yields one {@link LoginMethod} per configured SSO client.
 * </p>
 *
 * @see LoginMethod
 * @see LoginMethods
 */
public interface LoginMethodProvider {

	/**
	 * The login methods currently offered by this provider.
	 *
	 * <p>
	 * Called when a login UI is built; may reflect the current configuration (e.g. the set of
	 * enabled SSO clients). May return an empty list, never {@code null}.
	 * </p>
	 */
	List<? extends LoginMethod> getLoginMethods();

}

/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import org.pac4j.core.profile.CommonProfile;

import com.top_logic.basic.StringServices;

/**
 * Default {@link UserNameExtractor} using {@link CommonProfile#getUsername()} with
 * {@link CommonProfile#getId()} as fallback.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultUserNameExtractor implements UserNameExtractor {

	/**
	 * Singleton {@link DefaultUserNameExtractor} instance.
	 */
	public static final DefaultUserNameExtractor INSTANCE = new DefaultUserNameExtractor();

	private DefaultUserNameExtractor() {
		// Singleton constructor.
	}

	@Override
	public String getUserName(CommonProfile profile) {
		String result = profile.getUsername();
		if (StringServices.isEmpty(result)) {
			result = profile.getId();
		}
		return result;
	}

}

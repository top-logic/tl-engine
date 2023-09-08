/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import org.pac4j.jee.filter.CallbackFilter;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration aspect for the callback URL.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface HasCallbackUrl extends ConfigurationItem {

	/**
	 * URL where the the {@link CallbackFilter} is reachable.
	 * 
	 * @see #isCallbackUrlAbsolute()
	 */
	@Name("callback-url")
	@Nullable
	String getCallbackUrl();

	/**
	 * Whether the {@link #getCallbackUrl()} is an absolute URL.
	 * 
	 * <p>
	 * If <code>false</code>, the {@link #getCallbackUrl()} is relative to the application's context
	 * path.
	 * </p>
	 */
	@Name("callback-url-absolute")
	boolean isCallbackUrlAbsolute();

}

/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for the external authentication.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface ExternalAuthentication extends ConfigurationItem {
	String XML_KEY_REQUEST_HEADER = "HeaderKey";

	String XML_KEY_EXTAUTH_ACTIVATE = "isEnabled";

	String XML_KEY_ALLOW_NO_DOMAIN = "allowNoDomain";

	String XML_KEY_REUSE_SESSION = "reuseSession";

	@Name(XML_KEY_REQUEST_HEADER)
	String getHeaderKey();

	@Name(XML_KEY_EXTAUTH_ACTIVATE)
	boolean getIsEnabled();

	@Name(XML_KEY_ALLOW_NO_DOMAIN)
	boolean getAllowNoDomain();

	@Name(XML_KEY_REUSE_SESSION)
	boolean getReuseSession();
}

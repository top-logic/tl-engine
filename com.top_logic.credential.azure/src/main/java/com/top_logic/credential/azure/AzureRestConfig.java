/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * Configuration of the HTTP connection of an Azure SDK client to an Azure endpoint.
 *
 * @see AzureHttpClients#create(AzureRestConfig)
 */
@Abstract
public interface AzureRestConfig extends ConfigurationItem {

	/**
	 * @see #getConnectTimeout()
	 */
	String CONNECT_TIMEOUT = "connect-timeout";

	/**
	 * @see #getRequestTimeout()
	 */
	String REQUEST_TIMEOUT = "request-timeout";

	/**
	 * Maximum time for establishing a connection to the endpoint.
	 */
	@Name(CONNECT_TIMEOUT)
	@Format(MillisFormat.class)
	@FormattedDefault("10s")
	long getConnectTimeout();

	/**
	 * @see #getConnectTimeout()
	 */
	void setConnectTimeout(long value);

	/**
	 * Maximum time for a request to the endpoint, until the response has been received.
	 */
	@Name(REQUEST_TIMEOUT)
	@Format(MillisFormat.class)
	@FormattedDefault("30s")
	long getRequestTimeout();

	/**
	 * @see #getRequestTimeout()
	 */
	void setRequestTimeout(long value);

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.routing;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Binds a view channel to a URL route parameter.
 *
 * <p>
 * Example: {@code <bind channel="estateId" route-param="estateId"/>}
 * </p>
 */
@TagName("bind")
public interface ParamBindingConfig extends ConfigurationItem {

	/** Configuration name for {@link #getChannel()}. */
	String CHANNEL = "channel";

	/** Configuration name for {@link #getRouteParam()}. */
	String ROUTE_PARAM = "route-param";

	/**
	 * The name of the channel to bind to.
	 */
	@Name(CHANNEL)
	@Mandatory
	String getChannel();

	/**
	 * The name of the URL route parameter to bind from.
	 */
	@Name(ROUTE_PARAM)
	@Mandatory
	String getRouteParam();
}

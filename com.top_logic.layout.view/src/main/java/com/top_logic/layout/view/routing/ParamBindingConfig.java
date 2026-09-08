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
 *
 * <p>
 * The binding works in both directions: the value of the channel becomes the URL path segment, and
 * the segment of an opened URL becomes the value of the channel. A URL without the segment leaves
 * the channel as it is - it says nothing about the value, rather than saying that there is none - so
 * a view opened that way keeps what it establishes itself, the element a table selects by default
 * for instance, and the address bar then names that value.
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

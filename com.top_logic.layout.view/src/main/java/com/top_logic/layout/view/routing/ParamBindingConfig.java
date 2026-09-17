/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.routing;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
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
 *
 * <p>
 * The value fills one path segment and is percent-encoded, so a value of any shape - one containing
 * a slash or a space - stays the single segment the binding reads it back from. A
 * {@link #getPrefix()} puts static segments in front of it, which name what the value stands for.
 * </p>
 */
@TagName("bind")
public interface ParamBindingConfig extends ConfigurationItem {

	/** Configuration name for {@link #getChannel()}. */
	String CHANNEL = "channel";

	/** Configuration name for {@link #getRouteParam()}. */
	String ROUTE_PARAM = "route-param";

	/** Configuration name for {@link #getPrefix()}. */
	String PREFIX = "prefix";

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

	/**
	 * Static path segments placed in front of the value.
	 *
	 * <p>
	 * A prefix of {@code detail} makes the binding occupy {@code detail/&lt;value&gt;} instead of a
	 * bare value segment, which distinguishes it from the other segments of the URL and names what
	 * the value identifies. The prefix appears exactly when the value does: a URL that carries the
	 * prefix without a value behind it describes no value, and the channel keeps what it holds.
	 * </p>
	 *
	 * <p>
	 * Without a prefix, the binding occupies the value segment alone, and its position in the URL
	 * is what identifies it.
	 * </p>
	 */
	@Name(PREFIX)
	@Nullable
	String getPrefix();
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Configuration for a channel binding between a parent view and a referenced child view.
 *
 * <p>
 * Used inside {@code <view-ref>} to map a channel from the parent's scope to a channel declared in
 * the referenced view. At runtime, the parent's channel instance is shared with the child - both
 * sides read and write the same object.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view-ref view="detail.view.xml"&gt;
 *   &lt;bind channel="item" to="selectedCustomer"/&gt;
 * &lt;/view-ref&gt;
 * </pre>
 */
@TagName("bind")
public interface ChannelBindingConfig extends ConfigurationItem {

	/** Configuration name for {@link #getChannel()}. */
	String CHANNEL = "channel";

	/** Configuration name for {@link #getTo()}. */
	String TO = "to";

	/**
	 * Name of the channel in the referenced (child) view.
	 */
	@Name(CHANNEL)
	@Mandatory
	String getChannel();

	/**
	 * Reference to a channel in the parent scope.
	 */
	@Name(TO)
	@Mandatory
	@Format(ChannelRefFormat.class)
	ChannelRef getTo();
}

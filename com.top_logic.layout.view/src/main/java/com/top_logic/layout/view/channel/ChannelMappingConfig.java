/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for a single channel mapping between two scopes.
 *
 * <p>
 * Used in {@code <input>} and {@code <output>} elements within dialog command configuration to map
 * a channel from one context (parent) to another (dialog).
 * </p>
 */
public interface ChannelMappingConfig extends ConfigurationItem {

	/** Configuration name for {@link #getFrom()}. */
	String FROM = "from";

	/** Configuration name for {@link #getTo()}. */
	String TO = "to";

	/**
	 * The source channel name (in the context providing the value).
	 */
	@Name(FROM)
	@Mandatory
	String getFrom();

	/**
	 * The target channel name (in the context receiving the value).
	 */
	@Name(TO)
	@Mandatory
	String getTo();
}

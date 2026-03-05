/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Base configuration for channel declarations in a view.
 *
 * <p>
 * This is polymorphic so that different channel kinds (value channels, derived channels) can extend
 * it. The implementation class determines the channel behavior at runtime.
 * </p>
 */
public interface ChannelConfig extends PolymorphicConfiguration<ViewChannel> {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/**
	 * The unique name of this channel within the view.
	 */
	@Name(NAME)
	@Mandatory
	String getName();
}

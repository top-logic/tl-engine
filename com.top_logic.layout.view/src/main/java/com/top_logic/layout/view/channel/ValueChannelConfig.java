/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Configuration for a mutable value channel.
 *
 * <p>
 * A value channel holds a single mutable value that can be read and written by UI elements.
 * </p>
 */
@TagName("channel")
public interface ValueChannelConfig extends ChannelConfig {

	@Override
	@ClassDefault(ValueChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();
}

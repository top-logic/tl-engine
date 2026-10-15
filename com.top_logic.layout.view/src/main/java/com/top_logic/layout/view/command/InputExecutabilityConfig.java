/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * {@link ExecutabilityConfig} whose rules decide over the value of a single input channel.
 *
 * <p>
 * Used by operations that are not commands, such as dragging and dropping, whose rules decide for
 * an element as a whole.
 * </p>
 */
@Abstract
public interface InputExecutabilityConfig extends ExecutabilityConfig {

	/** Configuration name for {@link #getInput()}. */
	String INPUT = "input";

	/**
	 * Reference to a channel whose value the {@link #getExecutability() executability} rules
	 * decide over.
	 */
	@Name(INPUT)
	@Nullable
	@Format(ChannelRefFormat.class)
	ChannelRef getInput();

}

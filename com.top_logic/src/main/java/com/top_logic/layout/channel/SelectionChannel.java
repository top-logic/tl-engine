/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.layout.component.Selectable;

/**
 * The selection channel of a {@link Selectable} component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SelectionChannel {

	/**
	 * Name of {@link SelectionChannel}.
	 */
	public static final String NAME = "selection";

	/**
	 * Singleton {@link SelectionChannel} instance.
	 */
	public static final ChannelSPI INSTANCE = new DefaultChannelSPI(NAME, null);

}
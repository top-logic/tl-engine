/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collections;

import com.top_logic.layout.table.component.ComponentRowSource;

/**
 * The channel of currently displayed rows of a table.
 * 
 * @see ComponentRowSource
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class RowsChannel {

	/**
	 * Name of {@link RowsChannel}.
	 */
	public static final String NAME = "rows";

	/**
	 * Singleton rows channel instance.
	 */
	public static final ChannelSPI INSTANCE = new DefaultChannelSPI(NAME, Collections.emptyList());

}
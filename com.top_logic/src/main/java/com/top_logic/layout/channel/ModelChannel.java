/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelSPI} for the component model channel, see {@link LayoutComponent#getModel()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelChannel {

	/**
	 * Name of {@link ModelChannel}.
	 */
	public static final String NAME = "model";

	/**
	 * {@link ChannelSPI} for {@link LayoutComponent#modelChannel()}.
	 */
	public static final ChannelSPI INSTANCE = new DefaultChannelSPI(NAME, null);

}
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.channel;

import java.util.Collections;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;

/**
 * Channel that contains a list of objects as value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("List-valued channel")
public class ListChannelFactory implements ChannelFactory {

	/**
	 * @see com.top_logic.layout.channel.ChannelFactory#createChannel(java.lang.String)
	 */
	@Override
	public ChannelSPI createChannel(String name) {
		return new DefaultChannelSPI(name, Collections.emptyList());
	}

}


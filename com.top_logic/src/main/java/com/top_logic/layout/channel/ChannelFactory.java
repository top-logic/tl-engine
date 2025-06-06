/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.channel;

/**
 * Factory for component channels.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ChannelFactory {

	/**
	 * Creates a new {@link ChannelSPI} with the given name.
	 * 
	 * @param name
	 *        Name of the created channel.
	 */
	ChannelSPI createChannel(String name);

}


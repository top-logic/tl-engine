/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

/**
 * The receiving end a {@link ChannelSource} publishes the values of a channel into.
 *
 * <p>
 * A {@link ChannelSink} may be called from any thread and never blocks the caller: the value is
 * delivered to the channel later, on a thread allowed to write it. The channel holds state, not
 * events: a value published while an earlier one has not been delivered yet supersedes it, so only
 * the latest state reaches the channel.
 * </p>
 *
 * @see ChannelPublisher
 */
public interface ChannelSink {

	/**
	 * Publishes the value the channel is to hold.
	 *
	 * <p>
	 * May be called from any thread; does not block and does not write the channel on the calling
	 * thread. Publishing after the subscription the sink was handed to has ended is harmless: the
	 * value is ignored.
	 * </p>
	 *
	 * @param value
	 *        The value the channel is to hold.
	 */
	void publish(Object value);

}

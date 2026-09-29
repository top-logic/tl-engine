/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

/**
 * A reference to a {@link ViewChannel} used in configuration attributes.
 *
 * <p>
 * Currently holds just a channel name for local references within the same view. In the future,
 * this will be extended with a view path component for cross-view references
 * ({@code path/to/view.view.xml#channelName}).
 * </p>
 */
public class ChannelRef {

	private final String _channelName;

	/**
	 * Creates a {@link ChannelRef}.
	 *
	 * @param channelName
	 *        The name of the referenced channel.
	 */
	public ChannelRef(String channelName) {
		_channelName = channelName;
	}

	/**
	 * The name of the referenced channel.
	 */
	public String getChannelName() {
		return _channelName;
	}

	@Override
	public String toString() {
		return _channelName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ChannelRef)) {
			return false;
		}
		return _channelName.equals(((ChannelRef) obj)._channelName);
	}

	@Override
	public int hashCode() {
		return _channelName.hashCode();
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A reference to a {@link ViewChannel} whose value is a named argument, see {@link NamedInputs}.
 *
 * <p>
 * The argument is named after the channel, or by the name given with {@link #getAs()}.
 * </p>
 */
public final class InputRef {

	private final ChannelRef _channel;

	private final String _as;

	/**
	 * Creates an {@link InputRef}.
	 *
	 * @param channel
	 *        The referenced channel.
	 * @param as
	 *        The name of the argument, {@code null} to name it after the channel.
	 */
	public InputRef(ChannelRef channel, String as) {
		_channel = Objects.requireNonNull(channel);
		_as = as == null || as.isBlank() ? null : as.trim();
	}

	/**
	 * Creates an {@link InputRef} naming its argument after the channel.
	 */
	public InputRef(ChannelRef channel) {
		this(channel, null);
	}

	/**
	 * The referenced channel.
	 */
	public ChannelRef getChannel() {
		return _channel;
	}

	/**
	 * The explicitly given name of the argument, {@code null} when it is named after the channel.
	 */
	public String getAs() {
		return _as;
	}

	/**
	 * The name of the argument holding the channel's value.
	 */
	public String getName() {
		return _as != null ? _as : _channel.getChannelName();
	}

	/**
	 * The {@link #getName() argument names} of the given references, in order.
	 */
	public static List<String> names(List<InputRef> inputs) {
		List<String> result = new ArrayList<>(inputs.size());
		for (InputRef input : inputs) {
			result.add(input.getName());
		}
		return result;
	}

	/**
	 * The {@link #getChannel() channels} of the given references, in order.
	 */
	public static List<ChannelRef> channels(List<InputRef> inputs) {
		List<ChannelRef> result = new ArrayList<>(inputs.size());
		for (InputRef input : inputs) {
			result.add(input.getChannel());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InputRef other)) {
			return false;
		}
		return _channel.equals(other._channel) && Objects.equals(_as, other._as);
	}

	@Override
	public int hashCode() {
		return _channel.hashCode() * 31 + Objects.hashCode(_as);
	}

	@Override
	public String toString() {
		return _as == null ? _channel.toString() : _channel + " " + CommaSeparatedInputRefs.AS + " " + _as;
	}
}

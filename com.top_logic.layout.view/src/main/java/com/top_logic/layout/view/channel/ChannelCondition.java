/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * A condition over the current values of channels, together with the channels it reads, so that
 * whoever depends on it can follow it.
 *
 * <p>
 * The condition is evaluated anew on every {@link #holds()}; it keeps no value of its own. Whoever
 * follows it {@link #observe(Collection, Runnable) observes} its {@link #channels()} and asks it
 * again when one of them takes a new value.
 * </p>
 */
public interface ChannelCondition {

	/**
	 * The condition that always holds and reads no channel.
	 */
	ChannelCondition ALWAYS = new ChannelCondition() {
		@Override
		public boolean holds() {
			return true;
		}

		@Override
		public List<ViewChannel> channels() {
			return List.of();
		}

		@Override
		public ChannelCondition and(ChannelCondition other) {
			return other;
		}

		@Override
		public String toString() {
			return "always";
		}
	};

	/**
	 * Whether the condition holds for the current values of its {@link #channels()}.
	 */
	boolean holds();

	/**
	 * The channels whose values the condition depends on: whether it {@link #holds()} can change
	 * only when one of them takes a new value.
	 */
	List<ViewChannel> channels();

	/**
	 * The condition holding where both this one and the given one hold, reading the channels of
	 * both.
	 */
	default ChannelCondition and(ChannelCondition other) {
		if (other == ALWAYS) {
			return this;
		}
		ChannelCondition self = this;
		List<ViewChannel> channels = union(List.of(self, other));
		return new ChannelCondition() {
			@Override
			public boolean holds() {
				return self.holds() && other.holds();
			}

			@Override
			public List<ViewChannel> channels() {
				return channels;
			}
		};
	}

	/**
	 * The condition a TL-Script computes from the values of the given channels.
	 *
	 * <p>
	 * The condition holds where the script yields a value TL-Script takes as true, see
	 * {@link SearchExpression#isTrue(Object)}; nothing at all does not hold.
	 * </p>
	 *
	 * @param script
	 *        The compiled script, taking the values of the given channels followed by the given
	 *        supplied values, see {@link InputScript}. {@code null} for no script, which yields
	 *        {@link #ALWAYS}.
	 * @param inputs
	 *        The channels whose current values lead the arguments of the script.
	 * @param supplied
	 *        The values the script receives behind the values of the channels, fixed for the
	 *        condition.
	 */
	static ChannelCondition of(QueryExecutor script, List<ViewChannel> inputs, Object... supplied) {
		if (script == null) {
			return ALWAYS;
		}
		List<ViewChannel> channels = List.copyOf(inputs);
		return new ChannelCondition() {
			@Override
			public boolean holds() {
				return SearchExpression.isTrue(script.execute(ChannelInputs.arguments(channels, supplied)));
			}

			@Override
			public List<ViewChannel> channels() {
				return channels;
			}
		};
	}

	/**
	 * The channels the given conditions read, each once, in the order of their first occurrence.
	 */
	static List<ViewChannel> union(Collection<? extends ChannelCondition> conditions) {
		Set<ViewChannel> result = Collections.newSetFromMap(new IdentityHashMap<>());
		List<ViewChannel> ordered = new ArrayList<>();
		for (ChannelCondition condition : conditions) {
			for (ViewChannel channel : condition.channels()) {
				if (result.add(channel)) {
					ordered.add(channel);
				}
			}
		}
		return ordered;
	}

	/**
	 * Follows the given conditions: runs the given action whenever one of the channels they read
	 * takes a new value, once per change, however many of the conditions read that channel.
	 *
	 * @param conditions
	 *        The conditions to follow.
	 * @param onChange
	 *        What to run, typically asking the conditions again.
	 * @return The registration ending the observation. It has to be disposed by whoever follows
	 *         the conditions, at the latest when it is disposed itself.
	 */
	static Registration observe(Collection<? extends ChannelCondition> conditions, Runnable onChange) {
		List<ViewChannel> channels = union(conditions);
		if (channels.isEmpty()) {
			return Registration.NONE;
		}
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> onChange.run();
		Set<Registration> registrations = new LinkedHashSet<>();
		for (ViewChannel channel : channels) {
			registrations.add(channel.addListener(listener));
		}
		return Registration.onDispose(() -> {
			for (Registration registration : registrations) {
				registration.dispose();
			}
		});
	}

}

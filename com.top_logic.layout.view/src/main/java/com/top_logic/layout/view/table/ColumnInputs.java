/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * The channels a column's functions read their leading arguments from.
 *
 * <p>
 * A column can compute what it shows from more than the row: what is selected elsewhere, what a
 * filter above the table holds. Such a value comes from a channel the column declares, and every
 * function of the column - the value it displays, the object it embeds, the update it writes -
 * receives those values ahead of the arguments the table calls it with, in declaration order.
 * </p>
 */
public class ColumnInputs {

	/**
	 * This class describes how a column reads its declared inputs; it has no state of its own.
	 */
	private ColumnInputs() {
		// Static utilities only.
	}

	/**
	 * The channels the given references name, in declaration order.
	 *
	 * @param scope
	 *        What the column is resolved against, holding the session the channels belong to.
	 * @param refs
	 *        The declared references.
	 */
	public static List<ViewChannel> resolve(ColumnResolution scope, List<ChannelRef> refs) {
		List<ViewChannel> result = new ArrayList<>(refs.size());
		for (ChannelRef ref : refs) {
			result.add(scope.context().resolveChannel(ref));
		}
		return result;
	}

	/**
	 * The arguments one of a column's functions is called with: the current values of the given
	 * channels, in declaration order, and what the function is called with behind them.
	 *
	 * <p>
	 * The channels are read here, every time the function is applied, so a column over an input
	 * shows what that input holds now.
	 * </p>
	 *
	 * @param inputs
	 *        The channels leading the argument list.
	 * @param trailing
	 *        The arguments behind the inputs, in the order the function takes them.
	 */
	public static Object[] arguments(List<ViewChannel> inputs, Object... trailing) {
		Object[] arguments = new Object[inputs.size() + trailing.length];
		for (int n = 0; n < inputs.size(); n++) {
			arguments[n] = inputs.get(n).get();
		}
		System.arraycopy(trailing, 0, arguments, inputs.size(), trailing.length);
		return arguments;
	}

}

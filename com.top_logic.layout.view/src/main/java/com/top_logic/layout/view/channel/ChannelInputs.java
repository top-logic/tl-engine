/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.ViewContext;

/**
 * How the {@link Inputs} of a configuration reach the functions that read them.
 *
 * <p>
 * The declared references are resolved once against the session displaying the view, and the
 * resulting channels are read again on every application of a function, so that a function over an
 * input yields what that input holds now.
 * </p>
 */
public class ChannelInputs {

	/**
	 * This class describes how declared inputs are read; it has no state of its own.
	 */
	private ChannelInputs() {
		// Static utilities only.
	}

	/**
	 * The channels the given references name, in declaration order.
	 *
	 * @param context
	 *        The per-session context the references are resolved in.
	 * @param refs
	 *        The declared references, see {@link Inputs#getInputs()}.
	 */
	public static List<ViewChannel> resolve(ViewContext context, List<InputRef> refs) {
		List<ViewChannel> result = new ArrayList<>(refs.size());
		for (InputRef ref : refs) {
			result.add(context.resolveChannel(ref.getChannel()));
		}
		return result;
	}

	/**
	 * The values a script is evaluated with: the current values of the given channels, in
	 * declaration order, followed by the given values.
	 *
	 * <p>
	 * For a script compiled with {@link InputScript#compile(com.top_logic.basic.config.ConfigurationItem, String)},
	 * these are the values of its variables: the inputs followed by the values the element supplies,
	 * in the order of {@link InputScript#variables(com.top_logic.basic.config.ConfigurationItem, com.top_logic.basic.config.PropertyDescriptor)}.
	 * </p>
	 *
	 * @param inputs
	 *        The channels whose values lead the list.
	 * @param trailing
	 *        The values behind the inputs, for a script with implicit variables the values of the
	 *        supplied variables in their declaration order.
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

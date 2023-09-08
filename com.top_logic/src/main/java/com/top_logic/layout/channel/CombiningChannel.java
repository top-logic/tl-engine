/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.channel.linking.impl.CombineLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractDerivedComponentChannel} combining values from multiple input values into a list
 * of values.
 * 
 * @see CombineLinking
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CombiningChannel extends MultiInputChannel {

	/**
	 * Creates a {@link CombiningChannel}.
	 *
	 * @param component
	 *        The context component.
	 * @param sources
	 *        The source channels to take input values from.
	 */
	public CombiningChannel(LayoutComponent component, List<ComponentChannel> sources) {
		super(component, name(sources), sources);
	}

	private static String name(List<ComponentChannel> sources) {
		return "combine(" + sources.stream().map(ComponentChannel::toString).collect(Collectors.joining(", ")) + ")";
	}

	@Override
	protected Object lookupValue() {
		int size = size();
		Object[] args = new Object[size];
		for (int n = 0; n < size; n++) {
			args[n] = get(n);
		}
		return Arrays.asList(args);
	}

}
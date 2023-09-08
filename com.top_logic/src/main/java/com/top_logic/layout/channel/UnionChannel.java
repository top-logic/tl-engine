/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.channel.linking.impl.UnionLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractDerivedComponentChannel} selecting the first non-<code>null</code> value from
 * multiple input channels.
 * 
 * @see UnionLinking
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnionChannel extends MultiInputChannel {

	/**
	 * Creates a {@link UnionChannel}.
	 *
	 * @param component
	 *        The context component.
	 * @param sources
	 *        The source channels to take input values from.
	 */
	public UnionChannel(LayoutComponent component, List<ComponentChannel> sources) {
		super(component, name(sources), sources);
	}

	private static String name(List<ComponentChannel> sources) {
		return "union(" + sources.stream().map(ComponentChannel::toString).collect(Collectors.joining(", ")) + ")";
	}

	@Override
	protected Object lookupValue() {
		for (int n = 0, cnt = size(); n < cnt; n++) {
			Object result = get(n);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
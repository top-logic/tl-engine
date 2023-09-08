/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.UnionChannel;
import com.top_logic.layout.channel.linking.Union;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelLinking} specified by {@link Union}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnionLinking extends AbstractMultiChannelLinking<Union> {

	/**
	 * Creates a {@link UnionLinking}.
	 */
	public UnionLinking(InstantiationContext context, Union config) {
		super(context, config);
	}

	@Override
	public ComponentChannel createChannel(Log log, LayoutComponent contextComponent) {
		List<ComponentChannel> sourceChannels =
			inputs().stream().map(linking -> linking.resolveChannel(log, contextComponent)).filter(Objects::nonNull)
				.collect(Collectors.toList());
		return new UnionChannel(contextComponent, sourceChannels);
	}

	@Override
	public Object eval(LayoutComponent component) {
		for (ChannelLinking part : inputs()) {
			Object result = part.eval(component);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public void appendTo(StringBuilder result) {
		boolean first = true;
		for (ChannelLinking part : inputs()) {
			if (first) {
				first = false;
			} else {
				result.append(",");
			}
			part.appendTo(result);
		}
	}

	@Override
	public boolean hasCompactRepresentation() {
		for (ChannelLinking part : inputs()) {
			if (!part.hasCompactRepresentation()) {
				return false;
			}
		}
		return true;
	}
}

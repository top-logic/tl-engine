/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.channel.CombiningChannel;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelLinking} creating a {@link CombiningChannel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class CombineLinking extends AbstractMultiChannelLinking<CombineLinking.Config> {

	/**
	 * Configuration options for {@link CombineLinking}.
	 */
	@TagName("combine")
	public interface Config extends AbstractMultiChannelLinking.Config {
		@Override
		@ClassDefault(CombineLinking.class)
		Class<? extends ChannelLinking> getImplementationClass();
	}

	/**
	 * Creates a {@link CombineLinking}.
	 */
	public CombineLinking(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ComponentChannel createChannel(Log log, LayoutComponent contextComponent) {
		List<ComponentChannel> sources = inputs()
			.stream()
			.map(linking -> linking.resolveChannel(log, contextComponent))
			.collect(Collectors.toList());
		return new CombiningChannel(contextComponent, sources);
	}

	@Override
	public Object eval(LayoutComponent component) {
		return inputs()
			.stream()
			.map(linking -> linking.eval(component))
			.collect(Collectors.toList());
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append('[');
		boolean first = true;
		for (ChannelLinking input : inputs()) {
			if (first) {
				first = false;
			} else {
				result.append(',');
			}
			input.appendTo(result);
		}
		result.append(']');
	}

}

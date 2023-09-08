/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ConstantChannel;
import com.top_logic.layout.channel.linking.False;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelLinking} specified by {@link False}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FalseLinking extends AbstractCachedChannelLinking<False> {

	/**
	 * Creates a {@link FalseLinking}.
	 */
	public FalseLinking(InstantiationContext context, False config) {
		super(context, config);
	}

	@Override
	public ComponentChannel createChannel(Log log, LayoutComponent contextComponent) {
		return new ConstantChannel(contextComponent, Boolean.FALSE);
	}

	@Override
	public Object eval(LayoutComponent component) {
		return Boolean.FALSE;
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append("false");
	}

	@Override
	public boolean hasCompactRepresentation() {
		return true;
	}
}

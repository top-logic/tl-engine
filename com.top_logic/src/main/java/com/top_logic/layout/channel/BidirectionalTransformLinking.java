/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.function.BiFunction;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Linking creating a bidirectional transforming channel.
 * 
 * Using an inverse transformation to determine the value for the source channel.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class BidirectionalTransformLinking<C extends AbstractTransformLinking.Config>
		extends AbstractTransformLinking<C> {

	/**
	 * Creates a {@link BidirectionalTransformLinking}.
	 */
	public BidirectionalTransformLinking(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ComponentChannel createChannel(Log log, LayoutComponent contextComponent) {
		return new BidirectionalTransformingChannel(contextComponent, input().resolveChannel(log, contextComponent),
			transformation(), inverseTransformation());
	}

	/**
	 * Mapping to determine the value for the source channel from the new and the old value of the
	 * transformed channel.
	 */
	protected abstract BiFunction<Object, Object, ?> inverseTransformation();

}

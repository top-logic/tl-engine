/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.function.BiFunction;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TransformingChannel} applying in addition a transformation to the output values.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class BidirectionalTransformingChannel extends TransformingChannel {

	private final BiFunction<Object, Object, ?> _inverseTransform;

	/**
	 * Creates a {@link TransformingChannel}.
	 *
	 * @param component
	 *        The context component.
	 * @param source
	 *        The source channel to take input values from.
	 * @param transform
	 *        The transformation to apply to input values.
	 * @param inverseTransform
	 *        The transformation to apply to output values.
	 */
	public BidirectionalTransformingChannel(LayoutComponent component, ComponentChannel source,
			BiFunction<Object, Object, ?> transform,
			BiFunction<Object, Object, ?> inverseTransform) {
		super(component, source, transform);

		_inverseTransform = inverseTransform;
	}

	@Override
	protected void storeValue(Object newValue, Object oldValue) {
		super.storeValue(newValue, oldValue);

		for (ComponentChannel source : sources()) {
			source.set(_inverseTransform.apply(newValue, oldValue));
		}
	}

}

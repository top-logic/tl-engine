/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * Utilities for {@link Renderer} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RendererUtil {

	/**
	 * Implementation of {@link Renderer#generic(Class)}.
	 */
	static <X, T> Renderer<? super X> upgrade(Class<X> expectedType, Renderer<T> renderer) {
		@SuppressWarnings("unchecked")
		Class<T> supportedType = (Class<T>) JavaTypeUtil.getTypeBound(renderer.getClass(), Renderer.class, 0);
		if (supportedType.isAssignableFrom(expectedType)) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Renderer<X> compatibleRenderer = (Renderer) renderer;
			return compatibleRenderer;
		} else {
			return new SafeRenderer<>(supportedType, renderer);
		}
	}

}

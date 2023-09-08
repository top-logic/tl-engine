/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link TypedAnnotationContainer} that can be used concurrently.
 * 
 * @see TypedAnnotationContainer
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcurrentTypedAnnotationContainer extends TypedAnnotationContainer {

	@Override
	protected Map<Property<?>, Object> createStorage() {
		return new ConcurrentHashMap<>();
	}

}


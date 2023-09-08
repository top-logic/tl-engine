/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} using {@link Class#getSimpleName()} that is safe against classes with
 * missing dependencies given by a {@link Class}.
 * 
 * @see ClassLabelProvider#simpleName(Class)
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class SimpleClassNameProvider implements LabelProvider {

	/**
	 * Singleton {@link SimpleClassNameProvider} instance.
	 */
	public static final SimpleClassNameProvider INSTANCE = new SimpleClassNameProvider();

	@Override
	public String getLabel(Object object) {
		if (object instanceof Class) {
			return ClassLabelProvider.simpleName((Class<?>) object);
		}

		return null;
	}

}

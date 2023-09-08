/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} using the resource for the {@link ResKey} given by a {@link Class}.
 * 
 * @see ClassLabelProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class I18NClassNameProvider implements LabelProvider {

	/**
	 * Singleton {@link I18NClassNameProvider} instance.
	 */
	public static final I18NClassNameProvider INSTANCE = new I18NClassNameProvider();

	@Override
	public String getLabel(Object object) {
		if (object instanceof Class) {
			Class<?> clazz = (Class<?>) object;

			return Resources.getInstance().getString(ResKey.forClass(clazz), clazz.getSimpleName());
		}

		return null;
	}

}

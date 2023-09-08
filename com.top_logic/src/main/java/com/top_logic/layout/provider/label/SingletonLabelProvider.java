/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * A {@link LabelProvider} that creates labels for singletons by using their class name as i18n key.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SingletonLabelProvider implements LabelProvider {

	/**
	 * The instance of the {@link SingletonLabelProvider}.
	 */
	public static final SingletonLabelProvider INSTANCE = new SingletonLabelProvider();

	@Override
	public String getLabel(Object singleton) {
		return i18n(getKey(singleton));
	}

	private Class<?> getKey(Object singleton) {
		return singleton.getClass();
	}

	private String i18n(Class<?> i18nKey) {
		return Resources.getInstance().getString(ResKey.forClass(i18nKey));
	}

}

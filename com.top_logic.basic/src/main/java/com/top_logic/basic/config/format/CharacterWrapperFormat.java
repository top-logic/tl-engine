/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for properties of type {@link Character}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CharacterWrapperFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link CharacterWrapperFormat} instance.
	 */
	public static final CharacterWrapperFormat INSTANCE = new CharacterWrapperFormat();

	private CharacterWrapperFormat() {
		super(Character.class);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getCharacter(propertyName, propertyValue);
	}
}
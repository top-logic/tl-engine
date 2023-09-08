/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.text.ParseException;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.layout.form.format.ThemeImageFormat;

/**
 * {@link ConfigurationValueProvider} that creates {@link ThemeImage} from property value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ThemeImageConfigFormat extends AbstractConfigurationValueProvider<ThemeImage> {

	/** Instance og {@link ThemeImageConfigFormat}. */
	public static final ThemeImageConfigFormat INSTANCE = new ThemeImageConfigFormat();

	private ThemeImageConfigFormat() {
		super(ThemeImage.class);
	}

	@Override
	protected ThemeImage getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		try {
			return (ThemeImage) ThemeImageFormat.INSTANCE.parseObject(propertyValue.toString());
		} catch (ParseException ex) {
			throw new ConfigurationException(
				com.top_logic.layout.basic.I18NConstants.INVALID_THEME_IMAGE_FORMAT.fill(propertyName),
				propertyName, propertyValue, ex);
		}
	}

	@Override
	protected String getSpecificationNonNull(ThemeImage configValue) {
		return ThemeImageFormat.INSTANCE.format(configValue);
	}

}


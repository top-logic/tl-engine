/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link AbstractConfigurationValueProvider} to parse page size options of table configurations.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PageSizeOptionsFormat extends AbstractConfigurationValueProvider<int[]> {

	public static final String ALL_VALUE = "all";

	public static final PageSizeOptionsFormat INSTANCE = new PageSizeOptionsFormat();

	private PageSizeOptionsFormat() {
		super(int[].class);
	}

	@Override
	protected int[] getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	protected int[] getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String configValue = propertyValue.toString();
		String[] options = configValue.trim().split("\\s*,\\s*");
		if (options.length == 0) {
			throw parseError(propertyName, configValue, null);
		}
		int[] result = new int[options.length];
		for (int n = 0, cnt = options.length; n < cnt; n++) {
			if (ALL_VALUE.equals(options[n])) {
				result[n] = PagingModel.SHOW_ALL;
			} else {
				try {
					result[n] = Integer.parseInt(options[n]);
					if (result[n] == 0) {
						throw parseError(propertyName, configValue, null);
					}
				} catch (NumberFormatException ex) {
					throw parseError(propertyName, configValue, ex);
				}
			}
		}
		return result;
	}

	private ConfigurationException parseError(String property, String configValue, NumberFormatException ex) {
		return new ConfigurationException(I18NConstants.ILLEGAL_PAGE_SIZE_OPTIONS__ALL_OPTION.fill(ALL_VALUE), property,
			configValue, ex);
	}

	@Override
	protected String getSpecificationNonNull(int[] configValue) {
		StringBuilder result = new StringBuilder();
		boolean addSeparator = false;
		for (int value : configValue) {
			if (addSeparator) {
				result.append(',');
			} else {
				addSeparator = true;
			}
			if (PagingModel.SHOW_ALL == value) {
				result.append(ALL_VALUE);
			} else {
				result.append(value);
			}
		}
		return result.toString();
	}

	@Override
	public int[] defaultValue() {
		return new int[0];
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value instanceof int[];
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return value;
	}

}


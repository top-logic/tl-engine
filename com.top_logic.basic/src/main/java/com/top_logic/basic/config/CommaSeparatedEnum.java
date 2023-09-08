/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.EnumerationNameMapping;

/**
 * {@link ListConfigValueProvider} for serializing a list of enums as comma
 * separated list.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommaSeparatedEnum<T extends Enum<T>> extends ListConfigValueProvider<T> {

	private static final char SEPARATOR_CHAR = ',';
	private static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);

	protected final Class<T> _enumType;

	protected CommaSeparatedEnum(Class<T> enumType) {
		this._enumType = enumType;

	}

	@Override
	public List<T> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String[] encodedEnums = propertyValue.toString().split(SEPARATOR);
		List<T> result = new ArrayList<>(encodedEnums.length);
		for (String encodedEnum : encodedEnums) {
			encodedEnum = encodedEnum.trim();
			result.add(ConfigUtil.getEnum(_enumType, encodedEnum));
		}
		return result;
	}

	@Override
	public String getSpecificationNonNull(List<T> configValue) {
		StringBuilder encodedEnums = new StringBuilder();
		boolean addSeparator = false;
		for (T _enum : configValue) {
			if (addSeparator) {
				encodedEnums.append(SEPARATOR_CHAR);
			} else {
				addSeparator = true;
			}
			encodedEnums.append(EnumerationNameMapping.INSTANCE.map(_enum));
		}
		return encodedEnums.toString();
	}

}

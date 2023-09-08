/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.translation;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Format;

/**
 * A source and target language separated by white space.
 */
@Format(LanguagePair.ConfigFormat.class)
public final class LanguagePair {

	private final String source;

	private final String target;

	/**
	 * Creates a {@link LanguagePair}.
	 *
	 * @param source
	 *        See {@link #getSource()}.
	 * @param target
	 *        See {@link #getTarget()}.
	 */
	public LanguagePair(String source, String target) {
		super();
		this.source = source;
		this.target = target;
	}

	/**
	 * The source language.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * The target language.
	 */
	public String getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return source + " " + target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguagePair other = (LanguagePair) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	/**
	 * Format for parsing {@link LanguagePair}s from the configuration.
	 */
	public static final class ConfigFormat extends AbstractConfigurationValueProvider<LanguagePair> {

		/**
		 * Creates a {@link ConfigFormat}.
		 */
		public ConfigFormat() {
			super(LanguagePair.class);
		}

		@Override
		protected String getSpecificationNonNull(LanguagePair configValue) {
			return configValue.toString();
		}

		@Override
		protected LanguagePair getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			String[] languages = propertyValue.toString().trim().split("[-\\s]+");
			if (languages.length != 2) {
				throw new ConfigurationException(I18NConstants.ERROR_EXPECTED_SOURCE_AND_TARGET_LANGUAGE, propertyName,
					propertyValue);
			}
			return new LanguagePair(languages[0], languages[1]);
		}

	}
}

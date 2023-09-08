/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.xio.importer.I18NConstants;

/**
 * A reference to a {@link TLStructuredTypePart} from an import specification.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(PartRef.ConfigFormat.class)
public interface PartRef extends ConfigurationItem {

	/**
	 * @see #getPart()
	 */
	String PART = "part";

	/**
	 * @see #getType()
	 */
	String TYPE = "type";

	/**
	 * Name of the {@link TLStructuredType} defining the {@link #getPart()}.
	 */
	@Name(TYPE)
	String getType();

	/**
	 * Name of the {@link TLStructuredTypePart} within it's {@link #getType()}.
	 */
	@Name(PART)
	String getPart();

	/**
	 * {@link ConfigurationValueProvider} for parsing a {@link PartRef}.
	 */
	public class ConfigFormat extends AbstractConfigurationValueProvider<PartRef> {

		/**
		 * Creates a {@link ConfigFormat}.
		 */
		public ConfigFormat() {
			super(PartRef.class);
		}

		@Override
		protected PartRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			PartRef result = TypedConfiguration.newConfigItem(PartRef.class);
			String value = propertyValue.toString();
			int sepIndex = value.indexOf('#');
			if (sepIndex < 0) {
				throw new ConfigurationException(
					I18NConstants.ERROR_INVALID_PART_REF__VALUE.fill(value), propertyName,
					propertyValue);
			}

			result.update(result.descriptor().getProperty(TYPE), value.substring(0, sepIndex));
			result.update(result.descriptor().getProperty(PART), value.substring(sepIndex + 1));
			
			return result;
		}

		@Override
		protected String getSpecificationNonNull(PartRef configValue) {
			return configValue.getType() + "#" + configValue.getPart();
		}

	}

}

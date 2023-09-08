/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.formats;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;

/**
 * A concrete value that can be used in the configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(DumpValueSpec.Format.class)
public class DumpValueSpec {

	private final ValueType _type;

	private final String _valueSpec;

	/**
	 * Creates a {@link DumpValueSpec}.
	 * 
	 * @param type
	 *        The value type.
	 * @param valueSpec
	 *        The values serialised form, see {@link ValueDumper#writeValue(ValueType, Object)}.
	 */
	public DumpValueSpec(ValueType type, String valueSpec) {
		_type = type;
		_valueSpec = valueSpec;
	}

	/**
	 * The values {@link ValueType}.
	 */
	public ValueType getType() {
		return _type;
	}

	/**
	 * The value's serialized form.
	 * 
	 * @see ValueDumper#writeValue(ValueType, Object)
	 */
	public String getValueSpec() {
		return _valueSpec;
	}

	/**
	 * {@link ConfigurationValueProvider} to store a {@link DumpValueSpec} in the configuration.
	 */
	public static class Format extends AbstractConfigurationValueProvider<DumpValueSpec> {

		/**
		 * Singleton {@link DumpValueSpec.Format} instance.
		 */
		public static final DumpValueSpec.Format INSTANCE = new DumpValueSpec.Format();
		
		private Format() {
			super(Object.class);
		}

		@Override
		protected DumpValueSpec getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			String value = propertyValue.toString();
			int sepIndex = value.indexOf(':');
			if (sepIndex < 0) {
				throw new ConfigurationException("Value '" + propertyValue + "' for property '" + propertyName
					+ "' has unexpected format. Expected: <type>:<value>.");
			}
			ValueType type = ValueType.valueOf(value.substring(0, sepIndex));

			return new DumpValueSpec(type, value.substring(sepIndex + 1));
		}

		@Override
		protected String getSpecificationNonNull(DumpValueSpec configValue) {
			return configValue.getType().name() + ":" + configValue.getValueSpec();
		}

	}

	/**
	 * Resolves the specification to a concrete value.
	 * 
	 * @param valueParser
	 *        The {@link ValueParser} to use for conversion.
	 * @return The concrete value.
	 * @throws E
	 *         If a parsing error occurs.
	 */
	public <E extends Throwable> Object resolve(ValueParser<E> valueParser) throws E {
		return valueParser.parseValue(_type, _valueSpec);
	}

}

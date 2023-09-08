/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration of a column in the enclosing {@link IndexConfig}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IndexPartConfig extends NamedConfigMandatory {

	/**
	 * {@link ConfigurationValueProvider} for {@link ReferencePart} that also allows
	 * <code>null</code> as value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class ReferencePartValueProvider extends AbstractConfigurationValueProvider<ReferencePart> {
		
		/** Singleton {@link IndexPartConfig.ReferencePartValueProvider} instance. */
		public static final ReferencePartValueProvider INSTANCE = new ReferencePartValueProvider();

		private final ConfigurationValueProvider<ReferencePart> _fallback;

		private ReferencePartValueProvider() {
			super(ReferencePart.class);
			@SuppressWarnings("unchecked")
			Class<ReferencePart> valueType = (Class<ReferencePart>) getValueType();
			_fallback = BuiltInFormats.getPrimitiveValueProvider(valueType);
		}

		@Override
		protected ReferencePart getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return _fallback.getValue(propertyName, propertyValue);
		}

		@Override
		protected String getSpecificationNonNull(ReferencePart configValue) {
			return _fallback.getSpecification(configValue);
		}

	}

	/** Configuration name of the property {@link #getPart()} */
	String REFERENCE_PART_ATTRIBUTE = DOXMLConstants.PART_ATTRIBUTE;

	/**
	 * Name of the {@link MOAttribute} to include in the {@link MOIndex}.
	 */
	@Override
	String getName();

	/**
	 * Part of the {@link MOReference} to include in the index.
	 * 
	 * <p>
	 * If represented attribute ({@link #getName()}) is an {@link MOReference} this part describes
	 * which part of the reference is included in the index.
	 * </p>
	 */
	@Name(REFERENCE_PART_ATTRIBUTE)
	@Format(IndexPartConfig.ReferencePartValueProvider.class)
	ReferencePart getPart();

}


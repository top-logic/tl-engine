/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Default {@link Generator} for {@link TLStructuredTypePart}s of {@link LegacyTypeCodes#TYPE_STRING_SET}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringSetOptions extends ListGeneratorAdaptor {

	private AttributeValueLocator _valueLocator;

	/**
	 * Configuration options for {@link StringSetOptions}.
	 */
	public interface Config extends PolymorphicConfiguration<Generator> {

		/** @see #getValueLocator() */
		String LOCATOR_PROPERTY = "locator";

		/**
		 * Algorithm providing potential values.
		 */
		@Name(LOCATOR_PROPERTY)
		AttributeValueLocator getValueLocator();

	}

	/**
	 * Creates a {@link StringSetOptions} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StringSetOptions(InstantiationContext context, Config config) {
		_valueLocator = config.getValueLocator();
	}

	/**
	 * @see Config#getValueLocator()
	 */
	public final AttributeValueLocator getValueLocator() {
		return _valueLocator;
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		try {
			if (_valueLocator != null) {
				TLObject object = editContext.getObject();
				List<?> theVals = (List<?>) _valueLocator.locateAttributeValue(object);
				if (theVals != null) {
					return theVals;
				}
			}
		} catch (Exception ex) {
			// Ignore Logger.warn("Failed to get RVs", ex ,this);
		}

		return Collections.emptyList();
	}

}

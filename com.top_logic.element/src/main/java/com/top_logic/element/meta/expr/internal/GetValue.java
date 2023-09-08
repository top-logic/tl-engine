/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.knowledge.wrap.ValueProvider;

/**
 * {@link AttributeValueLocator} that invokes the {@link ValueProvider#getValue(String) getValue()
 * operation} of a {@link ValueProvider} object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class GetValue extends CustomSingleSourceValueLocator implements ConfiguredInstance<GetValue.Config> {
	
	/**
	 * Configuration options for {@link GetValue}.
	 */
	@TagName("get-value")
	public interface Config extends PolymorphicConfiguration<GetValue> {

		/**
		 * Name of the attribute to access.
		 * 
		 * <p>
		 * The object whose attribute is accessed is either the base object declaring the attribute
		 * this {@link AttributeValueLocator} is configured in, if it is the first locator, or the
		 * result of the previous {@link AttributeValueLocator} in a {@link Chain}.
		 * </p>
		 */
		@Mandatory
		String getAttribute();

		/**
		 * @see #getAttribute()
		 */
		void setAttribute(String value);

	}

	/**
	 * Creates a {@link GetValue} configuration.
	 * 
	 * @param attribute
	 *        The name of the attribute to access.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(String attribute) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setAttribute(attribute);
		return config;
	}

	private final String _attribute;

	private final Config _config;

	/**
	 * Creates a {@link GetValue} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GetValue(InstantiationContext context, Config config) {
		_config = config;
		_attribute = config.getAttribute();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		if (anObject instanceof ValueProvider) {
			return ((ValueProvider) anObject).getValue(_attribute);
		}
		if (anObject instanceof NamedValues) {
			try {
				return ((NamedValues) anObject).getAttributeValue(_attribute);
			} catch (NoSuchAttributeException ex) {
				return null;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getClass().getName() + " attribute: " + _attribute;
	}
}
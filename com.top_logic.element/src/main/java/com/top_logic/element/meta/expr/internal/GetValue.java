/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelPartRef;

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

		/**
		 * The owner type of {@link #getAttribute()}.
		 * 
		 * @implNote The value must be set to be able to {@link GetValue#locateReferers(Object)
		 *           locate referers} for a given value.
		 */
		TLModelPartRef getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(TLModelPartRef value);

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

	/**
	 * Creates a {@link GetValue} configuration.
	 * 
	 * @param attribute
	 *        The attribute to access.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(
			TLStructuredTypePart attribute) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setAttribute(attribute.getName());
		config.setType(TLModelPartRef.ref(attribute.getOwner()));
		return config;
	}

	private final String _attribute;

	private final Config _config;

	private TLReference _reference;

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
		_reference = resolveReference(context);
	}

	private TLReference resolveReference(InstantiationContext context) {
		TLModelPartRef type = getConfig().getType();
		if (type == null) {
			return null;
		}
		TLClass owner;
		try {
			owner = type.resolveClass();
		} catch (ConfigurationException ex) {
			context.error(type + " can not be resolved to a class in " + this + ".", ex);
			return null;
		}
		TLStructuredTypePart part = owner.getPart(_attribute);
		if (part == null) {
			context.error(_attribute + " is not an attribute of type " + owner + " in " + this + ".");
			return null;
		}

		if (part instanceof TLReference) {
			return (TLReference) part;
		}
		return null;
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
	public Set<? extends TLObject> locateReferers(Object value) {
		if (_reference == null) {
			return Collections.emptySet();
		}
		if (!(value instanceof TLObject)) {
			return Collections.emptySet();
		}
		return ((TLObject) value).tReferers(_reference);
	}

	@Override
	public String toString() {
		return getClass().getName() + " attribute: " + _attribute;
	}
}
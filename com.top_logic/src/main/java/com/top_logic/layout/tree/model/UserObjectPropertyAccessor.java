/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.PropertyAccessor;

/**
 * {@link PropertyAccessor} that unwraps a {@link TLTreeNode} before forwarding to a delegate
 * {@link PropertyAccessor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UserObjectPropertyAccessor implements PropertyAccessor<TLTreeNode> {

	public interface Config extends PolymorphicConfiguration<PropertyAccessor<?>> {
		@Name(INNER_ACCESSOR_CONFIG_PARAM)
		@Mandatory
		@InstanceFormat
		PropertyAccessor<?> getInnerAccessor();
	}

	/**
	 * Parameter to use in configuration for the inner accessor.
	 */
	public static final String INNER_ACCESSOR_CONFIG_PARAM = "innerAccessor";

	private final PropertyAccessor<Object> inner;

	/**
	 * Creates a {@link UserObjectPropertyAccessor}.
	 * 
	 * @param inner
	 *        The delegate {@link PropertyAccessor}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	// Type safety cannot be guaranteed, as long as Accessor has no possibility to inspect the
	// dynamic type.
	public UserObjectPropertyAccessor(PropertyAccessor<?> inner) {
		this.inner = (PropertyAccessor) inner;
	}

	/**
	 * Creates a {@link UserObjectPropertyAccessor} with an inner accessor configured using the key
	 * {@link #INNER_ACCESSOR_CONFIG_PARAM}
	 */
	public UserObjectPropertyAccessor(InstantiationContext context, Config config) throws ConfigurationException {
		this(config.getInnerAccessor());
	}

	@Override
	public Object getValue(TLTreeNode object) {
		return inner.getValue(object.getBusinessObject());
	}

	@Override
	public void setValue(TLTreeNode object, Object value) {
		inner.setValue(object.getBusinessObject(), value);
	}

}


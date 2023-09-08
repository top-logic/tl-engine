/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.StringValueProvider;

/**
 * {@link ConfigurationValueBinding} for lists of primitive configuration values.
 * 
 * <p>
 * Reads a XML structure like:
 * </p>
 * 
 * <xmp>
 * <property-element>
 *    <list-element value-attribute="configuration-element-value-1"/>
 *    <list-element value-attribute="configuration-element-value-2"/>
 *    ...
 *    <list-element value-attribute="configuration-element-value-n"/>
 * </property-element>
 * </xmp>
 * 
 * <p>
 * Produces a configuration value that is a list consisting of the elements "configuration-element-value-1",...
 * </p>
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ListValueBinding<T> extends AbstractListBinding<List<T>, T> {

	/**
	 * Creates a {@link ListValueBinding}.
	 * 
	 * @param elementName 
	 *        Name of the "list-element" XML element to use, see {@link ListValueBinding}.
	 * @param attributeName 
	 *        Name of the "value-attribute" XML attribute to use on "list-element" elements. 
	 * @param valueProvider 
	 *        The {@link ConfigurationValueProvider} for serializing the list element values.
	 */
	public ListValueBinding(String elementName, String attributeName, ConfigurationValueProvider<T> valueProvider) {
		super(elementName, attributeName, valueProvider);
	}

	@Override
	protected List<T> valueFromList(List<T> result) {
		return result;
	}

	@Override
	protected List<T> copyValueToList(List<T> baseValue) {
		return baseValue == null ? new ArrayList<>() : new ArrayList<>(baseValue);
	}

	@Override
	protected List<T> valueAsList(List<T> value) {
		return value == null ? Collections.<T> emptyList() : Collections.unmodifiableList(value);
	}

	@Override
	public List<T> defaultValue() {
		return Collections.emptyList();
	}
	
	/**
	 * Creates a {@link ListValueBinding} with the given <code>elementName</code> and
	 * <code>attributeName</code> and {@link StringValueProvider} as
	 * {@link ConfigurationValueProvider}.
	 * 
	 * @see #ListValueBinding(String, String, ConfigurationValueProvider)
	 * @see StringValueProvider
	 */
	public static final ListValueBinding<String> simpleStringListValueBinding(String elementName, String attributeName) {
		return new ListValueBinding<>(elementName, attributeName, StringValueProvider.INSTANCE);
	}

}

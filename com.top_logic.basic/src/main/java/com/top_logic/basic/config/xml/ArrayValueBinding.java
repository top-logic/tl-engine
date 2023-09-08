/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.xml;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueBinding} for arrays of primitive configuration values.
 * 
 * <p>
 * Reads a XML structure like:
 * </p>
 * 
 * <xmp> <property-element> <array-element value-attribute="configuration-element-value-1"/>
 * <array-element value-attribute="configuration-element-value-2"/> ...
 * <array-element value-attribute="configuration-element-value-n"/> </property-element> </xmp>
 * 
 * <p>
 * Produces a configuration value that is a list consisting of the elements
 * "configuration-element-value-1",...
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ArrayValueBinding<E> extends AbstractListBinding<Object, E> {

	private Class<E> _contentType;

	/**
	 * Creates a {@link ArrayValueBinding}.
	 * 
	 * @param elementName
	 *        Name of the "list-element" XML element to use, see {@link ArrayValueBinding}.
	 * @param attributeName
	 *        Name of the "value-attribute" XML attribute to use on "list-element" elements.
	 * @param valueProvider
	 *        The {@link ConfigurationValueProvider} for serializing the list element values.
	 */
	public ArrayValueBinding(Class<E> contentType, String elementName, String attributeName,
			ConfigurationValueProvider<E> valueProvider) {
		super(elementName, attributeName, valueProvider);
		_contentType = contentType;
	}

	@Override
	protected Object valueFromList(List<E> list) {
		Object result = Array.newInstance(_contentType, list.size());
		int index = 0;
		for (E entry : list) {
			Array.set(result, index++, entry);
		}
		return result;
	}

	@Override
	protected List<E> copyValueToList(Object baseValue) {
		return baseValue == null ? new ArrayList<>() : asList(baseValue);
	}

	@Override
	protected List<E> valueAsList(Object value) {
		return value == null ? Collections.<E> emptyList() : asList(value);
	}

	private ArrayList<E> asList(Object value) {
		int length = Array.getLength(value);
		ArrayList<E> result = new ArrayList<>(length);
		for (int n = 0; n < length; n++) {
			@SuppressWarnings("unchecked")
			E element = (E) Array.get(value, n);
			result.add(element);
		}
		return result;
	}

	@Override
	public Object defaultValue() {
		return Array.newInstance(_contentType, 0);
	}

}

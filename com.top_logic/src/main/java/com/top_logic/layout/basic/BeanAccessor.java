/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.top_logic.layout.Accessor;


/**
 * {@link Accessor} implementation that relies on the {@link BeanInfo} of a
 * given Java class to provide access to an object's properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BeanAccessor implements Accessor {
	
    private static final Object[] NO_ARGUMENTS = new Object[] {};
	
	HashMap<String,PropertyDescriptor>  propertyForName 
	    = new HashMap<>  ();

	/**
	 * Create a new {@link BeanAccessor} that provides access to all JavaBean
	 * properties of objects of the given class.
	 */
	public BeanAccessor(Class<?> beanClass) throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int cnt = propertyDescriptors.length, n = 0; n < cnt; n++) {
			PropertyDescriptor propertyDescriptor = propertyDescriptors[n];
			propertyForName.put(propertyDescriptor.getName(), propertyDescriptor);
		}
	}
	
	@Override
	public Object getValue(Object object, String property) {
		try {
			Method readMethod = propertyForName.get(property).getReadMethod();
			return readMethod.invoke(object, NO_ARGUMENTS);
		} catch (IllegalArgumentException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
	}

	@Override
	public void setValue(Object object, String property, Object value) {
		Method writeMethod = propertyForName.get(property).getWriteMethod();
		try {
			writeMethod.invoke(object, new Object[] {value});
		} catch (IllegalArgumentException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
	}

}

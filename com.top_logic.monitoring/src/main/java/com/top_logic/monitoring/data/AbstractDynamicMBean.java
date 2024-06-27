/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.annotation.Constraint;

/**
 * Dynamic MBean with information to expose for management (constructors, attributes and operations)
 * using reflection.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class AbstractDynamicMBean extends AbstractConfiguredInstance<AbstractDynamicMBean.Config>
		implements DynamicMBean {

	private MBeanInfo _beanInfo = null;

	/** {@link ConfigurationItem} for the {@link AbstractDynamicMBean}. */
	public interface Config extends NamedPolymorphicConfiguration<AbstractDynamicMBean> {

		@Constraint(MBeanNameConstraint.class)
		@Override
		String getName();

		/** The description exposed for this MBean. */
		String getDescription();
	}

	/** {@link TypedConfiguration} constructor for {@link AbstractDynamicMBean}. */
	public AbstractDynamicMBean(InstantiationContext context, Config config) {
		super(context, config);

		buildDynamicMBeanInfo(config);
	}

	/**
	 * Creates an array of constructors to expose.
	 */
	protected MBeanConstructorInfo[] createConstructorInfo() {
		return null;
	}

	/**
	 * Creates an array of attributes to expose.
	 */
	protected MBeanAttributeInfo[] createAttributeInfo() {
		return null;
	}

	/**
	 * Creates an array of operations to expose.
	 */
	protected MBeanOperationInfo[] createOperationInfo() {
		return null;
	}

	/**
	 * Create information about the constructors, attributes and operations which should be listed
	 * for this dynamic MBean.
	 */
	public void buildDynamicMBeanInfo(Config config) {
		MBeanInfo beanInfo = new MBeanInfo(getClass().getName(),
			getDescription(config),
			createAttributeInfo(),
			createConstructorInfo(),
			createOperationInfo(),
			new MBeanNotificationInfo[0]);

		_beanInfo = beanInfo;
	}

	/** Returns the description of this dynamic MBean. */
	private String getDescription(Config config) {
		return config.getDescription();
	}

	@Override
	public Object getAttribute(String attribute) {
		if (attribute != null) {
			return invokeGetter(attribute);
		}

		return null;
	}

	Object invokeGetter(String attribute) {
		MBeanAttributeInfo attributeInfo = findAttributeInfo(attribute);

		if(attributeInfo == null) {
			Logger.error("There is no attribute '" + attribute + "' registered for this dynamic MBean.", getClass());
			return null;
		}
		
		boolean isReadable = attributeInfo.isReadable();
		if (!isReadable) {
			Logger.error("The attribute '" + attribute + "' is not readable.", getClass());
			return null;
		}

		String prefix = attributeInfo.isIs() ? "is" : "get";
		String methodName = StringServices.concatenate(prefix, attribute);
		Method method = getDeclaredMethod(methodName);
			
		return ReflectionUtil.invokeMethod(this, method);
	}

	private MBeanAttributeInfo findAttributeInfo(String attribute) {
		for (MBeanAttributeInfo attributeInfo : _beanInfo.getAttributes()) {
			if (attribute.equals(attributeInfo.getName())) {
				return attributeInfo;
			}
		}

		return null;
	}

	private Method getDeclaredMethod(String methodName, Class<?>... parameterTypes) {
		try {
			return getClass().getDeclaredMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException | SecurityException ex) {
			Logger.error("Cannot find method '" + methodName + "' in '" + getClass() + "'.", ex, getClass());
			return null;
		}
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		AttributeList resultList = new AttributeList();

		if (attributes == null || attributes.length == 0) {
			return resultList;
		}

		for (int i = 0; i < attributes.length; i++) {
			Object value = getAttribute(attributes[i]);
			resultList.add(new Attribute(attributes[i], value));
		}

		return resultList;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList resultList = new AttributeList();

		if (attributes == null || attributes.isEmpty()) {
			return resultList;
		}

		for (Object attribute : attributes) {
			Attribute attr = (Attribute) attribute;
			setAttribute(attr);
			String name = attr.getName();
			Object value = getAttribute(name);
			resultList.add(new Attribute(name, value));
		}

		return resultList;
	}

	@Override
	public void setAttribute(Attribute attribute) {
		if (attribute == null) {
			Logger.error("The attribute must not be null.", getClass());
			return;
		}

		String name = attribute.getName();
		Object value = attribute.getValue();
		MBeanAttributeInfo attributeInfo = findAttributeInfo(name);

		if (attributeInfo == null) {
			Logger.error("There is no attribute '" + attribute + "' registered for this dynamic MBean.", getClass());
			return;
		}

		try {
			Class<?> type = Class.forName(attributeInfo.getType());
			String methodName = "set" + name;
			Method method = getDeclaredMethod(methodName, type);
			ReflectionUtil.invokeMethod(this, method, value);
		} catch (ClassNotFoundException ex) {
			Logger.error("Cannot find class named '" + attributeInfo.getType() + "'.", ex, AbstractDynamicMBean.class);
		}
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) {
		if (actionName == null) {
			return null;
		}

		Class<?>[] classes = toClasses(signature);
		Method declaredMethod = ReflectionUtil.getMethod(getClass(), actionName, classes);

		return ReflectionUtil.invokeMethod(this, declaredMethod, params);
	}

	private Class<?>[] toClasses(String[] signature) {
		Class<?>[] classes = new Class<?>[signature.length];
		for (int i = 0; i < signature.length; i++) {
			try {
				classes[i] = Class.forName(signature[i]);
			} catch (ClassNotFoundException ex) {
				Logger.error("Cannot find class named '" + signature[i] + "'.", ex, AbstractDynamicMBean.class);
			}
		}
		return classes;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return _beanInfo;
	}

}

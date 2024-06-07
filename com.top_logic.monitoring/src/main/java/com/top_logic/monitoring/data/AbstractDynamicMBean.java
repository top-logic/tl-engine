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
 * using reflection. The method {@link #buildDynamicMBeanInfo(Config)} must be called in the
 * constructors of its subclasses to respect the configured information.
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
	protected abstract MBeanAttributeInfo[] createAttributeInfo();

	/**
	 * Creates an array of operations to expose.
	 */
	protected abstract MBeanOperationInfo[] createOperationInfo();

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
			Logger.error(
				"There is no getter registered for this dynamic MBean for the attribute '" + attribute + "'.",
				AbstractDynamicMBean.class);
			return null;
		}
		
		boolean isReadable = attributeInfo.isReadable();
		String prefix = attributeInfo.isIs() ? "is" : "get";

		if (isReadable) {
			String methodName = StringServices.concatenate(prefix, attribute);
			Method method = ReflectionUtil.getMethod(getClass(), methodName);
			return ReflectionUtil.invokeMethod(this, method);
		}

		Logger.error(
			"The attribute '" + attribute + "' is not readable.",
			AbstractDynamicMBean.class);

		return null;
	}

	private MBeanAttributeInfo findAttributeInfo(String attribute) {
		for (MBeanAttributeInfo attributeInfo : _beanInfo.getAttributes()) {
			if (attribute.equals(attributeInfo.getName())) {
				return attributeInfo;
			}
		}

		return null;
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
		if (attribute != null) {
			String name = attribute.getName();
			Object value = attribute.getValue();
			MBeanAttributeInfo attributeInfo = findAttributeInfo(name);
			try {
				Class<?> type = Class.forName(attributeInfo.getType());
				String methodName = "set" + name;
				Method method = ReflectionUtil.getMethod(getClass(), methodName, type);

				ReflectionUtil.invokeMethod(this, method, value);
			} catch (ClassNotFoundException ex) {
				Logger.error("Cannot find class named '" + attributeInfo.getType() + "'.", ex, AbstractDynamicMBean.class);
			}
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

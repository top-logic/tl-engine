/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.lang.reflect.Method;
import java.util.Iterator;

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
public abstract class DynamicMBeanElement extends AbstractConfiguredInstance<DynamicMBeanElement.Config>
		implements DynamicMBean {

	private MBeanInfo _dMBeanInfo = null;

	/** {@link ConfigurationItem} for the {@link DynamicMBeanElement}. */
	public interface Config extends NamedPolymorphicConfiguration<DynamicMBeanElement> {

		@Constraint(MBeanNameConstraint.class)
		@Override
		String getName();

		/** The description exposed for this MBean. */
		String getDescription();
	}

	/** {@link TypedConfiguration} constructor for {@link DynamicMBeanElement}. */
	public DynamicMBeanElement(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Creates an array of constructors to expose.
	 */
	protected abstract MBeanConstructorInfo[] createConstructorInfo();

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
		MBeanInfo dMBeanInfo = new MBeanInfo(this.getClass().getName(),
			getDescription(config),
			createAttributeInfo(),
			createConstructorInfo(),
			createOperationInfo(),
			new MBeanNotificationInfo[0]);

		_dMBeanInfo = dMBeanInfo;
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
			return null;
		}
		
		boolean isReadable = attributeInfo.isReadable();
		String prefix = attributeInfo.isIs() ? "is" : "get";

		if (isReadable) {
			String methodName = StringServices.concatenate(prefix, attribute);
			Method method = ReflectionUtil.getMethod(this.getClass(), methodName);
			return ReflectionUtil.invokeMethod(this, method);
		}

		return null;
	}

	private MBeanAttributeInfo findAttributeInfo(String attribute) {
		for (MBeanAttributeInfo dAttribute : _dMBeanInfo.getAttributes()) {
			if (attribute.equals(dAttribute.getName())) {
				return dAttribute;
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

		for (Iterator<?> i = attributes.iterator(); i.hasNext();) {
			Attribute attr = (Attribute) i.next();
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
				String methodName = StringServices.concatenate("set", name);
				Method method = ReflectionUtil.getMethod(this.getClass(), methodName, type);

				ReflectionUtil.invokeMethod(this, method, value);
			} catch (ClassNotFoundException ex) {
				Logger.error("Cannot find class named '" + attributeInfo.getType() + "'.", ex, DynamicMBeanElement.class);
			}
		}
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) {
		if (actionName == null) {
			return null;
		}

		Class<?>[] classes = toClasses(signature);
		Method declaredMethod = ReflectionUtil.getMethod(this.getClass(), actionName, classes);

		return ReflectionUtil.invokeMethod(this, declaredMethod, params);
	}

	private Class<?>[] toClasses(String[] signature) {
		Class<?>[] classes = new Class<?>[signature.length];
		for (int i = 0; i < signature.length; i++) {
			try {
				classes[i] = Class.forName(signature[i]);
			} catch (ClassNotFoundException ex) {
				Logger.error("Cannot find class named '" + signature[i] + "'.", ex, DynamicMBeanElement.class);
			}
		}
		return classes;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return _dMBeanInfo;
	}

}

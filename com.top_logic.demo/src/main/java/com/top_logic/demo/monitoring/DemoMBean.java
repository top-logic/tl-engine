/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.monitoring;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.monitoring.data.AbstractDynamicMBean;

/**
 * Demonstration of a dynamic MBean.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Label("Dynamic MBean demo")
public class DemoMBean extends AbstractDynamicMBean {

	String _prename = "Alice";

	int _counter = 0;

	/** {@link ConfigurationItem} for the {@link DemoMBean}. */
	public interface Config extends AbstractDynamicMBean.Config {

		@Override
		@StringDefault("com.top_logic.monitoring.data:name=DemoMBean")
		public String getName();
	}

	/** {@link TypedConfiguration} constructor for {@link DemoMBean}. */
	public DemoMBean(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[2];

		attributes[0] = new MBeanAttributeInfo(
			"Prename", // name
			"java.lang.String", // type
			"The value of the prename attribute.", // description
			true, // readable
			true, // writable
			false); // isIs

		attributes[1] = new MBeanAttributeInfo(
			"NameSet", // name
			"java.lang.Boolean", // type
			"Checks whether the prename attribute is set.", // description
			true, // readable
			false, // writable
			true); // isIs

		return attributes;
	}

	@Override
	protected MBeanOperationInfo[] createOperationInfo() {
		MBeanOperationInfo[] operations = new MBeanOperationInfo[1];

		MBeanParameterInfo[] emptyParams = null;
		operations[0] = new MBeanOperationInfo(
			"count", // name
			"Counts the klicks.", // description
			emptyParams, // parameter types
			"int", // return type
			MBeanOperationInfo.ACTION_INFO); // impact

		return operations;
	}

	/** Returns the prename. */
	@CalledByReflection
	public String getPrename() {
		return _prename;
	}

	/** Sets the prename. */
	@CalledByReflection
	public void setPrename(String name) {
		_prename = name;
	}

	/** Checks whether the prename is set. */
	@CalledByReflection
	public boolean isNameSet() {
		return !StringServices.isEmpty(_prename);
	}

	/** Adds 1 to the counter. */
	@CalledByReflection
	public int count() {
		return ++_counter;
	}

}

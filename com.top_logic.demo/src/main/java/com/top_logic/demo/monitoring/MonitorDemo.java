/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.monitoring;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.monitoring.data.DynamicMBeanElement;

/**
 * Demonstration of a dynamic MBean.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Label("Dynamic MBean demo")
public class MonitorDemo extends DynamicMBeanElement {

	String _prename = "Alice";

	int _counter = 0;

	/** {@link ConfigurationItem} for the {@link MonitorDemo}. */
	public interface Config extends DynamicMBeanElement.Config {

		@Override
		@StringDefault("com.top_logic.monitoring.data:name=MonitorDemo")
		public String getName();
	}

	/** {@link TypedConfiguration} constructor for {@link MonitorDemo}. */
	public MonitorDemo(InstantiationContext context, Config config) {
		super(context, config);

		buildDynamicMBeanInfo(config);
	}

	@Override
	protected MBeanConstructorInfo[] createConstructorInfo() {
		return null;
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		MBeanAttributeInfo[] dAttributeInfo = new MBeanAttributeInfo[2];

		dAttributeInfo[0] = new MBeanAttributeInfo(
			"Prename", // name
			"java.lang.String", // type
			"The number of actual logged in users with respect to the last system start.", // description
			true, // readable
			true, // writable
			false); // isIs

		dAttributeInfo[1] = new MBeanAttributeInfo(
			"NameSet", // name
			"java.lang.Boolean", // type
			"Checks whether the prename attribute is set.", // description
			true, // readable
			false, // writable
			true); // isIs

		return dAttributeInfo;
	}

	@Override
	protected MBeanOperationInfo[] createOperationInfo() {
		MBeanOperationInfo[] dOperations = new MBeanOperationInfo[1];

		MBeanParameterInfo[] emptyParams = null;
		dOperations[0] = new MBeanOperationInfo(
			"count", // name
			"Counts the klicks.", // description
			emptyParams, // parameter types
			"java.lang.Integer", // return type
			MBeanOperationInfo.ACTION); // impact

		return dOperations;
	}

	/** Returns the prename. */
	public String getPrename() {
		return _prename;
	}

	/** Sets the prename. */
	public void setPrename(String name) {
		_prename = name;
	}

	/** Checks whether the prename is set. */
	public boolean isNameSet() {
		return !StringServices.isEmpty(_prename);
	}

	/** Adds 1 to the counter. */
	public void count() {
		_counter++;
	}

}

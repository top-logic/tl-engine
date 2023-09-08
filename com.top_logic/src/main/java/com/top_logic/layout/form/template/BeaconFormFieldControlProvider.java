/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.BeaconControl;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.SelectField;

/**
 * Creates {@link BeaconControl}s.
 *
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class BeaconFormFieldControlProvider extends DefaultFormFieldControlProvider {
	
	public interface Config extends PolymorphicConfiguration<ControlProvider> {
		@Name("type")
		@StringDefault(BeaconFormFieldControlProvider.VAL_TYPE_BEACON)
		String getType();
	}

	/** Default beacon type. */
	public static final String VAL_TYPE_BEACON = "bea3";

	private String type;

	/**
	 * Creates a new {@link BeaconFormFieldControlProvider} with type "bea3".
	 */
	public BeaconFormFieldControlProvider() {
		this(VAL_TYPE_BEACON);
	}

	/**
	 * Creates a new {@link BeaconFormFieldControlProvider} with the given type.
	 * 
	 * @param type
	 *            The beacon type (e.g. bea3).
	 */
	public BeaconFormFieldControlProvider(String type) {
		super();
		this.type = type;
	}

	/**
	 * Creates a new {@link BeaconFormFieldControlProvider}.
	 */
	public BeaconFormFieldControlProvider(InstantiationContext context, Config config) throws ConfigurationException {
		this.type = config.getType();
	}


	@Override
	public Control visitComplexField(ComplexField member, Void arg) {
		return new BeaconControl(member, type);
	}

	@Override
	public Control visitSelectField(SelectField member, Void arg) {
		return new BeaconControl(member, type);
	}

	/**
	 * Returns the type.
	 */
	public String getType() {
		return this.type;
	}

}

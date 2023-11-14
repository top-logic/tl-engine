/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.InfoControl;

/**
 * {@link ControlProvider} that adds an {@link InfoControl} to the actual control for the model
 * created by an inner {@link ControlProvider} if the model is an {@link FormMember}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class InfoAddingControlProvider implements ControlProvider {

	public interface Config extends PolymorphicConfiguration<ControlProvider> {
		@Name(CONTROL_PROVIDER_PROPERTY)
		@InstanceFormat
		ControlProvider getInnerControlProvider();
	}

	private static final String CONTROL_PROVIDER_PROPERTY = "innerControlProvider";

	/**
	 * {@link InfoAddingControlProvider} delegating creation of actual control to
	 * {@link DefaultFormFieldControlProvider}.
	 */
	public static InfoAddingControlProvider INSTANCE = new InfoAddingControlProvider(
		DefaultFormFieldControlProvider.INSTANCE);

	private final ControlProvider _innerControlProvider;

	/**
	 * Creates a new {@link InfoAddingControlProvider}.
	 * 
	 * @param innerControlProvider
	 *        Control provider to dispatch creation of actual control to.
	 */
	public InfoAddingControlProvider(ControlProvider innerControlProvider) {
		_innerControlProvider = innerControlProvider;
    }

	/**
	 * Creates a new {@link InfoAddingControlProvider} from the given configuration.
	 */
	public InfoAddingControlProvider(InstantiationContext context, Config config) throws ConfigurationException {
		this(getControlProvider(context, config));
	}

	private static ControlProvider getControlProvider(InstantiationContext context, Config config) throws ConfigurationException {
		return (config.getInnerControlProvider() == null) ? DefaultFormFieldControlProvider.INSTANCE : config.getInnerControlProvider();
	}

    @Override
	public Control createControl(Object model, String style) {
		Control innerControl = _innerControlProvider.createControl(model, style);
		if (innerControl == null) {
            return null;
		} else if (model instanceof FormMember) {
			BlockControl combinedControl = new BlockControl();
			combinedControl.addChild(innerControl);
			combinedControl.addChild(InfoControl.createInfoControl((FormMember) model, null));
			return combinedControl;
        } else {
			return innerControl;
        }
    }

	@Override
	public HTMLFragment createFragment(Object model, String style) {
		HTMLFragment innerControl = _innerControlProvider.createFragment(model, style);
		if (innerControl == null) {
			return null;
		} else if (model instanceof FormMember) {
			BlockControl combinedControl = new BlockControl();
			combinedControl.addChild(innerControl);
			combinedControl.addChild(InfoControl.createInfoControl((FormMember) model, null));
			return combinedControl;
		} else {
			return innerControl;
		}
	}

}


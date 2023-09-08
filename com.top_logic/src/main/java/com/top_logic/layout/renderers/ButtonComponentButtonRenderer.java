/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.AbstractButtonRenderer;

/**
 * The class {@link ButtonComponentButtonRenderer} is the default button renderer which is used
 * to render the buttons of this component, if no renderer is added as property to the according
 * {@link CommandModel}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class ButtonComponentButtonRenderer extends AbstractButtonRenderer<ButtonComponentButtonRenderer.Config> {

	public static final ButtonComponentButtonRenderer INSTANCE =
		TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(ButtonComponentButtonRenderer.Config.class));

	/**
	 * CSS class for an active command button.
	 */
	public static final String CSS_CLASS_ENABLED_BUTTON = "cmdButton";

	/**
	 * CSS class for a disabled command button.
	 */
	public static final String CSS_CLASS_DISABLED_BUTTON = "cmdButtonDisabled";

	/**
	 * CSS class for a an image within a command button.
	 */
	public static final String CSS_CLASS_IMAGE = "cmdImg";

	/**
	 * CSS class for the label of a command button.
	 */
	public static final String CSS_CLASS_LABEL = "cmdLabel";

	/**
	 * Typed configuration interface definition for {@link ButtonComponentButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<ButtonComponentButtonRenderer> {
		// configuration interface definition
	}

	/**
	 * Create a {@link ButtonComponentButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ButtonComponentButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

    @Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return button.isDisabled() ? CSS_CLASS_DISABLED_BUTTON : CSS_CLASS_ENABLED_BUTTON;
	}

	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		Icons.BUTTON_COMPONENT_BUTTON_TEMPLATE.get().write(context, out, button);
    }
}


/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * {@link ToggleCommandHandler} that toggles the "show all" state of
 * {@link MetaAttributeTableListModelBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ShowAllAttributesCommand extends ToggleCommandHandler {

	/**
	 * Creates a {@link ShowAllAttributesCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ShowAllAttributesCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(DisplayContext context, LayoutComponent component) {
		return MetaAttributeTableListModelBuilder.showAll(component);
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		MetaAttributeTableListModelBuilder.setShowAll(component, newValue);
		component.invalidate();
	}

}

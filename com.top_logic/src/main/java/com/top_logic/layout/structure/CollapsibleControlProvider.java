/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link CollapsibleControlProvider} can be used to create a {@link CollapsibleControl}
 * for some {@link Layout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CollapsibleControlProvider extends ConfiguredLayoutControlProvider<CollapsibleControlProvider.Config> {
	
	/**
	 * Configuration options of {@link CollapsibleControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<LayoutControlProvider>, ToolbarOptions {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link CollapsibleControlProvider}. Constructor is needed to
	 * use class in configuration.
	 */
	public CollapsibleControlProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		CollapsibleControl control =
			new CollapsibleControl(component.getTitleKey(), component, true, getConfig());
		LayoutControl contentControl = strategy.createDefaultLayout(component);
		control.setChildControl(contentControl);
		return control;
	}

}


/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.server.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.server.control.BlocksControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutComponent} displaying a {@link BlockModel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlocksComponent extends LayoutComponent implements ControlRepresentable {

	/**
	 * Configuration options for {@link BlocksComponent}.
	 */
	@TagName("blocks")
	public interface Config extends LayoutComponent.Config {
		@Override
		@ClassDefault(BlocksComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();
	}

	/**
	 * Creates a {@link BlocksComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BlocksComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Control getRenderingControl() {
		return new BlocksControl();
	}

}

/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} that
 * {@link com.top_logic.layout.structure.LayoutControlProvider.Strategy#decorate(LayoutComponent, Layouting)
 * decorates} its result.
 * 
 * @apiNote The concrete {@link LayoutControl} implementation must be created in
 *          {@link #mkLayout(Strategy, LayoutComponent)}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DecoratingLayoutControlProvider<C extends PolymorphicConfiguration<?>>
		extends ConfiguredLayoutControlProvider<C> implements Layouting {

	/**
	 * Creates a new {@link DecoratingLayoutControlProvider}.
	 * 
	 * @param context
	 *        Context to instantiate sub configurations.
	 * @param config
	 *        Configuration of the {@link DecoratingLayoutControlProvider}.
	 */
	public DecoratingLayoutControlProvider(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Control createLayoutControl(Strategy strategy, LayoutComponent component) {
		return strategy.decorate(component, this);
	}

}

/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.structure;

import javax.swing.text.View;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider}, which creates a {@link LayoutControl} from a configured
 * {@link ViewConfiguration}.
 * 
 * @see ConditionalViewLayout When the visibility of the displayed {@link View} changes dynamically.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class LayoutViewProvider extends DecoratingLayoutControlProvider<LayoutViewProvider.Config> {

	/**
	 * Configuration interface of {@link LayoutViewProvider}
	 */
	public interface Config extends PolymorphicConfiguration<LayoutControlProvider> {

		/**
		 * CSS class for the wrapping layout control.
		 */
		@Nullable
		String getCssClass();

		/**
		 * {@link ViewConfiguration} of the {@link LayoutControl}, which will be created by
		 *         this {@link LayoutViewProvider}.
		 */
		ViewConfiguration.Config<?> getView();
	}

	/**
	 * Constructor of {@link LayoutViewProvider}.
	 */
	public LayoutViewProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		ViewConfiguration viewConfiguration =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(getConfig().getView());
		LayoutControlAdapter adapterControl = new LayoutControlAdapter(viewConfiguration.createView(component));
		adapterControl.setCssClass(getConfig().getCssClass());
		return adapterControl;
	}
}

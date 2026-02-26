/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import java.io.IOException;
import java.util.function.BooleanSupplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ViewConfiguration} creating a view that dispatches to another view when a certain
 * condition is fulfilled.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConditionalViewConfiguration extends AbstractConfiguredInstance<ConditionalViewConfiguration.Config>
		implements ViewConfiguration {

	/**
	 * Typed configuration interface definition for {@link ConditionalViewConfiguration}.
	 */
	public interface Config extends PolymorphicConfiguration<ConditionalViewConfiguration> {

		/**
		 * The view to display when {@link #getCondition()} is fulfilled.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ViewConfiguration> getView();

		/**
		 * Whether {@link #getView()} must be displayed. When the condition returned
		 * <code>false</code>, then nothing is displayed.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends BooleanSupplier> getCondition();

		/**
		 * Whether {@link #getCondition()} must be inverted, i.e. the {@link #getView()} is
		 * displayed if and only if the {@link #getCondition()} returns <code>false</code>.
		 */
		boolean isInvert();

	}

	private final ViewConfiguration _view;

	private final BooleanSupplier _condition;

	/**
	 * Create a {@link ConditionalViewConfiguration}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConditionalViewConfiguration(InstantiationContext context, Config config) {
		super(context, config);
		_view = context.getInstance(config.getView());
		_condition = context.getInstance(config.getCondition());
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		HTMLFragment view = _view.createView(component);
		return new HTMLFragment() {
			
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				boolean condition = _condition.getAsBoolean();
				if (getConfig().isInvert()) {
					condition = !condition;
				}
				if (condition) {
					view.write(context, out);
				}
			}
		};
	}


}


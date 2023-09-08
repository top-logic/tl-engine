/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.title;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TitleProvider} displaying a constant title value.
 * 
 * @see Config#getTitle()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantTitle extends ConfiguredTitleProvider<ConstantTitle.Config<?>> {

	/**
	 * Configuration options for {@link ConstantTitle}.
	 */
	public interface Config<I extends ConstantTitle> extends PolymorphicConfiguration<ConstantTitle> {

		/**
		 * The internationalized title do display.
		 */
		@Label("title")
		@Mandatory
		ResKey getTitle();

	}

	/**
	 * Creates a {@link ConstantTitle} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstantTitle(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createTitle(LayoutComponent component) {
		return Fragments.message(getSimpleTitle(component.getConfig()));
	}

	@Override
	public ResKey getSimpleTitle(LayoutComponent.Config component) {
		return getConfig().getTitle();
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.IButtonRenderer;

/**
 * {@link IButtonRenderer} rendering with a configured template.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredButtonRenderer<C extends ConfiguredButtonRenderer.Config<?>>
		extends ConfiguredRenderer<AbstractButtonControl<?>, C>
		implements IButtonRenderer {

	/**
	 * Configuration options for {@link ConfiguredButtonRenderer}.
	 */
	public interface Config<I extends ConfiguredButtonRenderer<?>> extends ConfiguredRenderer.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link ConfiguredButtonRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredButtonRenderer(InstantiationContext context, C config) {
		super(context, config);
	}

}

/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.Renderer;

/**
 * Configurable default {@link AbstractButtonLinkRenderer} adapter that delegates to a
 * {@link Renderer} for displaying a {@link ButtonControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultButtonLinkRenderer<C extends DefaultButtonLinkRenderer.Config<?>>
		extends AbstractButtonLinkRenderer<C> {

	/**
	 * Configuration options for {@link DefaultButtonLinkRenderer}.
	 */
	public static interface Config<I extends DefaultButtonLinkRenderer<?>>
			extends AbstractButtonLinkRenderer.Config<I> {

		/**
		 * The {@link Renderer} that actually produces the visual representation of the
		 * {@link AbstractButtonControl}.
		 */
		@Mandatory
		@InstanceFormat
		Renderer getRenderer();

	}

	/**
	 * Creates a {@link DefaultButtonLinkRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultButtonLinkRenderer(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Renderer commandRenderer() {
		return getConfig().getRenderer();
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return null;
	}

}

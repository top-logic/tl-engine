/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Base class of a {@link SecondFactor} that takes its label and icon from its
 * {@link SecondFactor.Config configuration}.
 */
public abstract class AbstractSecondFactor<C extends SecondFactor.Config<?>> extends AbstractConfiguredInstance<C>
		implements SecondFactor {

	/**
	 * Creates a {@link AbstractSecondFactor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractSecondFactor(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public ResKey getLabel() {
		return getConfig().getLabel();
	}

	@Override
	public ThemeImage getIcon() {
		return getConfig().getImage();
	}

}

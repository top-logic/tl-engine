/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link com.top_logic.basic.config.ConfigurationValueProvider} that parses a {@link ViewRef} from
 * the path of a view file relative to {@link ViewLoader#VIEW_BASE_PATH}.
 *
 * <p>
 * Usage on a configuration property:
 * </p>
 *
 * <pre>
 * &#64;Format(ViewRefFormat.class)
 * ViewRef getView();
 * </pre>
 *
 * <p>
 * A default is given with {@link com.top_logic.basic.config.annotation.defaults.FormattedDefault},
 * e.g. {@code @FormattedDefault("app.view.xml")}.
 * </p>
 */
public class ViewRefFormat extends AbstractConfigurationValueProvider<ViewRef> {

	/** Singleton instance. */
	public static final ViewRefFormat INSTANCE = new ViewRefFormat();

	/**
	 * Creates a {@link ViewRefFormat}.
	 */
	public ViewRefFormat() {
		super(ViewRef.class);
	}

	@Override
	protected ViewRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return new ViewRef(propertyValue.toString().trim());
	}

	@Override
	protected String getSpecificationNonNull(ViewRef configValue) {
		return configValue.getPath();
	}
}

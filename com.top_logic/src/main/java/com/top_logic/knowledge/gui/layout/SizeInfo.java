/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;

/**
 * Configuration of the size of a component display.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SizeInfo extends ConfigurationItem {

	/** @see #getWidth() */
	String WIDTH = "width";

	/** @see #getHeight() */
	String HEIGHT = "height";

	/** @see #getDefaultI18N() */
	String DEFAULT_I18N = "defaultI18n";

	/**
	 * The component width.
	 */
	@FormattedDefault("800px")
	@Name(WIDTH)
	DisplayDimension getWidth();

	/**
	 * @see #getWidth()
	 */
	void setWidth(DisplayDimension value);

	/**
	 * The component height.
	 */
	@FormattedDefault("500px")
	@Name(HEIGHT)
	DisplayDimension getHeight();

	/**
	 * @see #getHeight()
	 */
	void setHeight(DisplayDimension value);

	/**
	 * Label of the open handler, if this used as dialog or window.
	 */
	@Name(DEFAULT_I18N)
	@InstanceFormat
	ResKey getDefaultI18N();

	/**
	 * @see #getDefaultI18N()
	 */
	void setDefaultI18N(ResKey value);
}
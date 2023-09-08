/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;

/**
 * Configuration of constant {@link ButtonUIModel} settings.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ButtonUIConfig extends ConfigurationItem {

	/**
	 * Property {@link #getLabelKey()}.
	 */
	String LABEL_KEY = "label-key";

	/**
	 * Property {@link #getTooltipKey()}.
	 */
	String TOOLTIP_KEY = "tooltip-key";

	/**
	 * Property {@link #getTooltipCaptionKey()}.
	 */
	String TOOLTIP_CAPTION_KEY = "tooltip-caption-key";

	/**
	 * Property {@link #getAccessKey()}.
	 */
	String ACCESS_KEY = "access-key";

	/**
	 * Property {@link #getImage()}.
	 */
	String IMAGE = "image";

	/**
	 * Property {@link #getNotExecutableImage()}.
	 */
	String NOT_EXECUTABLE_IMAGE = "not-executable-image";

	/**
	 * Property {@link #getAltKey()}.
	 */
	String ALT_KEY = "alt-key";

	/**
	 * Property {@link #getCssClasses()}.
	 */
	String CSS_CLASSES = "css-classes";

	/**
	 * @see ButtonUIModel#getLabel()
	 */
	@Name(LABEL_KEY)
	ResKey getLabelKey();

	/**
	 * @see ButtonUIModel#getTooltip()
	 */
	@Name(TOOLTIP_KEY)
	ResKey getTooltipKey();

	/**
	 * @see ButtonUIModel#getTooltipCaption()
	 */
	@Name(TOOLTIP_CAPTION_KEY)
	ResKey getTooltipCaptionKey();

	/**
	 * @see ButtonUIModel#getAccessKey()
	 */
	@Name(ACCESS_KEY)
	char getAccessKey();

	/**
	 * @see ButtonUIModel#getImage()
	 */
	@Name(IMAGE)
	ThemeImage getImage();

	/**
	 * @see ButtonUIModel#getImage()
	 */
	@Name(NOT_EXECUTABLE_IMAGE)
	ThemeImage getNotExecutableImage();

	/**
	 * @see ButtonUIModel#getAltText()
	 */
	@Name(ALT_KEY)
	ResKey getAltKey();

	/**
	 * @see ButtonUIModel#getCssClasses()
	 */
	@Name(CSS_CLASSES)
	String getCssClasses();

}

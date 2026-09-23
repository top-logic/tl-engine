/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactIconControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * A theme icon - a font icon such as {@code css:bi bi-chevron-right} or a picture resource - shown
 * decoratively, or named by its tooltip.
 */
@InApp
public class IconElement implements UIElement {

	/**
	 * Configuration for {@link IconElement}.
	 */
	@TagName("theme-icon")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		@Override
		@ClassDefault(IconElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The icon to show.
		 */
		@Name(IMAGE)
		@Mandatory
		ThemeImage getImage();

		/**
		 * The text shown on hover.
		 *
		 * <p>
		 * The text also names the icon for a reader who cannot see it. Left unset, the icon
		 * decorates what it sits beside and is not announced.
		 * </p>
		 */
		@Name(TOOLTIP)
		@Nullable
		ResKey getTooltip();

		/**
		 * Optional additional CSS class appended to the default {@code tlIcon} class.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCssClass();
	}

	private final ThemeImage _image;

	private final ResKey _tooltip;

	private final String _cssClass;

	/**
	 * Creates a new {@link IconElement} from configuration.
	 */
	@CalledByReflection
	public IconElement(InstantiationContext context, Config config) {
		_image = config.getImage();
		_tooltip = config.getTooltip();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ReactIconControl control = new ReactIconControl(context, _image);
		if (_tooltip != null) {
			control.setTooltip(Resources.getInstance().getString(_tooltip));
		}
		if (_cssClass != null) {
			control.setCssClasses(_cssClass);
		}
		return control;
	}
}

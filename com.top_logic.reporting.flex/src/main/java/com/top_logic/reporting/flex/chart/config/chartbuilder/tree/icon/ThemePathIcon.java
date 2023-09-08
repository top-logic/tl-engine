/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Implementation of {@link AbstractIconProvider} that works with {@link ThemeImage}s.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ThemePathIcon extends AbstractIconProvider {

	private Image _image;

	/**
	 * Config-interface for {@link ThemePathIcon}.
	 */
	@TagName("image")
	public interface Config extends AbstractIconProvider.Config {

		@Override
		@ClassDefault(ThemePathIcon.class)
		public Class<? extends AbstractIconProvider> getImplementationClass();

		/**
		 * the path of the {@link ThemeImage}
		 */
		public String getPath();

	}

	/**
	 * Config-Constructor for {@link ThemePathIcon}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ThemePathIcon(InstantiationContext context, Config config) {
		Theme theme = ThemeFactory.getTheme();
		_image = theme.getImageByPath(config.getPath());
	}

	@Override
	public Image getImage() {
		return _image;
	}

}
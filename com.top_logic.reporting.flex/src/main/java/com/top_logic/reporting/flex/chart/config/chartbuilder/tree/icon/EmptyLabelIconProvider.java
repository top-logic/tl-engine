/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;
import java.awt.image.BufferedImage;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * {@link LabelIconProvider} that does not show icons.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class EmptyLabelIconProvider implements LabelIconProvider {

	/**
	 * Config-interface for {@link EmptyLabelIconProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<DefaultLabelIconProvider> {

		/**
		 * Even if this class does not provide any icons, the provider must return an array
		 * because the icons are identified using the index. Therefore the array must be
		 * initialized with the correct size.
		 * 
		 * @return the number of possible icons
		 */
		public int getSize();
		
	}
	
	private Image[] _icons;

	/**
	 * Config-constructor for {@link EmptyLabelIconProvider}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public EmptyLabelIconProvider(InstantiationContext context, Config config) {
		_icons = new Image[config.getSize()];
		for (int i = 0; i < config.getSize(); i++) {
			_icons[i] = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); 
		}
	}
	
	@Override
	public Image[] getIcons() {
		return _icons;
	}
	
}
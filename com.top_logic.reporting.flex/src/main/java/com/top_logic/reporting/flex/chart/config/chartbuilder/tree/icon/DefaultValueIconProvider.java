/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.reporting.flex.chart.config.chartbuilder.bar.BarChartBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;

/**
 * Configurable default-implementation of {@link ValueIconProvider}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DefaultValueIconProvider implements ValueIconProvider {

	/**
	 * Config-interface for {@link DefaultValueIconProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<DefaultValueIconProvider> {

		/**
		 * the configured icons / color-shapes
		 */
		@Key(AbstractIconProvider.Config.KEY)
		@EntryTag("icon")
		public List<AbstractIconProvider.Config> getIcons();

		/**
		 * the key-provider used to retrieve a lookup-key for the image from the
		 *         {@link ChartNode}
		 */
		@InstanceFormat
		public KeyProvider getKeyProvider();
	}

	private Map<Integer, Image> _icons;

	private KeyProvider _keyProvider;

	/**
	 * Config-Constructor for {@link BarChartBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DefaultValueIconProvider(InstantiationContext context, Config config) {
		_keyProvider = config.getKeyProvider();
		List<AbstractIconProvider.Config> icons = config.getIcons();
		_icons = new HashMap<>(icons.size());
		for (AbstractIconProvider.Config conf : icons) {
			_icons.put(conf.getKey(), context.getInstance(conf).getImage());
		}
	}

	@Override
	public Map<Integer, Image> getIcons() {
		return _icons;
	}

	@Override
	public Image getIcon(ChartNode node) {
		if (node == null) {
			return null;
		}
		return _icons.get(_keyProvider.getKey(node));
	}

}
/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlRenderer;
import com.top_logic.layout.tabbar.TabBarControl;

/**
 * Renderer of tab bars.
 * 
 * @author <a href="mailto:mri@top-logic.com">mri</a>
 */
public final class DefaultTabBarRenderer extends AbstractControlRenderer<TabBarControl>
		implements TabBarRenderer, ConfiguredInstance<DefaultTabBarRenderer.Config> {

	/** Static instance of {@link DefaultTabBarRenderer}, using default configuration */
	public static final TabBarRenderer INSTANCE =
		TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(Config.class));

	/** Configuration interface of {@link DefaultTabBarRenderer} */
	public interface Config extends PolymorphicConfiguration<DefaultTabBarRenderer> {

		/** Configuration name for {@link #getTabBarClass()}. */
		String TAB_BAR_CLASS = "tab-bar-class";

		/** Configuration name for {@link #getTabBarHeight()}. */
		String XML_ATTRIBUTE_BAR_SIZE = "barSize";

		/** CSS class of the outermost HTML container */
		@Name(TAB_BAR_CLASS)
		String getTabBarClass();

		/** overall tab bar height in pixel */
		@Name(XML_ATTRIBUTE_BAR_SIZE)
		int getTabBarHeight();
	}

	private Config _config;

	/**
	 * Creates a {@link DefaultTabBarRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultTabBarRenderer(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, TabBarControl value) throws IOException {
		com.top_logic.layout.tabbar.Icons.TAB_BAR_TEMPLATE.get().write(context, out, value);
	}


	/**
	 * Gets the class of this configured tabbar.
	 * 
	 * @return the configured tabbar class if one exists, otherwise it returns the default one.
	 */
	public String getTabbarCSSClass() {
		Config config = getConfig();

		if (TypedConfigUtil.isValueSet(config, Config.TAB_BAR_CLASS)) {
			return config.getTabBarClass();
		} else {
			return Icons.TABBAR_CSS_CLASS.get();
		}
	}

	@Override
	public int getBarSize() {
		Config config = getConfig();

		if (TypedConfigUtil.isValueSet(config, Config.XML_ATTRIBUTE_BAR_SIZE)) {
			return config.getTabBarHeight();
		} else {
			return ThemeFactory.getTheme().getValue(Icons.TABBAR_HEIGHT);
		}
	}

	@Override
	public int getBottomSpacerSize() {
		return 0;
	}

	/** Gets the {@link #_config}. */
	@Override
	public Config getConfig() {
		return _config;
	}

}

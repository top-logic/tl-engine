/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.awt.Font;
import java.awt.Paint;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.reporting.flex.chart.config.color.HexEncodedPaint;

/**
 * Service initializing the current {@link ChartTheme} in the {@link ChartFactory} to a configured
 * value.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ChartThemeConfigurator extends ManagedClass {

	/**
	 * Configuration options for {@link ChartThemeConfigurator}.
	 */
	public interface Config extends ServiceConfiguration<ChartThemeConfigurator> {

		/**
		 * the name of the theme to be set as currentTheme in the {@link ChartFactory}
		 */
		@StringDefault("JFree")
		public String getDefaultTheme();

		/**
		 * the configured themes by name
		 */
		@Key(ChartThemeConfig.NAME_ATTRIBUTE)
		public Map<String, ConfiguredChartTheme> getThemes();

		/**
		 * Name of the theme to use as fallback for properties of configured themes that are not
		 * set. The fallback theme must exist. Built-in themes that are "JFree", "Legacy" and
		 * "Darkness".
		 */
		public String getFallback();

	}

	/**
	 * Config-interface for {@link ConfiguredChartTheme}.
	 */
	public interface ChartThemeConfig extends PolymorphicConfiguration<ConfiguredChartTheme>, NamedConfigMandatory {

		/**
		 * Name of the theme to use as fallback for properties of configured themes that are not
		 * set. The fallback theme must exist. Built-in themes that are "JFree", "Legacy" and
		 * "Darkness".
		 * 
		 * @see ChartThemeConfigurator.Config#getFallback()
		 */
		public String getFallback();

		/**
		 * The {@link XYBarPainter} to use.
		 * 
		 * @see StandardChartTheme#getXYBarPainter()
		 */
		@InstanceFormat
		public XYBarPainter getXYBarPainter();

		/**
		 * Returns the {@link BarPainter} to use.
		 * 
		 * @see StandardChartTheme#getBarPainter()
		 */
		@InstanceFormat
		public BarPainter getBarPainter();

		/**
		 * The plot background {@link Paint}.
		 * 
		 * @see StandardChartTheme#getPlotBackgroundPaint()
		 */
		@Format(HexEncodedPaint.class)
		public Paint getPlotBackgroundPaint();

		/**
		 * The chart background {@link Paint}.
		 * 
		 * @see StandardChartTheme#getChartBackgroundPaint()
		 */
		@Format(HexEncodedPaint.class)
		public Paint getChartBackgroundPaint();

		/**
		 * Whether shadows are painted.
		 * 
		 * @see StandardChartTheme#isShadowVisible()
		 */
		public Boolean isShadowVisible();

		/**
		 * The domain grid-line {@link Paint}.
		 * 
		 * @see StandardChartTheme#getDomainGridlinePaint()
		 */
		@Format(HexEncodedPaint.class)
		public Paint getDomainGridlinePaint();

		/**
		 * The range grid-line {@link Paint}.
		 * 
		 * @see StandardChartTheme#getRangeGridlinePaint()
		 */
		@Format(HexEncodedPaint.class)
		public Paint getRangeGridlinePaint();

		/**
		 * {@link AdditionalThemeConfigurator} to apply additional settings to the
		 * {@link ConfiguredChartTheme} by code that can not be configured directly, e.g.
		 * {@link Font}s.
		 */
		@InstanceFormat
		public AdditionalThemeConfigurator getConfigurator();

	}

	/**
	 * Interface for classes that should make additional settings to a StandardChartTheme that can
	 * not be configured.
	 */
	public interface AdditionalThemeConfigurator {

		/**
		 * @param targetTheme
		 *        the {@link ChartTheme} to be configured.
		 * @param fallback
		 *        the fallback to lookup default-values
		 */
		public void apply(ConfiguredChartTheme targetTheme, StandardChartTheme fallback);

	}

	/**
	 * Configurable version of {@link StandardChartTheme}.
	 */
	public static class ConfiguredChartTheme extends StandardChartTheme implements ConfiguredInstance<ChartThemeConfig> {

		private static final String IS = "is";

		private static final String GET = "get";

		private final ChartThemeConfig _config;

		/**
		 * Config-constructor for {@link ConfiguredChartTheme}
		 * 
		 * @param context
		 *        - default config-constructor
		 * @param config
		 *        - default config-constructor
		 */
		public ConfiguredChartTheme(InstantiationContext context, ChartThemeConfig config) {
			super(config.getName());
			_config = config;
		}

		@Override
		public ChartThemeConfig getConfig() {
			return _config;
		}

		/**
		 * Initializes the this ChartTheme from its config using values from fallback if the
		 * property is not configured.
		 */
		void init(StandardChartTheme fallback) {
			init("getPlotBackgroundPaint", fallback);
			init("getChartBackgroundPaint", fallback);
			init("getBarPainter", fallback);
			init("getXYBarPainter", fallback);
			init("isShadowVisible", fallback);
			init("getDomainGridlinePaint", fallback);
			init("getRangeGridlinePaint", fallback);
			AdditionalThemeConfigurator configurator = _config.getConfigurator();
			if (configurator != null) {
				configurator.apply(this, fallback);
			}
		}

		/**
		 * Same as {@link #init(String, String, StandardChartTheme)} using "get" as prefix for
		 * getter-methods.
		 */
		private void init(String name, StandardChartTheme fallback) {
			if (name.startsWith(GET)) {
				init(GET, name.substring(3), fallback);
				return;
			}
			else if (name.startsWith(IS)) {
				init(IS, name.substring(2), fallback);
				return;
			}
			else {
				throw new UnsupportedOperationException(
					"Unknown method-name-format. Only names starting with 'get' or 'is' are supported. Name: " + name);
			}
		}

		/**
		 * Generic version to initialize values based on the method-names.
		 */
		private void init(String prefix, String name, StandardChartTheme fallback) {
			try {
				Method method = _config.getClass().getMethod(prefix + name);
				Object res = method.invoke(_config);
				if (res == null) {
					method = fallback.getClass().getMethod(prefix + name);
					res = method.invoke(fallback);
				}
				if (res != null) {
					method = getMethod("set" + name);
					method.invoke(this, res);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		private Method getMethod(String string) {
			for (Method method : getClass().getMethods()) {
				if (method.getName().equals(string)) {
					return method;
				}
			}
			return null;
		}

	}

	private Map<String, StandardChartTheme> _themes;

	/**
	 * Config-constructor for {@link ChartThemeConfigurator}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ChartThemeConfigurator(InstantiationContext context, Config config) {
		super(context, config);
		_themes = new HashMap<>();
		_themes.put("Darkness", (StandardChartTheme) StandardChartTheme.createDarknessTheme());
		_themes.put("JFree", (StandardChartTheme) StandardChartTheme.createJFreeTheme());
		_themes.put("Legacy", (StandardChartTheme) createLegacyTheme());
		for (String name : config.getThemes().keySet()) {
			ConfiguredChartTheme theme = config.getThemes().get(name);
			StandardChartTheme fallback = getFallback(_themes, theme.getConfig(), config);
			theme.init(fallback);
			_themes.put(name, theme);
		}
		StandardChartTheme theme = _themes.get(config.getDefaultTheme());
		setDefaultTheme(theme);
	}

	/**
	 * @param theme
	 *        the {@link ChartTheme} to set as default in the {@link ChartFactory}
	 */
	public void setDefaultTheme(StandardChartTheme theme) {
		ChartFactory.setChartTheme(theme);
		// In ChartFactory.setChartTheme the BarPainter are initialized based on the name of the
		// theme and not on the actually set BarPainter. That's why we need to do this explicit.
		BarRenderer.setDefaultBarPainter(theme.getBarPainter());
		XYBarRenderer.setDefaultBarPainter(theme.getXYBarPainter());
	}

	/**
	 * a list with the names of all registered {@link ChartTheme}s
	 */
	public List<String> getThemeNames() {
		List<String> res = new ArrayList<>(_themes.keySet());
		Collections.sort(res);
		return res;
	}

	/**
	 * @param name
	 *        the name of the requeseted {@link ChartTheme}
	 * @return the {@link ChartTheme} for the given name, may be null
	 */
	public ChartTheme getTheme(String name) {
		return _themes.get(name);
	}

	/**
	 * The legacy-theme uses a {@link StandardBarPainter} and a {@link StandardXYBarPainter} but for
	 * some reason (laziness?) the painters are not initialized in the theme but will be set in the
	 * ChartFactory if the currentTheme is named "Legacy" (See
	 * {@link ChartFactory#setChartTheme(ChartTheme)}). Thats why the legacy thems is created
	 * manually here to have a valid fallback.
	 * 
	 * @return See {@link StandardChartTheme#createLegacyTheme()}
	 */
	private ChartTheme createLegacyTheme() {
		StandardChartTheme result = (StandardChartTheme) StandardChartTheme.createLegacyTheme();
		result.setBarPainter(new StandardBarPainter());
		result.setXYBarPainter(new StandardXYBarPainter());
		return result;
	}

	private StandardChartTheme getFallback(Map<String, StandardChartTheme> themes, ChartThemeConfig themeConfig,
			Config serviceConfig) {
		ChartTheme theme = themes.get(themeConfig.getFallback());
		if (theme == null) {
			theme = themes.get(serviceConfig.getFallback());
		}
		if (theme == null) {
			theme = StandardChartTheme.createLegacyTheme();
		}
		return (StandardChartTheme) theme;
	}

	/**
	 * Return the configured {@link ChartThemeConfigurator}
	 */
	public static ChartThemeConfigurator getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Singleton reference for the {@link ChartThemeConfigurator} service.
	 */
	public static final class Module extends TypedRuntimeModule<ChartThemeConfigurator> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ChartThemeConfigurator> getImplementation() {
			return ChartThemeConfigurator.class;
		}

	}
    
}

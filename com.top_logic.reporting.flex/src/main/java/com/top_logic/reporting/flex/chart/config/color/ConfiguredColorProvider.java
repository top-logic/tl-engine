/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Paint;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.util.ToStringText;

/**
 * This {@link ColorProvider} uses configured colors for objects. If no configured color is found
 * for an object, a random color is returned.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ConfiguredColorProvider implements ColorProvider {

	/**
	 * Application-config-interface for {@link ConfiguredColorProvider}.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * Mapping of value type keys to color maps to use.
		 * 
		 * @see ConfiguredColorProvider#getMapName(Object)
		 */
		@Key(ColorMap.NAME_ATTRIBUTE)
		public Map<String, ColorMap> getColorMaps();

		/**
		 * List of default colors to choose from, if no predefined colors are found.
		 */
		@ListBinding(format = HexEncodedPaint.class)
		public List<Paint> getColors();

	}

	/**
	 * A configured map of colors by key
	 */
	public interface ColorMap extends NamedConfigMandatory {

		/**
		 * Mapping of value keys to colors.
		 * 
		 * @see ConfiguredColorProvider#getKeyName(Object)
		 */
		@MapBinding(valueFormat = HexEncodedPaint.class)
		public Map<String, Paint> getColors();

	}

	/**
	 * Creates a new {@link ConfiguredColorProvider}
	 */
	public ConfiguredColorProvider() {
		super();
	}

	@Override
	public ColorContext createColorContext() {
		final GlobalConfig config = getConfig();
		return new ColorContext() {
			Map<String, ColorMap> _maps = config.getColorMaps();

			List<Paint> _colors = config.getColors();

			int _nextIndex = 0;

			@Override
			public Paint getColor(Object obj) {
				if (obj instanceof UniqueName) {
					obj = ((UniqueName) obj).getKey();
				}
				ColorMap colorMap = _maps.get(getMapName(obj));
				if (colorMap == null) {
					return nextColor();
				}

				Paint color = colorMap.getColors().get(getKeyName(obj));
				if (color == null) {
					return nextColor();
				}

				return color;
			}

			private Paint nextColor() {
				if (_nextIndex < _colors.size()) {
					return _colors.get(_nextIndex++);
				}

				// No more colors left.
				return RandomColorProvider.getRandomColor();
			}

		};
	}

	/**
	 * Derive color key from the given value.
	 * 
	 * @see ColorMap#getColors()
	 */
	protected String getKeyName(Object obj) {
		if (obj instanceof FastListElement) {
			return ((FastListElement) obj).getName();
		}
		if (obj instanceof ToStringText) {
			return ((ToStringText) obj).getKeyName();
		}
		if (obj instanceof TLObject) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("id:");
			try {
				((TLObject) obj).tHandle().getObjectName().appendExternalForm(buffer);
			} catch (IOException ex) {
				throw new UnreachableAssertion("Writing to buffer must not fail.", ex);
			}
			return buffer.toString();
		}
		return String.valueOf(obj);
	}

	/**
	 * Derive a color map key from the given value.
	 * 
	 * @see GlobalConfig#getColorMaps()
	 */
	protected Object getMapName(Object obj) {
		if (obj == null) {
			return "null";
		}
		if (obj instanceof FastListElement) {
			return ((FastListElement) obj).getList().getName();
		}
		if (obj instanceof ToStringText) {
			return ((ToStringText) obj).getMapName();
		}
		return obj.getClass().getSimpleName();
	}

	GlobalConfig getConfig() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
	}

}

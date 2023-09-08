/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import org.jfree.chart.JFreeChart;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.color.ConfiguredColorProvider;
import com.top_logic.reporting.flex.chart.config.color.ConfiguredColorProvider.ColorMap;
import com.top_logic.util.Resources;

/**
 * Simple way to create a unique key with an different translation where the translation must not be
 * unique. Useful for {@link JFreeChart} if partitions that are not functional equal should be
 * displayed with the same label.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ToStringText implements Comparable<ToStringText> {

	private final String _key;

	private final ResPrefix _prefix;

	/**
	 * Creates a new {@link ToStringText}
	 * 
	 * @param key
	 *        the key used to check for equality and used for translation (via {@link Resources}
	 *        ) when calling {@link #toString()}
	 */
	public ToStringText(String key) {
		this(ResPrefix.GLOBAL, key);
	}

	/**
	 * Creates a new {@link ToStringText}
	 * 
	 * @param prefix
	 *        the prefix for the key used for translation
	 * @param key
	 *        the key used to check for equality and used for translation (via {@link Resources}
	 *        ) when calling {@link #toString()}
	 */
	public ToStringText(ResPrefix prefix, String key) {
		_prefix = prefix;
		_key = key;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() + _key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ToStringText)) {
			return false;
		}
		return StringServices.equalsEmpty(_key, ((ToStringText) obj)._key);
	}

	@Override
	public String toString() {
		return Resources.getInstance().getString(_prefix.key(_key));
	}

	@Override
	public int compareTo(ToStringText o) {
		return _key.compareTo(o._key);
	}

	/**
	 * Used in {@link ConfiguredColorProvider} to determine which section contains the
	 * color-information.
	 * 
	 * @return the name to use in
	 *         {@link com.top_logic.reporting.flex.chart.config.color.ConfiguredColorProvider.GlobalConfig#getColorMaps()}
	 */
	public String getMapName() {
		return String.class.getSimpleName();
	}

	/**
	 * Used in {@link ConfiguredColorProvider} to determine the key to use in a {@link ColorMap}
	 * .
	 * 
	 * @return the name to use in
	 *         {@link com.top_logic.reporting.flex.chart.config.color.ConfiguredColorProvider.ColorMap#getColors()}
	 */
	public String getKeyName() {
		return _key;
	}

	/**
	 * Special implementation of {@link ToStringText} to provide more context-information in
	 * {@link #toString()}
	 */
	public static class NotSetText extends ToStringText {

		private final TLStructuredTypePart _ma;

		/**
		 * Creates a new {@link NotSetText} for the given {@link TLStructuredTypePart}. This allows
		 * custom not-set-texts and configured not-set-colors according to the attribute.
		 */
		public NotSetText(TLStructuredTypePart ma) {
			super("notSet_" + ma.getName());
			_ma = ma;
		}

		@Override
		public String getMapName() {
			return _ma.getType().getName();
		}

		@Override
		public String getKeyName() {
			return "reporting.classification.notset";
		}

		@Override
		public String toString() {
			Resources resources = Resources.getInstance();
			String attributeName = _ma.getName();
			String typeName = _ma.getType().getName();
			ResKey key1 = ResPrefix.legacyString(attributeName).append(typeName).key("notSet");
			ResKey key2 = ResPrefix.legacyString(typeName).key("notSet");
			return resources.getString(ResKey.fallback(key1, ResKey.fallback(key2, I18NConstants.NOT_SET)));
		}

	}
}
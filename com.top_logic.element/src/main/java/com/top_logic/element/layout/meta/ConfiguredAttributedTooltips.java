/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.KeyStorage;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TypeKeyProvider.Key;

/**
 * {@link KeyStorage} that holds for each key a list of attributes that are shown in the tooltip for
 * objects of the "type" of the key.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredAttributedTooltips extends KeyStorage<List<String>> {

	private static final List<String> DEFAULT_TOOLTIP_ATTRIBUTES = Arrays.asList(
		ConfiguredAttributedTooltipProvider.NAME_ATTRIBUTE,
		ConfiguredAttributedTooltipProvider.TYPE_ATTRIBUTE);

	/** Singleton {@link ConfiguredAttributedTooltips} instance. */
	public static final ConfiguredAttributedTooltips INSTANCE = new ConfiguredAttributedTooltips();

	private ConfiguredAttributedTooltips() {
		// singleton instance
	}

	@Override
	public String getName() {
		return "Attributes for tooltips.";
	}

	@Override
	public String getDescription() {
		return "Holds the attributes to show in tooltip for an element type.";
	}

	@Override
	protected List<String> newEntry(String propertyName, String configurationValue) throws Exception {
		return StringServices.toList(configurationValue, ',');
	}

	@Override
	protected List<String> getGlobalDefault() {
		return DEFAULT_TOOLTIP_ATTRIBUTES;
	}

	/**
	 * Returns the attributes configured for the given key.
	 */
	public static List<String> getValue(Key typeKey) {
		return INSTANCE.lookupEntry(typeKey);
	}

}


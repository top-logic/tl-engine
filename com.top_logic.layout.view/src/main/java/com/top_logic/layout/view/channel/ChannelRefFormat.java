/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link com.top_logic.basic.config.ConfigurationValueProvider} that parses a {@link ChannelRef}
 * from a string attribute value.
 *
 * <p>
 * Currently parses a plain channel name. In the future, will also parse cross-view references in
 * the format {@code path/to/view.view.xml#channelName}.
 * </p>
 *
 * <p>
 * Usage on a configuration property:
 * </p>
 *
 * <pre>
 * &#64;Format(ChannelRefFormat.class)
 * ChannelRef getSelection();
 * </pre>
 */
public class ChannelRefFormat extends AbstractConfigurationValueProvider<ChannelRef> {

	/** Singleton instance. */
	public static final ChannelRefFormat INSTANCE = new ChannelRefFormat();

	/**
	 * Creates a {@link ChannelRefFormat}.
	 */
	public ChannelRefFormat() {
		super(ChannelRef.class);
	}

	@Override
	protected ChannelRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return new ChannelRef(propertyValue.toString().trim());
	}

	@Override
	protected String getSpecificationNonNull(ChannelRef configValue) {
		return configValue.getChannelName();
	}
}

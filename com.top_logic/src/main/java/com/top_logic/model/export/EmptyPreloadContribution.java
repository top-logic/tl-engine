/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;

/**
 * {@link PreloadContribution} without any contribution.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class EmptyPreloadContribution implements PreloadContribution {

	/** Singleton {@link EmptyPreloadContribution}. */
	public static final EmptyPreloadContribution INSTANCE = new EmptyPreloadContribution();

	private EmptyPreloadContribution() {
		// singleton instance
	}

	@Override
	public void contribute(PreloadBuilder preloadBuilder) {
		// Ignore.
	}

	/**
	 * {@link DefaultValueProvider} that delivers the {@link EmptyPreloadContribution empty preload
	 * contribution} as default value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class EmptyDefault extends DefaultValueProviderShared {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return EmptyPreloadContribution.INSTANCE;
		}
	}
}

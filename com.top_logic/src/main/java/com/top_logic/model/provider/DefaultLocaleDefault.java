/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.util.Locale;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link DefaultValueProvider} delivering the default {@link Locale}.
 * 
 * @see ResourcesModule#getDefaultLocale()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.util:Language")
@Label("Default locale")
public class DefaultLocaleDefault extends DefaultValueProvider implements DefaultProvider, MODefaultProvider {

	/** Singleton {@link DefaultLocaleDefault} instance. */
	public static final DefaultLocaleDefault INSTANCE = new DefaultLocaleDefault();

	/**
	 * Creates a new {@link DefaultLocaleDefault}.
	 */
	protected DefaultLocaleDefault() {
		// singleton instance
	}

	@Override
	public Object createDefault(MOAttribute attribute) {
		return defaultLocale();
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		return defaultLocale();
	}

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return defaultLocale();
	}

	private Object defaultLocale() {
		return ResourcesModule.getInstance().getDefaultLocale();
	}

}


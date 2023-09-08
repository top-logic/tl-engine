/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of {@link ResKey} values.
 */
public class ResKeyNaming extends AbstractModelNamingScheme<ResKey, ResKeyNaming.Name> {

	/**
	 * Script value representing a custom {@link ResKey} value.
	 */
	public interface Name extends ModelName {

		/**
		 * The key of the resource.
		 */
		@Nullable
		String getKey();

		/**
		 * @see #getKey()
		 */
		void setKey(String key);

		/**
		 * The translations of a resource key.
		 */
		@MapBinding(tag = "translation", key = "lang", attribute = "value")
		Map<String, String> getTranslations();

		/**
		 * @see #getTranslations()
		 */
		void setTranslations(Map<String, String> translations);

	}

	/**
	 * Creates a {@link ResKeyNaming}.
	 */
	public ResKeyNaming() {
		super(ResKey.class, Name.class);
	}

	@Override
	protected void initName(Name name, ResKey model) {
		if (model.hasKey()) {
			name.setKey(model.getKey());
		}
		ResourcesModule resources = ResourcesModule.getInstance();

		Map<String, String> translations = new HashMap<>();
		for (String locale : resources.getSupportedLocaleNames()) {
			String value = resources.getBundle(ResourcesModule.localeFromString(locale)).getString(model);

			translations.put(locale, value);
		}
		name.setTranslations(translations);
	}

	@Override
	public ResKey locateModel(ActionContext context, Name name) {
		Builder builder = ResKey.builder(name.getKey());
		for (Map.Entry<String, String> entry : name.getTranslations().entrySet()) {
			builder.add(ResourcesModule.localeFromString(entry.getKey()), entry.getValue());
		}
		return builder.build();
	}

}

/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.Locale;
import java.util.Map.Entry;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKey.LiteralKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.tools.resources.translate.Translator;

/**
 * Algorithm completing translations of {@link LiteralKey} in {@link ConfigurationItem} properties.
 * 
 * @see #translateIfAutoTranslateEnabled(ConfigurationItem)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ConfigurationTranslator extends DescendingConfigurationItemVisitor {

	/**
	 * Singleton {@link ConfigurationTranslator} instance.
	 */
	public static final ConfigurationTranslator INSTANCE = new ConfigurationTranslator();

	private ConfigurationTranslator() {
		// Singleton constructor.
	}

	/**
	 * Completes the translation of all {@link PropertyDescriptor}s of type {@link ResKey} that are
	 * {@link LiteralKey}s, if {@link PersonalConfiguration#getAutoTranslate()} is enabled.
	 */
	public void translateIfAutoTranslateEnabled(ConfigurationItem config) {
		if (PersonalConfiguration.getPersonalConfiguration().getAutoTranslate()) {
			translate(config);
		}
	}

	/**
	 * Completes the translation of all {@link PropertyDescriptor}s of type {@link ResKey} that are
	 * {@link LiteralKey}s.
	 */
	public void translate(ConfigurationItem config) {
		handleProperties(config);
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		if (!property.canHaveSetter()) {
			return;
		}
		if (property.getType() == ResKey.class) {
			Object value = config.value(property);
			if (value instanceof LiteralKey) {
				LiteralKey literal = (LiteralKey) value;

				Builder builder = null;
				for (Locale target : ResourcesModule.getInstance().getSupportedLocales()) {
					String translation = literal.getTranslationWithoutFallbacks(target);
					if (translation == null) {
						Translator service = TranslationService.getInstance();
						if (service.isSupported(target)) {
							for (Entry<Locale, String> source : literal.getTranslations().entrySet()) {
								if (service.isSupported(source.getKey())) {
									if (builder == null) {
										builder = ResKey.builder(literal);
									}
									builder.add(target, service.translate(source.getValue(), source.getKey(), target));
									break;
								}
							}
						}
					}
				}

				if (builder != null) {
					builder.addAll(literal);
					config.update(property, builder.build());
				}
			}
		}
	}

}

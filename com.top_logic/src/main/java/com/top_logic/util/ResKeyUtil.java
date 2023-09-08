/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.customization.CustomizationContainer.TypeCustomizationConfig;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.model.TLObject;

/**
 * Utilities for working with {@link ResKey}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ResKeyUtil {

	/**
	 * Updates the {@link ResKey} attribute with the given name.
	 * 
	 * @param tlObject
	 *        Is not allowed to be null.
	 * @param attribute
	 *        Is not allowed to be null.
	 * @param locale
	 *        Is not allowed to be null.
	 * @param newTranlation
	 *        Is allowed to be null and empty: The value is passed to
	 *        {@link Builder#add(Locale, String)} without changes.
	 */
	public static void updateTranslation(TLObject tlObject, String attribute, Locale locale, String newTranlation) {
		ResKey oldResKey = (ResKey) tlObject.tValueByName(attribute);
		ResKey newResKey = updateTranslation(oldResKey, locale, newTranlation);
		tlObject.tUpdateByName(attribute, newResKey);
	}

	/**
	 * Creates a new {@link ResKey} where the translation for the given locale is replaced.
	 * 
	 * @param resKey
	 *        Null is treated as a {@link ResKey} without translations.
	 * @param locale
	 *        Is not allowed to be null.
	 * @param newTranslation
	 *        Is allowed to be null and empty: The value is passed to
	 *        {@link Builder#add(Locale, String)} without changes.
	 * @return Never null.
	 */
	public static ResKey updateTranslation(ResKey resKey, Locale locale, String newTranslation) {
		requireNonNull(locale);
		Map<Locale, String> translations = toMap(resKey);
		translations.put(locale, newTranslation);
		return fromMap(translations);
	}

	/**
	 * Creates a map with the translations of this {@link ResKey}.
	 * <p>
	 * The locales are taken from {@link ResourcesModule#getSupportedLocales()}.
	 * </p>
	 * <p>
	 * If there is no translation for a {@link Locale}, null is stored as value.
	 * </p>
	 * 
	 * @param key
	 *        If null, an empty {@link Map} is returned.
	 * @return Never null. A new, mutable and resizable {@link Map}.
	 */
	public static Map<Locale, String> toMap(ResKey key) {
		if (key == null) {
			return map();
		}
		Map<Locale, String> map = map();
		List<Locale> locales = ResourcesModule.getInstance().getSupportedLocales();
		for (Locale locale : locales) {
			/* Don't use the fallback translation: That would make it impossible for callers to
			 * detect whether a translation is missing. */
			String translation = translateWithoutFallback(locale, key);
			map.put(locale, translation);
		}
		return map;
	}

	/**
	 * Creates a new {@link ResKey} with the given translations.
	 * 
	 * @param translations
	 *        None of the locales must be null. Translations are allowed to be null and empty: These
	 *        values are passed to {@link Builder#add(Locale, String)} without changes.
	 * @return Never null.
	 */
	public static ResKey fromMap(Map<Locale, String> translations) {
		ResKey.Builder builder = ResKey.builder();
		for (Entry<Locale, String> entry : CollectionUtil.nonNull(translations.entrySet())) {
			Locale locale = requireNonNull(entry.getKey());
			String translation = entry.getValue();
			builder.add(locale, translation);
		}
		return requireNonNull(builder.build());
	}

	/**
	 * @param locale
	 *        If null, null is returned.
	 * @param key
	 *        If null, the empty {@link String} is returned.
	 * @return Null, if the locale is null.
	 */
	private static String translateWithoutFallback(Locale locale, ResKey key) {
		return Resources.getInstance(locale).getString(key, null);
	}

	/**
	 * Customized annotations of {@link ResKey}.
	 */
	public static TypeCustomizationConfig createResKeyCustomizationConfig() {
		TypeCustomizationConfig typeConfig = TypedConfiguration.newConfigItem(TypeCustomizationConfig.class);

		typeConfig.setName(ResKey.class.getName());
		TypedConfigUtil.setProperty(typeConfig, TypeCustomizationConfig.ANNOTATIONS, createResKeyAnnotations());

		return typeConfig;
	}

	private static Map<Class<? extends Annotation>, Annotation> createResKeyAnnotations() {
		Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

		ItemDisplay itemDisplay = TypedConfiguration.newAnnotationItem(ItemDisplay.class);
		TypedConfiguration.updateValue(itemDisplay, "value", ItemDisplay.ItemDisplayType.MONOMORPHIC);
		annotations.put(ItemDisplay.class, itemDisplay);

		PropertyEditor editor = TypedConfiguration.newAnnotationItem(PropertyEditor.class);
		TypedConfiguration.updateValue(editor, "value", InternationalizationEditor.class);
		annotations.put(PropertyEditor.class, editor);

		return annotations;
	}

}

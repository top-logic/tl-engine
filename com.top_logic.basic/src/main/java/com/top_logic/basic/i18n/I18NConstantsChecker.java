/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.top_logic.basic.Logger;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link I18NChecker} checking I18N in all {@link I18NConstantsBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstantsChecker implements I18NChecker {

	@Override
	public void checkI18N() {
		Logger.info("Checking resource keys.", I18NConstantsChecker.class);
		TypeIndex index = TypeIndex.Module.INSTANCE.getImplementationInstance();
		boolean transitive = true;
		boolean onlyInterfaces = false;
		boolean includeAbstract = true;
		Collection<Class<?>> i18nClasses =
			index.getSpecializations(I18NConstantsBase.class, transitive, onlyInterfaces, includeAbstract);
		ResourcesModule resources = ResourcesModule.getInstance();
		Collection<I18NBundle> bundles = new ArrayList<>();
		for (Locale locale : resources.getSupportedLocales()) {
			I18NBundle bundle = resources.getBundle(locale);
			assert bundle != null;
			bundles.add(bundle);
		}

		Locale locale = resources.getFallbackLocale();
		if (locale == null) {
			locale = resources.getDefaultLocale();
			Logger.info("No fallback locale found. Use default locale '" + locale + "' instead for check.",
				I18NConstantsChecker.class);
		}
		I18NBundle checkingBundle = resources.getBundle(locale);

		for (Class<?> i18NClass : i18nClasses) {
			if (skipClassCheck(i18NClass)) {
				continue;
			}
			for (Field field : i18NClass.getFields()) {
				if (field.getType() != ResKey.class) {
					continue;
				}

				if (skipFieldCheck(field)) {
					continue;
				}

				try {
					ResKey key = (ResKey) field.get(null);
					// Add key to missing resources file, or log failure, if the application is
					// configured to do so.
					checkingBundle.getString(key);
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					Logger.error("Error accessing I18N constant.", ex, I18NConstantsChecker.class);
				}
			}
		}
		Logger.info("Checking resource keys done.", I18NConstantsChecker.class);
	}

	/**
	 * Whether the given field in the checked {@link I18NConstantsBase} class must not be checked.
	 * 
	 * @param field
	 *        The field those value is checked.
	 * 
	 * @return Whether the value of the field must not be checked.
	 * 
	 * @see #skipClassCheck(Class)
	 */
	protected boolean skipFieldCheck(Field field) {
		return field.isSynthetic()
			|| !Modifier.isStatic(field.getModifiers())
			|| !Modifier.isPublic(field.getModifiers());
	}

	/**
	 * Whether the fields in the given {@link I18NConstantsBase} class must not be checked.
	 * 
	 * @param i18NClass
	 *        The class whose values are checked.
	 * 
	 * @return Whether the fields in the given class must not be checked.
	 * 
	 * @see #skipFieldCheck(Field)
	 */
	protected boolean skipClassCheck(Class<?> i18NClass) {
		return i18NClass.getPackage().getName().startsWith("test.");
	}

}


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
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.basic.util.ResKeyN;
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
				if (skipFieldCheck(field)) {
					continue;
				}
				checkField(field, checkingBundle);
			}
		}
		Logger.info("Checking resource keys done.", I18NConstantsChecker.class);
	}

	/**
	 * Checks the given field
	 *
	 * @param field
	 *        The field holding the value to check.
	 * @param checkingBundle
	 *        The bundle in which the value must occur.
	 */
	protected void checkField(Field field, I18NBundle checkingBundle) {
		Class<?> type = field.getType();
		if (type != ResKey.class &&
			type != ResKey1.class &&
			type != ResKey2.class &&
			type != ResKey3.class &&
			type != ResKey4.class &&
			type != ResKey5.class &&
			type != ResKeyN.class) {
			return;
		}

		try {
			/* NOTE: Also if the type is ResKey1, ResKey2, ResKey3, ResKey4, ResKey5, or ResKeyN,
			 * the cast to ResKey is correct, as actually ResKey is the single implementation of
			 * these interfaces. */
			ResKey key = (ResKey) field.get(null);
			// Add key to missing resources file, or log failure, if the application is
			// configured to do so.
			checkingBundle.getString(key);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			Logger.error("Error accessing I18N constant '" + qName(field) + "'.", ex, I18NConstantsChecker.class);
		}
	}


	/**
	 * Qualified name of a {@link Field}.
	 */
	protected static String qName(Field field) {
		return field.getDeclaringClass().getName() + "." + field.getName();
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


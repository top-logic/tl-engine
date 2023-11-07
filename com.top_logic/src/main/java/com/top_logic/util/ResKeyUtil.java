/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.customization.CustomizationContainer.TypeCustomizationConfig;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.model.TLObject;

/**
 * Utilities for working with {@link ResKey}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ResKeyUtil extends com.top_logic.basic.util.ResKeyUtil {

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

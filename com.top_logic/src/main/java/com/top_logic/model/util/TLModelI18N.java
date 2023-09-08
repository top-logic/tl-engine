/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.util.Resources;

/**
 * Service class to get {@link ResKey} for {@link TLModel} parts.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLModelI18N {

	public static String getI18NName(TLStructuredType aME, String anAccessPath, TLStructuredTypePart part) {
		return resolveKey(Resources.getInstance(), getI18NKey(aME, anAccessPath, part));
	}

	/**
	 * Get the internationalized name of a {@link TLStructuredTypePart} using
	 * {@link Resources#getInstance()} as resources.
	 * 
	 * @see #getI18NName(I18NBundle, TLStructuredTypePart)
	 */
	public static String getI18NName(TLStructuredTypePart part) {
		return getI18NName(Resources.getInstance(), part);
	}

	/**
	 * Get the internationalized name of a {@link TLStructuredTypePart} using the given resources.
	 * Uses the name of the given {@link TLStructuredTypePart} as default.
	 * 
	 * @param part
	 *        The {@link TLStructuredTypePart} to get I18N name for.
	 * @param resources
	 *        The resources to use to get internationalized name.
	 * 
	 * @return the I18N attribute name
	 * 
	 * @see #getI18NName(I18NBundle, TLStructuredTypePart)
	 */
	public static String getI18NName(I18NBundle resources, TLStructuredTypePart part) {
		return resolveKey(resources, getI18NKey(part));
	}

	private static String resolveKey(I18NBundle resources, ResKey i18nKey) {
		return resources.getString(i18nKey);
	}

	public static ResKey getI18NKey(TLStructuredType type, String accessPath, TLStructuredTypePart part) {
		ResKey result;
		if (StringServices.isEmpty(accessPath)) {
			result = partKey(type, part);
		} else {
			result = resPrefix(type).suffix(accessPath).suffix(part.getName());
		}
		return withNameFallback(part, result);
	}

	/**
	 * Gets the {@link ResKey} for the given part which is part of the given type.
	 */
	public static ResKey getI18NKey(TLStructuredType type, TLStructuredTypePart part) {
		return withNameFallback(part, partKey(type, part));
	}

	private static ResKey partKey(TLStructuredType type, TLStructuredTypePart part) {
		ResKey result;
		if (part.isOverride()) {
			result = getI18nKeyWithFallbacks(part);
		} else {
			result = getI18nKeyWithoutFallback(type, part);
		}
		return result;
	}

	private static ResKey withNameFallback(TLStructuredTypePart part, ResKey key) {
		return ResKey.fallback(key, ResKey.text(CodeUtil.englishLabel(part.getName())));
	}

	/**
	 * The {@link ResKey} to use for the table title for attribute-based tables by default.
	 * 
	 * @param part
	 *        The {@link TLStructuredTypePart} containing the row objects.
	 * @return The table title key.
	 * 
	 * @see TableConfiguration#getTitleKey()
	 */
	public static ResKey getTableTitleKey(TLStructuredTypePart part) {
		ResKey nameKey = partKey(part.getOwner(), part);
		ResKey titleKey = ResKey.fallback(nameKey.suffix(".title"), nameKey);
		return withNameFallback(part, titleKey);
	}

	/**
	 * The {@link ResKey} for the given {@link TLTypePart} <em>without fallbacks</em>.
	 * 
	 * @param part
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static ResKey getI18nKeyWithoutFallback(TLTypePart part) {
		return getI18nKeyWithoutFallback(part.getOwner(), part);
	}

	private static ResKey resPrefix(TLType type) {
		return TLModelNamingConvention.getTypeLabelKey(type);
	}

	static ResKey getI18nKeyWithoutFallback(TLType owner, TLTypePart part) {
		TLI18NKey annotation = part.getAnnotation(TLI18NKey.class);
		if (annotation != null) {
			return annotation.getValue();
		}
		return getI18NKey(owner, part.getName());
	}

	private static ResKey getI18NKey(TLType owner, String partName) {
		return resPrefix(owner).suffix(partName);
	}

	private static ResKey getI18nKeyWithFallbacks(TLStructuredTypePart overriddenPart) {
		ResKey originalPart = getI18nKeyWithoutFallback(overriddenPart);
		ResKey definitionPart = getI18nKeyWithoutFallback(overriddenPart.getDefinition());
		return ResKey.fallback(originalPart, definitionPart);
	}

	/**
	 * Get the {@link ResKey} for a {@link TLStructuredTypePart}.
	 *
	 * @param aMetaAttribute
	 *        the attribute. Must not be <code>null</code>.
	 * @return the I18N key
	 */
	public static ResKey getI18NKey(TLStructuredTypePart aMetaAttribute) {
		return getI18NKey(aMetaAttribute.getOwner(), aMetaAttribute);
	}

}

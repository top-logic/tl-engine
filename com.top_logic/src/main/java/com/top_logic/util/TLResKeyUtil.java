/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.model.TLObject;

/**
 * Utilities for working with {@link ResKey}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLResKeyUtil extends ResKeyUtil {

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
	 * Returns the {@link ResKey} for the given object if it is a {@link ResKey} or String,
	 * otherwise use the given fallback key.
	 */
	public static ResKey toResKey(Object object, ResKey fallback) {
		if (object instanceof ResKey) {
			return (ResKey) object;
		} else if (object instanceof String) {
			return ResKey.text((String) object);
		} else {
			return fallback;
		}
	}

	/**
	 * Checks if a user friendly readable resource for the given key exists.
	 */
	public static boolean existsResource(ResKey key) {
		ResKey directKey = key.direct();

		return (directKey != null) && (directKey.isLiteral() || existsResourceInternal(directKey));
	}

	private static boolean existsResourceInternal(ResKey key) {
		return key.hasKey() && Resources.getInstance().existsResource(key.getKey());
	}

}

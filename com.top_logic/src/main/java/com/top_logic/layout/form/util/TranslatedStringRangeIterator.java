/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.StringRangeIterator;
import com.top_logic.layout.ResPrefix;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * Extend the StringRangeIterator to translate its keys.
 * 
 * You must not use singletons of this class as the keys
 * are translated directly (with the benefit of a higher speed, later).
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TranslatedStringRangeIterator extends StringRangeIterator {
    
    private Map/*<String, String>*/ translated;
    
    /**
	 * Call super with {@link #translateKeys(String[], Resources, ResPrefix)}.
	 * 
	 * @param keys
	 *        The original keys
	 * @param aRes
	 *        Used for translation
	 * @param aPrefix
	 *        the Prefix usually found in {@link LayoutComponent#getResPrefix()}
	 */
	public TranslatedStringRangeIterator(String[] keys, Resources aRes, ResPrefix aPrefix) {
        super(keys);
        translated = translateKeys(keys, aRes, aPrefix);
    }


    /**
	 * Call super with {@link #translateKeys(String[], Resources, ResPrefix)} and
	 * {@link Resources#getInstance()}.
	 * 
	 * @param keys
	 *        The original keys
	 * @param aPrefix
	 *        the Prefix usually found in {@link LayoutComponent#getResPrefix()}
	 */
	public TranslatedStringRangeIterator(String[] keys, ResPrefix aPrefix) {
        super(keys);
        translated = translateKeys(keys,  Resources.getInstance(), aPrefix);
    }
    
    /**
     * Translate given object via {@link #translated}
     */
    @Override
	public String getUIStringFor(Object obj) {
        return (String) translated.get(obj);
    }

    /**
	 * Static helper to translate they keys on Construction.
	 * 
	 * @param keys
	 *        The original keys
	 * @param aRes
	 *        Used for translation
	 * @param resPrefix
	 *        the Prefix usually found in {@link LayoutComponent#getResPrefix()}
	 */
	public static Map translateKeys(String[] keys, Resources aRes, ResPrefix resPrefix) {
        Map translatedKeys = new HashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
			translatedKeys.put(key, aRes.getString(resPrefix.key(key)));
        }
        return translatedKeys;
    }

}


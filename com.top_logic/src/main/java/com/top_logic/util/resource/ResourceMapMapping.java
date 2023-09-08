/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource;

import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Mapping of objects to internationalized strings based on a key mapping.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ResourceMapMapping<T> implements Mapping<T, String> {

	protected final Map<? super T, ResKey> keyMapping;
    
    protected final Resources res;

	/**
	 * Creates new {@link ResourceMapMapping} based on a key {@link Mapping}.
	 * 
	 * @param keyMapping
	 *        A {@link Mapping} that translates objects to I18N keys.
	 */
	public ResourceMapMapping(Map<? super T, ResKey> keyMapping) {
        this.keyMapping = keyMapping;
        this.res         = Resources.getInstance(); 
    }

    @Override
	public String map(Object aInput) {
		return res.getString(keyMapping.get(aInput));
    }
}


/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.monitor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResKey OBJECT_COLLECTED;
	public static ResKey OBJECT_CACHE_HIT;
	public static ResKey OBJECT_CACHE_MISS;
	public static ResKey STATIC_ATTRIBUTE_READ;
	public static ResKey DYNAMIC_ATTRIBUTES_LOADED;
	public static ResKey DYNAMIC_ATTRIBUTES_MISSING;
	public static ResKey DYNAMIC_ATTRIBUTES_COLLECTED;
	public static ResKey DYNAMIC_ATTRIBUTE_READ;
	public static ResKey OBJECT_LOADED;

	static {
		initConstants(I18NConstants.class);
	}
	
}

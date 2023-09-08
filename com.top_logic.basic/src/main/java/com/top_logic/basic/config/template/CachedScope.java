/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.HashMap;
import java.util.Map;


/**
 * {@link TemplateScope} wrapper that caches results produced by this delegate.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CachedScope implements TemplateScope {

	private final Map<String, TemplateExpression> _cache = new HashMap<>();

	private final TemplateScope _impl;

	/**
	 * Creates a {@link CachedScope}.
	 * 
	 * @param impl
	 *        The delegate to retrieve {@link TemplateExpression}s from for caching.
	 */
	public CachedScope(TemplateScope impl) {
		_impl = impl;
	}

	@Override
	public TemplateExpression getTemplate(String name, boolean optional) {
		TemplateExpression result = _cache.get(name);
		if (result == null) {
			if (_cache.containsKey(name)) {
				// Missing template, marked as missing during last lookup.
				return null;
			}

			// Cache miss.
			result = _impl.getTemplate(name, optional);
			_cache.put(name, result);
		}
		return result;
	}

}

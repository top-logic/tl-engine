/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.gui.Theme;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link LayoutCache} which stores the {@link TLLayout}s inside a raw {@link Map}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultLayoutCache implements LayoutCache {

	private Map<Theme, Map<String, TLLayout>> _cache = new ConcurrentHashMap<>();

	@Override
	public void removeLayout(Theme theme, Person person, String layoutKey) {
		Map<String, TLLayout> map = _cache.get(theme);

		if (map != null) {
			map.remove(layoutKey);
		}
	}

	@Override
	public TLLayout getLayout(Theme theme, Person person, String layoutKey) {
		Map<String, TLLayout> map = _cache.get(theme);

		if (map != null) {
			return map.get(layoutKey);
		}

		return null;
	}

	@Override
	public void putLayout(Theme theme, Person person, String layoutKey, TLLayout layout) {
		TLLayout cachedLayout = findCachedLayoutInParentThemes(theme, layoutKey, layout);

		_cache.computeIfAbsent(theme, x -> new HashMap<>()).put(layoutKey, cachedLayout);
	}

	private TLLayout findCachedLayoutInParentThemes(Theme theme, String layoutKey, TLLayout layout) {
		for (Theme parentTheme : theme.getParentThemes()) {
			Map<String, TLLayout> themeCache = _cache.get(parentTheme);

			if (themeCache != null) {
				TLLayout parentLayout = themeCache.get(layoutKey);

				if(parentLayout != null) {
					if (LayoutUtils.hasSameLayoutConfig(layoutKey, layout, parentLayout)) {
						return parentLayout;
					}
				}
			}
		}

		return layout;
	}

	@Override
	public Map<String, TLLayout> getLayouts(Theme theme, Person person) {
		return _cache.get(theme);
	}

}

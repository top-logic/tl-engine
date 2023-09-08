/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.gui.Theme;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * General cache for {@link TLLayout}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface LayoutCache {

	/**
	 * Returns the {@link TLLayout} defined by the given identifiers.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param person
	 *        Owner of the layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 */
	TLLayout getLayout(Theme theme, Person person, String layoutKey);

	/**
	 * Returns all layouts for a given person and theme.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * @param person
	 *        Owner of the layouts.
	 * 
	 * @see #getLayout(Theme, Person, String)
	 */
	Map<String, TLLayout> getLayouts(Theme theme, Person person);

	/**
	 * Adds the given layout to the cache.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param person
	 *        Owner of the layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 */
	void putLayout(Theme theme, Person person, String layoutKey, TLLayout layout);

	/**
	 * Removes the {@link TLLayout} defined by the given identifier from the cache.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * 
	 * @param person
	 *        Owner of the layouts.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 */
	void removeLayout(Theme theme, Person person, String layoutKey);


}

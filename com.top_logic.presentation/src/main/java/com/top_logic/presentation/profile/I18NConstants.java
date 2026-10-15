/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.profile;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * <p>
 * The first argument of each error is the path of the faulty entry in the presentation description
 * or the template profile, e.g. <code>slides[2].layout</code> or <code>profile.layouts.content</code>.
 * </p>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The entry {0} refers to the slide layout with index {1}, but the template has only {2}
	 *     slide layouts, the first has index 0.
	 */
	public static ResKey3 ERROR_NO_SUCH_LAYOUT_INDEX__PATH_INDEX_COUNT;

	/**
	 * @en The entry {0} refers to the slide layout "{1}", which the template does not contain. Its
	 *     slide layouts are: {2}.
	 */
	public static ResKey3 ERROR_NO_SUCH_LAYOUT_NAME__PATH_NAME_NAMES;

	/**
	 * @en The entry {0} asks for the slide layout with the role "{1}", but the template profile
	 *     assigns no layout to this role. Roles with a layout are: {2}. Give the layout by its index
	 *     or name, or assign it to the role in the "layouts" entry of the profile.
	 */
	public static ResKey3 ERROR_NO_LAYOUT_FOR_ROLE__PATH_ROLE_ROLES;

	/**
	 * @en A slide layout: its index or its name.
	 */
	public static ResKey EXPECTED_LAYOUT;

	/**
	 * @en The index of a placeholder of the slide layout, or "none".
	 */
	public static ResKey EXPECTED_PLACEHOLDER;

	static {
		initConstants(I18NConstants.class);
	}
}

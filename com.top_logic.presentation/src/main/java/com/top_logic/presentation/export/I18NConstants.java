/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * <p>
 * The first argument of each error is the path of the faulty entry in the presentation
 * description, e.g. <code>slides[2].content[0].fill</code>.
 * </p>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The entry {0} uses the color role "{1}", which the template profile does not define.
	 *     Defined color roles are: {2}. Define the color in the "roles" entry of the profile.
	 */
	public static ResKey3 ERROR_UNKNOWN_COLOR_ROLE__PATH_ROLE_ROLES;

	/**
	 * @en The entry {0} uses the color role "{1}", whose definition in the template profile refers
	 *     to itself.
	 */
	public static ResKey2 ERROR_COLOR_ROLE_CYCLE__PATH_ROLE;

	/**
	 * @en The entry {0} fills the slide layout placeholder "{1}", which the slide layout "{2}" does
	 *     not have.
	 */
	public static ResKey3 ERROR_NO_PLACEHOLDER__PATH_PLACEHOLDER_LAYOUT;

	/**
	 * @en The text {0} does not fit into its element. Shorten the text, enlarge the element, or
	 *     choose another "overflow" handling ("shrink", "ellipsis" or "visible").
	 */
	public static ResKey1 ERROR_TEXT_OVERFLOW__PATH;

	/**
	 * @en The element {0} does not fit onto its slide and must be continued on a further slide,
	 *     like the element {1}. Only one element of a slide can be continued. Give the elements more
	 *     space, or distribute them over several slides.
	 */
	public static ResKey2 ERROR_SEVERAL_CONTINUED__PATH_OTHER;

	/**
	 * @en The entry {0} contains the image "{1}" in a format that cannot be placed on a slide. Use a
	 *     PNG, JPEG, GIF, BMP or TIFF image.
	 */
	public static ResKey2 ERROR_UNSUPPORTED_IMAGE__PATH_NAME;

	/**
	 * @en The outline {0} cannot be painted with a picture. Use a color or a gradient.
	 */
	public static ResKey1 ERROR_PICTURE_LINE__PATH;

	static {
		initConstants(I18NConstants.class);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;
import java.util.Map;

/**
 * A slide in a presentation description.
 *
 * @param layout
 *        The slide layout the slide is based on, <code>null</code> for the default layout.
 * @param title
 *        The text of the title placeholder, <code>null</code> to leave it empty.
 * @param subtitle
 *        The text of the subtitle placeholder, <code>null</code> to leave it empty.
 * @param placeholders
 *        The texts of further placeholders of the layout.
 * @param content
 *        The elements placed on the slide.
 * @param notes
 *        The speaker notes, <code>null</code> for none.
 * @param hidden
 *        Whether the slide is hidden in the slide show.
 */
public record SlideSpec(LayoutRef layout, TextSpec title, TextSpec subtitle, Map<PlaceholderKey, TextSpec> placeholders,
		List<ElementSpec> content, String notes, boolean hidden) {
	// Pure data.
}

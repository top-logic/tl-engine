/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.profile;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * The properties of a slide layout that a presentation export needs beyond the layout itself.
 *
 * @param name
 *        The name of the layout, for information only.
 * @param subtitle
 *        The index of the placeholder taking the subtitle of a slide, <code>null</code> if the
 *        layout has no subtitle.
 * @param footer
 *        The index of the placeholder taking the footer of the presentation, <code>null</code> if
 *        the layout has no footer.
 * @param entries
 *        The indexes of the placeholders taking the entries of an agenda slide, in the order of
 *        the entries.
 * @param contentArea
 *        The area in points in which the content elements of a slide are placed that have no
 *        position of their own.
 */
public record LayoutProfile(String name, Long subtitle, Long footer, List<Long> entries,
		Rectangle2D contentArea) {

	/**
	 * The data representation with the keys {@link PresentationData#NAME},
	 * {@link PresentationData#SUBTITLE}, {@link PresentationData#FOOTER},
	 * {@link PresentationData#ENTRIES} (if the layout has entry placeholders) and
	 * {@link PresentationData#CONTENT_AREA} (with {@link PresentationData#X},
	 * {@link PresentationData#Y}, {@link PresentationData#WIDTH} and {@link PresentationData#HEIGHT}).
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		PresentationData.put(result, PresentationData.NAME, name);
		PresentationData.put(result, PresentationData.SUBTITLE, subtitle == null ? null : Double.valueOf(subtitle));
		PresentationData.put(result, PresentationData.FOOTER, footer == null ? null : Double.valueOf(footer));
		if (!entries.isEmpty()) {
			result.put(PresentationData.ENTRIES, PresentationData.list(entries, idx -> Double.valueOf(idx.longValue())));
		}
		Map<String, Object> area = PresentationData.map();
		PresentationData.putBounds(area, contentArea);
		result.put(PresentationData.CONTENT_AREA, area);
		return result;
	}

}

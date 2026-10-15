/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * A slide master of a presentation template.
 *
 * @param index
 *        The position of the master in the presentation, starting with 0.
 * @param name
 *        The name of the master, or the name of its theme if the master has no name of its own.
 * @param theme
 *        The theme of the master and all its layouts and slides.
 * @param background
 *        The background, <code>null</code> if not defined.
 * @param placeholders
 *        The placeholders.
 * @param decorations
 *        The shapes that are no placeholders, e.g. logos. They are shown on all slides of layouts
 *        that do not hide the master graphics.
 */
public record MasterInfo(int index, String name, ThemeInfo theme, PaintInfo background,
		List<PlaceholderInfo> placeholders, List<ShapeInfo> decorations) {

	/**
	 * The data representation with the keys {@link PresentationData#INDEX}, {@link PresentationData#NAME},
	 * {@link PresentationData#THEME}, {@link PresentationData#BACKGROUND},
	 * {@link PresentationData#PLACEHOLDERS} and {@link PresentationData#DECORATIONS}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		result.put(PresentationData.INDEX, Double.valueOf(index));
		PresentationData.put(result, PresentationData.NAME, name);
		result.put(PresentationData.THEME, theme.toData());
		PresentationData.put(result, PresentationData.BACKGROUND, background == null ? null : background.toData());
		result.put(PresentationData.PLACEHOLDERS, PresentationData.list(placeholders, PlaceholderInfo::toData));
		result.put(PresentationData.DECORATIONS, PresentationData.list(decorations, ShapeInfo::toData));
		return result;
	}

}

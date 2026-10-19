/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

import org.apache.poi.xslf.usermodel.SlideLayout;

import com.top_logic.presentation.data.PresentationData;

/**
 * A slide layout of a presentation template.
 *
 * @param index
 *        The position of the layout in {@link TemplateAnalysis#layouts(org.apache.poi.xslf.usermodel.XMLSlideShow)}.
 * @param name
 *        The name of the layout.
 * @param type
 *        The predefined layout type, <code>null</code> for a custom layout.
 * @param masterIndex
 *        The {@link MasterInfo#index()} of the master the layout belongs to.
 * @param masterName
 *        The {@link MasterInfo#name()} of the master the layout belongs to.
 * @param background
 *        The effective background, inherited from the master if the layout defines none.
 * @param placeholders
 *        The placeholders of the layout.
 * @param decorations
 *        The shapes that are no placeholders, e.g. a helper raster or a colored bar.
 */
public record LayoutInfo(int index, String name, SlideLayout type, int masterIndex, String masterName,
		PaintInfo background, List<PlaceholderInfo> placeholders, List<ShapeInfo> decorations) {

	/**
	 * The first placeholder of the given type.
	 *
	 * @param placeholderType
	 *        The placeholder type, see {@link PlaceholderInfo#type()}.
	 * @return The placeholder, or <code>null</code> if the layout has none of this type.
	 */
	public PlaceholderInfo placeholder(String placeholderType) {
		for (PlaceholderInfo placeholder : placeholders) {
			if (placeholder.type().equals(placeholderType)) {
				return placeholder;
			}
		}
		return null;
	}

	/**
	 * The placeholder with the given type and index.
	 *
	 * @return The placeholder, or <code>null</code> if the layout has no such placeholder.
	 */
	public PlaceholderInfo placeholder(String placeholderType, long idx) {
		for (PlaceholderInfo placeholder : placeholders) {
			if (placeholder.type().equals(placeholderType) && placeholder.idx() == idx) {
				return placeholder;
			}
		}
		return null;
	}

	/**
	 * The data representation with the keys {@link PresentationData#INDEX}, {@link PresentationData#NAME},
	 * {@link PresentationData#TYPE}, {@link PresentationData#MASTER} (with {@link PresentationData#INDEX} and
	 * {@link PresentationData#NAME}), {@link PresentationData#BACKGROUND},
	 * {@link PresentationData#PLACEHOLDERS} and {@link PresentationData#DECORATIONS}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		result.put(PresentationData.INDEX, Double.valueOf(index));
		PresentationData.put(result, PresentationData.NAME, name);
		PresentationData.put(result, PresentationData.TYPE, type == null ? null : type.name());
		Map<String, Object> master = PresentationData.map();
		master.put(PresentationData.INDEX, Double.valueOf(masterIndex));
		PresentationData.put(master, PresentationData.NAME, masterName);
		result.put(PresentationData.MASTER, master);
		PresentationData.put(result, PresentationData.BACKGROUND, background == null ? null : background.toData());
		result.put(PresentationData.PLACEHOLDERS, PresentationData.list(placeholders, PlaceholderInfo::toData));
		result.put(PresentationData.DECORATIONS, PresentationData.list(decorations, ShapeInfo::toData));
		return result;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.profile;

import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xslf.usermodel.SlideLayout;

import com.top_logic.presentation.model.LayoutRole;
import com.top_logic.presentation.template.LayoutInfo;
import com.top_logic.presentation.template.PlaceholderInfo;
import com.top_logic.presentation.template.TemplateAnalysis;

/**
 * Derives the {@link TemplateProfile} of a template from its {@link TemplateAnalysis}.
 *
 * <p>
 * The derivation inspects the predefined layout types and the placeholders of the layouts. Their
 * positions are compared relative to the slide size, so that the rules apply to templates of any
 * size:
 * </p>
 *
 * <ul>
 * <li>A <b>content placeholder</b> is a <code>body</code> or <code>obj</code> placeholder covering
 * at least {@link #LARGE_AREA} of the slide; the largest one counts. Any placeholder of that size
 * is <b>large</b>.</li>
 * <li>The <b>footer</b> is the <code>ftr</code> placeholder, otherwise the first <code>body</code>
 * or <code>obj</code> placeholder starting in the bottom band of the slide (below
 * {@link #FOOTER_BAND} of its height).</li>
 * <li>The <b>subtitle</b> is the <code>subTitle</code> placeholder, otherwise the first other
 * <code>body</code> or <code>obj</code> placeholder starting directly below the title (within
 * {@link #SUBTITLE_GAP} of the slide height), being at most {@link #SUBTITLE_HEIGHT} of the slide
 * height high and spanning at least {@link #SUBTITLE_OVERLAP} of the title's width.</li>
 * <li>The <b>content area</b> is the bounds of the content placeholder. A layout without one
 * offers the area between the bottom of the subtitle (or the title) and the top of the footer (or
 * the slide bottom), horizontally from the left edge of the title to the same distance from the
 * right slide border.</li>
 * </ul>
 *
 * <p>
 * The layouts of the {@link LayoutRole roles} are:
 * </p>
 *
 * <ul>
 * <li>{@link LayoutRole#TITLE}: The first layout of type {@link SlideLayout#TITLE}, otherwise the
 * first layout with a <code>ctrTitle</code> placeholder.</li>
 * <li>{@link LayoutRole#CONTENT}: The first layout of type {@link SlideLayout#TITLE_AND_CONTENT}.
 * Otherwise the layout with a <code>title</code> placeholder, a
 * content placeholder and no further large placeholder (e.g. a picture), the one with the largest
 * content placeholder. Otherwise a layout with a <code>title</code> placeholder and no large
 * placeholder at all, the one with the fewest placeholders.</li>
 * <li>{@link LayoutRole#SECTION}: The first layout of type {@link SlideLayout#SECTION_HEADER}.</li>
 * <li>{@link LayoutRole#CLOSING}: The layout of the title slide.</li>
 * </ul>
 *
 * <p>
 * A role without a matching layout gets none. In each case, the first layout in the order of
 * {@link TemplateAnalysis#layouts()} wins a tie.
 * </p>
 */
final class ProfileDerivation {

	/**
	 * The fraction of the slide area a placeholder must cover to be large.
	 */
	static final double LARGE_AREA = 0.25;

	/**
	 * The fraction of the slide height below which a footer starts.
	 */
	static final double FOOTER_BAND = 0.85;

	/**
	 * The largest distance of a subtitle from the title bottom as fraction of the slide height.
	 */
	static final double SUBTITLE_GAP = 0.05;

	/**
	 * The largest height of a subtitle as fraction of the slide height.
	 */
	static final double SUBTITLE_HEIGHT = 0.2;

	/**
	 * The smallest fraction of the title's width a subtitle spans.
	 */
	static final double SUBTITLE_OVERLAP = 0.5;

	private static final String TITLE = "title";

	private static final String CENTER_TITLE = "ctrTitle";

	private static final String SUBTITLE = "subTitle";

	private static final String FOOTER = "ftr";

	private static final Set<String> TITLE_TYPES = Set.of(TITLE, CENTER_TITLE);

	private static final Set<String> CONTENT_TYPES = Set.of("body", "obj");

	private final TemplateAnalysis _analysis;

	private final double _slideArea;

	private ProfileDerivation(TemplateAnalysis analysis) {
		_analysis = analysis;
		_slideArea = analysis.slideWidth() * analysis.slideHeight();
	}

	/**
	 * Derives the profile of the analyzed template, without color roles.
	 */
	static TemplateProfile derive(TemplateAnalysis analysis) {
		return new ProfileDerivation(analysis).derive();
	}

	private TemplateProfile derive() {
		Map<Integer, LayoutProfile> slideLayouts = new LinkedHashMap<>();
		for (LayoutInfo layout : _analysis.layouts()) {
			slideLayouts.put(Integer.valueOf(layout.index()), layoutProfile(layout));
		}

		Map<LayoutRole, Integer> layouts = new EnumMap<>(LayoutRole.class);
		Integer title = titleLayout();
		putRole(layouts, LayoutRole.TITLE, title);
		putRole(layouts, LayoutRole.CONTENT, contentLayout());
		putRole(layouts, LayoutRole.SECTION, firstOfType(SlideLayout.SECTION_HEADER));
		putRole(layouts, LayoutRole.CLOSING, title);
		return new TemplateProfile(layouts, slideLayouts, Map.of());
	}

	private static void putRole(Map<LayoutRole, Integer> layouts, LayoutRole role, Integer index) {
		if (index != null) {
			layouts.put(role, index);
		}
	}

	private Integer titleLayout() {
		Integer result = firstOfType(SlideLayout.TITLE);
		if (result != null) {
			return result;
		}
		for (LayoutInfo layout : _analysis.layouts()) {
			if (layout.placeholder(CENTER_TITLE) != null) {
				return Integer.valueOf(layout.index());
			}
		}
		return null;
	}

	private Integer contentLayout() {
		Integer typed = firstOfType(SlideLayout.TITLE_AND_CONTENT);
		if (typed != null) {
			return typed;
		}

		LayoutInfo best = null;
		double bestArea = 0;
		for (LayoutInfo layout : _analysis.layouts()) {
			if (layout.placeholder(TITLE) == null) {
				continue;
			}
			PlaceholderInfo content = contentPlaceholder(layout, null, null);
			if (content == null || largeCount(layout) > 1) {
				continue;
			}
			double area = area(content.bounds());
			if (area > bestArea) {
				best = layout;
				bestArea = area;
			}
		}
		if (best != null) {
			return Integer.valueOf(best.index());
		}

		LayoutInfo titleOnly = null;
		for (LayoutInfo layout : _analysis.layouts()) {
			if (layout.placeholder(TITLE) == null || largeCount(layout) > 0) {
				continue;
			}
			if (titleOnly == null || layout.placeholders().size() < titleOnly.placeholders().size()) {
				titleOnly = layout;
			}
		}
		return titleOnly == null ? null : Integer.valueOf(titleOnly.index());
	}

	private Integer firstOfType(SlideLayout type) {
		List<LayoutInfo> layouts = _analysis.layouts(type);
		return layouts.isEmpty() ? null : Integer.valueOf(layouts.get(0).index());
	}

	private int largeCount(LayoutInfo layout) {
		int result = 0;
		for (PlaceholderInfo placeholder : layout.placeholders()) {
			if (isLarge(placeholder)) {
				result++;
			}
		}
		return result;
	}

	private LayoutProfile layoutProfile(LayoutInfo layout) {
		PlaceholderInfo title = title(layout);
		PlaceholderInfo footer = footer(layout);
		PlaceholderInfo subtitle = subtitle(layout, title, footer);
		PlaceholderInfo content = contentPlaceholder(layout, subtitle, footer);
		Rectangle2D area = content != null ? content.bounds() : freeArea(title, subtitle, footer);
		return new LayoutProfile(layout.name(), idx(subtitle), idx(footer), area);
	}

	private static PlaceholderInfo title(LayoutInfo layout) {
		for (PlaceholderInfo placeholder : layout.placeholders()) {
			if (TITLE_TYPES.contains(placeholder.type()) && placeholder.bounds() != null) {
				return placeholder;
			}
		}
		return null;
	}

	private PlaceholderInfo footer(LayoutInfo layout) {
		PlaceholderInfo footer = layout.placeholder(FOOTER);
		if (footer != null) {
			return footer;
		}
		double band = _analysis.slideHeight() * FOOTER_BAND;
		for (PlaceholderInfo placeholder : layout.placeholders()) {
			if (isContentType(placeholder) && placeholder.bounds().getY() >= band) {
				return placeholder;
			}
		}
		return null;
	}

	private PlaceholderInfo subtitle(LayoutInfo layout, PlaceholderInfo title, PlaceholderInfo footer) {
		PlaceholderInfo subtitle = layout.placeholder(SUBTITLE);
		if (subtitle != null) {
			return subtitle;
		}
		if (title == null) {
			return null;
		}
		Rectangle2D titleBounds = title.bounds();
		double height = _analysis.slideHeight();
		for (PlaceholderInfo placeholder : layout.placeholders()) {
			if (placeholder == footer || !isContentType(placeholder)) {
				continue;
			}
			Rectangle2D bounds = placeholder.bounds();
			double gap = Math.abs(bounds.getY() - titleBounds.getMaxY());
			double overlap = Math.min(bounds.getMaxX(), titleBounds.getMaxX())
				- Math.max(bounds.getX(), titleBounds.getX());
			if (gap <= height * SUBTITLE_GAP && bounds.getHeight() <= height * SUBTITLE_HEIGHT
				&& overlap >= titleBounds.getWidth() * SUBTITLE_OVERLAP) {
				return placeholder;
			}
		}
		return null;
	}

	/**
	 * The largest body or object placeholder that is large and neither the subtitle nor the
	 * footer.
	 */
	private PlaceholderInfo contentPlaceholder(LayoutInfo layout, PlaceholderInfo subtitle, PlaceholderInfo footer) {
		PlaceholderInfo result = null;
		for (PlaceholderInfo placeholder : layout.placeholders()) {
			if (placeholder == subtitle || placeholder == footer || !isContentType(placeholder)
				|| !isLarge(placeholder)) {
				continue;
			}
			if (result == null || area(placeholder.bounds()) > area(result.bounds())) {
				result = placeholder;
			}
		}
		return result;
	}

	private Rectangle2D freeArea(PlaceholderInfo title, PlaceholderInfo subtitle, PlaceholderInfo footer) {
		double width = _analysis.slideWidth();
		double height = _analysis.slideHeight();
		double left;
		double top;
		if (title != null) {
			left = title.bounds().getX();
			top = (subtitle != null ? subtitle : title).bounds().getMaxY();
		} else {
			left = 0;
			top = subtitle != null ? subtitle.bounds().getMaxY() : 0;
		}
		double bottom = footer != null ? footer.bounds().getY() : height;
		return new Rectangle2D.Double(left, top, Math.max(0, width - 2 * left), Math.max(0, bottom - top));
	}

	private boolean isLarge(PlaceholderInfo placeholder) {
		return placeholder.bounds() != null && area(placeholder.bounds()) >= _slideArea * LARGE_AREA;
	}

	private static boolean isContentType(PlaceholderInfo placeholder) {
		return CONTENT_TYPES.contains(placeholder.type()) && placeholder.bounds() != null;
	}

	private static double area(Rectangle2D bounds) {
		return bounds.getWidth() * bounds.getHeight();
	}

	private static Long idx(PlaceholderInfo placeholder) {
		return placeholder == null ? null : Long.valueOf(placeholder.idx());
	}

}

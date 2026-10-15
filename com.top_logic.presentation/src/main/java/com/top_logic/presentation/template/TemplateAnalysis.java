/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideLayoutIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideLayoutIdListEntry;

import com.top_logic.presentation.data.PresentationData;

/**
 * Structure and style of a PowerPoint presentation used as template: its slide size, its slide
 * masters with their themes, its slide layouts with their placeholders and its sample slides.
 *
 * <p>
 * The analysis is created by {@link #analyze(XMLSlideShow)}. Its {@link #toData() data
 * representation} is JSON-compatible and describes the template to a script or an AI agent.
 * </p>
 *
 * @param slideWidth
 *        The width of a slide in points.
 * @param slideHeight
 *        The height of a slide in points.
 * @param masters
 *        The slide masters in presentation order.
 * @param layouts
 *        The slide layouts in the order of {@link #layouts(XMLSlideShow)}.
 * @param slides
 *        The slides in presentation order.
 */
public record TemplateAnalysis(double slideWidth, double slideHeight, List<MasterInfo> masters,
		List<LayoutInfo> layouts, List<SlideInfo> slides) {

	/**
	 * Analyzes the given presentation.
	 */
	public static TemplateAnalysis analyze(XMLSlideShow slideShow) {
		return new TemplateAnalyzer(slideShow).analyze();
	}

	/**
	 * All slide layouts of the given presentation in the order PowerPoint shows them: The layouts
	 * of the first master in their order within the master, followed by the layouts of the second
	 * master, and so on.
	 *
	 * <p>
	 * A {@link LayoutInfo#index()} is the position of the layout in this list.
	 * </p>
	 */
	public static List<XSLFSlideLayout> layouts(XMLSlideShow slideShow) {
		List<XSLFSlideLayout> result = new ArrayList<>();
		for (XSLFSlideMaster master : slideShow.getSlideMasters()) {
			result.addAll(layouts(master));
		}
		return result;
	}

	/**
	 * The slide layouts of the given master in their order within the master.
	 */
	public static List<XSLFSlideLayout> layouts(XSLFSlideMaster master) {
		List<XSLFSlideLayout> result = new ArrayList<>();
		CTSlideLayoutIdList idList = master.getXmlObject().getSldLayoutIdLst();
		if (idList != null) {
			for (CTSlideLayoutIdListEntry entry : idList.getSldLayoutIdArray()) {
				POIXMLDocumentPart part = master.getRelationById(entry.getId2());
				if (part instanceof XSLFSlideLayout layout && !result.contains(layout)) {
					result.add(layout);
				}
			}
		}
		// Layouts related to the master but missing in its list are appended.
		for (POIXMLDocumentPart part : master.getRelations()) {
			if (part instanceof XSLFSlideLayout layout && !result.contains(layout)) {
				result.add(layout);
			}
		}
		return result;
	}

	/**
	 * The first layout with the given name.
	 *
	 * @return The layout, or <code>null</code> if there is no layout with this name.
	 */
	public LayoutInfo layout(String name) {
		for (LayoutInfo layout : layouts) {
			if (layout.name() != null && layout.name().equals(name)) {
				return layout;
			}
		}
		return null;
	}

	/**
	 * All layouts of the given predefined type.
	 */
	public List<LayoutInfo> layouts(SlideLayout type) {
		List<LayoutInfo> result = new ArrayList<>();
		for (LayoutInfo layout : layouts) {
			if (layout.type() == type) {
				result.add(layout);
			}
		}
		return result;
	}

	/**
	 * The data representation with the keys {@link PresentationData#SLIDE_SIZE} (with
	 * {@link PresentationData#WIDTH} and {@link PresentationData#HEIGHT}), {@link PresentationData#MASTERS},
	 * {@link PresentationData#LAYOUTS} and {@link PresentationData#SLIDES}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		Map<String, Object> size = PresentationData.map();
		size.put(PresentationData.WIDTH, PresentationData.number(slideWidth));
		size.put(PresentationData.HEIGHT, PresentationData.number(slideHeight));
		result.put(PresentationData.SLIDE_SIZE, size);
		result.put(PresentationData.MASTERS, PresentationData.list(masters, MasterInfo::toData));
		result.put(PresentationData.LAYOUTS, PresentationData.list(layouts, LayoutInfo::toData));
		result.put(PresentationData.SLIDES, PresentationData.list(slides, SlideInfo::toData));
		return result;
	}

}

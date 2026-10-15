/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.render.SlideRenderer;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions rendering PowerPoint slides into images.
 *
 * <p>
 * The images show how a presentation looks, e.g. to check a generated presentation for overflowing
 * text or to see the style of a template. They are rendered on the server by Apache POI, which
 * supports the common shapes, fills, outlines, pictures, tables and text, but no
 * <code>SmartArt</code> diagrams and not all effects. Bar and column charts are drawn from their data, other charts as a frame
 * naming the kind of chart.
 * </p>
 *
 * <p>
 * Text in a font that is not installed on the server is drawn in a standard font. The names of
 * such fonts are written to the log as a warning.
 * </p>
 */
@ScriptPrefix("ppt")
public class PptRenderFunctions extends TLScriptFunctions {

	/**
	 * The default image width in pixels.
	 */
	public static final int DEFAULT_WIDTH = 1280;

	/**
	 * Renders slides of a PowerPoint presentation into images.
	 *
	 * <p>
	 * The result contains a PNG image for each requested slide in the requested order. An image is
	 * named after the slide index, e.g. <code>slide-03.png</code> for the slide with index 3 (the
	 * fourth slide). Its height follows from the width and the aspect ratio of the slides.
	 * </p>
	 *
	 * <p>
	 * To see how the slide layouts of a template look, use <code>pptRenderLayouts()</code>.
	 * </p>
	 *
	 * @param presentation
	 *        The PowerPoint file (<code>.pptx</code> or <code>.potx</code>) to render.
	 * @param slides
	 *        The indices of the slides to render, the first slide has index 0. A single number
	 *        renders a single slide. If not given or empty, all slides are rendered.
	 * @param width
	 *        The width of the images in pixels, between 16 and 8000.
	 * @return The list of PNG images.
	 */
	@Label("Render slides")
	@SideEffectFree
	public static List<BinaryData> render(
			@Mandatory BinaryDataSource presentation,
			List<Integer> slides,
			@IntDefault(DEFAULT_WIDTH) int width) {
		checkWidth(width);
		try (XMLSlideShow slideShow = PptTemplateFunctions.open(presentation)) {
			List<XSLFSlide> all = slideShow.getSlides();
			List<Integer> indices = indices(slides, all.size(), I18NConstants.ERROR_NO_SUCH_SLIDE__INDEX_COUNT);
			return render(presentation, indices, width, "slide", all::get);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * Renders slide layouts of a PowerPoint template into images.
	 *
	 * <p>
	 * For each requested layout, a slide using this layout is rendered. Its placeholders show their
	 * prompt text, or their type name (e.g. <code>body</code>) if they have none, so that the
	 * position and the text style of each placeholder become visible. The layouts are numbered in
	 * the order of the <code>layouts</code> entry of <code>pptAnalyze()</code>.
	 * </p>
	 *
	 * <p>
	 * The result contains a PNG image for each requested layout in the requested order. An image is
	 * named after the layout index, e.g. <code>layout-11.png</code>.
	 * </p>
	 *
	 * <p>
	 * The sample slides of a template are rendered by <code>pptRender()</code>.
	 * </p>
	 *
	 * @param template
	 *        The PowerPoint file (<code>.pptx</code> or <code>.potx</code>) whose layouts to render.
	 * @param layouts
	 *        The indices of the layouts to render, the first layout has index 0. A single number
	 *        renders a single layout. If not given or empty, all layouts are rendered.
	 * @param width
	 *        The width of the images in pixels, between 16 and 8000.
	 * @return The list of PNG images.
	 */
	@Label("Render slide layouts")
	@SideEffectFree
	public static List<BinaryData> renderLayouts(
			@Mandatory BinaryDataSource template,
			List<Integer> layouts,
			@IntDefault(DEFAULT_WIDTH) int width) {
		checkWidth(width);
		try (XMLSlideShow slideShow = PptTemplateFunctions.open(template)) {
			List<XSLFSlideLayout> all = TemplateAnalysis.layouts(slideShow);
			List<Integer> indices = indices(layouts, all.size(), I18NConstants.ERROR_NO_SUCH_LAYOUT__INDEX_COUNT);
			return render(template, indices, width, "layout",
				index -> SlideRenderer.createLayoutPreview(slideShow, all.get(index)));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static List<BinaryData> render(BinaryDataSource source, List<Integer> indices, int width,
			String namePrefix, IntFunction<XSLFSlide> slides) {
		SlideRenderer renderer = new SlideRenderer();
		List<BinaryData> result = new ArrayList<>(indices.size());
		for (Integer index : indices) {
			int n = index.intValue();
			String name = String.format("%s-%02d.png", namePrefix, Integer.valueOf(n));
			result.add(SlideRenderer.toPng(renderer.render(slides.apply(n), width), name));
		}
		if (!renderer.getMissingFonts().isEmpty()) {
			Logger.warn("Fonts used in '" + source.getName() + "' are not installed and were replaced by a "
				+ "standard font: " + String.join(", ", renderer.getMissingFonts()), PptRenderFunctions.class);
		}
		return result;
	}

	private static void checkWidth(int width) {
		if (!SlideRenderer.isValidWidth(width)) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_IMAGE_WIDTH__WIDTH_MIN_MAX.fill(width,
				SlideRenderer.MIN_WIDTH, SlideRenderer.MAX_WIDTH));
		}
	}

	/**
	 * The requested indices, all indices if none are given.
	 */
	private static List<Integer> indices(List<Integer> requested, int count, ResKey2 error) {
		if (requested == null || requested.isEmpty()) {
			List<Integer> result = new ArrayList<>(count);
			for (int n = 0; n < count; n++) {
				result.add(Integer.valueOf(n));
			}
			return result;
		}
		for (Integer index : requested) {
			if (index == null || index.intValue() < 0 || index.intValue() >= count) {
				throw new TopLogicException(error.fill(index, count));
			}
		}
		return requested;
	}

}

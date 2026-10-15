/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.presentation.data.PresentationData;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions for inspecting PowerPoint templates.
 *
 * <p>
 * All lengths are given in points (1/72 inch).
 * </p>
 */
@ScriptPrefix("ppt")
public class PptTemplateFunctions extends TLScriptFunctions {

	/**
	 * Describes the structure and the style of a PowerPoint presentation or template.
	 *
	 * <p>
	 * The result tells which slide layouts a template offers, where their placeholders are and how
	 * their texts look, and which colors, fonts and shape styles the template uses. It consists of
	 * dictionaries, lists, texts, numbers and truth values only, so it can be converted to JSON.
	 * Entries without a value are left out. All lengths are given in points, colors as six
	 * hexadecimal digits <code>RRGGBB</code>.
	 * </p>
	 *
	 * <p>
	 * The result is a dictionary with the following entries:
	 * </p>
	 *
	 * <ul>
	 * <li>{@link PresentationData#SLIDE_SIZE}: The width and height of a slide.</li>
	 * <li>{@link PresentationData#MASTERS}: The slide masters with their theme (theme colors and
	 * fonts), their background, their placeholders and their other shapes such as logos.</li>
	 * <li>{@link PresentationData#LAYOUTS}: The slide layouts of all masters in the order PowerPoint
	 * shows them. Each layout has its position in this list, its name, its predefined type (e.g.
	 * <code>TITLE</code>, if it is no custom layout), its master, its background, its placeholders
	 * and its other shapes. A placeholder is described by its type (e.g. <code>title</code>,
	 * <code>body</code>), its index, its effective position and size, the style of its text and its
	 * prompt text.</li>
	 * <li>{@link PresentationData#SLIDES}: The slides of the presentation with their layout and their
	 * shapes. A shape is described by its kind, name, position and size, geometry, fill, outline,
	 * shadow and text. Shapes of a group are listed as its children.</li>
	 * </ul>
	 *
	 * <p>
	 * A color taken from the theme names the theme color it refers to together with the
	 * modifications applied to it (e.g. brightness or transparency), so that a shape can be created
	 * that follows the template's color scheme.
	 * </p>
	 *
	 * @param template
	 *        The PowerPoint file (<code>.pptx</code> or <code>.potx</code>) to analyze.
	 * @return The dictionary describing the template.
	 */
	@Label("Analyze presentation template")
	@SideEffectFree
	public static Map<String, Object> analyze(@Mandatory BinaryDataSource template) {
		try (XMLSlideShow slideShow = open(template)) {
			return TemplateAnalysis.analyze(slideShow).toData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * Reads the given PowerPoint file.
	 *
	 * @throws TopLogicException
	 *         If the data is not a PowerPoint presentation or template.
	 */
	static XMLSlideShow open(BinaryDataSource data) {
		try (InputStream in = data.toData().getStream()) {
			return new XMLSlideShow(in);
		} catch (IOException | RuntimeException ex) {
			// POI reports a malformed package with various unchecked exceptions.
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_PRESENTATION__NAME.fill(data.getName()), ex);
		}
	}

}

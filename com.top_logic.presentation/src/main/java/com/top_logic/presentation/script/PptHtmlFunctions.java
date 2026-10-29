/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import static com.top_logic.presentation.data.PresentationData.*;

import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.html.HtmlConverter;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions turning structured text (HTML) into slide content.
 *
 * @see HtmlConverter
 */
@ScriptPrefix("ppt")
public class PptHtmlFunctions extends TLScriptFunctions {

	/**
	 * Describes the content of an HTML text, e.g. the value of an HTML attribute, as slide content
	 * that continues on further slides when it is long.
	 *
	 * <p>
	 * Example: <code>pptContent("#3 Acceptance test", null,
	 * pptHtml($ticket.get(`my.module:Ticket#description`)))</code>
	 * </p>
	 *
	 * <p>
	 * The result is a column (<code>pptColumn()</code> with <code>split: true</code>) of the blocks
	 * of the text in their order: Paragraphs, headings, lists and quotations form text boxes, a
	 * <code>table</code> becomes a table (<code>pptTable()</code>), an <code>img</code> an image
	 * (<code>pptImage()</code>).
	 * </p>
	 *
	 * <ul>
	 * <li>Headings <code>h1</code> to <code>h6</code> are bold and larger than the base size.
	 * <code>b</code>/<code>strong</code>, <code>i</code>/<code>em</code>, <code>u</code> and
	 * links (<code>a href</code>) keep their formatting, <code>code</code> and <code>pre</code>
	 * are shown in a monospace font, <code>br</code> breaks the line.</li>
	 * <li>The items of a list (<code>ul</code>, <code>ol</code>, nested) get bullets, the items of
	 * an ordered list numbers (also with <code>start</code>, <code>type</code> and
	 * <code>value</code>); nested lists and quotations (<code>blockquote</code>) are indented.</li>
	 * <li>The leading table rows in <code>thead</code> or of <code>th</code> cells are header
	 * rows, repeated on continuation slides. A cell spanning several columns or rows is followed by
	 * empty cells.</li>
	 * <li>An image refers to an image of the structured text by its name, or is a
	 * <code>data:</code> URI. It is as wide as its <code>width</code> attribute (in pixels) or the
	 * image itself, at most as wide as the column, with its aspect ratio. An image that cannot be
	 * found is left out.</li>
	 * <li>The text of other elements is shown as text.</li>
	 * </ul>
	 *
	 * @param html
	 *        The HTML text: the value of an HTML attribute (with its images), or a string of HTML.
	 *        Without value, the result has no content.
	 * @param font
	 *        The font family of the text.
	 * @param size
	 *        The font size of normal text; headings are larger. The default is 16.
	 * @param color
	 *        The text color.
	 * @param gap
	 *        The vertical distance between text, tables and images. If not given, the
	 *        <code>gap</code> of the template profile is used.
	 * @return The dictionary describing the content.
	 */
	@Label("Describe HTML content")
	@SideEffectFree
	public static Map<String, Object> html(
			Object html,
			String font,
			Double size,
			Object color,
			Double gap) {
		String source;
		Map<String, BinaryData> images;
		Object text = html instanceof I18NStructuredText i18n ? i18n.localize() : html;
		if (text == null) {
			source = null;
			images = Map.of();
		} else if (text instanceof StructuredText structured) {
			source = structured.getSourceCode();
			images = structured.getImages();
		} else if (text instanceof CharSequence string) {
			source = string.toString();
			images = Map.of();
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NO_HTML__VALUE.fill(text));
		}
		Map<String, Object> style = PptDescriptionFunctions.map();
		PptDescriptionFunctions.put(style, FONT, font);
		PptDescriptionFunctions.put(style, COLOR, color);
		return HtmlConverter.convert(source, images, style, size, gap);
	}

}

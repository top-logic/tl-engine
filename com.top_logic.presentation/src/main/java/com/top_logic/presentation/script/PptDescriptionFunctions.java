/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import static com.top_logic.presentation.data.PresentationData.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.DescriptionReader;
import com.top_logic.presentation.model.Value;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.presentation.template.PaintInfo;

/**
 * TL-Script functions describing the slides of a PowerPoint presentation.
 *
 * <p>
 * A presentation is described as data: Each function creates a dictionary that describes a slide,
 * an element on a slide (shape, text box, table, image), a text part or a style. The functions
 * only assemble these dictionaries, arguments without a value are left out. The same dictionaries
 * can be written by hand, stored, or exchanged as JSON with <code>toJson()</code> and
 * <code>parseJson()</code>. The description is checked completely when the presentation is
 * created.
 * </p>
 *
 * <p>
 * All lengths are given in points (1/72 inch). Positions are relative to the top left corner of
 * the slide.
 * </p>
 *
 * <p>
 * Wherever a text is expected, a string is accepted (each line is a paragraph), as well as a
 * paragraph (<code>pptParagraph()</code>), a text run (<code>pptRun()</code>) or a list of those.
 * Wherever a color is expected, a theme color name (e.g. <code>accent1</code>, <code>tx1</code>,
 * <code>bg1</code>, <code>dk2</code>), a hexadecimal color (<code>RRGGBB</code> or
 * <code>#RRGGBB</code>), a color role name or a color dictionary (<code>pptColor()</code>, or a
 * color reported by <code>pptAnalyze()</code>) is accepted.
 * </p>
 */
@ScriptPrefix("ppt")
public class PptDescriptionFunctions extends TLScriptFunctions {

	/**
	 * Describes a slide.
	 *
	 * <p>
	 * The slide is created on a slide layout of the template. Its title and subtitle fill the
	 * title and subtitle placeholders of the layout, further placeholders are filled by their
	 * index or name. The content elements are placed on the slide: An element with a position
	 * (<code>x</code> or <code>y</code>) is placed there. The elements without position are
	 * arranged one below the other in the content area of the layout, like the children of a
	 * column (<code>pptColumn()</code>); a single one fills the content area.
	 * </p>
	 *
	 * @param layout
	 *        The slide layout: its index (as in the <code>layouts</code> of
	 *        <code>pptAnalyze()</code>), its name, or its role <code>title</code>,
	 *        <code>content</code>, <code>agenda</code>, <code>section</code> or
	 *        <code>closing</code>. If not given, the content layout is used.
	 * @param title
	 *        The text of the title placeholder.
	 * @param subtitle
	 *        The text of the subtitle placeholder. On a slide layout without subtitle placeholder,
	 *        the subtitle is shown as first content element.
	 * @param content
	 *        The elements on the slide: shapes, text boxes, tables, images, rows and columns.
	 * @param notes
	 *        The speaker notes as text, or a list of lines.
	 * @param placeholders
	 *        Texts of further placeholders: a dictionary from the placeholder index (as in the
	 *        <code>placeholders</code> of a layout in <code>pptAnalyze()</code>) or the placeholder
	 *        name to its text.
	 * @param hidden
	 *        Whether the slide is hidden in the slide show.
	 * @return The dictionary describing the slide.
	 */
	@Label("Describe a slide")
	@SideEffectFree
	public static Map<String, Object> slide(
			Object layout,
			Object title,
			Object subtitle,
			List<?> content,
			Object notes,
			Map<?, ?> placeholders,
			Boolean hidden) {
		Map<String, Object> result = typed(TYPE_SLIDE);
		put(result, LAYOUT, layout);
		put(result, TITLE, title);
		put(result, SUBTITLE, subtitle);
		putList(result, PLACEHOLDERS, placeholders == null || placeholders.isEmpty() ? null : placeholders);
		putList(result, CONTENT, content);
		put(result, NOTES, notes);
		put(result, HIDDEN, hidden);
		return result;
	}

	/**
	 * Describes a geometric shape, optionally with text.
	 *
	 * <p>
	 * Example: <code>pptShape(geometry: "roundRect", x: 30, y: 130, width: 300, height: 200,
	 * fill: "accent1", text: "Hello", color: "bg1")</code>
	 * </p>
	 *
	 * <p>
	 * The shape shows only what is described: without <code>fill</code> it is not filled, without
	 * <code>line</code> it has no outline, and its text has the text color of the template unless
	 * <code>color</code> is given. The text is centered horizontally and vertically unless
	 * <code>align</code> or <code>anchor</code> say otherwise.
	 * </p>
	 *
	 * <p>
	 * The formatting arguments (font, size, color, ...) apply to the whole text, unless a
	 * paragraph or text run defines its own formatting.
	 * </p>
	 *
	 * @param geometry
	 *        The name of the preset geometry, e.g. <code>rect</code> (the default),
	 *        <code>roundRect</code>, <code>ellipse</code>, <code>triangle</code>,
	 *        <code>rightArrow</code>, <code>chevron</code> or <code>line</code>.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of its text at its width.
	 * @param weight
	 *        The share in the free space of a row or column (<code>pptRow()</code>,
	 *        <code>pptColumn()</code>) or of the content area, if no own size is given. The
	 *        default is 1.
	 * @param fill
	 *        The fill: a color, <code>none</code>, or a gradient (<code>pptGradient()</code>). If
	 *        not given, the shape is not filled.
	 * @param line
	 *        The outline: a color, <code>none</code>, or an outline description
	 *        (<code>pptLine()</code>), drawn in the text color <code>tx1</code> if it gives no
	 *        color. If not given, the shape has no outline.
	 * @param shadow
	 *        The shadow: <code>true</code> for the default shadow or a shadow description
	 *        (<code>pptShadow()</code>).
	 * @param text
	 *        The text in the shape.
	 * @param cornerRadius
	 *        The corner radius of a <code>roundRect</code>. If given without geometry, the shape
	 *        is a <code>roundRect</code>.
	 * @param adjust
	 *        Adjust values of the preset geometry by their names (e.g. <code>adj</code>), as given
	 *        in the file (100000 for 100%).
	 * @param font
	 *        The font family.
	 * @param size
	 *        The font size.
	 * @param color
	 *        The text color.
	 * @param bold
	 *        Whether the text is bold.
	 * @param italic
	 *        Whether the text is italic.
	 * @param underline
	 *        Whether the text is underlined.
	 * @param caps
	 *        Whether the text is shown in capital letters.
	 * @param spacing
	 *        Additional space between characters, negative to condense the text.
	 * @param align
	 *        The horizontal alignment of the text: <code>left</code>, <code>center</code>,
	 *        <code>right</code> or <code>justify</code>.
	 * @param anchor
	 *        The vertical alignment of the text: <code>top</code>, <code>middle</code> or
	 *        <code>bottom</code>.
	 * @param insets
	 *        The inner margins: a number for all sides or a dictionary with the entries
	 *        <code>left</code>, <code>top</code>, <code>right</code> and <code>bottom</code>.
	 * @param overflow
	 *        What happens to a text that does not fit into the shape: <code>shrink</code> (the
	 *        default) reduces the font size, but not below the <code>minFontSize</code> of the
	 *        template profile, and cuts the text off with "…" if it still does not fit;
	 *        <code>ellipsis</code> cuts the text off without reducing the font size;
	 *        <code>visible</code> lets the text exceed the shape; <code>error</code> stops the
	 *        creation of the presentation.
	 * @param split
	 *        Whether paragraphs that do not fit into the shape continue on a further slide
	 *        repeating the title of the slide. Only one element of a slide can be continued.
	 * @param wrap
	 *        Whether the text is wrapped at the shape border.
	 * @param style
	 *        The name of a style of the template profile (e.g. <code>card</code>), giving the
	 *        properties the shape does not give itself.
	 * @param role
	 *        The color role of the shape (e.g. <code>topicA</code> or <code>risk</code>): within
	 *        the shape and its style, the color role <code>accent</code> stands for this role.
	 * @return The dictionary describing the shape.
	 */
	@Label("Describe a shape")
	@SideEffectFree
	public static Map<String, Object> shape(
			String geometry,
			Double x,
			Double y,
			Double width,
			Object height,
			Double weight,
			Object fill,
			Object line,
			Object shadow,
			Object text,
			Double cornerRadius,
			Map<?, ?> adjust,
			String font,
			Double size,
			Object color,
			Boolean bold,
			Boolean italic,
			Boolean underline,
			Boolean caps,
			Double spacing,
			String align,
			String anchor,
			Object insets,
			String overflow,
			Boolean split,
			Boolean wrap,
			String style,
			String role) {
		Map<String, Object> result = typed(TYPE_SHAPE);
		putBounds(result, x, y, width, height, weight);
		if (cornerRadius != null || (adjust != null && !adjust.isEmpty())) {
			Map<String, Object> geometryData = map();
			put(geometryData, PRESET, geometry);
			putList(geometryData, ADJUST, adjust == null || adjust.isEmpty() ? null : adjust);
			put(geometryData, CORNER_RADIUS, cornerRadius);
			result.put(GEOMETRY, geometryData);
		} else {
			put(result, GEOMETRY, geometry);
		}
		put(result, FILL, fill);
		put(result, LINE, line);
		put(result, SHADOW, shadow);
		putText(result, text, font, size, color, bold, italic, underline, caps, spacing, align, anchor, insets);
		put(result, OVERFLOW, overflow);
		put(result, SPLIT, split);
		put(result, WRAP, wrap);
		put(result, STYLE, style);
		put(result, ROLE, role);
		return result;
	}

	/**
	 * Describes a text box: a text without fill and outline.
	 *
	 * <p>
	 * Unless given otherwise, the text box has no inner margins, so that the text starts exactly
	 * at its position.
	 * </p>
	 *
	 * @param text
	 *        The text.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of its text at its width.
	 * @param weight
	 *        The share in the free space of a row or column (<code>pptRow()</code>,
	 *        <code>pptColumn()</code>) or of the content area, if no own size is given. The
	 *        default is 1.
	 * @param font
	 *        The font family.
	 * @param size
	 *        The font size.
	 * @param color
	 *        The text color.
	 * @param bold
	 *        Whether the text is bold.
	 * @param italic
	 *        Whether the text is italic.
	 * @param underline
	 *        Whether the text is underlined.
	 * @param caps
	 *        Whether the text is shown in capital letters.
	 * @param spacing
	 *        Additional space between characters, negative to condense the text.
	 * @param align
	 *        The horizontal alignment: <code>left</code>, <code>center</code>, <code>right</code>
	 *        or <code>justify</code>.
	 * @param anchor
	 *        The vertical alignment: <code>top</code>, <code>middle</code> or
	 *        <code>bottom</code>.
	 * @param insets
	 *        The inner margins: a number for all sides or a dictionary with the entries
	 *        <code>left</code>, <code>top</code>, <code>right</code> and <code>bottom</code>.
	 * @param overflow
	 *        What happens to a text that does not fit into the box: <code>shrink</code> (the
	 *        default) reduces the font size, but not below the <code>minFontSize</code> of the
	 *        template profile, and cuts the text off with "…" if it still does not fit;
	 *        <code>ellipsis</code> cuts the text off without reducing the font size;
	 *        <code>visible</code> lets the text exceed the box; <code>error</code> stops the
	 *        creation of the presentation.
	 * @param split
	 *        Whether paragraphs that do not fit into the box continue on a further slide repeating
	 *        the title of the slide, e.g. for a long list of bullet points. Only one element of a
	 *        slide can be continued.
	 * @param wrap
	 *        Whether the text is wrapped at the box border.
	 * @param role
	 *        The color role of the text box: within the text, the color role <code>accent</code>
	 *        stands for this role.
	 * @return The dictionary describing the text box.
	 */
	@Label("Describe a text box")
	@SideEffectFree
	public static Map<String, Object> textBox(
			@Mandatory Object text,
			Double x,
			Double y,
			Double width,
			Object height,
			Double weight,
			String font,
			Double size,
			Object color,
			Boolean bold,
			Boolean italic,
			Boolean underline,
			Boolean caps,
			Double spacing,
			String align,
			String anchor,
			Object insets,
			String overflow,
			Boolean split,
			Boolean wrap,
			String role) {
		Map<String, Object> result = typed(TYPE_TEXT_BOX);
		putBounds(result, x, y, width, height, weight);
		putText(result, text, font, size, color, bold, italic, underline, caps, spacing, align, anchor, insets);
		put(result, OVERFLOW, overflow);
		put(result, SPLIT, split);
		put(result, WRAP, wrap);
		put(result, ROLE, role);
		return result;
	}

	/**
	 * Describes a table.
	 *
	 * <p>
	 * Example: <code>pptTable(rows: [["Name", "Value"], ["A", 1], ["B", pptCell("2", fill:
	 * "accent2")]], headerRows: 1, columnWeights: [2, 1])</code>
	 * </p>
	 *
	 * <p>
	 * The rows grow with their text. Rows that do not fit into the height of the table continue
	 * on further slides, which repeat the header rows and the title of the slide extended by the
	 * <code>continuationSuffix</code> of the template profile. On these slides, the table fills the
	 * content area. Only one element of a slide can be continued.
	 * </p>
	 *
	 * @param rows
	 *        The rows of the table, each a list of cells with the same number of cells. A cell is
	 *        a text or a cell description (<code>pptCell()</code>).
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of its rows at its width.
	 * @param weight
	 *        The share in the free space of a row or column (<code>pptRow()</code>,
	 *        <code>pptColumn()</code>) or of the content area, if no own size is given. The
	 *        default is 1.
	 * @param columnWeights
	 *        The relative widths of the columns, distributing the width of the table. If neither
	 *        weights nor widths are given, all columns have the same width.
	 * @param columnWidths
	 *        The widths of the columns. Not to be given together with weights.
	 * @param headerRows
	 *        The number of rows at the top formatted as header.
	 * @param rowHeight
	 *        The minimum height of a row.
	 * @return The dictionary describing the table.
	 */
	@Label("Describe a table")
	@SideEffectFree
	public static Map<String, Object> table(
			@Mandatory List<?> rows,
			Double x,
			Double y,
			Double width,
			Object height,
			Double weight,
			List<?> columnWeights,
			List<?> columnWidths,
			Integer headerRows,
			Double rowHeight) {
		Map<String, Object> result = typed(TYPE_TABLE);
		putBounds(result, x, y, width, height, weight);
		putList(result, COLUMN_WEIGHTS, columnWeights);
		putList(result, COLUMN_WIDTHS, columnWidths);
		result.put(ROWS, rows);
		put(result, HEADER_ROWS, headerRows);
		put(result, ROW_HEIGHT, rowHeight);
		return result;
	}

	/**
	 * Describes a table cell with its own formatting.
	 *
	 * @param text
	 *        The text of the cell.
	 * @param fill
	 *        The fill of the cell: a color or <code>none</code>.
	 * @param font
	 *        The font family.
	 * @param size
	 *        The font size.
	 * @param color
	 *        The text color.
	 * @param bold
	 *        Whether the text is bold.
	 * @param italic
	 *        Whether the text is italic.
	 * @param underline
	 *        Whether the text is underlined.
	 * @param caps
	 *        Whether the text is shown in capital letters.
	 * @param spacing
	 *        Additional space between characters, negative to condense the text.
	 * @param align
	 *        The horizontal alignment: <code>left</code>, <code>center</code>, <code>right</code>
	 *        or <code>justify</code>.
	 * @param anchor
	 *        The vertical alignment: <code>top</code>, <code>middle</code> or
	 *        <code>bottom</code>.
	 * @param insets
	 *        The inner margins: a number for all sides or a dictionary with the entries
	 *        <code>left</code>, <code>top</code>, <code>right</code> and <code>bottom</code>.
	 * @param overflow
	 *        What happens to a word that is wider than the column: <code>shrink</code> (the
	 *        default) reduces the font size, but not below the <code>minFontSize</code> of the
	 *        template profile, and cuts the text off with "…" if the word still does not fit;
	 *        <code>ellipsis</code> cuts the text off without reducing the font size;
	 *        <code>visible</code> breaks the word; <code>error</code> stops the creation of the
	 *        presentation.
	 * @return The dictionary describing the cell.
	 */
	@Label("Describe a table cell")
	@SideEffectFree
	public static Map<String, Object> cell(
			Object text,
			Object fill,
			String font,
			Double size,
			Object color,
			Boolean bold,
			Boolean italic,
			Boolean underline,
			Boolean caps,
			Double spacing,
			String align,
			String anchor,
			Object insets,
			String overflow) {
		Map<String, Object> result = map();
		putText(result, text, font, size, color, bold, italic, underline, caps, spacing, align, anchor, insets);
		put(result, OVERFLOW, overflow);
		put(result, FILL, fill);
		return result;
	}

	/**
	 * Describes an image.
	 *
	 * @param data
	 *        The image, e.g. a PNG, JPEG or SVG file.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of the image keeping its aspect ratio at its width.
	 * @param weight
	 *        The share in the free space of a row or column (<code>pptRow()</code>,
	 *        <code>pptColumn()</code>) or of the content area, if no own size is given. The
	 *        default is 1.
	 * @param fit
	 *        How the image is fitted into its bounds: <code>contain</code> (the default) shows the
	 *        whole image as large as possible, <code>cover</code> fills the bounds and cuts off
	 *        what exceeds them, <code>stretch</code> distorts the image to the bounds.
	 * @return The dictionary describing the image.
	 */
	@Label("Describe an image")
	@SideEffectFree
	public static Map<String, Object> image(
			@Mandatory BinaryDataSource data,
			Double x,
			Double y,
			Double width,
			Object height,
			Double weight,
			String fit) {
		Map<String, Object> result = typed(TYPE_IMAGE);
		putBounds(result, x, y, width, height, weight);
		result.put(DATA, data);
		put(result, FIT, fit);
		return result;
	}

	/**
	 * Describes a row: elements arranged side by side.
	 *
	 * <p>
	 * Example: three cards of equal width, the middle one twice as wide:
	 * <code>pptRow([pptShape(fill: "accent1", text: "A"), pptShape(fill: "accent2", text: "B",
	 * weight: 2), pptShape(fill: "accent3", text: "C")])</code>
	 * </p>
	 *
	 * <p>
	 * A child with a <code>width</code> gets this width. The other children share the remaining
	 * width by their <code>weight</code> (default 1), or by the <code>weights</code> of the row.
	 * Vertically, a child without own height is stretched to the height of the row, unless
	 * <code>align</code> is given; then it gets the height of its content. Children have no
	 * position of their own. Rows and columns can be nested arbitrarily.
	 * </p>
	 *
	 * @param children
	 *        The elements of the row: shapes, text boxes, tables, images, rows and columns.
	 * @param gap
	 *        The horizontal distance between two children. If not given, the <code>gap</code> of
	 *        the template profile is used.
	 * @param weights
	 *        The shares of the children in the remaining width, one number for each child.
	 * @param align
	 *        The vertical alignment of the children: <code>stretch</code> (the default),
	 *        <code>start</code>, <code>center</code> or <code>end</code>.
	 * @param x
	 *        The horizontal position of the left border. If neither <code>x</code> nor
	 *        <code>y</code> is given, the row is arranged with the other content of the slide.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of the highest child.
	 * @param weight
	 *        The share in the free space of an enclosing row or column, or of the content area.
	 *        The default is 1.
	 * @return The dictionary describing the row.
	 */
	@Label("Describe a row")
	@SideEffectFree
	public static Map<String, Object> row(
			@Mandatory List<?> children,
			Double gap,
			List<?> weights,
			String align,
			Double x,
			Double y,
			Double width,
			Object height,
			Double weight) {
		return container(TYPE_ROW, children, gap, weights, align, x, y, width, height, weight);
	}

	/**
	 * Describes a column: elements arranged one below the other.
	 *
	 * <p>
	 * Example: a text of its natural height above a table taking the rest:
	 * <code>pptColumn([pptTextBox("Summary", height: "auto"), pptTable($rows, headerRows:
	 * 1)])</code>
	 * </p>
	 *
	 * <p>
	 * A child with a <code>height</code> gets this height, a child with height <code>auto</code>
	 * the height of its content at the width of the column. The other children share the
	 * remaining height by their <code>weight</code> (default 1), or by the <code>weights</code> of
	 * the column. Horizontally, a child without own width takes the width of the column. Children
	 * have no position of their own. Rows and columns can be nested arbitrarily.
	 * </p>
	 *
	 * <p>
	 * Several content elements of a slide without position are arranged as in a column.
	 * </p>
	 *
	 * @param children
	 *        The elements of the column: shapes, text boxes, tables, images, rows and columns.
	 * @param gap
	 *        The vertical distance between two children. If not given, the <code>gap</code> of
	 *        the template profile is used.
	 * @param weights
	 *        The shares of the children in the remaining height, one number for each child.
	 * @param align
	 *        The horizontal alignment of children with own width: <code>stretch</code> (the
	 *        default) and <code>start</code> align them left, <code>center</code> centers them,
	 *        <code>end</code> aligns them right.
	 * @param x
	 *        The horizontal position of the left border. If neither <code>x</code> nor
	 *        <code>y</code> is given, the column is arranged with the other content of the slide.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of its children.
	 * @param weight
	 *        The share in the free space of an enclosing row or column, or of the content area.
	 *        The default is 1.
	 * @return The dictionary describing the column.
	 */
	@Label("Describe a column")
	@SideEffectFree
	public static Map<String, Object> column(
			@Mandatory List<?> children,
			Double gap,
			List<?> weights,
			String align,
			Double x,
			Double y,
			Double width,
			Object height,
			Double weight) {
		return container(TYPE_COLUMN, children, gap, weights, align, x, y, width, height, weight);
	}

	private static Map<String, Object> container(String type, List<?> children, Double gap, List<?> weights,
			String align, Double x, Double y, Double width, Object height, Double weight) {
		Map<String, Object> result = typed(type);
		putBounds(result, x, y, width, height, weight);
		result.put(CHILDREN, children);
		put(result, GAP, gap);
		putList(result, WEIGHTS, weights);
		put(result, ALIGN, align);
		return result;
	}

	/**
	 * Describes a paragraph of a text.
	 *
	 * <p>
	 * The formatting arguments (font, size, color, ...) apply to all runs of the paragraph, unless
	 * a run defines its own formatting.
	 * </p>
	 *
	 * @param runs
	 *        The content of the paragraph: a string, a text run (<code>pptRun()</code>) or a list
	 *        of those.
	 * @param align
	 *        The horizontal alignment: <code>left</code>, <code>center</code>, <code>right</code>
	 *        or <code>justify</code>.
	 * @param level
	 *        The indentation level from 0 (the default) to 8.
	 * @param bullet
	 *        The bullet: <code>true</code> or <code>false</code> to switch the bullet of the
	 *        placeholder on or off, <code>none</code>, the bullet character, or a dictionary with
	 *        the entries <code>char</code> and <code>color</code>.
	 * @param spaceBefore
	 *        The space before the paragraph.
	 * @param spaceAfter
	 *        The space after the paragraph.
	 * @param lineSpacing
	 *        The line spacing as multiple of the single line height, e.g. 1.2.
	 * @param font
	 *        The font family.
	 * @param size
	 *        The font size.
	 * @param color
	 *        The text color.
	 * @param bold
	 *        Whether the text is bold.
	 * @param italic
	 *        Whether the text is italic.
	 * @param underline
	 *        Whether the text is underlined.
	 * @param caps
	 *        Whether the text is shown in capital letters.
	 * @param spacing
	 *        Additional space between characters, negative to condense the text.
	 * @return The dictionary describing the paragraph.
	 */
	@Label("Describe a paragraph")
	@SideEffectFree
	public static Map<String, Object> paragraph(
			@Mandatory Object runs,
			String align,
			Integer level,
			Object bullet,
			Double spaceBefore,
			Double spaceAfter,
			Double lineSpacing,
			String font,
			Double size,
			Object color,
			Boolean bold,
			Boolean italic,
			Boolean underline,
			Boolean caps,
			Double spacing) {
		Map<String, Object> result = map();
		result.put(RUNS, runs);
		putStyle(result, font, size, color, bold, italic, underline, caps, spacing);
		put(result, ALIGN, align);
		put(result, LEVEL, level);
		put(result, BULLET, bullet);
		put(result, SPACE_BEFORE, spaceBefore);
		put(result, SPACE_AFTER, spaceAfter);
		put(result, LINE_SPACING, lineSpacing);
		return result;
	}

	/**
	 * Describes a part of a paragraph with its own formatting.
	 *
	 * <p>
	 * Example: <code>pptParagraph([pptRun("Note: ", bold: true), "the rest"])</code>
	 * </p>
	 *
	 * @param text
	 *        The text. A line break breaks the line without starting a new paragraph.
	 * @param font
	 *        The font family.
	 * @param size
	 *        The font size.
	 * @param color
	 *        The text color.
	 * @param bold
	 *        Whether the text is bold.
	 * @param italic
	 *        Whether the text is italic.
	 * @param underline
	 *        Whether the text is underlined.
	 * @param caps
	 *        Whether the text is shown in capital letters.
	 * @param spacing
	 *        Additional space between characters, negative to condense the text.
	 * @param hyperlink
	 *        The URL the text links to.
	 * @return The dictionary describing the text run.
	 */
	@Label("Describe a text run")
	@SideEffectFree
	public static Map<String, Object> run(
			@Mandatory String text,
			String font,
			Double size,
			Object color,
			Boolean bold,
			Boolean italic,
			Boolean underline,
			Boolean caps,
			Double spacing,
			String hyperlink) {
		Map<String, Object> result = map();
		result.put(TEXT, text);
		putStyle(result, font, size, color, bold, italic, underline, caps, spacing);
		put(result, HYPERLINK, hyperlink);
		return result;
	}

	/**
	 * Describes a color modified in brightness, saturation or transparency.
	 *
	 * <p>
	 * Example: <code>pptColor("accent1", lumMod: 0.75)</code> is the first accent color of the
	 * theme, darkened to 75% brightness. All modifications are given as fractions, e.g. 0.75 for
	 * 75%.
	 * </p>
	 *
	 * <p>
	 * The result is a dictionary with the entry {@link PresentationData#SCHEME} (theme color),
	 * {@link PresentationData#RGB} (fixed color) or {@link PresentationData#ROLE} (color role) and
	 * the given modifications.
	 * </p>
	 *
	 * @param color
	 *        The base color: a theme color name, a hexadecimal color, a color role name or a color
	 *        dictionary, whose modifications are kept unless given here.
	 * @param lumMod
	 *        Factor for the brightness, e.g. 0.75 to darken.
	 * @param lumOff
	 *        Offset for the brightness between -1 and 1, e.g. 0.4 together with a
	 *        <code>lumMod</code> of 0.6 to lighten.
	 * @param alpha
	 *        The opacity between 0 (transparent) and 1 (opaque).
	 * @param tint
	 *        Mixes the color with white: 1 keeps the color, 0 is white.
	 * @param shade
	 *        Mixes the color with black: 1 keeps the color, 0 is black.
	 * @param satMod
	 *        Factor for the saturation.
	 * @param satOff
	 *        Offset for the saturation between -1 and 1.
	 * @return The dictionary describing the color.
	 */
	@Label("Describe a color")
	@SideEffectFree
	public static Map<String, Object> color(
			@Mandatory Object color,
			Double lumMod,
			Double lumOff,
			Double alpha,
			Double tint,
			Double shade,
			Double satMod,
			Double satOff) {
		Map<String, Object> result = DescriptionReader.readColor(new Value(color, "color")).toData();
		put(result, ColorInfo.LUM_MOD, lumMod);
		put(result, ColorInfo.LUM_OFF, lumOff);
		put(result, ColorInfo.ALPHA, alpha);
		put(result, ColorInfo.TINT, tint);
		put(result, ColorInfo.SHADE, shade);
		put(result, ColorInfo.SAT_MOD, satMod);
		put(result, ColorInfo.SAT_OFF, satOff);
		return result;
	}

	/**
	 * Describes a color gradient to fill a shape.
	 *
	 * <p>
	 * Example: <code>pptGradient(["accent1", "accent2"], angle: 90)</code> runs from the first
	 * accent color at the top to the second at the bottom.
	 * </p>
	 *
	 * @param stops
	 *        The colors along the gradient, at least two. A color is placed at an even distance
	 *        from its neighbors, unless given as dictionary with the entries
	 *        {@link PresentationData#POSITION} (between 0 and 1) and
	 *        {@link PresentationData#COLOR}.
	 * @param angle
	 *        The direction of a linear gradient in degrees, clockwise with 0 (the default) running
	 *        from left to right.
	 * @param path
	 *        The shape of a gradient spreading from the center: <code>circle</code>,
	 *        <code>rect</code> or <code>shape</code>. Not to be given together with an angle.
	 * @return The dictionary describing the gradient.
	 */
	@Label("Describe a color gradient")
	@SideEffectFree
	public static Map<String, Object> gradient(
			@Mandatory List<?> stops,
			Double angle,
			String path) {
		Map<String, Object> result = typed(PaintInfo.Type.GRADIENT.id());
		result.put(STOPS, stops);
		put(result, ANGLE, angle);
		put(result, PATH, path);
		return result;
	}

	/**
	 * Describes the outline of a shape.
	 *
	 * @param color
	 *        The line color.
	 * @param width
	 *        The line width.
	 * @param dash
	 *        The dash style: <code>solid</code>, <code>dot</code>, <code>dash</code>,
	 *        <code>lgDash</code>, <code>dashDot</code>, <code>lgDashDot</code>,
	 *        <code>lgDashDotDot</code>, <code>sysDash</code>, <code>sysDot</code>,
	 *        <code>sysDashDot</code> or <code>sysDashDotDot</code>.
	 * @return The dictionary describing the outline.
	 */
	@Label("Describe an outline")
	@SideEffectFree
	public static Map<String, Object> line(
			Object color,
			Double width,
			String dash) {
		Map<String, Object> result = map();
		put(result, COLOR, color);
		put(result, WIDTH, width);
		put(result, DASH, dash);
		return result;
	}

	/**
	 * Describes the shadow of a shape.
	 *
	 * @param color
	 *        The shadow color, its transparency given as <code>alpha</code> (see
	 *        <code>pptColor()</code>). The default is black with 35% opacity.
	 * @param blur
	 *        The blur radius. The default is 6.
	 * @param distance
	 *        The distance of the shadow from the shape. The default is 2.
	 * @param direction
	 *        The direction of the shadow in degrees, clockwise with 0 pointing to the right. The
	 *        default is 90 (downwards).
	 * @return The dictionary describing the shadow.
	 */
	@Label("Describe a shadow")
	@SideEffectFree
	public static Map<String, Object> shadow(
			Object color,
			Double blur,
			Double distance,
			Double direction) {
		Map<String, Object> result = map();
		put(result, COLOR, color);
		put(result, BLUR, blur);
		put(result, DISTANCE, distance);
		put(result, DIRECTION, direction);
		return result;
	}

	static Map<String, Object> typed(String type) {
		Map<String, Object> result = map();
		result.put(TYPE, type);
		return result;
	}

	static Map<String, Object> map() {
		return new LinkedHashMap<>();
	}

	static void putBounds(Map<String, Object> data, Double x, Double y, Double width, Object height,
			Double weight) {
		put(data, X, x);
		put(data, Y, y);
		put(data, WIDTH, width);
		put(data, HEIGHT, height);
		put(data, WEIGHT, weight);
	}

	private static void putText(Map<String, Object> data, Object text, String font, Double size, Object color,
			Boolean bold, Boolean italic, Boolean underline, Boolean caps, Double spacing, String align,
			String anchor, Object insets) {
		put(data, TEXT, text);
		putStyle(data, font, size, color, bold, italic, underline, caps, spacing);
		put(data, ALIGN, align);
		put(data, ANCHOR, anchor);
		put(data, INSETS, insets);
	}

	private static void putStyle(Map<String, Object> data, String font, Double size, Object color, Boolean bold,
			Boolean italic, Boolean underline, Boolean caps, Double spacing) {
		put(data, FONT, font);
		put(data, SIZE, size);
		put(data, COLOR, color);
		put(data, BOLD, bold);
		put(data, ITALIC, italic);
		put(data, UNDERLINE, underline);
		put(data, CAPS, caps);
		put(data, CHARACTER_SPACING, spacing);
	}

	static void putList(Map<String, Object> data, String key, Object value) {
		if (value instanceof List<?> list && list.isEmpty()) {
			return;
		}
		put(data, key, value);
	}

	/**
	 * Adds the given value, if it is not <code>null</code>, numbers as {@link Double}.
	 */
	static void put(Map<String, Object> data, String key, Object value) {
		if (value instanceof Number number && !(value instanceof Double)) {
			data.put(key, Double.valueOf(number.doubleValue()));
		} else {
			PresentationData.put(data, key, value);
		}
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import static com.top_logic.presentation.data.PresentationData.*;
import static com.top_logic.presentation.script.PptDescriptionFunctions.put;
import static com.top_logic.presentation.script.PptDescriptionFunctions.putBounds;
import static com.top_logic.presentation.script.PptDescriptionFunctions.typed;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.model.LayoutRole;

/**
 * TL-Script functions describing typical slides and the building blocks of a slide: cards,
 * banners, badges, key figures, bullet lists, ratings, legends and arrows.
 *
 * <p>
 * Like the functions of {@link PptDescriptionFunctions}, these functions only create the
 * dictionaries describing the presentation. Their look comes from the template profile (see
 * <code>pptProfile()</code>): A building block uses the style of the profile named like the
 * block, e.g. the style <code>card</code> for a card. The colors are given by color roles, e.g.
 * <code>topicA</code> to <code>topicF</code> for the topics of a presentation, <code>risk</code>,
 * <code>warning</code> and <code>done</code>, or <code>muted</code> for secondary content. The
 * profile maps the roles to the colors of the template and can define further roles. The role of
 * a block colors the parts its style paints in the color role <code>accent</code>, e.g. the
 * outline of a card.
 * </p>
 *
 * <p>
 * Each building block can be placed like any other element with <code>x</code>, <code>y</code>,
 * <code>width</code>, <code>height</code> and <code>weight</code>, or arranged in rows and
 * columns (<code>pptRow()</code>, <code>pptColumn()</code>). All lengths are given in points.
 * </p>
 */
@ScriptPrefix("ppt")
public class PptBlockFunctions extends TLScriptFunctions {

	/**
	 * Describes the title slide of a presentation.
	 *
	 * <p>
	 * Example: <code>pptTitle("Annual Report", "Fiscal year 2026")</code>
	 * </p>
	 *
	 * @param title
	 *        The title.
	 * @param subtitle
	 *        The subtitle.
	 * @param notes
	 *        The speaker notes as text, or a list of lines.
	 * @return The dictionary describing the slide on the layout with the role <code>title</code>.
	 */
	@Label("Describe a title slide")
	@SideEffectFree
	public static Map<String, Object> title(@Mandatory Object title, Object subtitle, Object notes) {
		return PptDescriptionFunctions.slide(LayoutRole.TITLE.id(), title, subtitle, null, notes, null, null);
	}

	/**
	 * Describes the closing slide of a presentation.
	 *
	 * <p>
	 * Example: <code>pptClosing("Thank you", "Questions?")</code>
	 * </p>
	 *
	 * @param title
	 *        The title.
	 * @param subtitle
	 *        The subtitle.
	 * @param notes
	 *        The speaker notes as text, or a list of lines.
	 * @return The dictionary describing the slide on the layout with the role
	 *         <code>closing</code>.
	 */
	@Label("Describe a closing slide")
	@SideEffectFree
	public static Map<String, Object> closing(@Mandatory Object title, Object subtitle, Object notes) {
		return PptDescriptionFunctions.slide(LayoutRole.CLOSING.id(), title, subtitle, null, notes, null, null);
	}

	/**
	 * Describes the first slide of a section of a presentation.
	 *
	 * <p>
	 * Example: <code>pptSection("Results", "What we achieved")</code>
	 * </p>
	 *
	 * @param title
	 *        The title of the section.
	 * @param subtitle
	 *        The subtitle.
	 * @param notes
	 *        The speaker notes as text, or a list of lines.
	 * @return The dictionary describing the slide on the layout with the role
	 *         <code>section</code>.
	 */
	@Label("Describe a section slide")
	@SideEffectFree
	public static Map<String, Object> section(@Mandatory Object title, Object subtitle, Object notes) {
		return PptDescriptionFunctions.slide(LayoutRole.SECTION.id(), title, subtitle, null, notes, null, null);
	}

	/**
	 * Describes a content slide.
	 *
	 * <p>
	 * Example: <code>pptContent("Results", "All goals reached", [pptRow([pptCard("Sales", "+12
	 * %", role: "topicA"), pptCard("Costs", "-3 %", role: "topicB")]), pptBanner("On track",
	 * role: "done")])</code>
	 * </p>
	 *
	 * <p>
	 * Content elements without position are arranged one below the other in the content area of
	 * the layout, see <code>pptSlide()</code>.
	 * </p>
	 *
	 * @param title
	 *        The title.
	 * @param subtitle
	 *        The subtitle.
	 * @param content
	 *        The content: an element or a list of elements.
	 * @param notes
	 *        The speaker notes as text, or a list of lines.
	 * @return The dictionary describing the slide on the layout with the role
	 *         <code>content</code>.
	 */
	@Label("Describe a content slide")
	@SideEffectFree
	public static Map<String, Object> content(@Mandatory Object title, Object subtitle, Object content,
			Object notes) {
		List<?> elements = content == null ? null : content instanceof List<?> list ? list : List.of(content);
		return PptDescriptionFunctions.slide(LayoutRole.CONTENT.id(), title, subtitle, elements, notes, null, null);
	}

	/**
	 * Describes a card: a framed box with a heading and a text.
	 *
	 * <p>
	 * Example: <code>pptCard("Goal", ["Faster releases", pptParagraph("every month",
	 * bullet: true)], role: "topicB")</code>
	 * </p>
	 *
	 * <p>
	 * The heading is bold and somewhat larger than the text. The frame, the fill and the text
	 * formatting come from the style <code>card</code> of the template profile; by default a
	 * rounded box whose outline and shadow have the color of the role.
	 * </p>
	 *
	 * @param heading
	 *        The heading.
	 * @param body
	 *        The text below the heading: a string, paragraphs (<code>pptParagraph()</code>) or a
	 *        list of those.
	 * @param role
	 *        The color role, e.g. <code>topicA</code>. If not given, the role of the style or
	 *        <code>accent</code>.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of the text.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @param style
	 *        The name of another style of the template profile to use instead of
	 *        <code>card</code>.
	 * @return The dictionary describing the card.
	 */
	@Label("Describe a card")
	@SideEffectFree
	public static Map<String, Object> card(Object heading, Object body, String role, Double x, Double y,
			Double width, Object height, Double weight, String style) {
		Map<String, Object> result = typed(TYPE_CARD);
		put(result, HEADING, heading);
		put(result, BODY, body);
		return block(result, role, x, y, width, height, weight, style);
	}

	/**
	 * Describes a banner: a filled box stating the key message of a slide.
	 *
	 * <p>
	 * Example: <code>pptBanner(pptParagraph(["Key message: ", pptRun("costs are falling", bold:
	 * false)]), role: "done")</code>
	 * </p>
	 *
	 * <p>
	 * By default, the box is filled with the color of the role, its text is bold and in the color
	 * of the role <code>inverse</code>, and its height is the height of the text (style
	 * <code>banner</code> of the template profile).
	 * </p>
	 *
	 * @param text
	 *        The text: a string, a paragraph (<code>pptParagraph()</code>), e.g. with a lead-in in
	 *        another formatting, or a list of paragraphs.
	 * @param role
	 *        The color role, e.g. <code>topicA</code>. If not given, the role of the style or
	 *        <code>accent</code>.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height. If neither height nor weight is given, the height of the text.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @param style
	 *        The name of another style of the template profile to use instead of
	 *        <code>banner</code>.
	 * @return The dictionary describing the banner.
	 */
	@Label("Describe a banner")
	@SideEffectFree
	public static Map<String, Object> banner(@Mandatory Object text, String role, Double x, Double y,
			Double width, Object height, Double weight, String style) {
		Map<String, Object> result = typed(TYPE_BANNER);
		put(result, TEXT, text);
		return block(result, role, x, y, width, height, weight, style);
	}

	/**
	 * Describes a badge: a filled circle with a short label, e.g. a number.
	 *
	 * <p>
	 * Example: <code>pptBadge(1, role: "topicA")</code>
	 * </p>
	 *
	 * <p>
	 * By default, the badge is a circle of 40 points filled with the color of the role, its label
	 * bold and centered (style <code>badge</code> of the template profile). If only the width or
	 * only the height is given, the badge is as high as wide.
	 * </p>
	 *
	 * @param label
	 *        The label.
	 * @param role
	 *        The color role, e.g. <code>topicA</code>. If not given, the role of the style or
	 *        <code>accent</code>.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @param style
	 *        The name of another style of the template profile to use instead of
	 *        <code>badge</code>.
	 * @return The dictionary describing the badge.
	 */
	@Label("Describe a badge")
	@SideEffectFree
	public static Map<String, Object> badge(@Mandatory Object label, String role, Double x, Double y,
			Double width, Object height, Double weight, String style) {
		Map<String, Object> result = typed(TYPE_BADGE);
		put(result, LABEL, label);
		return block(result, role, x, y, width, height, weight, style);
	}

	/**
	 * Describes a key figure: a card with a big value, a bold label and a caption.
	 *
	 * <p>
	 * Example: <code>pptStat("491", "Tickets", "closed in this release", role: "done")</code>
	 * </p>
	 *
	 * <p>
	 * The value is shown in the color of the role, the caption in the color of the role
	 * <code>muted</code>. The box comes from the style <code>stat</code> of the template profile.
	 * </p>
	 *
	 * @param value
	 *        The value, a number or a short text.
	 * @param label
	 *        The label naming the value.
	 * @param caption
	 *        An explanation below the label.
	 * @param role
	 *        The color role, e.g. <code>topicA</code>. If not given, the role of the style or
	 *        <code>accent</code>.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of the text.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @param style
	 *        The name of another style of the template profile to use instead of
	 *        <code>stat</code>.
	 * @return The dictionary describing the key figure.
	 */
	@Label("Describe a key figure")
	@SideEffectFree
	public static Map<String, Object> stat(@Mandatory Object value, Object label, Object caption, String role,
			Double x, Double y, Double width, Object height, Double weight, String style) {
		Map<String, Object> result = typed(TYPE_STAT);
		put(result, VALUE, value);
		put(result, LABEL, label);
		put(result, CAPTION, caption);
		return block(result, role, x, y, width, height, weight, style);
	}

	/**
	 * Describes a bullet list.
	 *
	 * <p>
	 * Example: <code>pptBullets(["First point", "Second point"], role: "topicC")</code>
	 * </p>
	 *
	 * <p>
	 * The bullets have the color of the role. Unless <code>split</code> is <code>false</code>,
	 * items that do not fit continue on a further slide repeating the title.
	 * </p>
	 *
	 * @param items
	 *        The items, each a string or a paragraph (<code>pptParagraph()</code>).
	 * @param role
	 *        The color role of the bullets, e.g. <code>topicA</code>. If not given,
	 *        <code>accent</code>.
	 * @param split
	 *        Whether items that do not fit continue on a further slide. The default is
	 *        <code>true</code>.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height, or <code>auto</code> for the height of the items.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @return The dictionary describing the bullet list.
	 */
	@Label("Describe a bullet list")
	@SideEffectFree
	public static Map<String, Object> bullets(@Mandatory List<?> items, String role, Boolean split, Double x,
			Double y, Double width, Object height, Double weight) {
		Map<String, Object> result = typed(TYPE_BULLETS);
		result.put(ITEMS, items);
		put(result, SPLIT, split);
		return block(result, role, x, y, width, height, weight, null);
	}

	/**
	 * Describes a table cell showing a rating.
	 *
	 * <p>
	 * Example: <code>pptTable([["Criterion", "Option A"], ["Effort", pptRating("low")]],
	 * headerRows: 1)</code>
	 * </p>
	 *
	 * <p>
	 * The fill and the text formatting of the cell come from the entry <code>ratings</code> of the
	 * template profile, which defines the rating words <code>met</code>, <code>partial</code>,
	 * <code>unmet</code>, <code>low</code>, <code>medium</code>, <code>high</code> and
	 * <code>none</code>, and can define further ones.
	 * </p>
	 *
	 * @param word
	 *        The rating word.
	 * @param text
	 *        The text of the cell. If not given, the rating word.
	 * @return The dictionary describing the table cell.
	 */
	@Label("Describe a rating cell")
	@SideEffectFree
	public static Map<String, Object> rating(@Mandatory String word, Object text) {
		Map<String, Object> result = map();
		result.put(TEXT, text == null ? word : text);
		result.put(RATING, word);
		return result;
	}

	/**
	 * Describes a legend explaining the colors of a slide.
	 *
	 * <p>
	 * Example: <code>pptLegend([["planned", "muted"], ["done", "done"]])</code>
	 * </p>
	 *
	 * <p>
	 * The entries are placed side by side, each a small square in the color of its role and its
	 * label. If neither height nor weight is given, the legend is as high as its labels.
	 * </p>
	 *
	 * @param items
	 *        The entries, each a list of the label and the color role.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @return The dictionary describing the legend.
	 */
	@Label("Describe a legend")
	@SideEffectFree
	public static Map<String, Object> legend(@Mandatory List<?> items, Double x, Double y, Double width,
			Object height, Double weight) {
		Map<String, Object> result = typed(TYPE_LEGEND);
		result.put(ITEMS, items);
		putBounds(result, x, y, width, height, weight);
		return result;
	}

	/**
	 * Describes a block arrow, e.g. between the steps of a flow.
	 *
	 * <p>
	 * Example: <code>pptRow([pptCard("Input"), pptArrow(), pptCard("Output")], align:
	 * "center")</code>
	 * </p>
	 *
	 * <p>
	 * Unless a size is given, the arrow is 30 points long and 24 points broad. By default, it is
	 * filled with the color of the role <code>muted</code> (style <code>arrow</code> of the
	 * template profile).
	 * </p>
	 *
	 * @param direction
	 *        The direction: <code>right</code> (the default), <code>left</code>, <code>up</code> or
	 *        <code>down</code>.
	 * @param role
	 *        The color role, e.g. <code>topicA</code>. If not given, the role of the style.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @param style
	 *        The name of another style of the template profile to use instead of
	 *        <code>arrow</code>.
	 * @return The dictionary describing the arrow.
	 */
	@Label("Describe an arrow")
	@SideEffectFree
	public static Map<String, Object> arrow(String direction, String role, Double x, Double y, Double width,
			Object height, Double weight, String style) {
		Map<String, Object> result = typed(TYPE_ARROW);
		put(result, DIRECTION, direction);
		return block(result, role, x, y, width, height, weight, style);
	}

	private static Map<String, Object> block(Map<String, Object> result, String role, Double x, Double y,
			Double width, Object height, Double weight, String style) {
		put(result, ROLE, role);
		put(result, STYLE, style);
		putBounds(result, x, y, width, height, weight);
		return result;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import static com.top_logic.presentation.data.PresentationData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.presentation.data.PresentationData;
import com.top_logic.util.Resources;

/**
 * Reads the building blocks of a presentation description: cards, banners, badges, key figures,
 * bullet lists, legends, arrows, timelines, flows and comparisons of strengths and limits.
 *
 * <p>
 * A building block is expanded into the elements shapes, text boxes and rows are made of. Its
 * look comes from the template profile: A block made of a shape refers to the style of the
 * profile named like the block type (e.g. {@link PresentationData#TYPE_CARD}), unless the block
 * names another style with the entry {@link PresentationData#STYLE}. The colors of a block are
 * {@link ColorSpec.Role color roles}; the block's entry {@link PresentationData#ROLE} gives the
 * color that the style and the block's text refer to as {@link PresentationData#ROLE_ACCENT}.
 * Each block accepts the placement entries {@link PresentationData#X}, {@link PresentationData#Y},
 * {@link PresentationData#WIDTH}, {@link PresentationData#HEIGHT} and
 * {@link PresentationData#WEIGHT}.
 * </p>
 *
 * <p>
 * Besides, {@link #agendaList(List)} builds the list of entries an agenda slide shows on a layout
 * without entry placeholders.
 * </p>
 */
public final class BlockReader {

	/**
	 * The font size of the heading of a card.
	 */
	static final double HEADING_SIZE = 16;

	/**
	 * The space in points after a heading.
	 */
	static final double HEADING_SPACE = 6;

	/**
	 * The space in points after a paragraph of a key figure.
	 */
	static final double PARAGRAPH_SPACE = 4;

	/**
	 * The space in points after an item of a bullet list.
	 */
	static final double BULLET_SPACE = 5;

	/**
	 * The font size of the value of a key figure.
	 */
	static final double VALUE_SIZE = 40;

	/**
	 * The font size of the label of a key figure.
	 */
	static final double LABEL_SIZE = 15;

	/**
	 * The font size of the caption of a key figure and of the labels of a legend.
	 */
	static final double CAPTION_SIZE = 13;

	/**
	 * The size of the color mark of a legend entry.
	 */
	static final double MARK_SIZE = 12;

	/**
	 * The distance between the color mark of a legend entry and its label.
	 */
	static final double MARK_GAP = 6;

	/**
	 * The diameter of a badge without size.
	 */
	static final double BADGE_SIZE = 40;

	/**
	 * The length of an arrow without size.
	 */
	static final double ARROW_LENGTH = 30;

	/**
	 * The breadth of an arrow without size.
	 */
	static final double ARROW_BREADTH = 24;

	/**
	 * The diameter of the markers of a timeline.
	 */
	static final double MARKER_SIZE = 28;

	/**
	 * The thickness of the line of a timeline.
	 */
	static final double TIMELINE_THICKNESS = 4;

	/**
	 * The distance between the dates, the markers and the titles of a timeline.
	 */
	static final double TIMELINE_GAP = 12;

	/**
	 * The font size of the dates of a timeline.
	 */
	static final double DATE_SIZE = 16;

	/**
	 * The left and right margin of the texts of a milestone.
	 */
	static final double MILESTONE_INSET = 4;

	/**
	 * The distance between the steps of a flow and the arrows between them.
	 */
	static final double FLOW_GAP = 8;

	/**
	 * The bullet character of the strengths of a comparison of strengths and limits.
	 */
	static final String PRO_BULLET = "\u2713";

	/**
	 * The bullet character of the limits of a comparison of strengths and limits.
	 */
	static final String CON_BULLET = "\u2013";

	/**
	 * The font size of the entries of an agenda listed in the content area.
	 */
	static final double ENTRY_SIZE = 20;

	/**
	 * The font size of a subtitle shown as content element, see {@link #subtitleText(TextSpec)}.
	 */
	static final double SUBTITLE_SIZE = 18;

	/**
	 * The color of the element's role.
	 */
	private static final ColorSpec ACCENT = new ColorSpec.Role(ROLE_ACCENT, Map.of());

	/**
	 * The color of secondary text.
	 */
	private static final ColorSpec MUTED = new ColorSpec.Role(ROLE_MUTED, Map.of());

	private static final RunStyle BOLD = new RunStyle(Boolean.TRUE, null, null, null, null, null, null, null);

	private static final RunStyle HEADING_STYLE = size(BOLD, HEADING_SIZE, null);

	private BlockReader() {
		// Static utility.
	}

	/**
	 * Registers the readers of the building blocks by their type names.
	 */
	static void register(Map<String, ElementReader> types) {
		types.put(TYPE_CARD, BlockReader::readCard);
		types.put(TYPE_BANNER, BlockReader::readBanner);
		types.put(TYPE_BADGE, BlockReader::readBadge);
		types.put(TYPE_STAT, BlockReader::readStat);
		types.put(TYPE_BULLETS, BlockReader::readBullets);
		types.put(TYPE_LEGEND, BlockReader::readLegend);
		types.put(TYPE_ARROW, BlockReader::readArrow);
		types.put(TYPE_TIMELINE, BlockReader::readTimeline);
		types.put(TYPE_FLOW, BlockReader::readFlow);
		types.put(TYPE_PRO_CON, BlockReader::readProCon);
	}

	/**
	 * A card: the heading (bold, in {@link #HEADING_SIZE}) followed by the body text in a shape of
	 * the style {@link PresentationData#TYPE_CARD}.
	 */
	private static ShapeSpec readCard(Entries entries) {
		Bounds bounds = DescriptionReader.readBounds(entries);
		List<ParagraphSpec> paragraphs = new ArrayList<>();
		Value heading = entries.get(HEADING);
		if (!heading.isAbsent()) {
			for (ParagraphSpec paragraph : DescriptionReader.readParagraphs(heading)) {
				paragraphs.add(format(paragraph, HEADING_STYLE, HEADING_SPACE, null));
			}
		}
		Value body = entries.get(BODY);
		if (!body.isAbsent()) {
			paragraphs.addAll(DescriptionReader.readParagraphs(body));
		}
		return styled(entries, bounds, null, paragraphs, TYPE_CARD);
	}

	/**
	 * A banner: the text in a shape of the style {@link PresentationData#TYPE_BANNER}, by default
	 * as high as its text.
	 */
	private static ShapeSpec readBanner(Entries entries) {
		Bounds bounds = autoHeight(DescriptionReader.readBounds(entries));
		List<ParagraphSpec> text =
			DescriptionReader.readParagraphs(entries.require(TEXT, I18NConstants.EXPECTED_TEXT_CONTENT));
		return styled(entries, bounds, null, text, TYPE_BANNER);
	}

	/**
	 * A badge: the label in a shape of the style {@link PresentationData#TYPE_BADGE}, by default a
	 * square of {@link #BADGE_SIZE}.
	 */
	private static ShapeSpec readBadge(Entries entries) {
		Bounds given = DescriptionReader.readBounds(entries);
		Double width = given.width() != null ? given.width() : given.height() != null ? given.height() : BADGE_SIZE;
		Bounds bounds = defaultSize(given, width.doubleValue(), width.doubleValue());
		List<ParagraphSpec> label =
			DescriptionReader.readParagraphs(entries.require(LABEL, I18NConstants.EXPECTED_TEXT_CONTENT));
		return styled(entries, bounds, null, label, TYPE_BADGE);
	}

	/**
	 * A key figure: the value (bold, in {@link #VALUE_SIZE}, in the color of the role), the label
	 * (bold) and the caption (in the color {@link PresentationData#ROLE_MUTED}) in a shape of the
	 * style {@link PresentationData#TYPE_STAT}.
	 */
	private static ShapeSpec readStat(Entries entries) {
		Bounds bounds = DescriptionReader.readBounds(entries);
		List<ParagraphSpec> paragraphs = new ArrayList<>();
		RunStyle valueStyle = size(BOLD, VALUE_SIZE, ACCENT);
		for (ParagraphSpec paragraph : DescriptionReader
			.readParagraphs(entries.require(VALUE, I18NConstants.EXPECTED_TEXT_CONTENT))) {
			paragraphs.add(format(paragraph, valueStyle, PARAGRAPH_SPACE, null));
		}
		Value label = entries.get(LABEL);
		if (!label.isAbsent()) {
			for (ParagraphSpec paragraph : DescriptionReader.readParagraphs(label)) {
				paragraphs.add(format(paragraph, size(BOLD, LABEL_SIZE, null), PARAGRAPH_SPACE, null));
			}
		}
		Value caption = entries.get(CAPTION);
		if (!caption.isAbsent()) {
			for (ParagraphSpec paragraph : DescriptionReader.readParagraphs(caption)) {
				paragraphs.add(format(paragraph, size(RunStyle.INHERIT, CAPTION_SIZE, MUTED), null, null));
			}
		}
		return styled(entries, bounds, null, paragraphs, TYPE_STAT);
	}

	/**
	 * A bullet list: a text box with a paragraph for each item, its bullets in the color of the
	 * role. Unless {@link PresentationData#SPLIT} is <code>false</code>, items that do not fit
	 * continue on a further slide.
	 */
	private static TextBoxSpec readBullets(Entries entries) {
		Bounds bounds = DescriptionReader.readBounds(entries);
		BulletSpec bullet = new BulletSpec(true, null, ACCENT);
		List<ParagraphSpec> paragraphs = new ArrayList<>();
		for (ParagraphSpec paragraph : DescriptionReader
			.readParagraphs(entries.require(ITEMS, I18NConstants.EXPECTED_TEXT_CONTENT))) {
			paragraphs.add(format(paragraph, RunStyle.INHERIT, BULLET_SPACE, bullet));
		}
		Boolean split = entries.get(SPLIT).optBoolean();
		String role = DescriptionReader.readRole(entries.get(ROLE));
		TextSpec text = new TextSpec(paragraphs, RunStyle.INHERIT, null, null, InsetsSpec.ZERO, null,
			split == null || split.booleanValue(), null);
		return new TextBoxSpec(bounds, text, role);
	}

	/**
	 * A legend: a row of entries, each a square color mark and its label (in the color
	 * {@link PresentationData#ROLE_MUTED}), by default as high as its labels.
	 *
	 * <p>
	 * The entry {@link PresentationData#ITEMS} is a list of entries, each a list of the label and
	 * the color role.
	 * </p>
	 */
	private static ContainerSpec readLegend(Entries entries) {
		Bounds bounds = autoHeight(DescriptionReader.readBounds(entries));
		RunStyle labelStyle = size(RunStyle.INHERIT, CAPTION_SIZE, MUTED);
		Bounds markBounds = new Bounds(null, null, Double.valueOf(MARK_SIZE), Double.valueOf(MARK_SIZE), false, null);
		Bounds labelBounds = new Bounds(null, null, null, null, true, null);
		List<ElementSpec> items = new ArrayList<>();
		for (Value item : entries.require(ITEMS, I18NConstants.EXPECTED_LIST).asList()) {
			if (!item.isList() || item.asList().size() != 2) {
				throw item.invalid(I18NConstants.EXPECTED_LEGEND_ITEM);
			}
			List<Value> parts = item.asList();
			List<ParagraphSpec> label =
				DescriptionReader.readParagraphs(parts.get(0).require(I18NConstants.EXPECTED_TEXT_CONTENT));
			String role = DescriptionReader.readRole(parts.get(1).require(I18NConstants.EXPECTED_ROLE));
			ShapeSpec mark = new ShapeSpec(markBounds, null, new PaintSpec.Solid(ACCENT), LineSpec.NONE, null, null,
				null, role);
			TextBoxSpec text = new TextBoxSpec(labelBounds,
				new TextSpec(label, labelStyle, null, null, InsetsSpec.ZERO, null, false, null), null);
			items.add(new ContainerSpec(Bounds.NONE, ContainerSpec.Direction.ROW, List.of(mark, text),
				Double.valueOf(MARK_GAP), null, CrossAlign.CENTER));
		}
		return new ContainerSpec(bounds, ContainerSpec.Direction.ROW, items, null, null, CrossAlign.STRETCH);
	}

	/**
	 * A block arrow in the {@link PresentationData#DIRECTION} (by default to the right) in the
	 * style {@link PresentationData#TYPE_ARROW}, by default {@link #ARROW_LENGTH} long and
	 * {@link #ARROW_BREADTH} broad.
	 */
	private static ShapeSpec readArrow(Entries entries) {
		Bounds given = DescriptionReader.readBounds(entries);
		ArrowDirection direction = entries.get(DIRECTION).optEnum(ArrowDirection.class);
		if (direction == null) {
			direction = ArrowDirection.RIGHT;
		}
		return styled(entries, arrowBounds(direction, given), arrowGeometry(direction), List.of(), TYPE_ARROW);
	}

	/**
	 * The given bounds of an arrow in the given direction, by default {@link #ARROW_LENGTH} long
	 * and {@link #ARROW_BREADTH} broad.
	 */
	private static Bounds arrowBounds(ArrowDirection direction, Bounds given) {
		return direction.isHorizontal()
			? defaultSize(given, ARROW_LENGTH, ARROW_BREADTH) : defaultSize(given, ARROW_BREADTH, ARROW_LENGTH);
	}

	private static GeometrySpec arrowGeometry(ArrowDirection direction) {
		return new GeometrySpec(direction.preset(), Map.of(), null);
	}

	/**
	 * A timeline: the {@link PresentationData#MILESTONES} side by side in equal widths, each a
	 * marker on a horizontal line (the style {@link PresentationData#TYPE_TIMELINE}), its date
	 * above (bold, in {@link #DATE_SIZE}, in the color of its role), its title (bold) and subtitle
	 * (in the color {@link PresentationData#ROLE_MUTED}) below. By default, the timeline is as high
	 * as its texts.
	 *
	 * <p>
	 * A milestone is a dictionary with the entries {@link PresentationData#DATE},
	 * {@link PresentationData#TITLE}, {@link PresentationData#SUBTITLE} and
	 * {@link PresentationData#ROLE}, or a list of these values in this order. The markers are
	 * circles in the style {@link PresentationData#TYPE_BADGE}. A milestone without role has the
	 * role of the timeline.
	 * </p>
	 */
	private static ContainerSpec readTimeline(Entries entries) {
		Bounds bounds = autoHeight(DescriptionReader.readBounds(entries));
		String defaultRole = DescriptionReader.readRole(entries.get(ROLE));
		List<Value> milestones = entries.require(MILESTONES, I18NConstants.EXPECTED_LIST).asList();

		Bounds lineBounds = new Bounds(null, null, null, Double.valueOf(TIMELINE_THICKNESS), false, null);
		Bounds markerBounds =
			new Bounds(null, null, Double.valueOf(MARKER_SIZE), Double.valueOf(MARKER_SIZE), false, null);
		InsetsSpec insets = new InsetsSpec(MILESTONE_INSET, 0, MILESTONE_INSET, 0);
		RunStyle dateStyle = size(BOLD, DATE_SIZE, ACCENT);
		RunStyle titleStyle = size(BOLD, CAPTION_SIZE, null);
		RunStyle subtitleStyle = size(RunStyle.INHERIT, CAPTION_SIZE, MUTED);

		List<ElementSpec> dates = new ArrayList<>();
		List<ElementSpec> markers = new ArrayList<>();
		List<ElementSpec> titles = new ArrayList<>();
		for (int n = 0, cnt = milestones.size(); n < cnt; n++) {
			Milestone milestone = readMilestone(milestones.get(n));
			String role = milestone.role() != null ? milestone.role() : defaultRole;

			dates.add(centeredText(milestone.date(), dateStyle, 0, insets, role));

			ElementSpec before = n == 0 ? spacer() : timelineLine(lineBounds);
			ElementSpec after = n == cnt - 1 ? spacer() : timelineLine(lineBounds);
			ShapeSpec marker = new ShapeSpec(markerBounds, null, null, null, null, null, TYPE_BADGE, role);
			markers.add(new ContainerSpec(Bounds.NONE, ContainerSpec.Direction.ROW, List.of(before, marker, after),
				Double.valueOf(0), null, CrossAlign.CENTER));

			List<ParagraphSpec> text = new ArrayList<>();
			for (ParagraphSpec paragraph : milestone.title()) {
				text.add(format(paragraph, titleStyle, PARAGRAPH_SPACE, null));
			}
			for (ParagraphSpec paragraph : milestone.subtitle()) {
				text.add(format(paragraph, subtitleStyle, null, null));
			}
			titles.add(centeredText(text, RunStyle.INHERIT, 0, insets, role));
		}

		Bounds rowBounds = new Bounds(null, null, null, null, true, null);
		Double noGap = Double.valueOf(0);
		List<ElementSpec> rows = List.of(
			new ContainerSpec(rowBounds, ContainerSpec.Direction.ROW, dates, noGap, null, CrossAlign.END),
			new ContainerSpec(new Bounds(null, null, null, Double.valueOf(MARKER_SIZE), false, null),
				ContainerSpec.Direction.ROW, markers, noGap, null, CrossAlign.STRETCH),
			new ContainerSpec(rowBounds, ContainerSpec.Direction.ROW, titles, noGap, null, CrossAlign.START));
		return new ContainerSpec(bounds, ContainerSpec.Direction.COLUMN, rows, Double.valueOf(TIMELINE_GAP), null,
			CrossAlign.STRETCH);
	}

	/**
	 * A milestone of a timeline.
	 *
	 * @param date
	 *        The paragraphs of the date.
	 * @param title
	 *        The paragraphs of the title.
	 * @param subtitle
	 *        The paragraphs of the subtitle.
	 * @param role
	 *        The color role, <code>null</code> for the role of the timeline.
	 */
	private record Milestone(List<ParagraphSpec> date, List<ParagraphSpec> title, List<ParagraphSpec> subtitle,
			String role) {
		// Pure data.
	}

	private static Milestone readMilestone(Value milestone) {
		if (milestone.isList()) {
			List<Value> parts = milestone.asList();
			if (parts.isEmpty() || parts.size() > 4) {
				throw milestone.invalid(I18NConstants.EXPECTED_MILESTONE);
			}
			return new Milestone(optParagraphs(parts.get(0)), optParagraphs(part(parts, 1)),
				optParagraphs(part(parts, 2)), DescriptionReader.readRole(part(parts, 3)));
		}
		if (milestone.isMap()) {
			Entries entries = milestone.asEntries();
			Milestone result = new Milestone(optParagraphs(entries.get(DATE)), optParagraphs(entries.get(TITLE)),
				optParagraphs(entries.get(SUBTITLE)), DescriptionReader.readRole(entries.get(ROLE)));
			entries.checkComplete();
			return result;
		}
		throw milestone.invalid(I18NConstants.EXPECTED_MILESTONE);
	}

	private static Value part(List<Value> parts, int index) {
		return index < parts.size() ? parts.get(index) : new Value(null, parts.get(0).path());
	}

	/**
	 * The paragraphs of the given text, empty if the value is missing.
	 */
	private static List<ParagraphSpec> optParagraphs(Value text) {
		return text.isAbsent() ? List.of() : DescriptionReader.readParagraphs(text);
	}

	private static ShapeSpec timelineLine(Bounds bounds) {
		return new ShapeSpec(bounds, null, null, null, null, null, TYPE_TIMELINE, null);
	}

	/**
	 * An element that takes space in a row or column without showing anything.
	 */
	private static ContainerSpec spacer() {
		return new ContainerSpec(Bounds.NONE, ContainerSpec.Direction.ROW, List.of(), null, null, null);
	}

	/**
	 * A text box with the given paragraphs, centered horizontally.
	 */
	private static TextBoxSpec centeredText(List<ParagraphSpec> paragraphs, RunStyle style, double spaceAfter,
			InsetsSpec insets, String role) {
		List<ParagraphSpec> formatted = new ArrayList<>();
		for (ParagraphSpec paragraph : paragraphs) {
			formatted.add(format(paragraph, style, Double.valueOf(spaceAfter), null));
		}
		if (formatted.isEmpty()) {
			formatted.add(ParagraphSpec.plain(""));
		}
		TextSpec text = new TextSpec(formatted, RunStyle.INHERIT, TextAlign.CENTER, null, insets, null, false, null);
		return new TextBoxSpec(Bounds.NONE, text, role);
	}

	/**
	 * A flow: the {@link PresentationData#STEPS} from left to right with an arrow in the style
	 * {@link PresentationData#TYPE_ARROW} between two steps, by default as high as its highest step.
	 *
	 * <p>
	 * A step is an element description, e.g. a card, or a text, which becomes a card in the role of
	 * the flow. The steps share the width of the flow by their weights.
	 * </p>
	 */
	private static ContainerSpec readFlow(Entries entries) {
		Bounds bounds = autoHeight(DescriptionReader.readBounds(entries));
		String role = DescriptionReader.readRole(entries.get(ROLE));
		List<Value> steps = entries.require(STEPS, I18NConstants.EXPECTED_LIST).asList();
		List<ElementSpec> children = new ArrayList<>();
		for (Value step : steps) {
			if (!children.isEmpty()) {
				ShapeSpec arrow = new ShapeSpec(arrowBounds(ArrowDirection.RIGHT, Bounds.NONE),
					arrowGeometry(ArrowDirection.RIGHT), null, null, null, null, TYPE_ARROW, null);
				children.add(centered(arrow, ARROW_LENGTH));
			}
			if (step.isMap() && step.asEntries().contains(TYPE)) {
				children.add(DescriptionReader.readChild(step));
			} else {
				List<ParagraphSpec> body = DescriptionReader.readParagraphs(step.require(I18NConstants.EXPECTED_STEP));
				children.add(new ShapeSpec(Bounds.NONE, null, null, null, null, TextSpec.of(body), TYPE_CARD, role));
			}
		}
		return new ContainerSpec(bounds, ContainerSpec.Direction.ROW, children, Double.valueOf(FLOW_GAP), null,
			CrossAlign.STRETCH);
	}

	/**
	 * A column of the given width, in which the given element is centered vertically.
	 */
	private static ContainerSpec centered(ElementSpec element, double width) {
		return new ContainerSpec(new Bounds(null, null, Double.valueOf(width), null, false, null),
			ContainerSpec.Direction.COLUMN, List.of(spacer(), element, spacer()), Double.valueOf(0), null,
			CrossAlign.CENTER);
	}

	/**
	 * A comparison of strengths and limits: two cards side by side, the strengths
	 * ({@link PresentationData#PROS}) with check marks in the role
	 * {@link PresentationData#ROLE_DONE}, the limits ({@link PresentationData#CONS}) with dashes in
	 * the role {@link PresentationData#ROLE_RISK}. The headings
	 * ({@link PresentationData#PROS_TITLE}, {@link PresentationData#CONS_TITLE}) are bold in the
	 * color of the card's role, by default "Strengths" and "Limits" in the language of the current
	 * user.
	 */
	private static ContainerSpec readProCon(Entries entries) {
		Bounds bounds = DescriptionReader.readBounds(entries);
		ShapeSpec pros = listCard(entries.get(PROS_TITLE), I18NConstants.PROS_TITLE,
			entries.require(PROS, I18NConstants.EXPECTED_TEXT_CONTENT), PRO_BULLET, ROLE_DONE);
		ShapeSpec cons = listCard(entries.get(CONS_TITLE), I18NConstants.CONS_TITLE,
			entries.require(CONS, I18NConstants.EXPECTED_TEXT_CONTENT), CON_BULLET, ROLE_RISK);
		return new ContainerSpec(bounds, ContainerSpec.Direction.ROW, List.of(pros, cons), null, null,
			CrossAlign.STRETCH);
	}

	/**
	 * A card with a heading in the color of the given role and the given items as bullet list.
	 */
	private static ShapeSpec listCard(Value heading, ResKey defaultHeading, Value items, String bulletChar,
			String role) {
		List<ParagraphSpec> paragraphs = new ArrayList<>();
		List<ParagraphSpec> headingParagraphs = heading.isAbsent()
			? List.of(ParagraphSpec.plain(Resources.getInstance().getString(defaultHeading)))
			: DescriptionReader.readParagraphs(heading);
		for (ParagraphSpec paragraph : headingParagraphs) {
			paragraphs.add(format(paragraph, size(BOLD, HEADING_SIZE, ACCENT), HEADING_SPACE, null));
		}
		BulletSpec bullet = new BulletSpec(true, bulletChar, ACCENT);
		for (ParagraphSpec paragraph : DescriptionReader.readParagraphs(items)) {
			paragraphs.add(format(paragraph, RunStyle.INHERIT, BULLET_SPACE, bullet));
		}
		return new ShapeSpec(Bounds.NONE, null, null, null, null, TextSpec.of(paragraphs), TYPE_CARD, role);
	}

	/**
	 * The list of entries an agenda slide shows in its content area: each entry in a row, preceded
	 * by a badge with its number.
	 *
	 * @param entries
	 *        The entries of the agenda.
	 * @return The column of the entries.
	 */
	public static ContainerSpec agendaList(List<TextSpec> entries) {
		Bounds badgeBounds =
			new Bounds(null, null, Double.valueOf(BADGE_SIZE), Double.valueOf(BADGE_SIZE), false, null);
		Bounds rowBounds = new Bounds(null, null, null, null, true, null);
		RunStyle entryStyle = size(RunStyle.INHERIT, ENTRY_SIZE, null);
		List<ElementSpec> rows = new ArrayList<>();
		for (int n = 0; n < entries.size(); n++) {
			ShapeSpec badge = new ShapeSpec(badgeBounds, null, null, null, null,
				TextSpec.of(List.of(ParagraphSpec.plain(Integer.toString(n + 1)))), TYPE_BADGE, null);
			TextSpec entry = entries.get(n);
			TextBoxSpec text = new TextBoxSpec(Bounds.NONE, new TextSpec(entry.paragraphs(),
				entry.style().inheritFrom(entryStyle), null, TextAnchor.MIDDLE, InsetsSpec.ZERO, null, false, null),
				null);
			rows.add(new ContainerSpec(rowBounds, ContainerSpec.Direction.ROW, List.of(badge, text), null, null,
				CrossAlign.CENTER));
		}
		return new ContainerSpec(Bounds.NONE, ContainerSpec.Direction.COLUMN, rows, null, null, CrossAlign.STRETCH);
	}

	/**
	 * The text element showing the subtitle of a slide whose layout has no subtitle placeholder.
	 *
	 * <p>
	 * The text takes the height of its paragraphs and is shown in the color
	 * {@link PresentationData#ROLE_MUTED}, unless the subtitle formats it.
	 * </p>
	 */
	public static TextBoxSpec subtitleText(TextSpec subtitle) {
		Bounds bounds = new Bounds(null, null, null, null, true, null);
		RunStyle style = size(RunStyle.INHERIT, SUBTITLE_SIZE, MUTED);
		return new TextBoxSpec(bounds, new TextSpec(subtitle.paragraphs(), subtitle.style().inheritFrom(style),
			subtitle.align(), TextAnchor.TOP, InsetsSpec.ZERO, subtitle.overflow(), false, subtitle.wrap()), null);
	}

	/**
	 * Creates a shape of the style given by the entry {@link PresentationData#STYLE}, or of the
	 * given default style, and of the role given by the entry {@link PresentationData#ROLE}.
	 */
	private static ShapeSpec styled(Entries entries, Bounds bounds, GeometrySpec geometry,
			List<ParagraphSpec> paragraphs, String defaultStyle) {
		String style = DescriptionReader.readStyleName(entries.get(STYLE));
		String role = DescriptionReader.readRole(entries.get(ROLE));
		TextSpec text = paragraphs.isEmpty() ? null : TextSpec.of(paragraphs);
		return new ShapeSpec(bounds, geometry, null, null, null, text, style == null ? defaultStyle : style, role);
	}

	/**
	 * The given paragraph with the given formatting where it gives none itself.
	 */
	private static ParagraphSpec format(ParagraphSpec paragraph, RunStyle style, Double spaceAfter,
			BulletSpec bullet) {
		return new ParagraphSpec(paragraph.runs(), paragraph.style().inheritFrom(style), paragraph.align(),
			paragraph.level(), paragraph.bullet() != null ? paragraph.bullet() : bullet, paragraph.spaceBefore(),
			paragraph.spaceAfter() != null ? paragraph.spaceAfter() : spaceAfter, paragraph.lineSpacing());
	}

	private static RunStyle size(RunStyle style, double size, ColorSpec color) {
		return new RunStyle(style.bold(), style.italic(), style.underline(), style.caps(), Double.valueOf(size),
			color, style.font(), style.characterSpacing());
	}

	/**
	 * The given bounds with automatic height, if they give neither height nor weight.
	 */
	private static Bounds autoHeight(Bounds bounds) {
		if (bounds.height() != null || bounds.autoHeight() || bounds.weight() != null) {
			return bounds;
		}
		return new Bounds(bounds.x(), bounds.y(), bounds.width(), null, true, null);
	}

	/**
	 * The given bounds with the given width and height where they give none.
	 */
	private static Bounds defaultSize(Bounds bounds, double width, double height) {
		Double newHeight = bounds.height() != null || bounds.autoHeight() ? bounds.height() : Double.valueOf(height);
		return new Bounds(bounds.x(), bounds.y(), bounds.width() != null ? bounds.width() : Double.valueOf(width),
			newHeight, bounds.autoHeight(), bounds.weight());
	}

}

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

import com.top_logic.presentation.data.PresentationData;

/**
 * Reads the building blocks of a presentation description: cards, banners, badges, key figures,
 * bullet lists, legends and arrows.
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
 */
final class BlockReader {

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
		Bounds bounds = direction.isHorizontal()
			? defaultSize(given, ARROW_LENGTH, ARROW_BREADTH) : defaultSize(given, ARROW_BREADTH, ARROW_LENGTH);
		return styled(entries, bounds, new GeometrySpec(direction.preset(), Map.of(), null), List.of(), TYPE_ARROW);
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

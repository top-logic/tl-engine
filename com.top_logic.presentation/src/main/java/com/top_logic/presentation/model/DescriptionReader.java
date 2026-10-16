/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import static com.top_logic.presentation.data.PresentationData.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.apache.poi.sl.usermodel.ShapeType;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.presentation.template.PaintInfo;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.util.error.TopLogicException;

/**
 * Reads a presentation description from its data representation.
 *
 * <p>
 * The data representation consists of dictionaries, lists, texts, numbers and truth values, as
 * created by the <code>ppt</code> builder functions of TL-Script or by <code>parseJson()</code>,
 * plus binary data for images. The keys are defined in {@link PresentationData}. The reader checks
 * the description completely: A missing mandatory entry, a value of the wrong kind or an entry
 * that is not supported at its position (e.g. a misspelled key) is reported as
 * {@link TopLogicException} naming the path of the faulty entry, e.g.
 * <code>slides[2].content[0].fill.color</code>, and the expected value.
 * </p>
 *
 * <p>
 * An element on a slide is a dictionary whose entry {@link PresentationData#TYPE} selects the
 * {@link ElementReader} that creates it. The element types are registered in
 * {@link #createElementTypes()}, the building blocks (cards, banners, ...) by {@link BlockReader},
 * the charts by {@link ChartReader}.
 * </p>
 */
public final class DescriptionReader {

	/**
	 * The {@link ElementReader}s by the element type names.
	 */
	private static final Map<String, ElementReader> ELEMENT_TYPES = createElementTypes();

	/**
	 * The names of the preset geometries.
	 */
	private static final Set<String> PRESETS = presets();

	/**
	 * The names of the supported fill types.
	 */
	private static final List<String> PAINT_TYPES = List.of(PaintInfo.Type.NONE.id(), PaintInfo.Type.SOLID.id(),
		PaintInfo.Type.GRADIENT.id(), PaintInfo.Type.PICTURE.id());

	/**
	 * The names of the numbering schemes of numbered bullets.
	 */
	private static final List<String> AUTO_NUMBER_SCHEMES = autoNumberSchemes();

	/**
	 * The highest number of a numbered bullet PowerPoint accepts.
	 */
	private static final int MAX_AUTO_NUMBER = 32767;

	private DescriptionReader() {
		// Static utility.
	}

	/**
	 * Registers the readers of all element types.
	 */
	private static Map<String, ElementReader> createElementTypes() {
		Map<String, ElementReader> result = new LinkedHashMap<>();
		result.put(TYPE_SHAPE, DescriptionReader::readShape);
		result.put(TYPE_TEXT_BOX, DescriptionReader::readTextBox);
		result.put(TYPE_TABLE, DescriptionReader::readTable);
		result.put(TYPE_IMAGE, DescriptionReader::readImage);
		result.put(TYPE_ROW, entries -> readContainer(entries, ContainerSpec.Direction.ROW));
		result.put(TYPE_COLUMN, entries -> readContainer(entries, ContainerSpec.Direction.COLUMN));
		BlockReader.register(result);
		ChartReader.register(result);
		return Collections.unmodifiableMap(result);
	}

	private static List<String> autoNumberSchemes() {
		List<String> result = new ArrayList<>();
		for (AutoNumberingScheme scheme : AutoNumberingScheme.values()) {
			result.add(scheme.name());
		}
		return Collections.unmodifiableList(result);
	}

	private static Set<String> presets() {
		Set<String> result = new HashSet<>();
		for (ShapeType type : ShapeType.values()) {
			String name = type.getOoxmlName();
			if (name != null) {
				result.add(name);
			}
		}
		return result;
	}

	/**
	 * The names of the element types that can be placed on a slide.
	 */
	public static Set<String> elementTypes() {
		return ELEMENT_TYPES.keySet();
	}

	/**
	 * Reads the slides of a presentation.
	 *
	 * @param slides
	 *        A list of slide descriptions, or a single slide description. Paths in errors start
	 *        with {@link PresentationData#SLIDES}.
	 * @return The slides.
	 */
	public static List<SlideSpec> readSlides(Object slides) {
		List<SlideSpec> result = new ArrayList<>();
		for (Value slide : new Value(slides, SLIDES).asListOrSingle()) {
			result.add(readSlide(slide));
		}
		return result;
	}

	/**
	 * Reads a slide.
	 *
	 * <p>
	 * A slide is a dictionary with the optional entries {@link PresentationData#TYPE} (only
	 * {@link PresentationData#TYPE_SLIDE}), {@link PresentationData#LAYOUT},
	 * {@link PresentationData#TITLE}, {@link PresentationData#SUBTITLE},
	 * {@link PresentationData#PLACEHOLDERS}, {@link PresentationData#ENTRIES} (a list of texts),
	 * {@link PresentationData#CONTENT},
	 * {@link PresentationData#NOTES} and {@link PresentationData#HIDDEN}.
	 * </p>
	 */
	public static SlideSpec readSlide(Value slide) {
		Entries entries = slide.asEntries();
		entries.get(TYPE).optOneOf(List.of(TYPE_SLIDE));
		LayoutRef layout = readLayout(entries.get(LAYOUT));
		TextSpec title = readText(entries.get(TITLE));
		TextSpec subtitle = readText(entries.get(SUBTITLE));
		Map<PlaceholderKey, TextSpec> placeholders = readPlaceholders(entries.get(PLACEHOLDERS));
		List<TextSpec> agendaEntries = new ArrayList<>();
		Value entriesValue = entries.get(ENTRIES);
		if (!entriesValue.isAbsent()) {
			for (Value entry : entriesValue.asList()) {
				agendaEntries.add(readText(entry.require(I18NConstants.EXPECTED_TEXT_CONTENT)));
			}
		}
		List<ElementSpec> content = readElements(entries.get(CONTENT));
		String notes = readNotes(entries.get(NOTES));
		Boolean hidden = entries.get(HIDDEN).optBoolean();
		entries.checkComplete();
		return new SlideSpec(layout, title, subtitle, placeholders, agendaEntries, content, notes,
			hidden != null && hidden.booleanValue());
	}

	/**
	 * Reads a slide layout reference.
	 *
	 * <p>
	 * A number is the index of the layout, a {@link LayoutRole#id() role name} a role, any other
	 * text the name of the layout.
	 * </p>
	 *
	 * @return The layout reference, <code>null</code> if the value is missing.
	 */
	public static LayoutRef readLayout(Value layout) {
		if (layout.isAbsent()) {
			return null;
		}
		if (layout.isNumber()) {
			return new LayoutRef.Index(layout.asInteger(0, Integer.MAX_VALUE));
		}
		if (layout.isString()) {
			String name = layout.asString();
			LayoutRole role = DataEnum.forId(LayoutRole.class, name);
			return role != null ? new LayoutRef.Role(role) : new LayoutRef.Name(name);
		}
		throw layout.invalid(
			I18NConstants.EXPECTED_LAYOUT__ROLES.fill(String.join(", ", DataEnum.ids(LayoutRole.class))));
	}

	private static Map<PlaceholderKey, TextSpec> readPlaceholders(Value placeholders) {
		if (placeholders.isAbsent()) {
			return Map.of();
		}
		Map<PlaceholderKey, TextSpec> result = new LinkedHashMap<>();
		for (Map.Entry<String, Value> entry : placeholders.asEntries().all().entrySet()) {
			Value text = entry.getValue().require(I18NConstants.EXPECTED_TEXT_CONTENT);
			result.put(placeholderKey(entry.getKey()), readText(text));
		}
		return result;
	}

	private static PlaceholderKey placeholderKey(String key) {
		if (!key.isEmpty() && key.chars().allMatch(Character::isDigit)) {
			try {
				return new PlaceholderKey.Index(Long.parseLong(key));
			} catch (NumberFormatException ex) {
				// Too long for an index, use as name.
			}
		}
		return new PlaceholderKey.Name(key);
	}

	private static String readNotes(Value notes) {
		if (notes.isAbsent()) {
			return null;
		}
		if (notes.isList()) {
			List<String> lines = new ArrayList<>();
			for (Value line : notes.asList()) {
				lines.add(text(line));
			}
			return String.join("\n", lines);
		}
		return text(notes);
	}

	/**
	 * Reads the elements of a slide.
	 *
	 * @param elements
	 *        A list of element descriptions, or a single element description.
	 * @return The elements, empty if the value is missing.
	 */
	public static List<ElementSpec> readElements(Value elements) {
		List<ElementSpec> result = new ArrayList<>();
		for (Value element : elements.asListOrSingle()) {
			result.add(readElement(element));
		}
		return result;
	}

	/**
	 * Reads an element of a slide.
	 *
	 * <p>
	 * The element is a dictionary whose entry {@link PresentationData#TYPE} names one of the
	 * {@link #elementTypes()}.
	 * </p>
	 */
	public static ElementSpec readElement(Value element) {
		if (!element.isMap()) {
			throw element.invalid(I18NConstants.EXPECTED_ELEMENT);
		}
		Entries entries = element.asEntries();
		Value typeValue = entries.require(TYPE, I18NConstants.EXPECTED_ELEMENT);
		String type = typeValue.asString();
		ElementReader reader = ELEMENT_TYPES.get(type);
		if (reader == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_TYPE__PATH_TYPE_TYPES.fill(element.path(), type,
				String.join(", ", ELEMENT_TYPES.keySet())));
		}
		ElementSpec result = reader.read(entries);
		entries.checkComplete();
		return result;
	}

	/**
	 * Reads a shape from the entries of the shape description: its bounds,
	 * {@link PresentationData#GEOMETRY}, {@link PresentationData#FILL},
	 * {@link PresentationData#LINE}, {@link PresentationData#SHADOW}, its text (see
	 * {@link #readElementText(Entries, InsetsSpec, boolean)}), {@link PresentationData#STYLE} and
	 * {@link PresentationData#ROLE}.
	 */
	private static ShapeSpec readShape(Entries entries) {
		Bounds bounds = readBounds(entries);
		GeometrySpec geometry = readGeometry(entries.get(GEOMETRY));
		PaintSpec fill = readFill(entries.get(FILL));
		LineSpec line = readLine(entries.get(LINE));
		ShadowSpec shadow = readShadow(entries.get(SHADOW));
		TextSpec text = readElementText(entries, null, true);
		String style = readStyleName(entries.get(STYLE));
		String role = readRole(entries.get(ROLE));
		return new ShapeSpec(bounds, geometry, fill, line, shadow, text, style, role);
	}

	private static TextBoxSpec readTextBox(Entries entries) {
		Bounds bounds = readBounds(entries);
		if (!entries.has(TEXT)) {
			throw entries.get(TEXT).missing(I18NConstants.EXPECTED_TEXT_CONTENT);
		}
		TextSpec text = readElementText(entries, InsetsSpec.ZERO, true);
		String role = readRole(entries.get(ROLE));
		return new TextBoxSpec(bounds, text, role);
	}

	/**
	 * Reads the name of a style of the template profile.
	 *
	 * @return The name, <code>null</code> if the value is missing.
	 */
	public static String readStyleName(Value style) {
		if (style.isAbsent()) {
			return null;
		}
		if (style.isString() && !style.asString().isEmpty()) {
			return style.asString();
		}
		throw style.invalid(I18NConstants.EXPECTED_STYLE);
	}

	/**
	 * Reads the name of a color role.
	 *
	 * @return The name, <code>null</code> if the value is missing.
	 */
	public static String readRole(Value role) {
		if (role.isAbsent()) {
			return null;
		}
		if (role.isString() && ColorSpec.ROLE_NAME.matcher(role.asString()).matches()) {
			return role.asString();
		}
		throw role.invalid(I18NConstants.EXPECTED_ROLE);
	}

	/**
	 * Reads a style of the template profile.
	 *
	 * <p>
	 * A style of a shape is a dictionary with the entries of a shape description except the
	 * bounds, the text content and the style: {@link PresentationData#ROLE} (the role of an element
	 * that gives none), {@link PresentationData#GEOMETRY}, {@link PresentationData#FILL},
	 * {@link PresentationData#LINE}, {@link PresentationData#SHADOW} and the text formatting (see
	 * {@link #readElementText(Entries, InsetsSpec, boolean)}). A style of a table cell has only the
	 * entries {@link PresentationData#FILL} and the text formatting of a cell.
	 * </p>
	 *
	 * @param style
	 *        The style description.
	 * @param shape
	 *        Whether the style is applied to shapes, otherwise to table cells.
	 */
	public static StyleSpec readStyle(Value style, boolean shape) {
		Entries entries = style.asEntries();
		String role = null;
		GeometrySpec geometry = null;
		LineSpec line = null;
		ShadowSpec shadow = null;
		if (shape) {
			role = readRole(entries.get(ROLE));
			geometry = readGeometry(entries.get(GEOMETRY));
			line = readLine(entries.get(LINE));
			shadow = readShadow(entries.get(SHADOW));
		}
		PaintSpec fill = readFill(entries.get(FILL));
		TextSpec text = readTextFormat(entries, shape);
		entries.checkComplete();
		return new StyleSpec(role, geometry, fill, line, shadow, text,
			Collections.unmodifiableMap(PresentationData.merge((Map<?, ?>) style.raw(), null)));
	}

	private static TableSpec readTable(Entries entries) {
		Bounds bounds = readBounds(entries);
		List<Double> weights = readNumbers(entries.get(COLUMN_WEIGHTS));
		List<Double> widths = readNumbers(entries.get(COLUMN_WIDTHS));
		if (weights != null && widths != null) {
			throw entries.exclusive(COLUMN_WIDTHS, COLUMN_WEIGHTS);
		}
		List<Value> rowValues = entries.require(ROWS, I18NConstants.EXPECTED_LIST).asList();
		int columns = weights != null ? weights.size() : widths != null ? widths.size() : -1;
		List<List<CellSpec>> rows = new ArrayList<>(rowValues.size());
		for (Value rowValue : rowValues) {
			List<Value> cellValues = rowValue.asList();
			if (columns < 0) {
				columns = cellValues.size();
			}
			if (cellValues.size() != columns) {
				throw rowValue.invalid(I18NConstants.EXPECTED_ROW_LENGTH__COUNT.fill(columns));
			}
			List<CellSpec> row = new ArrayList<>(cellValues.size());
			for (Value cellValue : cellValues) {
				row.add(readCell(cellValue));
			}
			rows.add(row);
		}
		Integer headerRows = entries.get(HEADER_ROWS).optInteger(0, rows.size());
		Double rowHeight = entries.get(ROW_HEIGHT).optPositive();
		return new TableSpec(bounds, weights, widths, rows, headerRows == null ? 0 : headerRows.intValue(),
			rowHeight);
	}

	private static List<Double> readNumbers(Value numbers) {
		if (numbers.isAbsent()) {
			return null;
		}
		List<Double> result = new ArrayList<>();
		for (Value number : numbers.asList()) {
			result.add(Double.valueOf(number.asPositive()));
		}
		return result;
	}

	/**
	 * Reads a table cell.
	 *
	 * <p>
	 * A dictionary without the entry {@link PresentationData#RUNS} describes the cell: its entries
	 * are {@link PresentationData#TEXT}, {@link PresentationData#FILL}, the character formatting,
	 * {@link PresentationData#ALIGN}, {@link PresentationData#ANCHOR},
	 * {@link PresentationData#INSETS}, {@link PresentationData#OVERFLOW} and
	 * {@link PresentationData#RATING}. Any other value is the text of the cell.
	 * </p>
	 */
	public static CellSpec readCell(Value cell) {
		if (cell.isAbsent()) {
			return CellSpec.EMPTY;
		}
		if (cell.isMap()) {
			Entries entries = cell.asEntries();
			if (!entries.contains(RUNS)) {
				TextSpec text = readElementText(entries, null, false);
				PaintSpec fill = readFill(entries.get(FILL));
				String rating = readStyleName(entries.get(RATING));
				entries.checkComplete();
				return new CellSpec(text, fill, rating);
			}
		}
		return new CellSpec(readText(cell), null);
	}

	private static ImageSpec readImage(Entries entries) {
		Bounds bounds = readBounds(entries);
		Value data = entries.require(DATA, I18NConstants.EXPECTED_BINARY);
		ImageFit fit = entries.get(FIT).optEnum(ImageFit.class);
		return new ImageSpec(bounds, data.asBinary(), fit == null ? ImageFit.CONTAIN : fit);
	}

	/**
	 * Reads a row or column from the entries {@link PresentationData#CHILDREN},
	 * {@link PresentationData#GAP}, {@link PresentationData#WEIGHTS}, {@link PresentationData#ALIGN},
	 * for a column {@link PresentationData#SPLIT}, and the bounds of the container.
	 *
	 * <p>
	 * The children must not give a position, since the container places them.
	 * </p>
	 */
	private static ContainerSpec readContainer(Entries entries, ContainerSpec.Direction direction) {
		Bounds bounds = readBounds(entries);
		List<ElementSpec> children = new ArrayList<>();
		for (Value child : entries.require(CHILDREN, I18NConstants.EXPECTED_LIST).asListOrSingle()) {
			children.add(readChild(child));
		}
		Double gap = entries.get(GAP).optNonNegative();
		Value weightsValue = entries.get(WEIGHTS);
		List<Double> weights = readNumbers(weightsValue);
		if (weights != null && weights.size() != children.size()) {
			throw weightsValue.invalid(I18NConstants.EXPECTED_WEIGHTS__COUNT.fill(children.size()));
		}
		CrossAlign align = entries.get(ALIGN).optEnum(CrossAlign.class);
		Boolean split = direction == ContainerSpec.Direction.COLUMN ? entries.get(SPLIT).optBoolean() : null;
		return new ContainerSpec(bounds, direction, children, gap, weights, align,
			split != null && split.booleanValue());
	}

	/**
	 * Reads an element placed by a row or column, which must not give a position.
	 */
	static ElementSpec readChild(Value child) {
		ElementSpec element = readElement(child);
		Bounds childBounds = element.bounds();
		if (childBounds.isPositioned()) {
			String key = childBounds.x() != null ? X : Y;
			throw new TopLogicException(I18NConstants.ERROR_POSITION_IN_CONTAINER__PATH.fill(child.path() + "." + key));
		}
		return element;
	}

	/**
	 * Reads the position and size of an element from the entries {@link PresentationData#X},
	 * {@link PresentationData#Y}, {@link PresentationData#WIDTH}, {@link PresentationData#HEIGHT}
	 * (a number or {@link PresentationData#AUTO}) and {@link PresentationData#WEIGHT}.
	 */
	public static Bounds readBounds(Entries entries) {
		Double x = entries.get(X).optNumber();
		Double y = entries.get(Y).optNumber();
		Double width = entries.get(WIDTH).optNonNegative();
		Value heightValue = entries.get(HEIGHT);
		boolean autoHeight = heightValue.isString() && AUTO.equals(heightValue.asString());
		Double height;
		if (autoHeight) {
			height = null;
		} else if (heightValue.isAbsent() || heightValue.isNumber()) {
			height = heightValue.optNonNegative();
		} else {
			throw heightValue.invalid(I18NConstants.EXPECTED_HEIGHT);
		}
		Double weight = entries.get(WEIGHT).optPositive();
		return new Bounds(x, y, width, height, autoHeight, weight);
	}

	/**
	 * Reads a geometry: the name of a preset geometry, or a dictionary with the entries
	 * {@link PresentationData#PRESET}, {@link PresentationData#ADJUST} and
	 * {@link PresentationData#CORNER_RADIUS}.
	 *
	 * @return The geometry, <code>null</code> if the value is missing.
	 */
	public static GeometrySpec readGeometry(Value geometry) {
		if (geometry.isAbsent()) {
			return null;
		}
		if (geometry.isString()) {
			return new GeometrySpec(preset(geometry), Map.of(), null);
		}
		if (geometry.isMap()) {
			Entries entries = geometry.asEntries();
			Value presetValue = entries.get(PRESET);
			String preset = presetValue.isAbsent() ? null : preset(presetValue);
			Map<String, Double> adjust = new LinkedHashMap<>();
			Value adjustValue = entries.get(ADJUST);
			if (!adjustValue.isAbsent()) {
				for (Map.Entry<String, Value> entry : adjustValue.asEntries().all().entrySet()) {
					adjust.put(entry.getKey(), Double.valueOf(entry.getValue().asNumber()));
				}
			}
			Double cornerRadius = entries.get(CORNER_RADIUS).optNonNegative();
			entries.checkComplete();
			if (preset == null) {
				preset = cornerRadius != null ? GeometrySpec.ROUND_RECT : GeometrySpec.RECT;
			}
			return new GeometrySpec(preset, adjust, cornerRadius);
		}
		throw geometry.invalid(I18NConstants.EXPECTED_GEOMETRY);
	}

	private static String preset(Value preset) {
		if (preset.isString() && PRESETS.contains(preset.asString())) {
			return preset.asString();
		}
		throw preset.invalid(I18NConstants.EXPECTED_GEOMETRY);
	}

	/**
	 * Reads a color.
	 *
	 * <p>
	 * A color is a name as described at {@link ColorSpec#parse(String, Map)}, or a dictionary
	 * with one of the entries {@link PresentationData#ROLE}, {@link PresentationData#SCHEME} or
	 * {@link PresentationData#RGB} and the modifications {@link ColorInfo#MODIFIERS}. If a
	 * dictionary has several of them, the first one in this order counts, so that the color
	 * descriptions of a template analysis, which name the theme color and its resolved RGB value,
	 * keep their reference to the theme.
	 * </p>
	 *
	 * @return The color, <code>null</code> if the value is missing.
	 */
	public static ColorSpec readColor(Value color) {
		if (color.isAbsent()) {
			return null;
		}
		if (color.isString()) {
			ColorSpec result = ColorSpec.parse(color.asString(), Map.of());
			if (result == null) {
				throw color.invalid(I18NConstants.EXPECTED_COLOR);
			}
			return result;
		}
		if (color.isMap()) {
			Entries entries = color.asEntries();
			Value role = entries.get(ROLE);
			Value scheme = entries.get(SCHEME);
			Value rgb = entries.get(RGB);
			if (!role.isAbsent() && !(role.isString() && ColorSpec.ROLE_NAME.matcher(role.asString()).matches())) {
				throw role.invalid(I18NConstants.EXPECTED_COLOR);
			}
			scheme.optOneOf(ColorSpec.SCHEME_COLORS);
			if (!rgb.isAbsent() && !(rgb.isString() && ColorSpec.HEX.matcher(rgb.asString()).matches())) {
				throw rgb.invalid(I18NConstants.EXPECTED_COLOR);
			}
			Map<String, Double> modifiers = readModifiers(entries);
			entries.checkComplete();
			String name = !role.isAbsent() ? role.asString() : !scheme.isAbsent() ? scheme.asString() : rgb.optString();
			if (name == null) {
				throw color.invalid(I18NConstants.EXPECTED_COLOR);
			}
			if (!role.isAbsent()) {
				return new ColorSpec.Role(name, modifiers);
			}
			return ColorSpec.parse(name, modifiers);
		}
		throw color.invalid(I18NConstants.EXPECTED_COLOR);
	}

	private static Map<String, Double> readModifiers(Entries entries) {
		Map<String, Double> result = new LinkedHashMap<>();
		for (String modifier : ColorInfo.MODIFIERS) {
			Value value = entries.get(modifier);
			Double number;
			switch (modifier) {
				case ColorInfo.LUM_MOD, ColorInfo.SAT_MOD -> number = value.optNonNegative();
				case ColorInfo.LUM_OFF, ColorInfo.SAT_OFF -> number = value.optRange(-1, 1);
				default -> number = value.optRange(0, 1);
			}
			if (number != null) {
				result.put(modifier, number);
			}
		}
		return result;
	}

	/**
	 * Reads a fill.
	 *
	 * <p>
	 * A fill is {@link PresentationData#NONE}, a color, or a dictionary with the entry
	 * {@link PresentationData#TYPE} naming the {@link PaintInfo.Type kind} of fill (except
	 * patterns) and its properties. A dictionary without the entry {@link PresentationData#TYPE} is
	 * a color.
	 * </p>
	 *
	 * @return The fill, <code>null</code> if the value is missing.
	 */
	public static PaintSpec readFill(Value fill) {
		if (fill.isAbsent()) {
			return null;
		}
		if (fill.isString()) {
			if (NONE.equals(fill.asString())) {
				return PaintSpec.NONE;
			}
			ColorSpec color = ColorSpec.parse(fill.asString(), Map.of());
			if (color == null) {
				throw fill.invalid(I18NConstants.EXPECTED_FILL);
			}
			return new PaintSpec.Solid(color);
		}
		if (fill.isMap()) {
			Entries entries = fill.asEntries();
			if (!entries.contains(TYPE)) {
				return new PaintSpec.Solid(readColor(fill));
			}
			PaintSpec result = readPaint(entries);
			entries.checkComplete();
			return result;
		}
		throw fill.invalid(I18NConstants.EXPECTED_FILL);
	}

	/**
	 * Reads a fill from a dictionary with the entry {@link PresentationData#TYPE}.
	 */
	private static PaintSpec readPaint(Entries entries) {
		String type = entries.get(TYPE).asOneOf(PAINT_TYPES);
		if (PaintInfo.Type.NONE.id().equals(type)) {
			return PaintSpec.NONE;
		} else if (PaintInfo.Type.SOLID.id().equals(type)) {
			return new PaintSpec.Solid(readColor(entries.require(COLOR, I18NConstants.EXPECTED_COLOR)));
		} else if (PaintInfo.Type.GRADIENT.id().equals(type)) {
			List<PaintSpec.Stop> stops = readStops(entries.require(STOPS, I18NConstants.EXPECTED_STOPS));
			Double angle = entries.get(ANGLE).optNumber();
			String path = entries.get(PATH).optOneOf(PaintSpec.Gradient.PATHS);
			if (angle != null && path != null) {
				throw entries.exclusive(PATH, ANGLE);
			}
			if (angle == null && path == null) {
				angle = Double.valueOf(0);
			}
			return new PaintSpec.Gradient(stops, angle, path);
		} else {
			Value data = entries.require(DATA, I18NConstants.EXPECTED_BINARY);
			Double opacity = entries.get(OPACITY).optRange(0, 1);
			return new PaintSpec.Picture(data.asBinary(), opacity);
		}
	}

	private static List<PaintSpec.Stop> readStops(Value stops) {
		if (!stops.isList()) {
			throw stops.invalid(I18NConstants.EXPECTED_STOPS);
		}
		List<Value> values = stops.asList();
		if (values.size() < 2) {
			throw stops.invalid(I18NConstants.EXPECTED_STOPS);
		}
		List<PaintSpec.Stop> result = new ArrayList<>(values.size());
		for (int n = 0; n < values.size(); n++) {
			Value stop = values.get(n);
			if (stop.isMap() && stop.asEntries().contains(POSITION)) {
				Entries entries = stop.asEntries();
				double position = entries.get(POSITION).asRange(0, 1);
				ColorSpec color = readColor(entries.require(COLOR, I18NConstants.EXPECTED_COLOR));
				entries.checkComplete();
				result.add(new PaintSpec.Stop(position, color));
			} else {
				ColorSpec color = readColor(stop.require(I18NConstants.EXPECTED_COLOR));
				result.add(new PaintSpec.Stop((double) n / (values.size() - 1), color));
			}
		}
		return result;
	}

	/**
	 * Reads an outline.
	 *
	 * <p>
	 * An outline is {@link PresentationData#NONE}, a color, or a dictionary with the entries
	 * {@link PresentationData#COLOR}, {@link PresentationData#WIDTH} and
	 * {@link PresentationData#DASH}. Instead of a color, the dictionary may describe the fill of
	 * the line with the entry {@link PresentationData#TYPE}, like the outlines reported by the
	 * template analysis.
	 * </p>
	 *
	 * @return The outline, <code>null</code> if the value is missing.
	 */
	public static LineSpec readLine(Value line) {
		if (line.isAbsent()) {
			return null;
		}
		if (line.isString()) {
			if (NONE.equals(line.asString())) {
				return LineSpec.NONE;
			}
			ColorSpec color = ColorSpec.parse(line.asString(), Map.of());
			if (color == null) {
				throw line.invalid(I18NConstants.EXPECTED_LINE);
			}
			return new LineSpec(new PaintSpec.Solid(color), null, null);
		}
		if (line.isMap()) {
			Entries entries = line.asEntries();
			PaintSpec paint;
			if (entries.contains(TYPE)) {
				paint = readPaint(entries);
			} else {
				ColorSpec color = readColor(entries.get(COLOR));
				paint = color == null ? null : new PaintSpec.Solid(color);
			}
			Double width = entries.get(WIDTH).optNonNegative();
			String dash = entries.get(DASH).optOneOf(LineSpec.DASHES);
			entries.checkComplete();
			return new LineSpec(paint, width, dash);
		}
		throw line.invalid(I18NConstants.EXPECTED_LINE);
	}

	/**
	 * Reads a shadow.
	 *
	 * <p>
	 * A shadow is a dictionary with the entries {@link PresentationData#COLOR},
	 * {@link PresentationData#BLUR}, {@link PresentationData#DISTANCE} and
	 * {@link PresentationData#DIRECTION}, each optional. The truth value <code>true</code> is the
	 * default shadow, <code>false</code> no shadow, also not the shadow of a style.
	 * </p>
	 *
	 * @return The shadow, {@link ShadowSpec#NONE} for <code>false</code>, <code>null</code> if the
	 *         value is missing.
	 */
	public static ShadowSpec readShadow(Value shadow) {
		if (shadow.isAbsent()) {
			return null;
		}
		if (shadow.isBoolean()) {
			return shadow.asBoolean() ? new ShadowSpec(ShadowSpec.DEFAULT_COLOR, ShadowSpec.DEFAULT_BLUR,
				ShadowSpec.DEFAULT_DISTANCE, ShadowSpec.DEFAULT_DIRECTION) : ShadowSpec.NONE;
		}
		if (shadow.isMap()) {
			Entries entries = shadow.asEntries();
			ColorSpec color = readColor(entries.get(COLOR));
			Double blur = entries.get(BLUR).optNonNegative();
			Double distance = entries.get(DISTANCE).optNonNegative();
			Double direction = entries.get(DIRECTION).optNumber();
			entries.checkComplete();
			return new ShadowSpec(
				color == null ? ShadowSpec.DEFAULT_COLOR : color,
				blur == null ? ShadowSpec.DEFAULT_BLUR : blur.doubleValue(),
				distance == null ? ShadowSpec.DEFAULT_DISTANCE : distance.doubleValue(),
				direction == null ? ShadowSpec.DEFAULT_DIRECTION : direction.doubleValue());
		}
		throw shadow.invalid(I18NConstants.EXPECTED_SHADOW);
	}

	/**
	 * Reads the text of an element from its entries: the text in {@link PresentationData#TEXT},
	 * the character formatting of {@link #readRunStyle(Entries)}, {@link PresentationData#ALIGN},
	 * {@link PresentationData#ANCHOR}, {@link PresentationData#INSETS},
	 * {@link PresentationData#OVERFLOW} and, for shapes, {@link PresentationData#SPLIT} and
	 * {@link PresentationData#WRAP}.
	 *
	 * @param entries
	 *        The entries of the element.
	 * @param defaultInsets
	 *        The inner margins if the entry {@link PresentationData#INSETS} is not given,
	 *        <code>null</code> to inherit them.
	 * @param shapeBody
	 *        Whether the entries {@link PresentationData#SPLIT} and {@link PresentationData#WRAP}
	 *        are supported.
	 * @return The text, <code>null</code> if no text and no text property is given.
	 */
	public static TextSpec readElementText(Entries entries, InsetsSpec defaultInsets, boolean shapeBody) {
		Value textValue = entries.get(TEXT);
		List<ParagraphSpec> paragraphs = textValue.isAbsent() ? null : readParagraphs(textValue);
		TextSpec format = readTextFormat(entries, shapeBody);
		if (paragraphs == null && format == null) {
			return null;
		}
		if (format == null) {
			format = TextSpec.of(List.of());
		}
		return new TextSpec(paragraphs == null ? List.of() : paragraphs, format.style(), format.align(),
			format.anchor(), format.insets() == null ? defaultInsets : format.insets(), format.overflow(),
			format.split(), format.wrap());
	}

	/**
	 * Reads the formatting of a text from the entries of its element: the character formatting of
	 * {@link #readRunStyle(Entries)}, {@link PresentationData#ALIGN},
	 * {@link PresentationData#ANCHOR}, {@link PresentationData#INSETS},
	 * {@link PresentationData#OVERFLOW} and, for shapes, {@link PresentationData#SPLIT} and
	 * {@link PresentationData#WRAP}.
	 *
	 * @return The formatting as text without paragraphs, <code>null</code> if no text property is
	 *         given.
	 */
	private static TextSpec readTextFormat(Entries entries, boolean shapeBody) {
		RunStyle style = readRunStyle(entries);
		TextAlign align = entries.get(ALIGN).optEnum(TextAlign.class);
		TextAnchor anchor = entries.get(ANCHOR).optEnum(TextAnchor.class);
		InsetsSpec insets = readInsets(entries.get(INSETS));
		Overflow overflow = entries.get(OVERFLOW).optEnum(Overflow.class);
		Boolean split = shapeBody ? entries.get(SPLIT).optBoolean() : null;
		Boolean wrap = shapeBody ? entries.get(WRAP).optBoolean() : null;
		if (style.isInherit() && align == null && anchor == null && insets == null && overflow == null
			&& split == null && wrap == null) {
			return null;
		}
		return new TextSpec(List.of(), style, align, anchor, insets, overflow, split != null && split.booleanValue(),
			wrap);
	}

	/**
	 * Reads inner margins: a number for all sides, or a dictionary with the entries
	 * {@link PresentationData#LEFT}, {@link PresentationData#TOP}, {@link PresentationData#RIGHT}
	 * and {@link PresentationData#BOTTOM}, missing sides being zero.
	 *
	 * @return The margins, <code>null</code> if the value is missing.
	 */
	public static InsetsSpec readInsets(Value insets) {
		if (insets.isAbsent()) {
			return null;
		}
		if (insets.isNumber()) {
			double all = insets.asNonNegative();
			return new InsetsSpec(all, all, all, all);
		}
		if (insets.isMap()) {
			Entries entries = insets.asEntries();
			InsetsSpec result = new InsetsSpec(
				orZero(entries.get(LEFT).optNonNegative()),
				orZero(entries.get(TOP).optNonNegative()),
				orZero(entries.get(RIGHT).optNonNegative()),
				orZero(entries.get(BOTTOM).optNonNegative()));
			entries.checkComplete();
			return result;
		}
		throw insets.invalid(I18NConstants.EXPECTED_INSETS);
	}

	private static double orZero(Double value) {
		return value == null ? 0 : value.doubleValue();
	}

	/**
	 * Reads a text that is not part of an element, e.g. a title or a placeholder text.
	 *
	 * @return The text, <code>null</code> if the value is missing.
	 * @see #readParagraphs(Value)
	 */
	public static TextSpec readText(Value text) {
		if (text.isAbsent()) {
			return null;
		}
		return TextSpec.of(readParagraphs(text));
	}

	/**
	 * Reads the paragraphs of a text.
	 *
	 * <p>
	 * A text is a string, each line of which is a paragraph, a paragraph dictionary (with the entry
	 * {@link PresentationData#RUNS}), a run dictionary (with the entry
	 * {@link PresentationData#TEXT}) forming a paragraph of its own, or a list of those. A number
	 * is accepted as text.
	 * </p>
	 */
	public static List<ParagraphSpec> readParagraphs(Value text) {
		List<ParagraphSpec> result = new ArrayList<>();
		if (text.isList()) {
			for (Value item : text.asList()) {
				if (item.isList()) {
					throw item.invalid(I18NConstants.EXPECTED_TEXT_CONTENT);
				}
				addParagraphs(result, item);
			}
		} else {
			addParagraphs(result, text);
		}
		return result;
	}

	private static void addParagraphs(List<ParagraphSpec> result, Value text) {
		if (text.isString()) {
			for (String line : text.asString().split("\r\n|\r|\n", -1)) {
				result.add(ParagraphSpec.plain(line));
			}
		} else if (text.isNumber()) {
			result.add(ParagraphSpec.plain(text(text)));
		} else if (text.isMap()) {
			Entries entries = text.asEntries();
			if (entries.contains(RUNS)) {
				result.add(readParagraph(entries));
			} else if (entries.contains(TEXT)) {
				result.add(ParagraphSpec.of(List.of(readRun(entries))));
			} else {
				throw text.invalid(I18NConstants.EXPECTED_TEXT_CONTENT);
			}
		} else {
			throw text.invalid(I18NConstants.EXPECTED_TEXT_CONTENT);
		}
	}

	private static ParagraphSpec readParagraph(Entries entries) {
		List<RunSpec> runs = readRuns(entries.get(RUNS));
		RunStyle style = readRunStyle(entries);
		TextAlign align = entries.get(ALIGN).optEnum(TextAlign.class);
		Integer level = entries.get(LEVEL).optInteger(0, ParagraphSpec.MAX_LEVEL);
		BulletSpec bullet = readBullet(entries.get(BULLET));
		Double spaceBefore = entries.get(SPACE_BEFORE).optNonNegative();
		Double spaceAfter = entries.get(SPACE_AFTER).optNonNegative();
		Double lineSpacing = entries.get(LINE_SPACING).optPositive();
		entries.checkComplete();
		return new ParagraphSpec(runs, style, align, level, bullet, spaceBefore, spaceAfter, lineSpacing);
	}

	private static List<RunSpec> readRuns(Value runs) {
		List<RunSpec> result = new ArrayList<>();
		if (runs.isList()) {
			for (Value run : runs.asList()) {
				result.add(readRun(run));
			}
		} else {
			result.add(readRun(runs));
		}
		return result;
	}

	private static RunSpec readRun(Value run) {
		if (run.isString() || run.isNumber()) {
			return RunSpec.plain(text(run));
		}
		if (run.isMap()) {
			return readRun(run.asEntries());
		}
		throw run.invalid(I18NConstants.EXPECTED_RUNS);
	}

	private static RunSpec readRun(Entries entries) {
		String text = text(entries.require(TEXT, I18NConstants.EXPECTED_TEXT));
		RunStyle style = readRunStyle(entries);
		String hyperlink = entries.get(HYPERLINK).optString();
		entries.checkComplete();
		return new RunSpec(text, style, hyperlink);
	}

	/**
	 * Reads the character formatting from the entries {@link PresentationData#BOLD},
	 * {@link PresentationData#ITALIC}, {@link PresentationData#UNDERLINE},
	 * {@link PresentationData#CAPS}, {@link PresentationData#SIZE}, {@link PresentationData#COLOR},
	 * {@link PresentationData#FONT} and {@link PresentationData#CHARACTER_SPACING}.
	 */
	public static RunStyle readRunStyle(Entries entries) {
		return new RunStyle(
			entries.get(BOLD).optBoolean(),
			entries.get(ITALIC).optBoolean(),
			entries.get(UNDERLINE).optBoolean(),
			entries.get(CAPS).optBoolean(),
			entries.get(SIZE).optRange(TextMeasure.MIN_FONT_SIZE, TextMeasure.MAX_FONT_SIZE),
			readColor(entries.get(COLOR)),
			entries.get(FONT).optString(),
			entries.get(CHARACTER_SPACING).optNumber());
	}

	/**
	 * Reads a bullet: a truth value switching the default bullet on or off,
	 * {@link PresentationData#NONE}, the bullet character, or a dictionary with the entries
	 * {@link PresentationData#CHAR} or {@link PresentationData#AUTO_NUMBER} (the name of an
	 * {@link AutoNumberingScheme}), {@link PresentationData#START} (the number of a numbered
	 * paragraph) and {@link PresentationData#COLOR}.
	 *
	 * @return The bullet, <code>null</code> if the value is missing.
	 */
	public static BulletSpec readBullet(Value bullet) {
		if (bullet.isAbsent()) {
			return null;
		}
		if (bullet.isBoolean()) {
			return bullet.asBoolean() ? BulletSpec.DEFAULT : BulletSpec.NONE;
		}
		if (bullet.isString() && !bullet.asString().isEmpty()) {
			return NONE.equals(bullet.asString()) ? BulletSpec.NONE : new BulletSpec(true, bullet.asString(), null);
		}
		if (bullet.isMap()) {
			Entries entries = bullet.asEntries();
			String character = entries.get(CHAR).optString();
			AutoNumberingScheme autoNumber = readAutoNumber(entries.get(AUTO_NUMBER));
			if (character != null && autoNumber != null) {
				throw entries.exclusive(AUTO_NUMBER, CHAR);
			}
			Integer start = entries.get(START).optInteger(1, MAX_AUTO_NUMBER);
			ColorSpec color = readColor(entries.get(COLOR));
			entries.checkComplete();
			return new BulletSpec(true, character, color, autoNumber, start);
		}
		throw bullet.invalid(I18NConstants.EXPECTED_BULLET);
	}

	private static AutoNumberingScheme readAutoNumber(Value autoNumber) {
		String name = autoNumber.optOneOf(AUTO_NUMBER_SCHEMES);
		return name == null ? null : AutoNumberingScheme.valueOf(name);
	}

	/**
	 * The text of a string or number value.
	 */
	static String text(Value value) {
		if (value.isNumber()) {
			return BigDecimal.valueOf(value.asNumber()).stripTrailingZeros().toPlainString();
		}
		return value.asString();
	}

}

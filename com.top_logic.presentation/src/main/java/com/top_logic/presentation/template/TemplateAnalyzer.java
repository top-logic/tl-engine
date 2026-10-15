/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.ParagraphPropertyFetcher;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFColor;
import org.apache.poi.xslf.usermodel.XSLFConnectorShape;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableStyle;
import org.apache.poi.xslf.usermodel.XSLFTableStyles;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFTheme;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectStyleItem;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPatternFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

/**
 * Builds a {@link TemplateAnalysis} from a presentation.
 *
 * <p>
 * Where the POI object model drops information that a template-specific shape must reproduce
 * (theme color references with their modifications, gradient stops, shadows, references to the
 * style matrix of the theme), the analysis reads the XML of the presentation parts directly.
 * </p>
 */
final class TemplateAnalyzer {

	private static final String PML_NS = "http://schemas.openxmlformats.org/presentationml/2006/main";

	private static final String DML_NS = "http://schemas.openxmlformats.org/drawingml/2006/main";

	/**
	 * Placeholder type of a placeholder without explicit type.
	 */
	private static final String DEFAULT_PLACEHOLDER_TYPE = "body";

	/**
	 * Name of the placeholder color in theme styles that is replaced by the color of the
	 * referencing shape.
	 */
	private static final String PLACEHOLDER_COLOR = "phClr";

	/**
	 * Preset geometry name of a rounded rectangle.
	 */
	private static final String ROUND_RECT = "roundRect";

	/**
	 * Default corner adjust value of a rounded rectangle.
	 */
	private static final double ROUND_RECT_DEFAULT_ADJUST = 16667;

	/**
	 * Index offset of background fills in the style matrix of a theme.
	 */
	private static final int BACKGROUND_FILL_OFFSET = 1000;

	/**
	 * Value of the text capitalization attribute for all capitals.
	 */
	private static final String CAPS_ALL = "all";

	private static final String THEME_MAJOR_LATIN = "+mj-lt";

	private static final String THEME_MINOR_LATIN = "+mn-lt";

	private final XMLSlideShow _slideShow;

	private final Map<XSLFSlideMaster, Integer> _masterIndex = new HashMap<>();

	private final Map<XSLFSlideLayout, Integer> _layoutIndex = new HashMap<>();

	private Map<String, String> _tableStyleNames;

	/**
	 * Creates a {@link TemplateAnalyzer}.
	 */
	TemplateAnalyzer(XMLSlideShow slideShow) {
		_slideShow = slideShow;
	}

	/**
	 * Analyzes the presentation.
	 */
	TemplateAnalysis analyze() {
		Dimension size = _slideShow.getPageSize();

		List<MasterInfo> masters = new ArrayList<>();
		for (XSLFSlideMaster master : _slideShow.getSlideMasters()) {
			int index = masters.size();
			_masterIndex.put(master, index);
			masters.add(master(index, master));
		}

		List<LayoutInfo> layouts = new ArrayList<>();
		for (XSLFSlideLayout layout : TemplateAnalysis.layouts(_slideShow)) {
			int index = layouts.size();
			_layoutIndex.put(layout, index);
			layouts.add(layout(index, layout, masters));
		}

		List<SlideInfo> slides = new ArrayList<>();
		for (XSLFSlide slide : _slideShow.getSlides()) {
			slides.add(slide(slides.size(), slide));
		}

		return new TemplateAnalysis(size.getWidth(), size.getHeight(), masters, layouts, slides);
	}

	private MasterInfo master(int index, XSLFSlideMaster master) {
		ThemeInfo theme = theme(master);
		String name = master.getXmlObject().getCSld().getName();
		if (name == null || name.isEmpty()) {
			name = theme.name();
		}
		List<PlaceholderInfo> placeholders = new ArrayList<>();
		List<ShapeInfo> decorations = new ArrayList<>();
		sheetShapes(master, placeholders, decorations);
		return new MasterInfo(index, name, theme, background(master, master.getXmlObject().getCSld()),
			placeholders, decorations);
	}

	private LayoutInfo layout(int index, XSLFSlideLayout layout, List<MasterInfo> masters) {
		XSLFSlideMaster master = layout.getSlideMaster();
		Integer masterIndex = _masterIndex.get(master);
		int masterPos = masterIndex == null ? -1 : masterIndex.intValue();
		String masterName = masterPos < 0 ? null : masters.get(masterPos).name();

		SlideLayout type = layout.getType();
		if (type == SlideLayout.CUST) {
			type = null;
		}

		PaintInfo background = background(layout, layout.getXmlObject().getCSld());
		if (background == null && masterPos >= 0) {
			background = masters.get(masterPos).background();
		}

		List<PlaceholderInfo> placeholders = new ArrayList<>();
		List<ShapeInfo> decorations = new ArrayList<>();
		sheetShapes(layout, placeholders, decorations);
		return new LayoutInfo(index, layout.getName(), type, masterPos, masterName, background, placeholders,
			decorations);
	}

	private SlideInfo slide(int index, XSLFSlide slide) {
		XSLFSlideLayout layout = slide.getSlideLayout();
		Integer layoutIndex = _layoutIndex.get(layout);
		List<ShapeInfo> shapes = new ArrayList<>();
		for (XSLFShape shape : slide.getShapes()) {
			shapes.add(shape(shape, new AffineTransform()));
		}
		return new SlideInfo(index, layoutIndex == null ? -1 : layoutIndex.intValue(),
			layout == null ? null : layout.getName(), background(slide, slide.getXmlObject().getCSld()), shapes);
	}

	private void sheetShapes(XSLFSheet sheet, List<PlaceholderInfo> placeholders, List<ShapeInfo> decorations) {
		for (XSLFShape shape : sheet.getShapes()) {
			CTPlaceholder ph = placeholder(shape);
			if (ph != null) {
				placeholders.add(placeholderInfo(shape, ph));
			} else {
				decorations.add(shape(shape, new AffineTransform()));
			}
		}
	}

	private ThemeInfo theme(XSLFSlideMaster master) {
		XSLFTheme theme = master.getTheme();
		if (theme == null) {
			return new ThemeInfo(null, null, Map.of(), null, null);
		}
		CTBaseStyles elements = theme.getXmlObject().getThemeElements();
		String schemeName = elements == null || elements.getClrScheme() == null ? null : elements.getClrScheme().getName();
		Map<String, String> colors = new LinkedHashMap<>();
		for (String name : ThemeInfo.COLOR_NAMES) {
			CTColor color = theme.getCTColor(name);
			if (color != null) {
				String rgb = rgb(new XSLFColor(color, theme, null, master).getColor());
				if (rgb != null) {
					colors.put(name, rgb);
				}
			}
		}
		return new ThemeInfo(theme.getName(), schemeName, colors, theme.getMajorFont(), theme.getMinorFont());
	}

	private PaintInfo background(XSLFSheet sheet, CTCommonSlideData data) {
		if (data == null || !data.isSetBg()) {
			return null;
		}
		CTBackground bg = data.getBg();
		if (bg.isSetBgPr()) {
			return readPaint(bg.getBgPr(), null, sheet);
		}
		if (bg.isSetBgRef()) {
			CTStyleMatrixReference ref = bg.getBgRef();
			XmlObject fill = themeFill(sheet, (int) ref.getIdx());
			if (fill != null) {
				return paint(fill, ref.isSetSchemeClr() ? ref.getSchemeClr() : null, sheet);
			}
		}
		return null;
	}

	private PlaceholderInfo placeholderInfo(XSLFShape shape, CTPlaceholder ph) {
		TextStyle text = null;
		String prompt = null;
		if (shape instanceof XSLFTextShape textShape) {
			List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
			if (!paragraphs.isEmpty()) {
				XSLFTextParagraph paragraph = firstParagraph(paragraphs);
				text = textStyle(textShape, paragraph, firstRun(paragraph));
			}
			prompt = nonEmpty(TextInfo.shorten(textShape.getText()));
		}
		return new PlaceholderInfo(placeholderType(ph), placeholderIdx(ph), shape.getShapeName(), shape.getAnchor(),
			text, prompt);
	}

	private ShapeInfo shape(XSLFShape shape, AffineTransform transform) {
		Rectangle2D anchor = shape.getAnchor();
		Rectangle2D bounds = anchor == null ? null : transform.createTransformedShape(anchor).getBounds2D();

		ShapeInfo.Kind kind = kind(shape);
		double rotation = rotation(shape);

		CTShapeProperties spPr = child(shape.getXmlObject(), PML_NS, "spPr", CTShapeProperties.class);
		CTShapeStyle style = child(shape.getXmlObject(), PML_NS, "style", CTShapeStyle.class);
		XSLFSheet sheet = shape.getSheet();

		GeometryInfo geometry = spPr == null ? null : geometry(spPr, bounds);
		PaintInfo fill = kind == ShapeInfo.Kind.PICTURE ? null : fill(spPr, style, sheet);
		LineInfo line = line(spPr, style, sheet);
		ShadowInfo shadow = shadow(spPr, style, sheet);

		TextInfo text = shape instanceof XSLFTextShape textShape ? textInfo(textShape) : null;

		CTPlaceholder ph = placeholder(shape);
		ShapeInfo.PlaceholderRef placeholder =
			ph == null ? null : new ShapeInfo.PlaceholderRef(placeholderType(ph), placeholderIdx(ph));

		String contentType = null;
		if (shape instanceof XSLFPictureShape picture) {
			XSLFPictureData data = picture.getPictureData();
			contentType = data == null ? null : data.getContentType();
		}

		TableInfo table = shape instanceof XSLFTable tableShape ? table(tableShape) : null;
		ChartInfo chart = kind == ShapeInfo.Kind.CHART ? ChartInfo.of(((XSLFGraphicFrame) shape).getChart()) : null;

		List<ShapeInfo> children = new ArrayList<>();
		if (shape instanceof XSLFGroupShape group) {
			AffineTransform childTransform = childTransform(group, transform);
			for (XSLFShape child : group.getShapes()) {
				children.add(shape(child, childTransform));
			}
		}

		return new ShapeInfo(kind, shape.getShapeName(), bounds, rotation, geometry, fill, line, shadow, text,
			placeholder, contentType, table, chart, children);
	}

	private static AffineTransform childTransform(XSLFGroupShape group, AffineTransform transform) {
		AffineTransform result = new AffineTransform(transform);
		Rectangle2D anchor = group.getAnchor();
		Rectangle2D interior = group.getInteriorAnchor();
		if (anchor == null || interior == null) {
			return result;
		}
		result.translate(anchor.getX(), anchor.getY());
		if (interior.getWidth() > 0 && interior.getHeight() > 0) {
			result.scale(anchor.getWidth() / interior.getWidth(), anchor.getHeight() / interior.getHeight());
		}
		result.translate(-interior.getX(), -interior.getY());
		return result;
	}

	private static ShapeInfo.Kind kind(XSLFShape shape) {
		if (shape instanceof XSLFGroupShape) {
			return ShapeInfo.Kind.GROUP;
		}
		if (shape instanceof XSLFTable) {
			return ShapeInfo.Kind.TABLE;
		}
		if (shape instanceof XSLFGraphicFrame frame) {
			return frame.hasChart() ? ShapeInfo.Kind.CHART : ShapeInfo.Kind.OTHER;
		}
		if (shape instanceof XSLFPictureShape) {
			return ShapeInfo.Kind.PICTURE;
		}
		if (shape instanceof XSLFConnectorShape) {
			return ShapeInfo.Kind.CONNECTOR;
		}
		if (shape instanceof XSLFTextBox) {
			return ShapeInfo.Kind.TEXT_BOX;
		}
		if (shape instanceof XSLFAutoShape) {
			return ShapeInfo.Kind.SHAPE;
		}
		return ShapeInfo.Kind.OTHER;
	}

	private static double rotation(XSLFShape shape) {
		if (shape instanceof org.apache.poi.sl.usermodel.PlaceableShape<?, ?> placeable) {
			return placeable.getRotation();
		}
		return 0;
	}

	private static GeometryInfo geometry(CTShapeProperties spPr, Rectangle2D bounds) {
		if (spPr.isSetPrstGeom()) {
			CTPresetGeometry2D preset = spPr.getPrstGeom();
			String name = preset.getPrst().toString();
			Map<String, Double> adjust = new LinkedHashMap<>();
			if (preset.isSetAvLst()) {
				for (CTGeomGuide guide : preset.getAvLst().getGdArray()) {
					Double value = guideValue(guide.getFmla());
					if (value != null) {
						adjust.put(guide.getName(), value);
					}
				}
			}
			Double cornerRadius = null;
			if (ROUND_RECT.equals(name) && bounds != null) {
				Double adj = adjust.values().stream().findFirst().orElse(null);
				double factor = (adj == null ? ROUND_RECT_DEFAULT_ADJUST : adj.doubleValue()) / 100000;
				cornerRadius = Double.valueOf(Math.min(bounds.getWidth(), bounds.getHeight()) * factor);
			}
			return new GeometryInfo(name, adjust, cornerRadius);
		}
		if (spPr.isSetCustGeom()) {
			return new GeometryInfo(null, Map.of(), null);
		}
		return null;
	}

	private static Double guideValue(String formula) {
		if (formula == null) {
			return null;
		}
		String value = formula.trim();
		if (!value.startsWith("val ")) {
			return null;
		}
		try {
			return Double.valueOf(Double.parseDouble(value.substring(4).trim()));
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private PaintInfo fill(CTShapeProperties spPr, CTShapeStyle style, XSLFSheet sheet) {
		PaintInfo result = spPr == null ? null : readPaint(spPr, null, sheet);
		if (result == null && style != null && style.getFillRef() != null) {
			CTStyleMatrixReference ref = style.getFillRef();
			XmlObject themeFill = themeFill(sheet, (int) ref.getIdx());
			if (themeFill != null) {
				result = paint(themeFill, ref.isSetSchemeClr() ? ref.getSchemeClr() : null, sheet);
			}
		}
		return result;
	}

	private LineInfo line(CTShapeProperties spPr, CTShapeStyle style, XSLFSheet sheet) {
		CTLineProperties ln = spPr != null && spPr.isSetLn() ? spPr.getLn() : null;
		PaintInfo paint = ln == null ? null : readPaint(ln, null, sheet);
		Double width = ln != null && ln.isSetW() ? Double.valueOf(Units.toPoints(ln.getW())) : null;
		String dash = dash(ln);

		CTStyleMatrixReference ref = style == null ? null : style.getLnRef();
		if (ref != null && ref.getIdx() > 0 && (paint == null || width == null || dash == null)) {
			CTLineProperties themeLine = themeLine(sheet, (int) ref.getIdx());
			if (themeLine != null) {
				if (paint == null) {
					paint = readPaint(themeLine, ref.isSetSchemeClr() ? ref.getSchemeClr() : null, sheet);
				}
				if (width == null && themeLine.isSetW()) {
					width = Double.valueOf(Units.toPoints(themeLine.getW()));
				}
				if (dash == null) {
					dash = dash(themeLine);
				}
			}
		}
		if (paint == null || paint.isNone()) {
			return null;
		}
		return new LineInfo(paint, width, dash);
	}

	private static String dash(CTLineProperties ln) {
		if (ln != null && ln.isSetPrstDash() && ln.getPrstDash().isSetVal()) {
			return ln.getPrstDash().getVal().toString();
		}
		return null;
	}

	private ShadowInfo shadow(CTShapeProperties spPr, CTShapeStyle style, XSLFSheet sheet) {
		CTEffectList effects = null;
		CTSchemeColor phClr = null;
		if (spPr != null && spPr.isSetEffectLst()) {
			effects = spPr.getEffectLst();
		} else if (spPr != null && spPr.isSetEffectDag()) {
			return null;
		} else if (style != null && style.getEffectRef() != null && style.getEffectRef().getIdx() > 0) {
			CTStyleMatrixReference ref = style.getEffectRef();
			CTEffectStyleItem item = themeEffect(sheet, (int) ref.getIdx());
			if (item != null && item.isSetEffectLst()) {
				effects = item.getEffectLst();
				phClr = ref.isSetSchemeClr() ? ref.getSchemeClr() : null;
			}
		}
		if (effects == null || !effects.isSetOuterShdw()) {
			return null;
		}
		CTOuterShadowEffect shadow = effects.getOuterShdw();
		return new ShadowInfo(color(shadow, phClr, sheet),
			shadow.isSetBlurRad() ? Units.toPoints(shadow.getBlurRad()) : 0,
			shadow.isSetDist() ? Units.toPoints(shadow.getDist()) : 0,
			shadow.isSetDir() ? shadow.getDir() / 60000.0 : 0);
	}

	private TextInfo textInfo(XSLFTextShape shape) {
		List<XSLFTextParagraph> paragraphs = shape.getTextParagraphs();
		String text = shape.getText();
		if (paragraphs.isEmpty() || text == null || text.isBlank()) {
			return null;
		}
		XSLFTextParagraph paragraph = firstParagraph(paragraphs);
		return TextInfo.create(textStyle(shape, paragraph, firstRun(paragraph)), paragraphs.size(), text);
	}

	private static XSLFTextParagraph firstParagraph(List<XSLFTextParagraph> paragraphs) {
		for (XSLFTextParagraph paragraph : paragraphs) {
			String text = paragraph.getText();
			if (text != null && !text.isBlank()) {
				return paragraph;
			}
		}
		return paragraphs.get(0);
	}

	private static XSLFTextRun firstRun(XSLFTextParagraph paragraph) {
		XSLFTextRun first = null;
		for (XSLFTextRun run : paragraph.getTextRuns()) {
			if (first == null) {
				first = run;
			}
			String text = run.getRawText();
			if (text != null && !text.isBlank()) {
				return run;
			}
		}
		return first;
	}

	private TextStyle textStyle(XSLFTextShape shape, XSLFTextParagraph paragraph, XSLFTextRun run) {
		XSLFSheet sheet = shape.getSheet();
		XSLFTheme theme = sheet.getTheme();

		String font = font(charProperty(paragraph, run, p -> p.isSetLatin() ? p.getLatin().getTypeface() : null),
			theme);
		Double size = charProperty(paragraph, run, p -> p.isSetSz() ? Double.valueOf(p.getSz() / 100.0) : null);
		boolean bold = Boolean.TRUE.equals(charProperty(paragraph, run, p -> p.isSetB() ? Boolean.valueOf(p.getB()) : null));
		boolean italic =
			Boolean.TRUE.equals(charProperty(paragraph, run, p -> p.isSetI() ? Boolean.valueOf(p.getI()) : null));
		String cap = charProperty(paragraph, run, p -> p.isSetCap() ? p.getCap().toString() : null);
		Double spacing = charProperty(paragraph, run,
			p -> p.isSetSpc() ? Double.valueOf(Units.toPoints(POIXMLUnits.parseLength(p.xgetSpc()))) : null);

		CTSolidColorFillProperties colorFill =
			charProperty(paragraph, run, p -> p.isSetSolidFill() ? p.getSolidFill() : null);
		ColorInfo color;
		if (colorFill != null) {
			color = color(colorFill, null, sheet);
		} else {
			CTShapeStyle style = child(shape.getXmlObject(), PML_NS, "style", CTShapeStyle.class);
			color = style != null && style.getFontRef() != null ? color(style.getFontRef(), null, sheet) : null;
		}

		TextAlign textAlign = paragraph.getTextAlign();
		VerticalAlignment anchor = shape.getVerticalAlignment();
		Double lineSpacing = paragraph.getLineSpacing();
		if (lineSpacing != null) {
			lineSpacing = lineSpacing.doubleValue() > 0 ? Double.valueOf(lineSpacing.doubleValue() / 100) : null;
		}

		return new TextStyle(font, size, bold, italic, color, textAlign == null ? null : lowerCase(textAlign.name()),
			CAPS_ALL.equals(cap), anchor == null ? null : lowerCase(anchor.name()), shape.getInsets(), spacing,
			lineSpacing);
	}

	/**
	 * Looks up the effective value of a character property of the given run.
	 *
	 * <p>
	 * The run's own properties take precedence, then the default run properties of the paragraph
	 * level as inherited through the shape, the layout and master placeholders and the master text
	 * styles.
	 * </p>
	 */
	private static <T> T charProperty(XSLFTextParagraph paragraph, XSLFTextRun run,
			Function<CTTextCharacterProperties, T> property) {
		if (run != null) {
			CTTextCharacterProperties own = run.getRPr(false);
			if (own != null) {
				T value = property.apply(own);
				if (value != null) {
					return value;
				}
			}
		}
		return new ParagraphPropertyFetcher<T>(paragraph, (props, consumer) -> {
			if (props.isSetDefRPr()) {
				T value = property.apply(props.getDefRPr());
				if (value != null) {
					consumer.accept(value);
				}
			}
		}).fetchProperty(paragraph.getParentShape());
	}

	private static String font(String typeface, XSLFTheme theme) {
		if (theme == null) {
			return typeface;
		}
		if (typeface == null || THEME_MINOR_LATIN.equals(typeface)) {
			return theme.getMinorFont();
		}
		if (THEME_MAJOR_LATIN.equals(typeface)) {
			return theme.getMajorFont();
		}
		return typeface;
	}

	private TableInfo table(XSLFTable table) {
		int rows = table.getNumberOfRows();
		int columns = table.getNumberOfColumns();
		List<Double> widths = new ArrayList<>(columns);
		for (int column = 0; column < columns; column++) {
			widths.add(Double.valueOf(table.getColumnWidth(column)));
		}
		CTTableProperties properties = table.getCTTable().getTblPr();
		String style = properties != null && properties.isSetTableStyleId()
			? tableStyleName(properties.getTableStyleId()) : null;
		TableInfo.CellInfo header = rows > 0 && columns > 0 ? cell(table.getCell(0, 0)) : null;
		TableInfo.CellInfo body = rows > 1 && columns > 0 ? cell(table.getCell(1, 0)) : null;
		return new TableInfo(rows, columns, widths, style, header, body);
	}

	private TableInfo.CellInfo cell(XSLFTableCell cell) {
		if (cell == null) {
			return null;
		}
		CTTableCell xml = (CTTableCell) cell.getXmlObject();
		PaintInfo fill = xml.isSetTcPr() ? readPaint(xml.getTcPr(), null, cell.getSheet()) : null;
		return new TableInfo.CellInfo(fill, textInfo(cell));
	}

	private String tableStyleName(String id) {
		if (_tableStyleNames == null) {
			_tableStyleNames = new HashMap<>();
			XSLFTableStyles styles = _slideShow.getTableStyles();
			if (styles != null) {
				for (XSLFTableStyle style : styles.getStyles()) {
					_tableStyleNames.put(style.getStyleId(), style.getStyleName());
				}
			}
		}
		return _tableStyleNames.getOrDefault(id, id);
	}

	/**
	 * The fill defined by a child of the given element, <code>null</code> if there is none.
	 */
	private static PaintInfo readPaint(XmlObject container, CTSchemeColor phClr, XSLFSheet sheet) {
		if (container == null) {
			return null;
		}
		try (XmlCursor cursor = container.newCursor()) {
			for (boolean found = cursor.toFirstChild(); found; found = cursor.toNextSibling()) {
				PaintInfo result = paint(cursor.getObject(), phClr, sheet);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * The fill described by the given fill element, <code>null</code> if it is no fill element.
	 */
	private static PaintInfo paint(XmlObject fill, CTSchemeColor phClr, XSLFSheet sheet) {
		if (fill instanceof CTNoFillProperties) {
			return PaintInfo.of(PaintInfo.Type.NONE);
		}
		if (fill instanceof CTSolidColorFillProperties) {
			return PaintInfo.solid(color(fill, phClr, sheet));
		}
		if (fill instanceof CTGradientFillProperties gradient) {
			List<PaintInfo.GradientStop> stops = new ArrayList<>();
			if (gradient.isSetGsLst()) {
				for (CTGradientStop stop : gradient.getGsLst().getGsArray()) {
					double position = POIXMLUnits.parsePercent(stop.xgetPos()) / 100000.0;
					stops.add(new PaintInfo.GradientStop(position, color(stop, phClr, sheet)));
				}
			}
			Double angle = gradient.isSetLin() && gradient.getLin().isSetAng()
				? Double.valueOf(gradient.getLin().getAng() / 60000.0) : null;
			String path = gradient.isSetPath() && gradient.getPath().isSetPath()
				? gradient.getPath().getPath().toString() : null;
			return new PaintInfo(PaintInfo.Type.GRADIENT, null, null, stops, angle, path, null, null);
		}
		if (fill instanceof CTBlipFillProperties blipFill) {
			String contentType = null;
			Double opacity = null;
			if (blipFill.isSetBlip()) {
				if (blipFill.getBlip().isSetEmbed()) {
					POIXMLDocumentPart part = sheet.getRelationById(blipFill.getBlip().getEmbed());
					if (part instanceof XSLFPictureData data) {
						contentType = data.getContentType();
					}
				}
				XmlObject alpha = child(blipFill.getBlip(), DML_NS, "alphaModFix", XmlObject.class);
				String amount = attribute(alpha, "amt");
				if (amount != null) {
					opacity = percentage(amount);
				}
			}
			return new PaintInfo(PaintInfo.Type.PICTURE, null, null, List.of(), null, null, contentType, opacity);
		}
		if (fill instanceof CTPatternFillProperties pattern) {
			return new PaintInfo(PaintInfo.Type.PATTERN,
				pattern.isSetFgClr() ? color(pattern.getFgClr(), phClr, sheet) : null,
				pattern.isSetBgClr() ? color(pattern.getBgClr(), phClr, sheet) : null,
				List.of(), null, pattern.isSetPrst() ? pattern.getPrst().toString() : null, null, null);
		}
		if (fill instanceof CTGroupFillProperties) {
			return PaintInfo.of(PaintInfo.Type.GROUP);
		}
		return null;
	}

	/**
	 * The color defined by the given element or its first color child.
	 *
	 * @param container
	 *        A color element or an element containing a color element.
	 * @param phClr
	 *        The color replacing the placeholder color in a theme style, <code>null</code> outside
	 *        theme styles.
	 * @param sheet
	 *        The sheet whose theme and color mapping resolve theme colors.
	 */
	private static ColorInfo color(XmlObject container, CTSchemeColor phClr, XSLFSheet sheet) {
		XmlObject element = colorElement(container);
		if (element == null) {
			if (phClr == null) {
				return null;
			}
			element = phClr;
		}

		boolean placeholder = element instanceof CTSchemeColor scheme
			&& PLACEHOLDER_COLOR.equals(scheme.getVal().toString()) && phClr != null;

		String schemeName = null;
		Map<String, Double> modifiers = new LinkedHashMap<>();
		if (placeholder) {
			schemeName = phClr.getVal().toString();
			readModifiers(phClr, modifiers);
		} else if (element instanceof CTSchemeColor scheme) {
			schemeName = scheme.getVal().toString();
		}
		readModifiers(element, modifiers);

		Color color = new XSLFColor(element, sheet.getTheme(), placeholder ? phClr : null, sheet).getColor();
		return new ColorInfo(rgb(color), schemeName, modifiers);
	}

	private static XmlObject colorElement(XmlObject container) {
		if (container == null) {
			return null;
		}
		if (isColor(container)) {
			return container;
		}
		try (XmlCursor cursor = container.newCursor()) {
			for (boolean found = cursor.toFirstChild(); found; found = cursor.toNextSibling()) {
				XmlObject child = cursor.getObject();
				if (isColor(child)) {
					return child;
				}
			}
		}
		return null;
	}

	private static boolean isColor(XmlObject element) {
		return element instanceof CTSRgbColor || element instanceof CTSchemeColor || element instanceof CTSystemColor
			|| element instanceof CTPresetColor || element instanceof CTScRgbColor || element instanceof CTHslColor;
	}

	private static void readModifiers(XmlObject color, Map<String, Double> modifiers) {
		try (XmlCursor cursor = color.newCursor()) {
			for (boolean found = cursor.toFirstChild(); found; found = cursor.toNextSibling()) {
				String name = cursor.getName().getLocalPart();
				if (ColorInfo.MODIFIERS.contains(name)) {
					String value = cursor.getAttributeText(new QName("val"));
					if (value != null) {
						Double percentage = percentage(value);
						if (percentage != null) {
							modifiers.put(name, percentage);
						}
					}
				}
			}
		}
	}

	/**
	 * Parses a percentage given in thousandths of a percent or with a percent sign.
	 */
	private static Double percentage(String value) {
		try {
			String text = value.trim();
			if (text.endsWith("%")) {
				return Double.valueOf(Double.parseDouble(text.substring(0, text.length() - 1)) / 100);
			}
			return Double.valueOf(Double.parseDouble(text) / 100000);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static String rgb(Color color) {
		if (color == null) {
			return null;
		}
		return String.format(Locale.ROOT, "%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}

	private static XmlObject themeFill(XSLFSheet sheet, int idx) {
		CTStyleMatrix matrix = styleMatrix(sheet);
		if (matrix == null || idx <= 0) {
			return null;
		}
		if (idx > BACKGROUND_FILL_OFFSET) {
			return matrix.getBgFillStyleLst() == null ? null
				: nthChild(matrix.getBgFillStyleLst(), idx - BACKGROUND_FILL_OFFSET - 1);
		}
		return matrix.getFillStyleLst() == null ? null : nthChild(matrix.getFillStyleLst(), idx - 1);
	}

	private static CTLineProperties themeLine(XSLFSheet sheet, int idx) {
		CTStyleMatrix matrix = styleMatrix(sheet);
		if (matrix == null || matrix.getLnStyleLst() == null || idx <= 0
			|| idx > matrix.getLnStyleLst().sizeOfLnArray()) {
			return null;
		}
		return matrix.getLnStyleLst().getLnArray(idx - 1);
	}

	private static CTEffectStyleItem themeEffect(XSLFSheet sheet, int idx) {
		CTStyleMatrix matrix = styleMatrix(sheet);
		if (matrix == null || matrix.getEffectStyleLst() == null || idx <= 0
			|| idx > matrix.getEffectStyleLst().sizeOfEffectStyleArray()) {
			return null;
		}
		return matrix.getEffectStyleLst().getEffectStyleArray(idx - 1);
	}

	private static CTStyleMatrix styleMatrix(XSLFSheet sheet) {
		XSLFTheme theme = sheet.getTheme();
		if (theme == null) {
			return null;
		}
		CTBaseStyles elements = theme.getXmlObject().getThemeElements();
		return elements == null ? null : elements.getFmtScheme();
	}

	static CTPlaceholder placeholder(XSLFShape shape) {
		try (XmlCursor cursor = shape.getXmlObject().newCursor()) {
			for (boolean found = cursor.toFirstChild(); found; found = cursor.toNextSibling()) {
				if (cursor.getName().getLocalPart().startsWith("nv")) {
					if (cursor.toChild(PML_NS, "nvPr") && cursor.toChild(PML_NS, "ph")) {
						XmlObject result = cursor.getObject();
						return result instanceof CTPlaceholder ph ? ph : null;
					}
					return null;
				}
			}
		}
		return null;
	}

	static String placeholderType(CTPlaceholder ph) {
		return ph.isSetType() ? ph.getType().toString() : DEFAULT_PLACEHOLDER_TYPE;
	}

	private static long placeholderIdx(CTPlaceholder ph) {
		return ph.isSetIdx() ? ph.getIdx() : 0;
	}

	private static <T extends XmlObject> T child(XmlObject parent, String namespace, String name, Class<T> type) {
		if (parent == null) {
			return null;
		}
		try (XmlCursor cursor = parent.newCursor()) {
			if (cursor.toChild(namespace, name)) {
				XmlObject result = cursor.getObject();
				return type.isInstance(result) ? type.cast(result) : null;
			}
		}
		return null;
	}

	private static XmlObject nthChild(XmlObject parent, int n) {
		if (n < 0) {
			return null;
		}
		try (XmlCursor cursor = parent.newCursor()) {
			if (!cursor.toFirstChild()) {
				return null;
			}
			for (int i = 0; i < n; i++) {
				if (!cursor.toNextSibling()) {
					return null;
				}
			}
			return cursor.getObject();
		}
	}

	private static String attribute(XmlObject element, String name) {
		if (element == null) {
			return null;
		}
		try (XmlCursor cursor = element.newCursor()) {
			return cursor.getAttributeText(new QName(name));
		}
	}

	private static String nonEmpty(String text) {
		return text == null || text.isBlank() ? null : text;
	}

	private static String lowerCase(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

}

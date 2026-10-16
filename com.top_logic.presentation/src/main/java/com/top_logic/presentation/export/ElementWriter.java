/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRelativeRect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCellProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.CellSpec;
import com.top_logic.presentation.model.ChartSpec;
import com.top_logic.presentation.model.ColorSpec;
import com.top_logic.presentation.model.ContainerSpec;
import com.top_logic.presentation.model.CrossAlign;
import com.top_logic.presentation.model.ElementSpec;
import com.top_logic.presentation.model.GeometrySpec;
import com.top_logic.presentation.model.ImageSpec;
import com.top_logic.presentation.model.LineSpec;
import com.top_logic.presentation.model.PaintSpec;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.model.ShadowSpec;
import com.top_logic.presentation.model.ShapeSpec;
import com.top_logic.presentation.model.TableSpec;
import com.top_logic.presentation.model.TextAlign;
import com.top_logic.presentation.model.TextAnchor;
import com.top_logic.presentation.model.TextBoxSpec;
import com.top_logic.presentation.model.TextSpec;
import com.top_logic.presentation.profile.TemplateProfile;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.presentation.text.TableLayout;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.util.error.TopLogicException;

/**
 * Creates the content elements of a slide.
 *
 * <ul>
 * <li>A {@link ShapeSpec shape} shows only what its description gives: it has no fill without
 * {@link ShapeSpec#fill()} and no outline without {@link ShapeSpec#line()}; its text color is
 * inherited from the slide master unless given. An outline without color is drawn in the text
 * color <code>tx1</code>. The text is centered horizontally and vertically, unless the
 * description aligns it otherwise.</li>
 * <li>A {@link TextBoxSpec text box} has neither fill nor outline.</li>
 * <li>A {@link TableSpec table} takes the width of its bounds; its height results from its rows.
 * Rows that do not fit into the height of its bounds continue on a further slide. The text of the
 * header rows is bold. Each row is separated from the next by a thin line, the
 * header rows by a stronger line.</li>
 * <li>An {@link ImageSpec image} is fitted into its bounds as requested.</li>
 * <li>A {@link ChartSpec chart} fills its bounds, see {@link ChartWriter}.</li>
 * <li>A {@link ContainerSpec#split() split column} places its children one below the other, each
 * with its own height or the height of its content, as long as they fit. A table or a text with
 * {@link TextSpec#split()} that does not fit shows the rows or paragraphs that fit, if at least
 * one of them does; any other child that does not fit continues on a further slide as a whole.
 * The first child on a slide is always shown, reduced to the height of the column if it is
 * higher.</li>
 * </ul>
 *
 * <p>
 * The texts of shapes, text boxes and table cells are fitted into their element by
 * {@link TextFitting}.
 * </p>
 *
 * <p>
 * A shape referring to a {@link ShapeSpec#style() style} gets the properties of the style of the
 * template profile where it gives none itself, a table cell with a {@link CellSpec#rating()
 * rating} the fill and text formatting of the rating's style. Within an element with a
 * {@link ElementSpec#role() color role}, the role {@link PresentationData#ROLE_ACCENT} stands for
 * the element's role.
 * </p>
 */
public final class ElementWriter {

	/**
	 * The text style of header cells, unless the description formats them otherwise.
	 */
	private static final RunStyle HEADER_STYLE = new RunStyle(Boolean.TRUE, null, null, null, null, null, null, null);

	/**
	 * The line below each row of a table.
	 */
	private static final LineSpec ROW_RULE = new LineSpec(
		new PaintSpec.Solid(new ColorSpec.Scheme("bg1", Map.of(ColorInfo.LUM_MOD, 0.75))), 0.75, null);

	/**
	 * The line below the last header row of a table.
	 */
	private static final LineSpec HEADER_RULE = new LineSpec(
		new PaintSpec.Solid(new ColorSpec.Scheme("tx1", Map.of())), 1.5, null);

	/**
	 * The color of an outline whose description gives no color.
	 */
	private static final PaintSpec DEFAULT_LINE_PAINT = new PaintSpec.Solid(new ColorSpec.Scheme("tx1", Map.of()));

	/**
	 * The amount in points by which a table may exceed its height without being continued,
	 * compensating rounding errors.
	 */
	private static final double TOLERANCE = 0.01;

	private final XSLFSlide _slide;

	private final DrawingWriter _drawing;

	private final TextWriter _text;

	private final TextFitting _fitting;

	private final TemplateProfile _profile;

	/**
	 * Creates an {@link ElementWriter}.
	 *
	 * @param slide
	 *        The slide to place the elements on.
	 * @param drawing
	 *        Writes colors, fills, outlines and shadows.
	 * @param text
	 *        Writes texts.
	 * @param profile
	 *        The profile of the template, giving the styles, the rating styles and the smallest
	 *        font size to which a text is shrunk to fit into its element.
	 */
	ElementWriter(XSLFSlide slide, DrawingWriter drawing, TextWriter text, TemplateProfile profile) {
		_slide = slide;
		_drawing = drawing;
		_text = text;
		_profile = profile;
		_fitting = new TextFitting(text, profile.minFontSize());
	}

	/**
	 * The given element with the properties of its style.
	 */
	private ElementSpec resolve(ElementSpec element, String path) {
		if (element instanceof ShapeSpec shape && shape.style() != null) {
			return _profile.style(shape.style(), child(path, PresentationData.STYLE)).applyTo(shape);
		}
		return element;
	}

	/**
	 * The writer for the content of an element with the given color role.
	 */
	private ElementWriter forRole(String role, String path) {
		DrawingWriter drawing = _drawing.forRole(role, child(path, PresentationData.ROLE));
		if (drawing == _drawing) {
			return this;
		}
		return new ElementWriter(_slide, drawing, new TextWriter(drawing), _profile);
	}

	/**
	 * The part of an element that is shown on a slide, starting at the given position.
	 *
	 * @param child
	 *        The index of the first child of a {@link ContainerSpec#split() split column} to show,
	 *        0 for any other element.
	 * @param start
	 *        The index of the first table row (after the header rows) or paragraph to show of the
	 *        element, or of the first child of a split column; 0 for the whole element or child.
	 */
	record Position(int child, int start) {

		/**
		 * The position showing the whole element.
		 */
		static final Position START = new Position(0, 0);

	}

	/**
	 * Creates the given element, or the part of it starting at the given position, on the slide.
	 *
	 * <p>
	 * A table that is higher than its bounds shows its header rows and as many further rows as fit
	 * (at least one), a text marked with {@link TextSpec#split()} as many paragraphs as fit (at
	 * least one), a {@link ContainerSpec#split() split column} as many children as fit. The rest
	 * continues on a further slide.
	 * </p>
	 *
	 * @param element
	 *        The element description, a {@link ContainerSpec} only if it is a split column.
	 * @param from
	 *        The part of the element to show, {@link Position#START} for the whole element.
	 * @param bounds
	 *        The position and size of the element on the slide, see {@link Arrangement}.
	 * @param path
	 *        The path of the element in the description, for error messages.
	 * @return The position of the rest that did not fit and continues on a further slide,
	 *         <code>null</code> if the element is complete.
	 */
	Position write(ElementSpec element, Position from, Rectangle2D bounds, String path) {
		if (element instanceof ContainerSpec column) {
			return writeSplitColumn(column, from, bounds, path);
		}
		ElementSpec resolved = resolve(element, path);
		int rest = forRole(resolved.role(), path).writeResolved(resolved, from.start(), bounds, path);
		return rest > 0 ? new Position(0, rest) : null;
	}

	/**
	 * Writes the children of a split column from the given position on, as far as they fit.
	 *
	 * @return The position of the first child or its part that did not fit, <code>null</code> if
	 *         all children are written.
	 */
	private Position writeSplitColumn(ContainerSpec column, Position from, Rectangle2D bounds, String path) {
		Arrangement arrangement = new Arrangement(_profile.gap(), this::naturalHeight);
		List<ElementSpec> children = column.children();
		double gap = column.gap() != null ? column.gap().doubleValue() : _profile.gap();
		CrossAlign align = column.align() != null ? column.align() : CrossAlign.STRETCH;
		double y = bounds.getY();
		boolean first = true;
		List<Written> written = new ArrayList<>();
		for (int index = from.child(); index < children.size(); index++) {
			ElementSpec child = children.get(index);
			String childPath = Arrangement.childPath(path, index);
			int start = index == from.child() ? from.start() : 0;
			double width = Arrangement.crossSize(child.bounds().width(), bounds.getWidth());
			double x = Arrangement.crossOffset(align, bounds.getX(), bounds.getWidth(), width);
			if (!first) {
				y += gap;
			}
			double space = Math.max(0, bounds.getMaxY() - y);
			Double ownHeight = child.bounds().height();
			double height = ownHeight != null ? ownHeight.doubleValue()
				: arrangement.naturalHeight(rest(child, start, false), width, childPath);
			if (height <= space + TOLERANCE) {
				Rectangle2D childBounds = new Rectangle2D.Double(x, y, width, height);
				written.add(new Written(index, start, _slide.getShapes().size(), child, childBounds, childPath));
				Position rest = writeChild(arrangement, child, start, childBounds, childPath, path);
				if (rest != null) {
					return new Position(index, rest.start());
				}
				y += height;
				first = false;
				continue;
			}
			if (ownHeight == null && isSplittable(resolve(child, childPath))) {
				if (!first && arrangement.naturalHeight(rest(child, start, true), width, childPath) > space + TOLERANCE) {
					// Not even the first row or paragraph fits.
					return keepWithNext(written, new Position(index, start));
				}
				Rectangle2D childBounds = new Rectangle2D.Double(x, y, width, space);
				written.add(new Written(index, start, _slide.getShapes().size(), child, childBounds, childPath));
				Position rest = writeChild(arrangement, child, start, childBounds, childPath, path);
				if (rest != null) {
					return new Position(index, rest.start());
				}
			} else if (first) {
				// The child alone is higher than the column.
				Rectangle2D childBounds = new Rectangle2D.Double(x, y, width, space);
				written.add(new Written(index, start, _slide.getShapes().size(), child, childBounds, childPath));
				writeChild(arrangement, child, start, childBounds, childPath, path);
			} else {
				return keepWithNext(written, new Position(index, 0));
			}
			y = bounds.getMaxY();
			first = false;
		}
		return null;
	}

	/**
	 * A child of a split column written onto the current slide.
	 *
	 * @param index
	 *        The index of the child in the column.
	 * @param start
	 *        The first paragraph or row of the child shown on the slide.
	 * @param shapes
	 *        The number of shapes of the slide before the child was written.
	 * @param child
	 *        The child.
	 * @param bounds
	 *        The bounds of the child.
	 * @param path
	 *        The path of the child in the description.
	 */
	private record Written(int index, int start, int shapes, ElementSpec child, Rectangle2D bounds, String path) {
		// Pure data.
	}

	/**
	 * Moves the paragraphs at the end of the children written onto the slide that are kept with
	 * the next one to the next slide, together with the given rest of the split column.
	 *
	 * <p>
	 * A text with {@link TextSpec#split()} whose last paragraphs are
	 * {@link ParagraphSpec#keepWithNext() kept with the next one} is shown without them, a text
	 * consisting of such paragraphs only is moved as a whole. The first child on the slide is never
	 * moved as a whole, so that the slide does not become empty.
	 * </p>
	 *
	 * @param written
	 *        The children written onto the slide, in their order.
	 * @param rest
	 *        The rest of the column that continues on the next slide.
	 * @return The rest of the column including the moved paragraphs.
	 */
	private Position keepWithNext(List<Written> written, Position rest) {
		Position result = rest;
		for (int n = written.size() - 1; n >= 0; n--) {
			Written child = written.get(n);
			TextSpec text = splitText(child.child());
			if (text == null) {
				break;
			}
			List<ParagraphSpec> paragraphs = text.paragraphs();
			int cut = paragraphs.size();
			while (cut > child.start() && paragraphs.get(cut - 1).keepWithNext()) {
				cut--;
			}
			if (cut == paragraphs.size() || (cut == child.start() && n == 0)) {
				break;
			}
			removeShapes(child.shapes());
			if (cut > child.start()) {
				write(withText(child.child(), text.withParagraphs(paragraphs.subList(0, cut))),
					new Position(0, child.start()), child.bounds(), child.path());
				return new Position(child.index(), cut);
			}
			result = new Position(child.index(), child.start());
		}
		return result;
	}

	/**
	 * The text of the given text box or shape if it is marked with {@link TextSpec#split()},
	 * <code>null</code> otherwise.
	 */
	private static TextSpec splitText(ElementSpec element) {
		TextSpec text = element instanceof TextBoxSpec textBox ? textBox.text()
			: element instanceof ShapeSpec shape ? shape.text() : null;
		return text != null && text.split() ? text : null;
	}

	private static ElementSpec withText(ElementSpec element, TextSpec text) {
		if (element instanceof TextBoxSpec textBox) {
			return new TextBoxSpec(textBox.bounds(), text, textBox.role());
		}
		ShapeSpec shape = (ShapeSpec) element;
		return new ShapeSpec(shape.bounds(), shape.geometry(), shape.fill(), shape.line(), shape.shadow(), text,
			shape.style(), shape.role());
	}

	/**
	 * Removes the shapes of the slide from the given index on.
	 */
	private void removeShapes(int from) {
		List<XSLFShape> shapes = _slide.getShapes();
		for (int n = shapes.size() - 1; n >= from; n--) {
			_slide.removeShape(shapes.get(n));
		}
	}

	/**
	 * Writes a child of a split column.
	 *
	 * @return The position of the rest of a table or a split text that did not fit,
	 *         <code>null</code> if the child is complete.
	 */
	private Position writeChild(Arrangement arrangement, ElementSpec child, int start, Rectangle2D bounds,
			String path, String columnPath) {
		if (isSplittable(resolve(child, path))) {
			return write(child, new Position(0, start), bounds, path);
		}
		for (Arrangement.Placed placed : arrangement.place(child, bounds, path)) {
			if (write(placed.element(), Position.START, placed.bounds(), placed.path()) != null) {
				throw new TopLogicException(
					I18NConstants.ERROR_SEVERAL_CONTINUED__PATH_OTHER.fill(placed.path(), columnPath));
			}
		}
		return null;
	}

	/**
	 * Whether the given element can show a part of its content and continue with the rest on a
	 * further slide.
	 */
	private static boolean isSplittable(ElementSpec element) {
		if (element instanceof TableSpec table) {
			return !table.rows().isEmpty() && table.columnCount() > 0;
		}
		TextSpec text = element instanceof TextBoxSpec textBox ? textBox.text()
			: element instanceof ShapeSpec shape ? shape.text() : null;
		return text != null && text.split();
	}

	/**
	 * The part of the given element starting at the given row or paragraph, to measure its
	 * height.
	 *
	 * @param unit
	 *        Whether only the first row (with the header rows) or paragraph of the part is
	 *        measured.
	 */
	private static ElementSpec rest(ElementSpec element, int start, boolean unit) {
		if (start == 0 && !unit) {
			return element;
		}
		if (element instanceof TableSpec table) {
			List<List<CellSpec>> rows = new ArrayList<>();
			for (int row : rowIndices(table, start, table.rows().size())) {
				if (!unit || row < table.headerRows() || rows.size() == table.headerRows()) {
					rows.add(table.rows().get(row));
				}
			}
			return new TableSpec(table.bounds(), table.columnWeights(), table.columnWidths(), rows,
				table.headerRows(), table.rowHeight());
		} else if (element instanceof TextBoxSpec textBox) {
			return withText(textBox, restText(textBox.text(), start, unit));
		} else if (element instanceof ShapeSpec shape && shape.text() != null) {
			return withText(shape, restText(shape.text(), start, unit));
		}
		return element;
	}

	private static TextSpec restText(TextSpec text, int start, boolean unit) {
		List<ParagraphSpec> paragraphs = text.paragraphs();
		int from = Math.min(start, paragraphs.size());
		int end = paragraphs.size();
		if (unit) {
			// The first paragraph together with the paragraphs it is kept with.
			end = from;
			while (end < paragraphs.size() && paragraphs.get(end).keepWithNext()) {
				end++;
			}
			end = Math.min(end + 1, paragraphs.size());
		}
		return text.withParagraphs(paragraphs.subList(from, end));
	}

	/**
	 * The end of the part of the given paragraphs to show on a slide, so that the part does not end
	 * with a paragraph kept with the next one.
	 *
	 * @param start
	 *        The first paragraph of the part.
	 * @param end
	 *        The end of the paragraphs that fit.
	 * @return The given end, reduced by the {@link ParagraphSpec#keepWithNext()} paragraphs at the
	 *         end of the part, unless the part would become empty.
	 */
	private static int keepWithNext(List<ParagraphSpec> paragraphs, int start, int end) {
		int result = end;
		while (result > start && paragraphs.get(result - 1).keepWithNext()) {
			result--;
		}
		return result > start ? result : end;
	}

	private int writeResolved(ElementSpec element, int start, Rectangle2D bounds, String path) {
		if (element instanceof ShapeSpec shape) {
			return writeShape(shape, start, bounds, path);
		} else if (element instanceof TextBoxSpec textBox) {
			return writeTextBox(textBox, start, bounds, path);
		} else if (element instanceof TableSpec table) {
			return writeTable(table, start, bounds, path);
		} else if (element instanceof ImageSpec image) {
			writeImage(image, bounds, path);
			return 0;
		} else if (element instanceof ChartSpec chart) {
			new ChartWriter(_slide, _drawing, _text).write(chart, bounds, path);
			return 0;
		} else {
			throw new IllegalArgumentException("Unsupported element: " + element);
		}
	}

	/**
	 * The height in points the given element needs to show its content at the given width.
	 *
	 * <p>
	 * A shape or text box needs the height of its text plus its inner margins, a table the height
	 * of its rows, an image the height that keeps its aspect ratio, a chart
	 * {@link ChartWriter#NATURAL_ASPECT} of its width. A shape without text needs no height.
	 * </p>
	 *
	 * @see Arrangement.HeightMeasure
	 */
	double naturalHeight(ElementSpec spec, double width, String path) {
		ElementSpec element = resolve(spec, path);
		if (element instanceof ShapeSpec shape) {
			if (shape.text() == null) {
				return 0;
			}
			XSLFAutoShape probe = _slide.createAutoShape();
			TextWriter.addTextBody((CTShape) probe.getXmlObject());
			return textHeight(probe, centered(shape.text()), width, child(path, PresentationData.TEXT));
		} else if (element instanceof TextBoxSpec textBox) {
			return textHeight(_slide.createTextBox(), textBox.text(), width, child(path, PresentationData.TEXT));
		} else if (element instanceof TableSpec table) {
			if (table.rows().isEmpty() || table.columnCount() == 0) {
				return 0;
			}
			XSLFTable probe = createTable(table, rowIndices(table, 0, table.rows().size()),
				new Rectangle2D.Double(0, 0, width, 0), path);
			try {
				return probe.getAnchor().getHeight();
			} finally {
				_slide.removeShape(probe);
			}
		} else if (element instanceof ImageSpec image) {
			Dimension2D size =
				Pictures.add(_slide.getSlideShow(), image.data(), child(path, PresentationData.DATA)).size();
			return size.getWidth() > 0 ? width * size.getHeight() / size.getWidth() : 0;
		} else if (element instanceof ChartSpec) {
			return width * ChartWriter.NATURAL_ASPECT;
		} else {
			throw new IllegalArgumentException("Unsupported element: " + element);
		}
	}

	private double textHeight(XSLFTextShape probe, TextSpec text, double width, String path) {
		try {
			probe.setAnchor(new Rectangle2D.Double(0, 0, width, 0));
			_text.write(probe, text, path);
			Insets2D insets = probe.getInsets();
			return TextMeasure.measure(probe).height() + insets.top + insets.bottom;
		} finally {
			_slide.removeShape(probe);
		}
	}

	private int writeShape(ShapeSpec spec, int start, Rectangle2D bounds, String path) {
		XSLFAutoShape shape = _slide.createAutoShape();
		shape.setAnchor(bounds);
		CTShape xml = (CTShape) shape.getXmlObject();
		TextWriter.addTextBody(xml);
		CTShapeProperties spPr = xml.getSpPr();
		_drawing.writeGeometry(spPr, spec.geometry() != null ? spec.geometry() : GeometrySpec.RECTANGLE, bounds);
		if (spec.fill() != null) {
			_drawing.writeFill(DrawingWriter.FillTarget.of(spPr), spec.fill(), _slide, child(path, PresentationData.FILL));
		}
		LineSpec line = spec.line();
		if (line != null) {
			if (line.paint() == null) {
				line = new LineSpec(DEFAULT_LINE_PAINT, line.width(), line.dash());
			}
			_drawing.writeLine(spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn(), line, _slide,
				child(path, PresentationData.LINE));
		}
		if (spec.shadow() != null && spec.shadow() != ShadowSpec.NONE) {
			_drawing.writeShadow(spPr, spec.shadow(), child(path, PresentationData.SHADOW));
		}
		TextSpec text = spec.text();
		if (text == null) {
			text = TextSpec.of(List.of());
		}
		return writeText(shape, centered(text), start, child(path, PresentationData.TEXT));
	}

	private static TextSpec centered(TextSpec text) {
		return text.withDefaultAlignment(TextAlign.CENTER, TextAnchor.MIDDLE);
	}

	private int writeTextBox(TextBoxSpec spec, int start, Rectangle2D bounds, String path) {
		XSLFTextBox box = _slide.createTextBox();
		box.setAnchor(bounds);
		return writeText(box, spec.text(), start, child(path, PresentationData.TEXT));
	}

	/**
	 * Writes the paragraphs of the given text from the given index on and fits them into the
	 * shape.
	 *
	 * @return The index of the first paragraph that did not fit, 0 if all paragraphs are written.
	 */
	private int writeText(XSLFTextShape shape, TextSpec text, int start, String path) {
		List<ParagraphSpec> paragraphs = text.paragraphs();
		int end = paragraphs.size();
		if (text.split() && end - start > 1) {
			_text.write(shape, text.withParagraphs(paragraphs.subList(start, end)), path, start);
			if (!TextFitting.fits(shape, true)) {
				int fitting = 1;
				int tooMany = end - start;
				while (tooMany - fitting > 1) {
					int candidate = (fitting + tooMany) / 2;
					_text.write(shape, text.withParagraphs(paragraphs.subList(start, start + candidate)), path, start);
					if (TextFitting.fits(shape, true)) {
						fitting = candidate;
					} else {
						tooMany = candidate;
					}
				}
				end = keepWithNext(paragraphs, start, start + fitting);
			}
		}
		_fitting.write(shape, text.withParagraphs(paragraphs.subList(start, end)), true, path, start);
		return end < paragraphs.size() ? end : 0;
	}

	/**
	 * Writes the header rows and the rows from the given index on, as far as they fit into the
	 * height of the given bounds.
	 *
	 * @return The index of the first row that did not fit, 0 if all rows are written.
	 */
	private int writeTable(TableSpec spec, int start, Rectangle2D bounds, String path) {
		int rowCount = spec.rows().size();
		if (rowCount == 0 || spec.columnCount() == 0) {
			return 0;
		}
		int headerRows = spec.headerRows();
		int first = Math.max(start, headerRows);
		XSLFTable table = createTable(spec, rowIndices(spec, first, rowCount), bounds, path);
		int bodyRows = rowCount - first;
		if (bodyRows > 1 && table.getAnchor().getHeight() > bounds.getHeight() + TOLERANCE) {
			double height = 0;
			for (int row = 0; row < headerRows; row++) {
				height += table.getRowHeight(row);
			}
			int fitting = 0;
			while (fitting < bodyRows) {
				height += table.getRowHeight(headerRows + fitting);
				if (fitting > 0 && height > bounds.getHeight() + TOLERANCE) {
					break;
				}
				fitting++;
			}
			if (fitting < bodyRows) {
				_slide.removeShape(table);
				createTable(spec, rowIndices(spec, first, first + fitting), bounds, path);
				return first + fitting;
			}
		}
		return 0;
	}

	/**
	 * The indices of the header rows of the given table followed by the indices of the given
	 * range of rows.
	 */
	private static List<Integer> rowIndices(TableSpec spec, int first, int end) {
		List<Integer> result = new ArrayList<>();
		int headerRows = spec.headerRows();
		for (int row = 0; row < headerRows; row++) {
			result.add(Integer.valueOf(row));
		}
		for (int row = Math.max(first, headerRows); row < end; row++) {
			result.add(Integer.valueOf(row));
		}
		return result;
	}

	/**
	 * Creates a table with the given rows of the description.
	 *
	 * <p>
	 * The table takes the width of the given bounds, each row the height of its highest cell
	 * text plus the cell insets, but at least the row height of the description, see
	 * {@link TableLayout}.
	 * </p>
	 */
	private XSLFTable createTable(TableSpec spec, List<Integer> rows, Rectangle2D bounds, String path) {
		int columnCount = spec.columnCount();
		XSLFTable table = _slide.createTable(rows.size(), columnCount);
		CTTableProperties tblPr = table.getCTTable().getTblPr();
		tblPr.setFirstRow(spec.headerRows() > 0);

		double[] widths = columnWidths(spec, bounds.getWidth());
		double totalWidth = 0;
		for (int column = 0; column < columnCount; column++) {
			table.setColumnWidth(column, widths[column]);
			totalWidth += widths[column];
		}

		String rowsPath = child(path, PresentationData.ROWS);
		double minHeight = spec.rowHeight() == null ? 0 : spec.rowHeight().doubleValue();
		for (int row = 0; row < rows.size(); row++) {
			int index = rows.get(row).intValue();
			boolean header = index < spec.headerRows();
			LineSpec rule = index == spec.headerRows() - 1 ? HEADER_RULE : ROW_RULE;
			for (int column = 0; column < columnCount; column++) {
				String cellPath = rowsPath + "[" + index + "][" + column + "]";
				XSLFTableCell cell = table.getCell(row, column);
				// The cell text is wrapped at the column width.
				cell.setAnchor(new Rectangle2D.Double(0, 0, widths[column], 0));
				writeCell(cell, spec.rows().get(index).get(column), header, rule, cellPath);
			}
			table.setRowHeight(row, minHeight);
		}

		table.setAnchor(new Rectangle2D.Double(bounds.getX(), bounds.getY(), totalWidth, 0));
		TableLayout.layout(table);
		return table;
	}

	private static double[] columnWidths(TableSpec spec, double width) {
		int columnCount = spec.columnCount();
		double[] result = new double[columnCount];
		if (spec.columnWidths() != null) {
			for (int column = 0; column < columnCount; column++) {
				result[column] = spec.columnWidths().get(column).doubleValue();
			}
			return result;
		}
		double totalWeight = 0;
		for (int column = 0; column < columnCount; column++) {
			totalWeight += weight(spec, column);
		}
		for (int column = 0; column < columnCount; column++) {
			result[column] = width * weight(spec, column) / totalWeight;
		}
		return result;
	}

	private static double weight(TableSpec spec, int column) {
		return spec.columnWeights() == null ? 1 : spec.columnWeights().get(column).doubleValue();
	}

	private void writeCell(XSLFTableCell cell, CellSpec cellSpec, boolean header, LineSpec rule, String path) {
		CellSpec spec = cellSpec.rating() == null ? cellSpec
			: _profile.rating(cellSpec.rating(), child(path, PresentationData.RATING)).applyTo(cellSpec);
		TextSpec text = spec.text();
		if (text != null) {
			if (header) {
				text = text.withStyle(text.style().inheritFrom(HEADER_STYLE));
			}
			_fitting.write(cell, text, false, path, 0);
		}
		CTTableCell xml = (CTTableCell) cell.getXmlObject();
		CTTableCellProperties tcPr = xml.isSetTcPr() ? xml.getTcPr() : xml.addNewTcPr();
		// Replaces the bottom border of the table cell prototype.
		tcPr.setLnB(CTLineProperties.Factory.newInstance());
		_drawing.writeLine(tcPr.getLnB(), rule, _slide, path);
		if (spec.fill() != null) {
			_drawing.writeFill(DrawingWriter.FillTarget.of(tcPr), spec.fill(), _slide,
				child(path, PresentationData.FILL));
		}
	}

	private void writeImage(ImageSpec spec, Rectangle2D bounds, String path) {
		Pictures.Picture picture = Pictures.add(_slide.getSlideShow(), spec.data(), child(path, PresentationData.DATA));
		Dimension2D size = picture.size();
		double scaleX = bounds.getWidth() / size.getWidth();
		double scaleY = bounds.getHeight() / size.getHeight();

		Rectangle2D anchor = bounds;
		double cropX = 0;
		double cropY = 0;
		switch (spec.fit()) {
			case CONTAIN: {
				double scale = Math.min(scaleX, scaleY);
				double width = size.getWidth() * scale;
				double height = size.getHeight() * scale;
				anchor = new Rectangle2D.Double(bounds.getCenterX() - width / 2, bounds.getCenterY() - height / 2,
					width, height);
				break;
			}
			case COVER: {
				double scale = Math.max(scaleX, scaleY);
				cropX = (1 - bounds.getWidth() / (size.getWidth() * scale)) / 2;
				cropY = (1 - bounds.getHeight() / (size.getHeight() * scale)) / 2;
				break;
			}
			default:
				break;
		}

		XSLFPictureShape shape;
		if (picture.isSvg()) {
			try {
				shape = XSLFPictureShape.addSvgImage(_slide, picture.data(), PictureType.PNG, anchor);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		} else {
			shape = _slide.createPicture(picture.data());
			shape.setAnchor(anchor);
		}
		if (cropX > 0 || cropY > 0) {
			CTRelativeRect crop = ((CTPicture) shape.getXmlObject()).getBlipFill().addNewSrcRect();
			Integer horizontal = Integer.valueOf((int) DrawingWriter.percent(cropX));
			Integer vertical = Integer.valueOf((int) DrawingWriter.percent(cropY));
			crop.setL(horizontal);
			crop.setR(horizontal);
			crop.setT(vertical);
			crop.setB(vertical);
		}
	}

	private static String child(String path, String key) {
		return path + "." + key;
	}

}

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
import com.top_logic.presentation.model.ColorSpec;
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
import com.top_logic.presentation.text.TextMeasure;

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
	 * Creates the given element, or the part of it starting at the given position, on the slide.
	 *
	 * <p>
	 * A table that is higher than its bounds shows its header rows and as many further rows as fit
	 * (at least one), a text marked with {@link TextSpec#split()} as many paragraphs as fit (at
	 * least one). The rest continues on a further slide.
	 * </p>
	 *
	 * @param element
	 *        The element description, no {@link com.top_logic.presentation.model.ContainerSpec}.
	 * @param start
	 *        The index of the first table row (after the header rows) or paragraph to show, 0 for
	 *        the whole element.
	 * @param bounds
	 *        The position and size of the element on the slide, see {@link Arrangement}.
	 * @param path
	 *        The path of the element in the description, for error messages.
	 * @return The index of the first row or paragraph that did not fit and continues on a further
	 *         slide, 0 if the element is complete.
	 */
	int write(ElementSpec element, int start, Rectangle2D bounds, String path) {
		ElementSpec resolved = resolve(element, path);
		return forRole(resolved.role(), path).writeResolved(resolved, start, bounds, path);
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
		} else {
			throw new IllegalArgumentException("Unsupported element: " + element);
		}
	}

	/**
	 * The height in points the given element needs to show its content at the given width.
	 *
	 * <p>
	 * A shape or text box needs the height of its text plus its inner margins, a table the height
	 * of its rows, an image the height that keeps its aspect ratio. A shape without text needs no
	 * height.
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
				end = start + fitting;
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
	 * text, but at least the row height of the description.
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
		double height = 0;
		for (int row = 0; row < rows.size(); row++) {
			int index = rows.get(row).intValue();
			boolean header = index < spec.headerRows();
			LineSpec rule = index == spec.headerRows() - 1 ? HEADER_RULE : ROW_RULE;
			double rowHeight = spec.rowHeight() == null ? 0 : spec.rowHeight().doubleValue();
			for (int column = 0; column < columnCount; column++) {
				String cellPath = rowsPath + "[" + index + "][" + column + "]";
				XSLFTableCell cell = table.getCell(row, column);
				// The cell text is wrapped at the column width.
				cell.setAnchor(new Rectangle2D.Double(0, 0, widths[column], 0));
				writeCell(cell, spec.rows().get(index).get(column), header, rule, cellPath);
				Insets2D insets = cell.getInsets();
				rowHeight = Math.max(rowHeight, TextMeasure.measure(cell).height() + insets.top + insets.bottom);
			}
			table.setRowHeight(row, rowHeight);
			height += rowHeight;
		}

		table.setAnchor(new Rectangle2D.Double(bounds.getX(), bounds.getY(), totalWidth, height));
		// Takes over rows that POI's own text layout makes higher.
		table.updateCellAnchor();
		double y = bounds.getY();
		for (int row = 0; row < rows.size(); row++) {
			double rowHeight = table.getCell(row, 0).getAnchor().getHeight();
			table.setRowHeight(row, rowHeight);
			// Places the cells without the border distance POI inserts between them, as PowerPoint
			// does.
			double x = bounds.getX();
			for (int column = 0; column < columnCount; column++) {
				table.getCell(row, column).setAnchor(new Rectangle2D.Double(x, y, widths[column], rowHeight));
				x += widths[column];
			}
			y += rowHeight;
		}
		table.setAnchor(new Rectangle2D.Double(bounds.getX(), bounds.getY(), totalWidth, y - bounds.getY()));
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

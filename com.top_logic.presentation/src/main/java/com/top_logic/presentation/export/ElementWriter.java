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
import java.util.List;
import java.util.Map;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
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
import com.top_logic.presentation.model.ImageSpec;
import com.top_logic.presentation.model.LineSpec;
import com.top_logic.presentation.model.PaintSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.model.ShapeSpec;
import com.top_logic.presentation.model.TableSpec;
import com.top_logic.presentation.model.TextAlign;
import com.top_logic.presentation.model.TextAnchor;
import com.top_logic.presentation.model.TextBoxSpec;
import com.top_logic.presentation.model.TextSpec;
import com.top_logic.presentation.template.ColorInfo;

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
 * The text of the header rows is bold. Each row is separated from the next by a thin line, the
 * header rows by a stronger line.</li>
 * <li>An {@link ImageSpec image} is fitted into its bounds as requested.</li>
 * </ul>
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

	private final XSLFSlide _slide;

	private final DrawingWriter _drawing;

	private final TextWriter _text;

	/**
	 * Creates an {@link ElementWriter}.
	 *
	 * @param slide
	 *        The slide to place the elements on.
	 * @param drawing
	 *        Writes colors, fills, outlines and shadows.
	 * @param text
	 *        Writes texts.
	 */
	ElementWriter(XSLFSlide slide, DrawingWriter drawing, TextWriter text) {
		_slide = slide;
		_drawing = drawing;
		_text = text;
	}

	/**
	 * Creates the given element on the slide.
	 *
	 * @param element
	 *        The element description.
	 * @param bounds
	 *        The position and size of the element on the slide, see {@link Placement}.
	 * @param path
	 *        The path of the element in the description, for error messages.
	 */
	void write(ElementSpec element, Rectangle2D bounds, String path) {
		if (element instanceof ShapeSpec shape) {
			writeShape(shape, bounds, path);
		} else if (element instanceof TextBoxSpec textBox) {
			writeTextBox(textBox, bounds, path);
		} else if (element instanceof TableSpec table) {
			writeTable(table, bounds, path);
		} else if (element instanceof ImageSpec image) {
			writeImage(image, bounds, path);
		} else {
			throw new IllegalArgumentException("Unsupported element: " + element);
		}
	}

	private void writeShape(ShapeSpec spec, Rectangle2D bounds, String path) {
		XSLFAutoShape shape = _slide.createAutoShape();
		shape.setAnchor(bounds);
		CTShape xml = (CTShape) shape.getXmlObject();
		TextWriter.addTextBody(xml);
		CTShapeProperties spPr = xml.getSpPr();
		_drawing.writeGeometry(spPr, spec.geometry(), bounds);
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
		if (spec.shadow() != null) {
			_drawing.writeShadow(spPr, spec.shadow(), child(path, PresentationData.SHADOW));
		}
		TextSpec text = spec.text();
		if (text == null) {
			text = TextSpec.of(List.of());
		}
		_text.write(shape, centered(text), child(path, PresentationData.TEXT));
	}

	private static TextSpec centered(TextSpec text) {
		return new TextSpec(text.paragraphs(), text.style(), text.align() != null ? text.align() : TextAlign.CENTER,
			text.anchor() != null ? text.anchor() : TextAnchor.MIDDLE, text.insets(), text.autofit(), text.wrap());
	}

	private void writeTextBox(TextBoxSpec spec, Rectangle2D bounds, String path) {
		XSLFTextBox box = _slide.createTextBox();
		box.setAnchor(bounds);
		_text.write(box, spec.text(), child(path, PresentationData.TEXT));
	}

	private void writeTable(TableSpec spec, Rectangle2D bounds, String path) {
		int rowCount = spec.rows().size();
		int columnCount = spec.columnCount();
		if (rowCount == 0 || columnCount == 0) {
			return;
		}
		XSLFTable table = _slide.createTable(rowCount, columnCount);
		CTTableProperties tblPr = table.getCTTable().getTblPr();
		tblPr.setFirstRow(spec.headerRows() > 0);

		double[] widths = columnWidths(spec, bounds.getWidth());
		double totalWidth = 0;
		for (int column = 0; column < columnCount; column++) {
			table.setColumnWidth(column, widths[column]);
			totalWidth += widths[column];
		}

		String rowsPath = child(path, PresentationData.ROWS);
		for (int row = 0; row < rowCount; row++) {
			boolean header = row < spec.headerRows();
			LineSpec rule = row == spec.headerRows() - 1 ? HEADER_RULE : ROW_RULE;
			table.setRowHeight(row, spec.rowHeight() == null ? 0 : spec.rowHeight().doubleValue());
			for (int column = 0; column < columnCount; column++) {
				String cellPath = rowsPath + "[" + row + "][" + column + "]";
				writeCell(table.getCell(row, column), spec.rows().get(row).get(column), header, rule, cellPath);
			}
		}

		table.setAnchor(new Rectangle2D.Double(bounds.getX(), bounds.getY(), totalWidth, bounds.getHeight()));
		// Grows the rows to the height of their text.
		table.updateCellAnchor();
		double height = 0;
		for (int row = 0; row < rowCount; row++) {
			double rowHeight = table.getCell(row, 0).getAnchor().getHeight();
			table.setRowHeight(row, rowHeight);
			height += rowHeight;
		}
		table.setAnchor(new Rectangle2D.Double(bounds.getX(), bounds.getY(), totalWidth, height));
		table.updateCellAnchor();
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

	private void writeCell(XSLFTableCell cell, CellSpec spec, boolean header, LineSpec rule, String path) {
		TextSpec text = spec.text();
		if (text != null) {
			if (header) {
				text = new TextSpec(text.paragraphs(), text.style().inheritFrom(HEADER_STYLE), text.align(),
					text.anchor(), text.insets(), text.autofit(), text.wrap());
			}
			_text.write(cell, text, path);
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

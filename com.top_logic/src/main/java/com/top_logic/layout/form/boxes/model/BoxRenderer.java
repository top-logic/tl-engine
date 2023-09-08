/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Arrays;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Renderer;

/**
 * {@link Renderer} for {@link Box}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BoxRenderer implements Renderer<Box> {

	/**
	 * Singleton {@link BoxRenderer} instance.
	 */
	public static final BoxRenderer INSTANCE = new BoxRenderer();

	private BoxRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Box value) throws IOException {
		writeBox(context, out, value, null);
	}

	/**
	 * Type safe variant of #{@link #write(DisplayContext, TagWriter, Box)}.
	 */
	public void writeBox(DisplayContext displayContext, TagWriter out, Box value, String id) throws IOException {
		value.layout();
		int columns = value.getColumns();
		int rows = value.getRows();
		Table<ContentBox> table = new Table<>(columns, rows);
		value.enter(0, 0, columns, rows, table);
		
		DisplayDimension[] dimensions = computeColumnWidths(table);
		float percentageFactor = computePercentageFactor(dimensions);
		
		out.beginBeginTag(TABLE);
		out.writeAttribute(ID_ATTR, id);
		out.writeAttribute(CLASS_ATTR, "frm");
		out.endBeginTag();
		
		out.beginTag(COLGROUP);
		for (int column = 0; column < columns; column++) {
			out.beginBeginTag(COL);
			out.beginAttribute(STYLE_ATTR);
			writeWidthProperty(out, percentageFactor, dimensions[column]);
			out.endAttribute();
			out.endEmptyTag();
		}
		out.endTag(COLGROUP);
		
		for (int row = 0; row < rows; row++) {
			out.beginBeginTag(TR);
			out.endBeginTag();
			for (int column = 0; column < columns; column++) {
				ContentBox box = table.get(column, row);
				if (box == null) {
					// Space is covered by a box spanning some rows or columns.
					continue;
				}
		
				out.beginBeginTag(TD);
				{
					writeCssClasses(out, box);

					String customStyle = box.getStyle();
					if (row == 0) {
						// Annotate width styles to the first row, just in case, the browser does
						// ignore the column definitions above.
						out.beginAttribute(STYLE_ATTR);
						writeWidthProperty(out, percentageFactor, dimensions[column]);
						if (customStyle != null) {
							out.write(customStyle);
						}
						out.endAttribute();
					} else {
						out.writeAttribute(STYLE_ATTR, customStyle);
					}
					if (box.getColumns() > 1) {
						out.writeAttribute(COLSPAN_ATTR, box.getColumns());
					}
					if (box.getRows() > 1) {
						out.writeAttribute(ROWSPAN_ATTR, box.getRows());
					}
				}
				out.endBeginTag();
				{
					box.getContentRenderer().write(displayContext, out);
				}
				out.endTag(TD);
			}
			out.endTag(TR);
		}
		out.endTag(TABLE);
	}

	private void writeCssClasses(TagWriter out, Box box) throws IOException {
		boolean hasClasses = false;
		for (Box anchestor = box; anchestor != null; anchestor = anchestor.getParent()) {
			String localClass = anchestor.getCssClass();
			if (localClass != null) {
				if (hasClasses) {
					out.append(' ');
				} else {
					out.beginAttribute(CLASS_ATTR);
					hasClasses = true;
				}
				out.append(localClass);
			}
		}
		if (hasClasses) {
			out.endAttribute();
		}
	}

	void writeWidthProperty(TagWriter out, float percentageFactor, DisplayDimension dimension)
			throws IOException {
		out.write("width:");
		writeDimension(out, percentageFactor, dimension);
		out.write(";");
	}

	private void writeDimension(TagWriter out, float percentageFactor, DisplayDimension dimension) throws IOException {
		if (dimension.getUnit() == DisplayUnit.PERCENT) {
			writeNumber(out, dimension.getValue() * percentageFactor);
			out.write("%");
		} else {
			writeNumber(out, dimension.getValue());
			out.write("px");
		}
	}

	float computePercentageFactor(DisplayDimension[] dimensions) {
		int columns = dimensions.length;
		float sumPercentage = 0.0F;
		for (int n = 0; n < columns; n++) {
			DisplayDimension dimension = dimensions[n];
			if (dimension.getUnit() == DisplayUnit.PERCENT) {
				sumPercentage += dimension.getValue();
			}
		}

		float percentageFactor;
		if (sumPercentage > 0.0F) {
			percentageFactor = 100.0F / sumPercentage;
		} else {
			percentageFactor = 1.0F;
		}
		return percentageFactor;
	}

	DisplayDimension[] computeColumnWidths(Table<ContentBox> table) {
		int columns = table.getColumns();
		int rows = table.getRows();

		int[] definingBoxColumns = new int[columns];
		Arrays.fill(definingBoxColumns, Integer.MAX_VALUE);
		DisplayDimension[] dimensions = new DisplayDimension[columns];

		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				Box box = table.get(column, row);
				if (box == null) {
					// No box placed in this column.
					continue;
				}

				int boxColumns = box.getColumns();
				if (boxColumns > definingBoxColumns[column]) {
					// There is a box with a smaller colspan in this column that defined the width.
					continue;
				}

				DisplayDimension boxWidth = box.getWidth();
				if (dimensions[column] != null && boxWidth.getValue() >= dimensions[column].getValue()
					&& boxColumns == definingBoxColumns[column]) {
					// The current box is "smaller" than the box defining the current columns width.
					continue;
				}

				definingBoxColumns[column] = boxColumns;
				dimensions[column] = boxWidth;
			}
		}

		for (int n = 0; n < columns; n++) {
			if (dimensions[n] == null) {
				dimensions[n] = DisplayDimension.ZERO_PERCENT;
			}
		}
		return dimensions;
	}

	private void writeNumber(TagWriter out, float floatNum) throws IOException {
		int intNum = (int) floatNum;
		if (floatNum == intNum) {
			out.write(Integer.toString(intNum));
		} else {
			out.write(Float.toString(floatNum));
		}
	}

}

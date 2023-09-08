/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.boxes.model;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.boxes.layout.ColumnsLayout;
import com.top_logic.layout.form.boxes.layout.FlowLayout;
import com.top_logic.layout.form.boxes.layout.HorizontalLayout;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.BoxFactory;
import com.top_logic.layout.form.boxes.model.BoxRenderer;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;
import com.top_logic.layout.form.boxes.model.DescriptionBox;
import com.top_logic.layout.form.boxes.model.FragmentBox;
import com.top_logic.layout.form.boxes.model.LineBox;

/**
 * Test case for {@link Box}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBox extends TestCase {

	static class DoubleSeparator extends DefaultCollectionBox {
		public DoubleSeparator() {
			super(HorizontalLayout.INSTANCE);
			addContent(Boxes.textBox("]"));
			addContent(Boxes.textBox("["));
		}
	}

	private static final BoxFactory SIMPLE_SEPARATORS = new BoxFactory() {
		@Override
		public Box newBox() {
			FragmentBox result = Boxes.textBox("separator");
			result.setWidth(DisplayDimension.dim(5, DisplayUnit.PIXEL));
			return result;
		}
	};

	public void testHorizontally() throws IOException {
		DefaultCollectionBox boxes = new DefaultCollectionBox(HorizontalLayout.INSTANCE);
		LineBox left = new LineBox();
		boxes.addContent(left);

		boxes.addContent(new DoubleSeparator());

		LineBox right = new LineBox();
		boxes.addContent(right);

		DescriptionBox entry1 = new DescriptionBox(text("e1"));
		left.addContent(entry1);

		DescriptionBox entry2 = new DescriptionBox(text("e2"));
		left.addContent(entry2);

		DescriptionBox entry3 = new DescriptionBox(text("e3"));
		right.addContent(entry3);

		TagWriter buffer = new TagWriter();
		BoxRenderer.INSTANCE.write(null, buffer, boxes);

		assertEquals(
			"<table class=\"frm\">"
				+ "<colgroup>"
				+ "<col style=\"width:25%;\"/>"
				+ "<col style=\"width:25%;\"/>"
				+ "<col style=\"width:25%;\"/>"
				+ "<col style=\"width:25%;\"/>"
				+ "</colgroup>"
				+ "<tr>"
				+ "<td style=\"width:25%;\">e1</td>"
				+ "<td style=\"width:25%;\" rowspan=\"2\">]</td>"
				+ "<td style=\"width:25%;\" rowspan=\"2\">[</td>"
				+ "<td style=\"width:25%;\" rowspan=\"2\">e3</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td>e2</td>"
				+ "</tr>"
				+ "</table>",
			buffer.toString());
	}

	public void testFlowLayout() throws IOException {
		DefaultCollectionBox boxes = new DefaultCollectionBox(new FlowLayout(2));
		boxes.addContent(text("e1", 2, 1));
		boxes.addContent(text("e2", 1, 2));
		boxes.addContent(text("e3", 3, 3));

		TagWriter buffer = new TagWriter();
		BoxRenderer.INSTANCE.write(null, buffer, boxes);

		assertEquals(
			"<table class=\"frm\">"
				+ "<colgroup>"
				+ "<col style=\"width:50%;\"/>"
				+ "<col style=\"width:0%;\"/>"
				+ "<col style=\"width:0%;\"/>"
				+ "<col style=\"width:50%;\"/>"
				+ "</colgroup>"
				+ "<tr>"
				+ "<td style=\"width:50%;\" colspan=\"3\" rowspan=\"2\">e1</td>"
				+ "<td style=\"width:50%;\" rowspan=\"2\">e2</td>"
				+ "</tr>"
				+ "<tr>"
				+ "</tr>"
				+ "<tr>"
				+ "<td colspan=\"4\" rowspan=\"3\">e3</td>"
				+ "</tr>"
				+ "<tr>"
				+ "</tr>"
				+ "<tr>"
				+ "</tr>"
				+ "</table>",
			buffer.toString());
	}

	public void testColumnsLayout() throws IOException {
		DefaultCollectionBox boxes = new DefaultCollectionBox(new ColumnsLayout(2, SIMPLE_SEPARATORS));
		boxes.addContent(text("e1", 2, 2));
		boxes.addContent(text("e2", 2, 3));
		boxes.addContent(text("e3", 1, 2));
		boxes.addContent(text("e4", 1, 1));
		boxes.addContent(text("e5", 3, 3));

		// Total rows: 11
		// Avg. rows per column: 6
		// e3 extends the container rows to 7, since its beginning fits into the first super column.
		// Fist super column has 2 columns, the second one 3.

		TagWriter buffer = new TagWriter();
		BoxRenderer.INSTANCE.write(null, buffer, boxes);

		assertEquals(
			"<table class=\"frm\">"
				+ "<colgroup>"
				+ "<col style=\"width:50%;\"/>"
				+ "<col style=\"width:0%;\"/>"
				+ "<col style=\"width:5px;\"/>"
				+ "<col style=\"width:50%;\"/>"
				+ "<col style=\"width:0%;\"/>"
				+ "<col style=\"width:0%;\"/>"
				+ "</colgroup>"
				+ "<tr>"
				+ "<td style=\"width:50%;\" colspan=\"2\" rowspan=\"2\">e1</td>"
				+ "<td style=\"width:5px;\" rowspan=\"7\">separator</td>"
				+ "<td style=\"width:50%;\" colspan=\"3\">e4</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td colspan=\"3\" rowspan=\"6\">e5</td>"
				+ "</tr>"
				+ "<tr>"
				+ "<td colspan=\"2\" rowspan=\"3\">e2</td>"
				+ "</tr>"
				+ "<tr>"
				+ "</tr>"
				+ "<tr>"
				+ "</tr>"
				+ "<tr>"
				+ "<td colspan=\"2\" rowspan=\"2\">e3</td>"
				+ "</tr>"
				+ "<tr>"
				+ "</tr>"
				+ "</table>",
			buffer.toString());
	}

	private ContentBox text(String text, int columns, int rows) {
		ContentBox result = text(text);
		result.setInitialColumns(columns);
		result.setInitialRows(rows);
		return result;
	}

	private ContentBox text(String text) {
		return Boxes.contentBox(Fragments.text(text));
	}

}

/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package test.com.top_logic.graphic.blocks.server.svg;

import java.io.IOException;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Test case for {@link SvgTagWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSvgTagWriter extends TestCase {

	public void testWrite() throws IOException {
		try (TagWriter out = new TagWriter(new OutputStreamWriter(System.out))) {
			try (SvgWriter svg = new SvgTagWriter(out)) {
				svg.beginSvg();
				svg.beginGroup();
				svg.translate(50, 150);
				svg.beginPath();
				svg.beginData();
				svg.moveToRel(0, 0);
				svg.roundedCorner(false, 10, -10);
				svg.lineToHorizontalRel(100);
				svg.roundedCorner(false, 10, 10);
				svg.lineToVerticalRel(50);
				svg.roundedCorner(false, -10, 10);
				svg.lineToHorizontalRel(-100);
				svg.roundedCorner(false, -10, -10);
				svg.closePath();
				svg.endData();
				svg.endPath();
				svg.endGroup();
				svg.endSvg();
			}
		}
	}
}

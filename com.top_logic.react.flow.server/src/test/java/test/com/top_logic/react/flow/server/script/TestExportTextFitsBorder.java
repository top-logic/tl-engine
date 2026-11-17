/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.script;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.script.FlowFactory;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.server.ui.FontFamilyResolver;

/**
 * Test that a text in an exported diagram, drawn by a renderer on the exporting machine, fills
 * exactly the box the layout measured for it.
 *
 * <p>
 * The diagram is exported like {@link FlowFactory#toSvg(Diagram, String, double, Double, Double)}
 * does, but with an explicit set of installed font families, so that the result does not depend
 * on the fonts of the machine. The renderer is simulated by resolving the {@code font-family} of
 * the exported SVG against the same installed families and measuring the text in the font found.
 * </p>
 */
public class TestExportTextFitsBorder extends TestCase {

	private static final String LONG_TEXT =
		"A_very_long_label_without_spaces_that_names_an_item_and_its_current_state_12345";

	private static final double PADDING = 4;

	private static final float TEXT_SIZE_PT = 12f;

	private static final double DELTA = 1e-6;

	/**
	 * Installed families in which {@code Arial}, the first family of the default list, is missing.
	 */
	private static final List<String> INSTALLED = Arrays.asList("DejaVu Sans", "Liberation Sans");

	/**
	 * A long text in a padded border fills the border in the font a renderer on the exporting
	 * machine draws it with.
	 */
	public void testLongTextFitsBorder() throws Exception {
		Diagram diagram = Diagram.create().setRoot(Border.create()
			.setContent(Padding.create().setAll(PADDING)
				.setContent(Text.create().setValue(LONG_TEXT))));

		Document svg = parse(export(diagram));
		Element root = svg.getDocumentElement();
		Element text = (Element) root.getElementsByTagName("text").item(0);
		Element border = (Element) root.getElementsByTagName("path").item(0);

		double renderedWidth = renderedWidth(text.getTextContent(),
			root.getAttribute("font-family"), root.getAttribute("font-size"));

		double[] borderX = xRange(border.getAttribute("d"));
		double strokeWidth = Double.parseDouble(border.getAttribute("stroke-width"));
		double innerLeft = borderX[0] + strokeWidth / 2;
		double innerRight = borderX[1] - strokeWidth / 2;
		double textX = Double.parseDouble(text.getAttribute("x"));

		assertEquals("Text must start after the padding.", innerLeft + PADDING, textX, DELTA);
		assertEquals("Rendered text must fill the box measured for it.",
			innerRight - PADDING, textX + renderedWidth, DELTA);
	}

	private static String export(Diagram diagram) throws IOException {
		AWTContext context = new AWTContext(TEXT_SIZE_PT, new FontFamilyResolver(INSTALLED));
		diagram.layout(context);
		diagram.setViewBoxWidth(diagram.getRoot().getWidth());
		diagram.setViewBoxHeight(diagram.getRoot().getHeight());

		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter)) {
			diagram.draw(svgWriter, context, "");
		}
		return buffer.toString();
	}

	/**
	 * The width of the given text in the first family of the given {@code font-family} list that
	 * is {@link #INSTALLED}, as a renderer on the exporting machine draws it.
	 */
	private static double renderedWidth(String value, String fontFamily, String fontSize) {
		String family = Font.SANS_SERIF;
		for (String entry : fontFamily.split(",")) {
			String name = entry.trim().replace("'", "").replace("\"", "");
			if (INSTALLED.contains(name)) {
				family = name;
				break;
			}
		}
		double sizePx = Double.parseDouble(fontSize.replace("px", "").trim());
		Font font = new Font(family, Font.PLAIN, 1).deriveFont((float) (sizePx * AWTContext.PT_PER_PX));
		return font.getStringBounds(value, new FontRenderContext(null, true, true)).getWidth()
			* AWTContext.PX_PER_PT;
	}

	/**
	 * The smallest and largest x coordinate of the given SVG path data of absolute commands.
	 */
	private static double[] xRange(String pathData) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (String point : pathData.replaceAll("[A-Za-z]", " ").trim().split("\\s+")) {
			double x = Double.parseDouble(point.split(",")[0]);
			min = Math.min(min, x);
			max = Math.max(max, x);
		}
		return new double[] { min, max };
	}

	private static Document parse(String svg) throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
			.parse(new InputSource(new StringReader(svg)));
	}

}

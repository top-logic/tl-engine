/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.model.Align;
import com.top_logic.graphic.flow.model.Border;
import com.top_logic.graphic.flow.model.FlowDiagram;
import com.top_logic.graphic.flow.model.TextLine;
import com.top_logic.graphic.flow.model.layout.GridLayout;
import com.top_logic.graphic.flow.model.layout.HorizontalLayout;
import com.top_logic.graphic.flow.model.layout.VerticalLayout;
import com.top_logic.graphic.flow.param.HAlign;
import com.top_logic.graphic.flow.param.SpaceDistribution;

/**
 * Test case for {@link FlowDiagram}.
 */
public class TestFlowDiagram extends TestCase {

	public void testCreate() {
		FlowDiagram diagram = new FlowDiagram()
			.setRoot(new VerticalLayout()
				.setGap(20)
				.addRow(new VerticalLayout()
					.addRow(new HorizontalLayout()
						.setFill(SpaceDistribution.STRETCH_CONTENT)
						.addCol(new Border()
							.setContent(new TextLine("Hello")))
						.addCol(
							new Border()
								.setLeft(false).setStrokeStyle("red").setContent(new TextLine("world!"))))
					.addRow(new HorizontalLayout()
						.setFill(SpaceDistribution.STRETCH_CONTENT)
						.addCol(
							new Border()
								.setTop(false).setStrokeStyle("blue").setContent(new TextLine("Greetings")))
						.addCol(
							new Border()
								.setTop(false).setLeft(false).setStrokeStyle("green")
								.setContent(new TextLine("from Moon to Earth!")))))
				.addRow(new VerticalLayout()
					.setGap(-1)
					.addRow(new HorizontalLayout()
						.addCol(new Border()
							.setContent(new TextLine("Hello")))
						.addCol(
							new Border()
								.setLeft(false)
								.setStrokeStyle("red").setContent(new TextLine("world!"))))
					.addRow(new HorizontalLayout()
						.addCol(
							new Border()
								.setStrokeStyle("blue").setContent(new TextLine("Greetings")))
						.addCol(
							new Border()
								.setLeft(false)
								.setStrokeStyle("green")
								.setContent(new TextLine("from Moon to Earth!")))))
				.addRow(new GridLayout(2, 2)
					.set(0, 0, new Border()
						.setContent(new TextLine("Hello")))
					.set(1, 0, new Border()
						.setLeft(false)
						.setStrokeStyle("red").setContent(new TextLine("world!")))
					.set(0, 1, new Border()
						.setTop(false)
						.setStrokeStyle("blue").setContent(new TextLine("Greetings")))
					.set(1, 1, new Border()
						.setTop(false)
						.setLeft(false)
						.setStrokeStyle("green")
						.setContent(new TextLine("from Moon to Earth!"))))
				.addRow(new GridLayout(2, 2)
					.setGapY(-1)
					.set(0, 0, new Border()
						.setContent(new TextLine("Hello")))
					.set(1, 0, new Align()
						.setHAlign(HAlign.LEFT)
						.setContent(new Border()
							.setLeft(false)
							.setStrokeStyle("red").setContent(new TextLine("world!"))))
					.set(0, 1, new Border()
						.setStrokeStyle("blue").setContent(new TextLine("Greetings")))
					.set(1, 1, new Align()
						.setHAlign(HAlign.RIGHT)
						.setContent(new Border()
							.setLeft(false)
							.setStrokeStyle("green")
							.setContent(new TextLine("from Moon to Earth!"))))));

		diagram.layout(new TestingRenderContext());

		TagWriter out = new TagWriter();
		SvgWriter svg = new SvgTagWriter(out);
		diagram.draw(svg);

		System.out.println(out.toString());

		assertEquals(
			"<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"><text x=\"1.0\" y=\"11.0\">Hello</text><path stroke-width=\"1.0\" stroke=\"black\" fill=\"none\" d=\"M 0.5,0.5 L 163.5,0.5 L 163.5,13.5 L 0.5,13.5 z\"></path><text x=\"164.0\" y=\"11.0\">world!</text><path stroke-width=\"1.0\" stroke=\"red\" fill=\"none\" d=\"M 164.0,0.5 L 338.5,0.5 L 338.5,13.5 L 164.0,13.5\"></path><text x=\"1.0\" y=\"24.0\">Greetings</text><path stroke-width=\"1.0\" stroke=\"blue\" fill=\"none\" d=\"M 0.5,14.5 M 109.5,14.0 L 109.5,26.5 L 0.5,26.5 L 0.5,14.0\"></path><text x=\"110.0\" y=\"24.0\">from Moon to Earth!</text><path stroke-width=\"1.0\" stroke=\"green\" fill=\"none\" d=\"M 110.0,14.5 M 338.5,14.0 L 338.5,26.5 L 110.0,26.5\"></path><text x=\"1.0\" y=\"58.0\">Hello</text><path stroke-width=\"1.0\" stroke=\"black\" fill=\"none\" d=\"M 0.5,47.5 L 61.5,47.5 L 61.5,60.5 L 0.5,60.5 z\"></path><text x=\"62.0\" y=\"58.0\">world!</text><path stroke-width=\"1.0\" stroke=\"red\" fill=\"none\" d=\"M 62.0,47.5 L 134.5,47.5 L 134.5,60.5 L 62.0,60.5\"></path><text x=\"1.0\" y=\"71.0\">Greetings</text><path stroke-width=\"1.0\" stroke=\"blue\" fill=\"none\" d=\"M 0.5,60.5 L 109.5,60.5 L 109.5,73.5 L 0.5,73.5 z\"></path><text x=\"110.0\" y=\"71.0\">from Moon to Earth!</text><path stroke-width=\"1.0\" stroke=\"green\" fill=\"none\" d=\"M 110.0,60.5 L 338.5,60.5 L 338.5,73.5 L 110.0,73.5\"></path><text x=\"1.0\" y=\"105.0\">Hello</text><path stroke-width=\"1.0\" stroke=\"black\" fill=\"none\" d=\"M 0.5,94.5 L 109.5,94.5 L 109.5,107.5 L 0.5,107.5 z\"></path><text x=\"110.0\" y=\"105.0\">world!</text><path stroke-width=\"1.0\" stroke=\"red\" fill=\"none\" d=\"M 110.0,94.5 L 338.5,94.5 L 338.5,107.5 L 110.0,107.5\"></path><text x=\"1.0\" y=\"118.0\">Greetings</text><path stroke-width=\"1.0\" stroke=\"blue\" fill=\"none\" d=\"M 0.5,108.5 M 109.5,108.0 L 109.5,120.5 L 0.5,120.5 L 0.5,108.0\"></path><text x=\"110.0\" y=\"118.0\">from Moon to Earth!</text><path stroke-width=\"1.0\" stroke=\"green\" fill=\"none\" d=\"M 110.0,108.5 M 338.5,108.0 L 338.5,120.5 L 110.0,120.5\"></path><text x=\"1.0\" y=\"152.0\">Hello</text><path stroke-width=\"1.0\" stroke=\"black\" fill=\"none\" d=\"M 0.5,141.5 L 109.5,141.5 L 109.5,154.5 L 0.5,154.5 z\"></path><text x=\"110.0\" y=\"152.0\">world!</text><path stroke-width=\"1.0\" stroke=\"red\" fill=\"none\" d=\"M 110.0,141.5 L 182.5,141.5 L 182.5,154.5 L 110.0,154.5\"></path><text x=\"1.0\" y=\"165.0\">Greetings</text><path stroke-width=\"1.0\" stroke=\"blue\" fill=\"none\" d=\"M 0.5,154.5 L 109.5,154.5 L 109.5,167.5 L 0.5,167.5 z\"></path><text x=\"110.0\" y=\"165.0\">from Moon to Earth!</text><path stroke-width=\"1.0\" stroke=\"green\" fill=\"none\" d=\"M 110.0,154.5 L 338.5,154.5 L 338.5,167.5 L 110.0,167.5\"></path></svg>",
			out.toString());
	}

}

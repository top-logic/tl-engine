/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow;

import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Align;
import com.top_logic.graphic.flow.data.Alignment;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.GridLayout;
import com.top_logic.graphic.flow.data.HorizontalLayout;
import com.top_logic.graphic.flow.data.SpaceDistribution;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.VerticalLayout;
import com.top_logic.graphic.flow.data.Widget;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.server.io.ReaderAdapter;

/**
 * Test case for {@link Diagram}.
 */
public class TestFlowDiagram extends TestCase {

	public void testCreate() {
		Diagram diagram = Diagram.create()
			.setRoot(VerticalLayout.create()
				.setGap(20)
				.addContent(VerticalLayout.create()
					.addContent(HorizontalLayout.create()
						.setFill(SpaceDistribution.STRETCH_CONTENT)
						.addContent(Border.create()
							.setContent(Text.create().setValue("Hello")))
						.addContent(
							Border.create()
								.setLeft(false).setStrokeStyle("red").setContent(Text.create().setValue("world!"))))
					.addContent(HorizontalLayout.create()
						.setFill(SpaceDistribution.STRETCH_CONTENT)
						.addContent(
							Border.create()
								.setTop(false).setStrokeStyle("blue").setContent(Text.create().setValue("Greetings")))
						.addContent(
							Border.create()
								.setTop(false).setLeft(false).setStrokeStyle("green")
								.setContent(Text.create().setValue("from Moon to Earth!")))))
				.addContent(VerticalLayout.create()
					.setGap(-1)
					.addContent(HorizontalLayout.create()
						.addContent(Border.create()
							.setContent(Text.create().setValue("Hello")))
						.addContent(
							Border.create()
								.setLeft(false)
								.setStrokeStyle("red").setContent(Text.create().setValue("world!"))))
					.addContent(HorizontalLayout.create()
						.addContent(
							Border.create()
								.setStrokeStyle("blue").setContent(Text.create().setValue("Greetings")))
						.addContent(
							Border.create()
								.setLeft(false)
								.setStrokeStyle("green")
								.setContent(Text.create().setValue("from Moon to Earth!")))))
				.addContent(GridLayout.create().setRows(2).setCols(2)
					.set(0, 0, Border.create()
						.setContent(Text.create().setValue("Hello")))
					.set(1, 0, Border.create()
						.setLeft(false)
						.setStrokeStyle("red").setContent(Text.create().setValue("world!")))
					.set(0, 1, Border.create()
						.setTop(false)
						.setStrokeStyle("blue").setContent(Text.create().setValue("Greetings")))
					.set(1, 1, Border.create()
						.setTop(false)
						.setLeft(false)
						.setStrokeStyle("green")
						.setContent(Text.create().setValue("from Moon to Earth!"))))
				.addContent(GridLayout.create().setRows(2).setCols(2)
					.setGapY(-1)
					.set(0, 0, Border.create()
						.setContent(Text.create().setValue("Hello")))
					.set(1, 0, Align.create()
						.setXAlign(Alignment.START)
						.setContent(Border.create()
							.setLeft(false)
							.setStrokeStyle("red").setContent(Text.create().setValue("world!"))))
					.set(0, 1, Border.create()
						.setStrokeStyle("blue").setContent(Text.create().setValue("Greetings")))
					.set(1, 1, Align.create()
						.setXAlign(Alignment.STOP)
						.setContent(Border.create()
							.setLeft(false)
							.setStrokeStyle("green")
							.setContent(Text.create().setValue("from Moon to Earth!"))))));

		diagram.layout(new TestingRenderContext());

		TagWriter out = new TagWriter();
		SvgWriter svgOut = new SvgTagWriter(out);
		diagram.draw(svgOut);

		String svg = XMLPrettyPrinter.prettyPrint(out.toString());
		System.out.println(svg);

		assertEquals(
			"""
			<?xml version="1.0" encoding="utf-8" ?>
			
			<svg
				xmlns="http://www.w3.org/2000/svg"
				height="168.0"
				version="1.1"
				viewBox="0.0 0.0 339.0 168.0"
				width="339.0"
			>
				<text
					x="1.0"
					y="11.0"
				>Hello</text>
				<path
					d="M 0.5,0.5 L 163.5,0.5 L 163.5,13.5 L 0.5,13.5 z"
					fill="none"
					stroke="black"
					stroke-width="1.0"
				/>
				<text
					x="164.0"
					y="11.0"
				>world!</text>
				<path
					d="M 164.0,0.5 L 338.5,0.5 L 338.5,13.5 L 164.0,13.5"
					fill="none"
					stroke="red"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="24.0"
				>Greetings</text>
				<path
					d="M 0.5,14.5 M 109.5,14.0 L 109.5,26.5 L 0.5,26.5 L 0.5,14.0"
					fill="none"
					stroke="blue"
					stroke-width="1.0"
				/>
				<text
					x="110.0"
					y="24.0"
				>from Moon to Earth!</text>
				<path
					d="M 110.0,14.5 M 338.5,14.0 L 338.5,26.5 L 110.0,26.5"
					fill="none"
					stroke="green"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="58.0"
				>Hello</text>
				<path
					d="M 0.5,47.5 L 61.5,47.5 L 61.5,60.5 L 0.5,60.5 z"
					fill="none"
					stroke="black"
					stroke-width="1.0"
				/>
				<text
					x="62.0"
					y="58.0"
				>world!</text>
				<path
					d="M 62.0,47.5 L 134.5,47.5 L 134.5,60.5 L 62.0,60.5"
					fill="none"
					stroke="red"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="71.0"
				>Greetings</text>
				<path
					d="M 0.5,60.5 L 109.5,60.5 L 109.5,73.5 L 0.5,73.5 z"
					fill="none"
					stroke="blue"
					stroke-width="1.0"
				/>
				<text
					x="110.0"
					y="71.0"
				>from Moon to Earth!</text>
				<path
					d="M 110.0,60.5 L 338.5,60.5 L 338.5,73.5 L 110.0,73.5"
					fill="none"
					stroke="green"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="105.0"
				>Hello</text>
				<path
					d="M 0.5,94.5 L 109.5,94.5 L 109.5,107.5 L 0.5,107.5 z"
					fill="none"
					stroke="black"
					stroke-width="1.0"
				/>
				<text
					x="110.0"
					y="105.0"
				>world!</text>
				<path
					d="M 110.0,94.5 L 338.5,94.5 L 338.5,107.5 L 110.0,107.5"
					fill="none"
					stroke="red"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="118.0"
				>Greetings</text>
				<path
					d="M 0.5,108.5 M 109.5,108.0 L 109.5,120.5 L 0.5,120.5 L 0.5,108.0"
					fill="none"
					stroke="blue"
					stroke-width="1.0"
				/>
				<text
					x="110.0"
					y="118.0"
				>from Moon to Earth!</text>
				<path
					d="M 110.0,108.5 M 338.5,108.0 L 338.5,120.5 L 110.0,120.5"
					fill="none"
					stroke="green"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="152.0"
				>Hello</text>
				<path
					d="M 0.5,141.5 L 109.5,141.5 L 109.5,154.5 L 0.5,154.5 z"
					fill="none"
					stroke="black"
					stroke-width="1.0"
				/>
				<text
					x="110.0"
					y="152.0"
				>world!</text>
				<path
					d="M 110.0,141.5 L 182.5,141.5 L 182.5,154.5 L 110.0,154.5"
					fill="none"
					stroke="red"
					stroke-width="1.0"
				/>
				<text
					x="1.0"
					y="165.0"
				>Greetings</text>
				<path
					d="M 0.5,154.5 L 109.5,154.5 L 109.5,167.5 L 0.5,167.5 z"
					fill="none"
					stroke="blue"
					stroke-width="1.0"
				/>
				<text
					x="110.0"
					y="165.0"
				>from Moon to Earth!</text>
				<path
					d="M 110.0,154.5 L 338.5,154.5 L 338.5,167.5 L 110.0,167.5"
					fill="none"
					stroke="green"
					stroke-width="1.0"
				/>
			</svg>""",
			svg);
	}

	public void testComplex() throws IOException {
		Diagram diagram = (Diagram) Widget.readWidget(new DefaultScope(1, 0), new JsonReader(
			new ReaderAdapter(new InputStreamReader(TestFlowDiagram.class.getResourceAsStream("input.json")))));
		diagram.layout(new TestingRenderContext());
	}

}

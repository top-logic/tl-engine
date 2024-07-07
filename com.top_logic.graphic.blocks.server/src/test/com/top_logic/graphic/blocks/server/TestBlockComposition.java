/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package test.com.top_logic.graphic.blocks.server;

import static com.top_logic.graphic.blocks.model.factory.BlockFactory.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import com.top_logic.basic.shared.io.StringR;
import com.top_logic.basic.shared.io.StringW;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.BlockCss;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.connector.ConnectorTypes;
import com.top_logic.graphic.blocks.model.factory.BlockFactory;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Test case for the layout and serialization of a {@link BlockModel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBlockComposition extends TestCase {

	private static final String A = "tlbBlockA";

	private static final String B = "tlbBlockB";

	private static final String C = "tlbBlockC";

	private static final int TEXT_SIZE = 12;

	public void testCompose() throws IOException {

		BlockSchema schema = BlockFactory.createSchema();

		schema.defineBlockType("t1", A, ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.SEQUENCE_KIND,
			schema.mouthType(ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.VOID_KIND),
			schema.mouthType(ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.SEQUENCE_KIND));

		schema.defineBlockType("mapType", B, ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.SEQUENCE_KIND,
			rowType(label("map" + HTMLConstants.NBSP), select("s1", option("x"), option("zzz"))),
			schema.mouthType(ConnectorTypes.VALUE_KIND, ConnectorTypes.VALUE_KIND));

		schema.defineBlockType("t3", C, ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.SEQUENCE_KIND);
		schema.defineBlockType("t4", C, ConnectorTypes.VALUE_KIND, ConnectorTypes.VALUE_KIND);

		schema.defineBlockType("filterType", C, ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.SEQUENCE_KIND,
			rowType(label("filter" + HTMLConstants.NBSP), select("s1", option("y"))),
			schema.mouthType(ConnectorTypes.VALUE_KIND, ConnectorTypes.BOOLEAN_KIND));

		schema.defineBlockType("andType", C, ConnectorTypes.VOID_KIND, ConnectorTypes.BOOLEAN_KIND,
			schema.mouthType(ConnectorTypes.VOID_KIND, ConnectorTypes.BOOLEAN_KIND),
			schema.mouthType(ConnectorTypes.VOID_KIND, ConnectorTypes.BOOLEAN_KIND));

		schema.defineBlockType("countType", A, ConnectorTypes.SEQUENCE_KIND, ConnectorTypes.NUMBER_KIND,
			rowType(label("count")));
		schema.defineBlockType("literalNumber", C, ConnectorTypes.VOID_KIND, ConnectorTypes.NUMBER_KIND,
			rowType(text("s1", "0")));

		schema.defineBlockType("compareType", B, ConnectorTypes.NUMBER_KIND, ConnectorTypes.BOOLEAN_KIND,
			rowType(select("s1", option(">="))),
			schema.mouthType(ConnectorTypes.VOID_KIND, ConnectorTypes.NUMBER_KIND));

		schema.defineBlockType("checkType", C, ConnectorTypes.VALUE_KIND, ConnectorTypes.BOOLEAN_KIND);

		BlockList blocks = BlockFactory.blockList(50.5, 30.5,
			schema.block("t1")
				.mouth(0).init(
					schema.block("t3"),
					schema.block("mapType").mouth(0).init(
						schema.block("t4")),
					schema.block("filterType").mouth(0).init(
						schema.block("checkType")),
					schema.block("countType"),
					schema.block("compareType").mouth(0).init(
						schema.block("literalNumber"))
				), 
			schema.block("t3"));

		String schemaJson = toJson(schema);
		System.out.println(schemaJson);

		BlockSchema schemaCopy = BlockSchema.read(new JsonReader(new StringR(schemaJson)));
		assertEquals(schemaJson, toJson(schemaCopy));

		String jsonEncoded = toJson(blocks);
		System.out.println(jsonEncoded);

		BlockList copy = BlockList.read(schema, new JsonReader(new StringR(jsonEncoded)));
		assertEquals(jsonEncoded, toJson(copy));

		blocks.updateDimensions(new AWTContext(TEXT_SIZE), 0.0, 0.0);

		try (TagWriter out = new TagWriter(new OutputStreamWriter(
			new FileOutputStream(TestBlockComposition.class.getSimpleName() + ".html"), "utf-8"))) {

			out.beginTag(HTML);
			out.beginTag(HEAD);
			out.beginTag(STYLE_ELEMENT);
			out.write("text {font-family: Arial; font-size: " + TEXT_SIZE + "px; }");
			out.write("path {fill: none; }");
			out.write("g." + A + " path." + BlockCss.SHAPE_CLASS + " {stroke: #265699; fill: #4C97FF; }");
			out.write("g." + A + " path." + BlockCss.BORDER_CLASS + " {stroke: #6897d9; fill: none; }");
			out.write("g." + B + " path." + BlockCss.SHAPE_CLASS + " {stroke: #996711; fill: #FFAB19; }");
			out.write("g." + B + " path." + BlockCss.BORDER_CLASS + " {stroke: #e0b05e; fill: none; }");
			out.write("g." + C + " path." + BlockCss.SHAPE_CLASS + " {stroke: #2f792f; fill: #59C059; } ");
			out.write("g." + C + " path." + BlockCss.BORDER_CLASS + " {stroke: #63ad63; fill: none; } ");
			out.write("g." + BlockCss.INPUT_CLASS + " rect {fill: white; } ");
			out.write("g." + BlockCss.INPUT_CLASS + " text {fill: black; } ");
			out.write("g." + BlockCss.SELECT_CSS + " {cursor: pointer; } ");
			out.write("g." + BlockCss.SELECT_CSS + " rect {fill: #00000000; stroke: white; } ");
			out.write("g." + BlockCss.SELECT_CSS + ":hover rect {fill: #FFFFFF30; } ");
			out.write("g." + BlockCss.SELECT_CSS + " text {fill: white; } ");
			out.write("g." + BlockCss.SELECT_CSS + " path {fill: white; stroke: none; } ");
			out.write("g." + BlockCss.LABEL_CSS + " text {fill: white; } ");
			out.endTag(STYLE_ELEMENT);
			out.endTag(HEAD);
			out.beginTag(BODY);

			try (SvgWriter svg = new SvgTagWriter(out)) {
				svg.beginSvg();
				svg.dimensions("800px", "600px", 0, 0, 800, 600);

				svg.rect(0, 0, 30, 10);
				svg.rect(700, 500, 10, 10);

				blocks.draw(svg);

				svg.endSvg();
			}

			out.endTag(BODY);
			out.endTag(HTML);
		}
	}

	private String toJson(JsonSerializable<?> blocks) throws IOException {
		StringW buffer = new StringW();
		blocks.writeTo(new JsonWriter(buffer));
		return buffer.toString();
	}

}

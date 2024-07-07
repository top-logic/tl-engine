/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.server.control;

import static com.top_logic.graphic.blocks.model.factory.BlockFactory.*;
import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import java.io.IOException;

import com.top_logic.ajax.server.util.JSControlUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.control.JSBlocksControlCommon;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.connector.ConnectorTypes;
import com.top_logic.graphic.blocks.model.factory.BlockFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Server-side part of {@link JSBlocksControlCommon}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlocksControl extends AbstractControlBase implements JSBlocksControlCommon {

	@Override
	public Object getModel() {
		// TODO #24649: Define.
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Ignore.
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {

		BlockSchema schema = BlockFactory.createSchema();

		String A = "tlbA";
		String B = "tlbB";
		String C = "tlbC";

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
						schema.block("literalNumber"))),
			schema.block("t3"));
		
		blocks.visit(IdAssignmentVisitor.INSTANCE, getScope().getFrameScope());

		out.beginBeginTag(SVG);
		writeControlAttributes(context, out);
		out.beginAttribute(JSBlocksControlCommon.TL_BLOCK_SCHEMA);
		{
			schema.writeTo(new JsonWriter(out));
		}
		out.endAttribute();
		out.beginAttribute(JSBlocksControlCommon.TL_BLOCK_DATA);
		{
			blocks.writeTo(new JsonWriter(out));
		}
		out.endAttribute();

		// TODO #24649: Make reactive.
		out.writeAttribute("width", "800px");
		out.writeAttribute("height", "600px");
		out.writeAttribute("viewBox", 0 + " " + 0 + " " + 800 + " " + 600);

		out.endBeginTag();
		out.endTag(SVG);

		JSControlUtil.writeCreateJSControlScript(out, JSBlocksControlCommon.BLOCKS_CONTROL_TYPE, getID());
	}

}

/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.factory;

import java.util.Arrays;
import java.util.List;

import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.BlockType;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.connector.BooleanConnector;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.connector.NumberConnector;
import com.top_logic.graphic.blocks.model.connector.SequenceConnector;
import com.top_logic.graphic.blocks.model.connector.ValueConnector;
import com.top_logic.graphic.blocks.model.connector.VoidConnector;
import com.top_logic.graphic.blocks.model.content.BlockContentType;
import com.top_logic.graphic.blocks.model.content.mouth.MouthType;
import com.top_logic.graphic.blocks.model.content.row.BlockRowType;
import com.top_logic.graphic.blocks.model.content.row.part.LabelType;
import com.top_logic.graphic.blocks.model.content.row.part.Option;
import com.top_logic.graphic.blocks.model.content.row.part.RowPartType;
import com.top_logic.graphic.blocks.model.content.row.part.RowParts;
import com.top_logic.graphic.blocks.model.content.row.part.SelectInputType;
import com.top_logic.graphic.blocks.model.content.row.part.TextInputType;

/**
 * Factory methods for creating blocks.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockFactory {

	/**
	 * Creates a {@link BlockSchema} and registers all known connector and content types.
	 */
	public static BlockSchema createSchema() {
		BlockSchema schema = new BlockSchema();
		schema.defineRowPartType(RowParts.LABEL_KIND, LabelType::new);
		schema.defineRowPartType(RowParts.TEXT_KIND, TextInputType::new);
		schema.defineRowPartType(RowParts.SELECT_KIND, SelectInputType::new);
		
		schema.defineConnectorType(VoidConnector.INSTANCE);
		schema.defineConnectorType(BooleanConnector.INSTANCE);
		schema.defineConnectorType(ValueConnector.INSTANCE);
		schema.defineConnectorType(SequenceConnector.INSTANCE);
		schema.defineConnectorType(NumberConnector.INSTANCE);
		return schema;
	}

	/**
	 * Creates a {@link BlockType}.
	 */
	public static BlockType blockType(String id, String cssClass, ConnectorType topType, ConnectorType bottomType,
			BlockContentType... partTypes) {
		BlockType result = new BlockType();
		result.setId(id);
		result.setCssClass(cssClass);
		result.setTopType(topType);
		result.setBottomType(bottomType);
		for (BlockContentType partType : partTypes) {
			result.addPartType(partType);
		}
		return result;
	}

	/**
	 * Creates a {@link MouthType}.
	 */
	public static MouthType mouthType(ConnectorType topType, ConnectorType bottomType) {
		MouthType result = new MouthType();
		result.setTopType(topType);
		result.setBottomType(bottomType);
		return result;
	}

	/**
	 * Creates a {@link BlockRowType}.
	 */
	public static BlockRowType rowType(RowPartType...contentTypes) {
		BlockRowType result = new BlockRowType();
		for (RowPartType contentType : contentTypes) {
			result.append(contentType);
		}
		return result;
	}

	/**
	 * Creates a {@link LabelType}.
	 */
	public static LabelType label(String text) {
		LabelType result = new LabelType();
		result.setText(text);
		return result;
	}

	/**
	 * Creates a {@link SelectInputType}.
	 */
	public static SelectInputType select(String name, Option... options) {
		return select(name, Arrays.asList(options));
	}

	/**
	 * Creates a {@link SelectInputType}.
	 */
	public static SelectInputType select(String id, List<Option> options) {
		SelectInputType result = new SelectInputType();
		result.setInputId(id);
		result.setOptions(options);
		return result;
	}

	/**
	 * Creates an {@link Option} for a {@link SelectInputType}.
	 */
	public static Option option(String name) {
		return new Option(name);
	}

	/**
	 * Creates a {@link TextInputType}.
	 */
	public static TextInputType text(String id, String defaultValue) {
		TextInputType result = new TextInputType();
		result.setInputId(id);
		result.setDefaultValue(defaultValue);
		return result;
	}

	/**
	 * Creates a {@link BlockList}.
	 */
	public static BlockList blockList(double x, double y, Block... blocks) {
		BlockList result = new BlockList();
		result.setX(x);
		result.setY(y);
		for (Block block : blocks) {
			result.append(block);
		}
		return result;
	}

}

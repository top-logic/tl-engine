/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.AbstractBlockModel;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.model.block.Connector;
import com.top_logic.graphic.blocks.model.content.BlockContent;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;
import com.top_logic.graphic.blocks.model.content.row.part.RowPart;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * {@link BlockContent} representing a row of {@link RowPart}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockRow extends AbstractBlockModel implements BlockContent {

	private Block _owner;

	private List<RowPart> _contents;

	private double _width;

	private double _height;

	private double _baseLine;

	/**
	 * Creates a {@link Mouth}.
	 */
	public BlockRow(Block owner, BlockRowType type) {
		_owner = owner;
		_contents = type.createContents(this);
	}

	@Override
	public Block getOwner() {
		return _owner;
	}

	/**
	 * The {@link RowPart}s contained in this {@link BlockRow}.
	 */
	public List<RowPart> getContents() {
		return _contents;
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		double width = BlockDimensions.ROW_PADDING_LEFT;
		double height = 0.0;
		double baseLine = 0.0;

		double xPos = offsetX + BlockDimensions.ROW_PADDING_LEFT;
		for (RowPart content : _contents) {
			content.updateDimensions(context, xPos, offsetY);
			TextMetrics metrics = content.getMetrics();
			double contentWidth = metrics.getWidth();
			width += contentWidth;
			height = Math.max(height, metrics.getHeight());
			baseLine = Math.max(baseLine, metrics.getBaseLine());
			xPos += contentWidth;
		}

		for (RowPart content : _contents) {
			content.setTargetBaseLine(baseLine);
		}

		_width = width;
		_height = height;
		_baseLine = baseLine;
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginGroup();
		out.writeId(getId());
		for (RowPart content : _contents) {
			content.draw(out);
		}
		out.endGroup();
	}

	@Override
	public double getWidth() {
		return _width;
	}

	@Override
	public double getHeight() {
		return _height;
	}

	/**
	 * The offset of the common base-line of all {@link #getContents()} to the top border of area of
	 * this {@link BlockRow}.
	 */
	public double getBaseLine() {
		return _baseLine;
	}

	@Override
	public void outlineRight(SvgWriter out) {
		out.lineToVerticalRel(getHeight());
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);

		json.name("contents");
		JsonSerializable.writeArray(json, getContents());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "contents":
				json.beginArray();
				for (RowPart part : getContents()) {
					part.readFrom(context, json);
				}
				json.endArray();
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

	@Override
	public void forEachConnector(Consumer<Connector> consumer) {
		// No connectors.
	}

	@Override
	public <R, A> R visit(BlockVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
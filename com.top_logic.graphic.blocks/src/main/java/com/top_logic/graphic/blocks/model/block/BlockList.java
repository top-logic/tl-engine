/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.block;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Top-level {@link BlockCollection} not linked to any other connector.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockList extends AbstractBlockConcatenation {

	private double _x;

	private double _y;

	/**
	 * Creates a {@link BlockList}.
	 */
	public BlockList() {
		super();
	}

	@Override
	public BlockModel getOwner() {
		return null;
	}

	@Override
	public BlockList top() {
		return this;
	}

	@Override
	public double getX() {
		return _x;
	}

	/**
	 * @see #getX()
	 */
	public void setX(double x) {
		_x = x;
	}

	@Override
	public double getY() {
		return _y;
	}

	/**
	 * @see #getY()
	 */
	public void setY(double y) {
		_y = y;
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);
		json.name("x");
		json.value(getX());
		json.name("y");
		json.value(getY());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "x":
				setX(json.nextDouble());
				break;

			case "y":
				setY(json.nextDouble());
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

	/**
	 * Reads a {@link BlockList} from the given {@link JsonReader}.
	 */
	public static BlockList read(BlockSchema blockSchema, JsonReader json) throws IOException {
		BlockList result = new BlockList();
		result.readFrom(blockSchema, json);
		return result;
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginGroup();
		out.translate(getX(), getY());
		out.writeId(getId());
		super.draw(out);
		out.endGroup();
	}

	@Override
	public <R, A> R visit(BlockVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}

/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.BlockCss;
import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * A text input field as {@link RowPart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextInput extends AbstractRowPart<TextInputType> {

	private String _value;

	private TextMetrics _measure;

	/**
	 * Creates a {@link TextInput}.
	 */
	public TextInput(BlockRow owner, TextInputType type) {
		super(owner, type);
		_value = type.getDefaultValue();
	}

	/**
	 * The typed text in the input field.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * {@link #getValue()}
	 */
	public void setValue(String value) {
		_value = value;
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		super.updateDimensions(context, offsetX, offsetY);
		_measure = context.measure(_value).padding(0, BlockDimensions.TEXT_PADDING_HORIZONTAL, 0, BlockDimensions.TEXT_PADDING_HORIZONTAL);
	}

	@Override
	public TextMetrics getMetrics() {
		return _measure;
	}

	@Override
	public void draw(SvgWriter out) {
		double x = getOffsetX();
		double baseLine = getOffsetY() + getTargetBaseLine();
		out.beginGroup();
		out.writeId(getId());
		out.writeCssClass(BlockCss.INPUT_CLASS);
		TextMetrics metrics = getMetrics();
		out.rect(x, baseLine - metrics.getBaseLine(), metrics.getWidth(), metrics.getHeight(), BlockDimensions.TEXT_RADIUS);
		out.text(x + BlockDimensions.TEXT_PADDING_HORIZONTAL, baseLine, _value);
		out.endGroup();
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);

		json.name("value");
		json.value(getValue());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "value":
				setValue(json.nextString());
				break;

			default:
				super.readPropertyFrom(context, json, name);
				break;
		}
	}

	@Override
	public <R, A> R visit(BlockVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}

}

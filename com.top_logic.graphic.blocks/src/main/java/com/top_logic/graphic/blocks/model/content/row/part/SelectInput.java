/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
 * A select input field as a {@link RowPart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectInput extends AbstractRowPart<SelectInputType> {

	private Option _selected;

	private TextMetrics _metrics;

	/**
	 * Creates a {@link SelectInput}.
	 */
	public SelectInput(BlockRow owner, SelectInputType type) {
		super(owner, type);
		_selected = type.getDefaultOption();
	}

	/**
	 * The currently selected {@link Option}.
	 */
	public Option getSelected() {
		return _selected;
	}

	/**
	 * @see #getSelected()
	 */
	public void setSelected(Option selected) {
		_selected = selected;
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		super.updateDimensions(context, offsetX, offsetY);
		_metrics = context.measure(_selected.getLabel()).padding(BlockDimensions.SELECT_PADDING, BlockDimensions.SELECT_PADDING, BlockDimensions.SELECT_PADDING, BlockDimensions.SELECT_PADDING_RIGHT);
	}

	@Override
	public TextMetrics getMetrics() {
		return _metrics;
	}

	@Override
	public void draw(SvgWriter out) {
		double x = getOffsetX();
		double baseLine = getOffsetY() + getTargetBaseLine();
		TextMetrics metrics = getMetrics();
		out.beginGroup();
		{
			out.writeId(getId());
			out.writeCssClass(BlockCss.SELECT_CSS);

			out.rect(x, baseLine - metrics.getBaseLine(), metrics.getWidth(), metrics.getHeight(),
				BlockDimensions.SELECT_RADIUS);
			out.text(x + BlockDimensions.SELECT_PADDING, baseLine, _selected.getLabel());

			out.beginPath();
			out.beginData();
			{
				out.moveToRel(x + metrics.getWidth() - BlockDimensions.SELECT_PADDING - 6,
					baseLine - metrics.getBaseLine() + metrics.getHeight() / 2 - BlockDimensions.SELECT_MARKER_HEIGHT);
				out.lineToHorizontalRel(2 * BlockDimensions.SELECT_MARKER_HEIGHT);
				out.lineToRel(-BlockDimensions.SELECT_MARKER_HEIGHT, BlockDimensions.SELECT_MARKER_HEIGHT);
				out.closePath();
			}
			out.endData();
			out.endPath();
		}
		out.endGroup();
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);

		json.name("selected");
		json.value(_selected.getLabel());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "selected":
				setSelected(getType().getOption(json.nextString()));
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

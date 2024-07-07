/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.model.block.BlockCss;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * {@link RowPart} displaying a label text.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelDisplay extends AbstractRowPart<LabelType> {

	private TextMetrics _metrics;

	/**
	 * Creates a {@link LabelDisplay}.
	 */
	public LabelDisplay(BlockRow owner, LabelType type) {
		super(owner, type);
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		super.updateDimensions(context, offsetX, offsetY);
		_metrics = context.measure(getType().getText());
	}

	@Override
	public TextMetrics getMetrics() {
		return _metrics;
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginGroup();
		out.writeId(getId());
		out.writeCssClass(BlockCss.LABEL_CSS);
		out.text(getOffsetX(), getOffsetY() + getTargetBaseLine(), getType().getText());
		out.endGroup();
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);
	}

	@Override
	public <R, A> R visit(BlockVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}

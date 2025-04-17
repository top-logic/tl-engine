/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import java.util.List;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.event.SVGClickEvent;
import com.top_logic.graphic.blocks.svg.event.SVGClickHandler;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.Widget;

/**
 * 
 */
public interface SelectableBoxOperations extends DecorationOperations, SVGClickHandler {

	@Override
	SelectableBox self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		DecorationOperations.super.computeIntrinsicSize(context, 0, 0);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().getContent().distributeSize(context, 0, 0, width, height);
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		out.beginGroup();
		out.translate(self().getX(), self().getY());
		out.writeCssClass(self().isSelected() ? "tlSelected" : "tlCanSelect");
		out.attachOnClick(this, this);

		DecorationOperations.super.draw(out);

		out.endGroup();
	}

	@Override
	default void onClick(SVGClickEvent event) {
		Diagram diagram = self().getDiagram();
		List<Widget> selection = diagram.getSelection();
		if (self().isSelected()) {
			selection.remove(self());
			self().setSelected(false);
		} else {
			selection.add(self());
			self().setSelected(true);
		}
	}

}

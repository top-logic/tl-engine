/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import java.util.Collections;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.event.MouseButton;
import com.top_logic.graphic.blocks.svg.event.Registration;
import com.top_logic.graphic.blocks.svg.event.SVGClickEvent;
import com.top_logic.graphic.blocks.svg.event.SVGClickHandler;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.SelectableBox;

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
		Registration clickHandler = self().getClickHandler();
		if (clickHandler != null) {
			clickHandler.cancel();
		}

		out.beginGroup(self());
		out.translate(self().getX(), self().getY());
		out.writeCssClass(self().isSelected() ? "tlSelected" : "tlCanSelect");
		self().setClickHandler(out.attachOnClick(this, self()));

		drawContent(out);

		out.endGroup();
	}

	@Override
	default void onClick(SVGClickEvent event) {
		if (!event.getButton(MouseButton.LEFT)) {
			return;
		}

		Diagram diagram = self().getDiagram();
		if (self().isSelected()) {
			if (event.isCtrlKey()) {
				// Toggle
				diagram.getSelection().remove(self());
				self().setSelected(false);
			} else if (event.isShiftKey()) {
				// Ignore.
			} else {
				// Make unique.
				for (SelectableBox selected : diagram.getSelection()) {
					if (selected == self()) {
						continue;
					}
					selected.setSelected(false);
				}
				diagram.setSelection(Collections.singletonList(self()));
			}
		} else {
			if (event.isShiftKey() || event.isCtrlKey()) {
				diagram.getSelection().add(self());
			} else {
				// Make unique.
				for (SelectableBox selected : diagram.getSelection()) {
					if (selected == self()) {
						continue;
					}
					selected.setSelected(false);
				}
				diagram.setSelection(Collections.singletonList(self()));
			}
			self().setSelected(true);
		}
		event.stopPropagation();
	}

}

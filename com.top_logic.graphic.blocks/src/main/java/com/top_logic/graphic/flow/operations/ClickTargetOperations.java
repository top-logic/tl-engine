/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.event.Registration;
import com.top_logic.graphic.blocks.svg.event.SVGClickEvent;
import com.top_logic.graphic.blocks.svg.event.SVGClickHandler;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.MouseButton;

/**
 * Custom operations for {@link ClickTarget} diagram nodes.
 */
public interface ClickTargetOperations extends DecorationOperations, SVGClickHandler {

	@Override
	ClickTarget self();

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		// No visible content.
		self().getContent().distributeSize(context, offsetX, offsetY, width, height);
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		Registration clickHandler = self().getHandlerRegistration();
		if (clickHandler != null) {
			clickHandler.cancel();
		}

		out.beginGroup(self());
		out.writeCssClass(self().getCssClass());
		self().setHandlerRegistration(out.attachOnClick(this, self()));
		{
			DecorationOperations.super.draw(out);
		}
		out.endGroup();
	}

	@Override
	default void onClick(SVGClickEvent event) {
		List<MouseButton> pressedButtons = new ArrayList<>();
		List<MouseButton> expectedButtons = self().getButtons();
		if (expectedButtons.isEmpty()) {
			// None means all.
			expectedButtons = Arrays.asList(MouseButton.values());
		}
		for (MouseButton button : expectedButtons) {
			com.top_logic.graphic.blocks.svg.event.MouseButton svgButton;
			switch (button) {
				case LEFT:
					svgButton = com.top_logic.graphic.blocks.svg.event.MouseButton.LEFT;
					break;
				case MIDDLE:
					svgButton = com.top_logic.graphic.blocks.svg.event.MouseButton.MIDDLE;
					break;
				case RIGHT:
					svgButton = com.top_logic.graphic.blocks.svg.event.MouseButton.RIGHT;
					break;
				default:
					throw new IllegalArgumentException("No such button: " + button);
			}

			if (event.getButton(svgButton)) {
				pressedButtons.add(button);
			}
		}
		if (pressedButtons.isEmpty()) {
			// No relevant buttons pressed.
			return;
		}
		
		self().getDiagram().getContext().processClick(self(), pressedButtons);
	}

}

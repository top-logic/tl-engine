/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import java.util.List;
import java.util.OptionalInt;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.graphic.flow.model.Align;
import com.top_logic.graphic.flow.model.Border;
import com.top_logic.graphic.flow.model.Decoration;
import com.top_logic.graphic.flow.model.DrawElement;
import com.top_logic.graphic.flow.model.EmptyBlock;
import com.top_logic.graphic.flow.model.Fill;
import com.top_logic.graphic.flow.model.FlowDiagram;
import com.top_logic.graphic.flow.model.Padding;
import com.top_logic.graphic.flow.model.TextLine;
import com.top_logic.graphic.flow.model.layout.CompassLayout;
import com.top_logic.graphic.flow.model.layout.GridLayout;
import com.top_logic.graphic.flow.model.layout.HorizontalLayout;
import com.top_logic.graphic.flow.model.layout.VerticalLayout;
import com.top_logic.graphic.flow.param.HAlign;
import com.top_logic.graphic.flow.param.SpaceDistribution;
import com.top_logic.graphic.flow.param.VAlign;

/**
 * Factory for flow chart diagram elements.
 */
public class FlowFactory {
	
	/**
	 * Factory for {@link TextLine}.
	 */
	public static FlowDiagram flowChart(DrawElement root) {
		return new FlowDiagram().setRoot(root == null ? new EmptyBlock() : root);
	}

	/**
	 * Factory for {@link TextLine}.
	 */
	public static DrawElement flowText(
			@Mandatory String text) {
		if (text == null) {
			return new EmptyBlock();
		}
		return new TextLine(text);
	}

	/**
	 * Factory for {@link Align}.
	 */
	public static Decoration flowAlign(
			@Mandatory DrawElement content,
			HAlign hAlign,
			VAlign vAlign) {
		return new Align().setHAlign(hAlign).setVAlign(vAlign).setContent(content);
	}

	/**
	 * Factory for {@link Border}.
	 */
	public static Decoration flowBorder(
			@Mandatory DrawElement content,
			@BooleanDefault(true) boolean top,
			@BooleanDefault(true) boolean left,
			@BooleanDefault(true) boolean right,
			@BooleanDefault(true) boolean bottom, 
			@DoubleDefault(1.0) double thickness,
			@StringDefault("black") @ScriptConversion(ToStyle.class) String stroke,
			double[] dashes) {
		return new Border().setThickness(thickness).setStrokeStyle(stroke).setTop(top).setLeft(left).setRight(right)
			.setBottom(bottom).setDashes(dashes).setContent(content);
	}

	/**
	 * Factory for {@link Fill}.
	 */
	public static Decoration flowFill(
			@Mandatory DrawElement content,
			String fill) {
		return new Fill().setFillStyle(fill).setContent(content);
	}

	/**
	 * Factory for {@link Padding}.
	 */
	public static DrawElement flowPadding(
			@Mandatory DrawElement content,
			Double all,
			Double horizontal,
			Double vertical,
			Double top,
			Double left,
			Double right,
			Double bottom) {
		Padding result = new Padding();
		if (all != null) {
			result.setTop(all);
			result.setLeft(all);
			result.setRight(all);
			result.setBottom(all);
		}
		if (horizontal != null) {
			result.setLeft(horizontal);
			result.setRight(horizontal);
		}
		if (vertical != null) {
			result.setTop(vertical);
			result.setBottom(vertical);
		}
		if (top != null) {
			result.setTop(top);
		}
		if (left != null) {
			result.setLeft(left);
		}
		if (right != null) {
			result.setRight(right);
		}
		if (bottom != null) {
			result.setBottom(bottom);
		}
		result.setContent(content);
		return result;
	}

	/**
	 * Factory for {@link HorizontalLayout}.
	 */
	public static DrawElement flowHorizontal(
			@Mandatory List<DrawElement> contents,
			double gap,
			SpaceDistribution distribution) {
		if (contents.isEmpty()) {
			return new EmptyBlock();
		}

		for (int i = 0; i < contents.size(); i++) {
			DrawElement e = contents.get(i);
			if (e == null) {
				contents.set(i, new EmptyBlock());
			}
		}

		return new HorizontalLayout().setGap(gap).setFill(distribution).setCols(contents);
	}

	/**
	 * Factory for {@link VerticalLayout}.
	 */
	public static DrawElement flowVertical(
			@Mandatory List<DrawElement> contents,
			double gap,
			SpaceDistribution distribution) {

		if (contents.isEmpty()) {
			return new EmptyBlock();
		}

		for (int i = 0; i < contents.size(); i++) {
			DrawElement e = contents.get(i);
			if (e == null) {
				contents.set(i, new EmptyBlock());
			}
		}

		return new VerticalLayout().setGap(gap).setFill(distribution).setRows(contents);
	}

	/**
	 * Factory for {@link CompassLayout}.
	 */
	public static CompassLayout flowCompass(
			@Mandatory DrawElement center,
			DrawElement north,
			DrawElement west,
			DrawElement east,
			DrawElement south) {
		return new CompassLayout().setCenter(center).setNorth(north).setWest(west).setEast(east).setSouth(south);
	}

	/**
	 * Factory for {@link GridLayout}.
	 */
	public static DrawElement flowGrid(
			@Mandatory List<List<DrawElement>> contents,
			double gapX,
			double gapY) {
		int rows = contents.size();
		OptionalInt colsSearch = contents.stream().mapToInt(row -> row.size()).max();

		if (rows == 0 || colsSearch.isEmpty()) {
			return new EmptyBlock();
		}

		int cols = colsSearch.getAsInt();
		GridLayout result = new GridLayout(cols, rows).setGapX(gapX).setGapY(gapY);
		
		int y = 0;
		for (List<DrawElement> row : contents) {
			if (row != null) {
				int x = 0;
				for (DrawElement element : row) {
					if (element != null) {
						result.set(x, y, element);
					}
					x++;
				}
			}
			y++;
		}
		return result;
	}

}

/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.graphic.flow.data.Align;
import com.top_logic.graphic.flow.data.Alignment;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.CompassLayout;
import com.top_logic.graphic.flow.data.Decoration;
import com.top_logic.graphic.flow.data.Empty;
import com.top_logic.graphic.flow.data.Fill;
import com.top_logic.graphic.flow.data.GridLayout;
import com.top_logic.graphic.flow.data.HorizontalLayout;
import com.top_logic.graphic.flow.data.Image;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.SpaceDistribution;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.VerticalLayout;
import com.top_logic.graphic.flow.model.FlowDiagram;
import com.top_logic.graphic.flow.model.TextLine;
import com.top_logic.model.search.expr.ToString;

/**
 * Factory for flow chart diagram elements.
 */
public class FlowFactory {
	
	/**
	 * Factory for {@link TextLine}.
	 */
	public static FlowDiagram flowChart(Box root) {
		return new FlowDiagram().setRoot(root == null ? Empty.create() : root);
	}

	/**
	 * Factory for {@link TextLine}.
	 */
	public static Box flowText(
			@Mandatory String text) {
		if (text == null) {
			return Empty.create();
		}
		return Text.create().setValue(text);
	}

	/**
	 * Factory for {@link Align}.
	 */
	public static Decoration flowAlign(
			@Mandatory Box content,
			Alignment hAlign,
			Alignment vAlign) {
		return Align.create().setXAlign(hAlign).setYAlign(vAlign).setContent(content);
	}

	/**
	 * Factory for {@link Border}.
	 */
	public static Decoration flowBorder(
			@Mandatory Box content,
			@BooleanDefault(true) boolean top,
			@BooleanDefault(true) boolean left,
			@BooleanDefault(true) boolean right,
			@BooleanDefault(true) boolean bottom, 
			@DoubleDefault(1.0) double thickness,
			@StringDefault("black") @ScriptConversion(ToStyle.class) String stroke,
			List<Double> dashes) {
		return com.top_logic.graphic.flow.data.Border.create().setThickness(thickness).setStrokeStyle(stroke)
			.setTop(top).setLeft(left).setRight(right)
			.setBottom(bottom).setDashes(dashes).setContent(content);
	}

	/**
	 * Factory for {@link Fill}.
	 */
	public static Decoration flowFill(
			@Mandatory Box content,
			@StringDefault("gray") @ScriptConversion(ToStyle.class) String fill) {
		return Fill.create().setFillStyle(fill).setContent(content);
	}

	/**
	 * Factory for {@link Padding}.
	 */
	public static Box flowPadding(
			@Mandatory Box content,
			Double all,
			Double horizontal,
			Double vertical,
			Double top,
			Double left,
			Double right,
			Double bottom) {
		Padding result = Padding.create();
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
	public static Box flowHorizontal(
			@Mandatory List<Box> contents,
			double gap,
			SpaceDistribution distribution) {
		if (contents.isEmpty()) {
			return Empty.create();
		}

		for (int i = 0; i < contents.size(); i++) {
			Box e = contents.get(i);
			if (e == null) {
				contents.set(i, Empty.create());
			}
		}

		return HorizontalLayout.create().setGap(gap).setFill(distribution).setContents(contents);
	}

	/**
	 * Factory for {@link VerticalLayout}.
	 */
	public static Box flowVertical(
			@Mandatory List<Box> contents,
			double gap,
			SpaceDistribution distribution) {

		if (contents.isEmpty()) {
			return Empty.create();
		}

		for (int i = 0; i < contents.size(); i++) {
			Box e = contents.get(i);
			if (e == null) {
				contents.set(i, Empty.create());
			}
		}

		return VerticalLayout.create().setGap(gap).setFill(distribution).setContents(contents);
	}

	/**
	 * Factory for {@link CompassLayout}.
	 */
	public static CompassLayout flowCompass(
			@Mandatory Box center,
			Box north,
			Box west,
			Box east,
			Box south) {
		return CompassLayout.create().setCenter(center).setNorth(north).setWest(west).setEast(east).setSouth(south);
	}

	/**
	 * Factory for {@link GridLayout}.
	 */
	public static Box flowGrid(
			@Mandatory List<List<Box>> contents,
			double gapX,
			double gapY) {
		int rows = contents.size();
		OptionalInt colsSearch = contents.stream().mapToInt(row -> row.size()).max();

		if (rows == 0 || colsSearch.isEmpty()) {
			return Empty.create();
		}

		int cols = colsSearch.getAsInt();
		GridLayout result = GridLayout.create().setCols(cols).setRows(rows).setGapX(gapX).setGapY(gapY);
		
		int y = 0;
		for (List<Box> row : contents) {
			if (row != null) {
				int x = 0;
				for (Box element : row) {
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

	/**
	 * Factory for {@link Image}.
	 */
	public static Box flowImage(
			@Mandatory Object data,
			Double width,
			Double height,
			ImageAlign align,
			ImageScale scale) {
		if (data == null) {
			return Empty.create();
		}
		String href;
		try {
			href = data instanceof BinaryDataSource c
				? "data:" + c.getContentType() + ";base64,"
					+ Base64.getEncoder().encodeToString(StreamUtilities.readStreamContents(c.toData()))
				: ToString.toString(data);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
		double imgWidth;
		double imgHeight;
		findDim:
		if (width == null || height == null) {
			if (data instanceof BinaryDataSource c) {
				try (InputStream in = c.toData().getStream()) {
					ImageInputStream iis = ImageIO.createImageInputStream(in);
					Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
					if (readers.hasNext()) {
						ImageReader reader = readers.next();
						reader.setInput(iis, true);

						imgWidth = Double.valueOf(reader.getWidth(0));
						imgHeight = Double.valueOf(reader.getHeight(0));
						break findDim;
					}
				} catch (IOException ex) {
					Logger.debug("Failed to access image.", ex, FlowFactory.class);
				}
			}
			imgWidth = 100;
			imgHeight = 100;
		} else {
			imgWidth = width.doubleValue();
			imgHeight = height.doubleValue();
		}
		
		Image result = Image.create().setImgWidth(imgWidth).setImgHeight(imgHeight).setHref(href);
		if (align != null) {
			result.setAlign(align);
		}
		if (scale != null) {
			result.setScale(scale);
		}
		return result;
	}

//	public static DrawElement flowTree(
//			EvalContext context,
//			Object root,
//			SearchExpression getChildren,
//			SearchExpression createDisplay) {
//		FlowNode rootDisplay = (FlowNode) createDisplay.eval(context, root);
//		List<List<DrawElement>> levels = new ArrayList<>();
//		List<FlowNode> currentLevel = Collections.singletonList(rootDisplay);
//		while (!currentLevel.isEmpty()) {
//			if (!levels.isEmpty()) {
//
//			}
//			levels.add(currentLevel.stream().map(FlowNode::getDisplay).toList());
//			List<FlowNode> nextLevel = new ArrayList<>();
//
//			for (FlowNode current : currentLevel) {
//				List<?> children = (List<?>) getChildren.eval(context, current.getBusinessObject());
//				
//				List<FlowNode> childNodes =
//					children.stream().map(c -> (FlowNode) createDisplay.eval(context, c)).toList();
//				
//				Connector source = current.getBox().getRight();
//				List<Connector> target = childNodes.stream().map(n -> n.getBox().getLeft()).toList();
//				
//				new TreeConnection();
//			}
//
//			currentLevel = nextLevel;
//		}
//	}
}

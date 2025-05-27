/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.DiagramDirection;
import com.top_logic.graphic.flow.data.Empty;
import com.top_logic.graphic.flow.data.Fill;
import com.top_logic.graphic.flow.data.GridLayout;
import com.top_logic.graphic.flow.data.HorizontalLayout;
import com.top_logic.graphic.flow.data.Image;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.SpaceDistribution;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.data.VerticalLayout;
import com.top_logic.model.search.expr.ToString;

/**
 * Factory for flow chart diagram elements.
 */
public class FlowFactory {
	
	/**
	 * Factory for Diagrams.
	 */
	public static Diagram flowChart(
		Box root,
		String cssClass,
		Object userObject
	) {
		return Diagram.create()
			.setRoot(root == null ? Empty.create() : root)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Creates a text element.
	 */
	public static Box flowText(
		@Mandatory String text,
		String strokeStyle, 
		String fillStyle, 
		String fontFamily, 
		String fontSize, 
		String fontWeight,
		String cssClass,
		Object userObject 
	) {
		if (text == null) {
			return Empty.create().setUserObject(userObject);
		}
		return Text.create()
			.setValue(text)
			.setStrokeStyle(strokeStyle)
			.setFillStyle(fillStyle)
			.setFontFamily(fontFamily)
			.setFontSize(fontSize)
			.setFontWeight(fontWeight)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link Align}.
	 */
	public static Decoration flowAlign(
		@Mandatory Box content,
		Alignment hAlign,
		Alignment vAlign,
		String cssClass,
		Object userObject
	) {
		return Align.create()
			.setXAlign(hAlign)
			.setYAlign(vAlign)
			.setContent(nonNull(content))
			.setCssClass(cssClass)
			.setUserObject(userObject);
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
		List<Double> dashes,
		String cssClass,
		Object userObject
	) {
		if (dashes == null) {
			dashes = Collections.emptyList();
		}
		return com.top_logic.graphic.flow.data.Border.create().setThickness(thickness).setStrokeStyle(stroke)
			.setTop(top)
			.setLeft(left)
			.setRight(right)
			.setBottom(bottom)
			.setDashes(dashes)
			.setContent(nonNull(content))
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link Fill}.
	 */
	public static Decoration flowFill(
		@Mandatory Box content,
		@StringDefault("gray") @ScriptConversion(ToStyle.class) String fill,
		String cssClass,
		Object userObject
	) {
		return Fill.create()
			.setFillStyle(fill)
			.setContent(nonNull(content))
			.setCssClass(cssClass)
			.setUserObject(userObject);
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
		Double bottom,
		String cssClass,
		Object userObject
	) {
		Padding result = Padding.create();
		if (all != null) {
			result.setAll(all);
		}
		if (horizontal != null) {
			result.setHorizontal(horizontal);
		}
		if (vertical != null) {
			result.setVertical(vertical);
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
		result.setContent(nonNull(content));
		result.setCssClass(cssClass);
		result.setUserObject(userObject);
		return result;
	}

	/**
	 * Factory for {@link HorizontalLayout}.
	 */
	public static Box flowHorizontal(
		@Mandatory List<Box> contents,
		double gap,
		SpaceDistribution distribution,
		String cssClass,
		Object userObject
	) {
		if (contents.isEmpty()) {
			return Empty.create().setUserObject(userObject);
		}

		for (int i = 0; i < contents.size(); i++) {
			Box e = contents.get(i);
			if (e == null) {
				contents.set(i, Empty.create());
			}
		}

		return HorizontalLayout.create()
			.setGap(gap)
			.setFill(distribution)
			.setContents(contents)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link VerticalLayout}.
	 */
	public static Box flowVertical(
		@Mandatory List<Box> contents,
		double gap,
		SpaceDistribution distribution,
		String cssClass,
		Object userObject
	) {
		if (contents.isEmpty()) {
			return Empty.create().setUserObject(userObject);
		}

		for (int i = 0; i < contents.size(); i++) {
			Box e = contents.get(i);
			if (e == null) {
				contents.set(i, Empty.create());
			}
		}

		return VerticalLayout.create().setGap(gap).setFill(distribution).setContents(contents)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link CompassLayout}.
	 */
	public static CompassLayout flowCompass(
		@Mandatory Box center,
		Box north,
		Box west,
		Box east,
		Box south,
		String cssClass,
		Object userObject
	) {
		return CompassLayout.create()
			.setCenter(center)
			.setNorth(north)
			.setWest(west)
			.setEast(east)
			.setSouth(south)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link GridLayout}.
	 */
	public static Box flowGrid(
		@Mandatory List<List<Box>> contents,
		double gapX,
		double gapY,
		String cssClass,
		Object userObject
	) {
		int rows = contents.size();
		OptionalInt colsSearch = contents.stream().mapToInt(row -> row.size()).max();

		if (rows == 0 || colsSearch.isEmpty()) {
			return Empty.create().setUserObject(userObject);
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
		return result
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link Empty} boxes that produce gaps.
	 */
	public static Box flowEmpty(
		Double width,
		Double height,
		String cssClass,
		Object userObject
	) {
		return Empty.create()
			.setMinWidth(width)
			.setMinHeight(height)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link Image}.
	 */
	public static Box flowImage(
		@Mandatory Object data,
		Double width,
		Double height,
		ImageAlign align,
		ImageScale scale,
		String cssClass,
		Object userObject
	) {
		if (data == null) {
			return Empty.create().setUserObject(userObject);
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
		return result
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Creates an are that is user-selectable.
	 */
	public static Box flowSelection(
		Box content,
		String cssClass,
		Object userObject
	) {
		return SelectableBox.create()
			.setContent(nonNull(content))
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}
	
	/**
	 * Creates a tree layout.
	 *
	 * @param nodes
	 *        The nodes to layout.
	 * @param connections
	 *        Connections between nodes.
	 * @param gapX
	 *        Gap between columns of tree nodes.
	 * @param gapY
	 *        Vertical gap between children.
	 * @param stroke
	 *        The stroke style for connections.
	 * @param strokeWidth
	 *        The stroke width used for connections.
	 * @param direction
	 *        The layout direction.
	 * @param compact
	 *        Whether a compact layout is preferred.
	 * @param userObject
	 *        An arbitrary object to associate with the graphics element.
	 */
	public static Box flowTree(
		@Mandatory List<? extends Box> nodes, 
		@Mandatory List<? extends TreeConnection> connections,
		@DoubleDefault(30) double gapX, 
		@DoubleDefault(30) double gapY, 
		DiagramDirection direction,
		@StringDefault("black") String stroke, 
		@DoubleDefault(1) double strokeWidth, 
		boolean compact,
		boolean alignTop,
		String cssClass,
		Object userObject 
	) {
		return TreeLayout.create()
			.setNodes(nodes.stream().filter(Objects::nonNull).toList())
			.setConnections(connections.stream().filter(Objects::nonNull).toList())
			.setGapX(gapX)
			.setGapY(gapY)
			.setDirection(direction)
			.setStrokeStyle(stroke)
			.setThickness(strokeWidth)
			.setCompact(compact)
			.setAlignTop(alignTop)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Creates a connection for a tree layout.
	 * 
	 * @param parent
	 *        The parent node or connector that should be connected to children.
	 * @param children
	 *        The children nodes or connectors to connect to the given parent.
	 */
	public static TreeConnection flowConnection(
			@Mandatory Object parent,
			@Mandatory List<?> children
	) {
		if (parent == null) {
			return null;
		}
		return TreeConnection.create()
			.setParent(asConnector(parent))
			.setChildren(children.stream().filter(Objects::nonNull).map(FlowFactory::asConnector).toList());
	}

	private static TreeConnector asConnector(Object node) {
		return node instanceof TreeConnector connector ? connector : TreeConnector.create().setAnchor((Box) node);
	}

	/**
	 * A connector to attach to a node to connect.
	 */
	public static TreeConnector flowConnector(
		@Mandatory Box anchor,
		@DoubleDefault(0.5) double pos,
		String cssClass,
		Object userObject
	) {
		if (anchor == null) {
			return null;
		}
		return TreeConnector.create()
			.setAnchor(anchor)
			.setConnectPosition(pos)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	private static Box nonNull(Box content) {
		return content == null ? Empty.create() : content;
	}

}

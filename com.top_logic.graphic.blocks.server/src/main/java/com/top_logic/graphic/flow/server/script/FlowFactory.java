/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.graphic.flow.callback.ClickHandler;
import com.top_logic.graphic.flow.data.Align;
import com.top_logic.graphic.flow.data.Alignment;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.ClipBox;
import com.top_logic.graphic.flow.data.CompassLayout;
import com.top_logic.graphic.flow.data.ConnectorSymbol;
import com.top_logic.graphic.flow.data.Decoration;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.DiagramDirection;
import com.top_logic.graphic.flow.data.DropRegion;
import com.top_logic.graphic.flow.data.EdgeDecoration;
import com.top_logic.graphic.flow.data.Empty;
import com.top_logic.graphic.flow.data.Fill;
import com.top_logic.graphic.flow.data.FloatingLayout;
import com.top_logic.graphic.flow.data.GridLayout;
import com.top_logic.graphic.flow.data.HorizontalLayout;
import com.top_logic.graphic.flow.data.Image;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageOrientation;
import com.top_logic.graphic.flow.data.ImageScale;
import com.top_logic.graphic.flow.data.MouseButton;
import com.top_logic.graphic.flow.data.OffsetPosition;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.Point;
import com.top_logic.graphic.flow.data.PolygonalChain;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.Sized;
import com.top_logic.graphic.flow.data.SpaceDistribution;
import com.top_logic.graphic.flow.data.Stack;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.Tooltip;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.data.VerticalLayout;
import com.top_logic.graphic.flow.server.ui.handler.ServerDropHandler;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.operations.ScriptConversion;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * Factory for flow chart diagram elements.
 */
@ScriptPrefix("flow")
public class FlowFactory extends TLScriptFunctions {
	
	/**
	 * Factory for {@link Diagram}s.
	 * 
	 * @param root
	 *        The root box to render.
	 * @param cssClass
	 *        The css class for the diagram.
	 * @param userObject
	 *        User object of the new diagram.
	 * @return The newly created diagram.
	 */
	@SideEffectFree
	@Label("Create chart")
	public static Diagram chart(
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
	 * 
	 * @param text
	 *        The actual text to render.
	 * @param strokeStyle
	 *        Stroke style for the text box.
	 * @param fillStyle
	 *        Style that is is used to fill the text box.
	 * @param fontFamily
	 *        Font family for the text.
	 * @param fontSize
	 *        Size of the text.
	 * @param fontWeight
	 *        Weight for the text
	 * @param cssClass
	 *        The css class for the text box.
	 * @param userObject
	 *        User object of the new text box.
	 * @return The newly created text box.
	 */
	@SideEffectFree
	@Label("Create text")
	public static Box text(
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

		String[] lines = text.split("\\r?\\n");
		if (lines.length > 1) {
			// This could be done better during layout, when the render context and text metrics are
			// known. Additionally, a line height setting could be given.
			return VerticalLayout.create().setContents(
				Arrays.stream(lines).map(line -> line.isBlank()
					? Sized.create().setMinHeight(12.0).setContent(Empty.create())
					: Text.create()
					.setValue(line)
					.setStrokeStyle(strokeStyle)
					.setFillStyle(fillStyle)
					.setFontFamily(fontFamily)
					.setFontSize(fontSize)
					.setFontWeight(fontWeight)
					.setCssClass(cssClass)).toList())
				.setGap(6)
				.setUserObject(userObject);
		} else {
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
	}

	/**
	 * Factory for {@link Align}.
	 * 
	 * @param content
	 *        The box to align.
	 * @param hAlign
	 *        Horizontal alignment for the content.
	 * @param vAlign
	 *        Vertical alignment for the content.
	 * @param cssClass
	 *        The css class for the aligned box.
	 * @param userObject
	 *        User object of the new aligned box.
	 * @return The new aligned box.
	 */
	@SideEffectFree
	@Label("Align")
	public static Decoration align(
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
	 * Factory for {@link Stack}.
	 * 
	 * <p>
	 * A {@link Stack} writes several boxes on top of each other.
	 * </p>
	 * 
	 * @param contents
	 *        The boxes to stack
	 * @param cssClass
	 *        The css class for the stacking box.
	 * @param userObject
	 *        User object of the new stacking box.
	 * @return The new stacking box.
	 */
	@SideEffectFree
	@Label("Stack elements")
	public static Stack stack(
			List<Box> contents,
			String cssClass,
			Object userObject) {
		return Stack.create()
			.setContents(contents)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Creates a {@link Border} around the given content.
	 * 
	 * @param content
	 *        Content to create box for.
	 * @param top
	 *        Whether a border must be drawn at the top of the box.
	 * @param left
	 *        Whether a border must be drawn at the left side of the box.
	 * @param right
	 *        Whether a border must be drawn at the right side of the box.
	 * @param bottom
	 *        Whether a border must be drawn at the bottom of the box.
	 * @param thickness
	 *        Thickness of the border.
	 * @param stroke
	 *        Color of the border.
	 * @param dashes
	 *        Dashes of the border.
	 * @param cssClass
	 *        The css class for the border box.
	 * @param userObject
	 *        User object of the new border box.
	 * @return The new border box.
	 */
	@SideEffectFree
	@Label("Create borders")
	public static Decoration border(
		Box content,
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
	 * Fills the given content with a given color.
	 * 
	 * @param content
	 *        The content to fill with color.
	 * @param fill
	 *        The fill color of the box.
	 * @param cssClass
	 *        The css class for the fill box.
	 * @param userObject
	 *        User object of the new fill box.
	 * @return The new fill box.
	 */
	@SideEffectFree
	@Label("Fill")
	public static Decoration fill(
			Box content,
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
	 * Factory for a box with an explicit position.
	 * 
	 * <p>
	 * This can be used in combination with {@link #floating(List, String, Object)}.
	 * </p>
	 * 
	 * @param content
	 *        The box to position.
	 * @param x
	 *        Desired X position of the box.
	 * @param y
	 *        Desired Y position of the box
	 * @param minWidth
	 *        Minimum width of the new {@link Box}.
	 * @param maxWidth
	 *        Maximium width of the new {@link Box}.
	 * @param width
	 *        Concrete desired width of the new {@link Box}
	 * @param minHeight
	 *        Minimum height of the new {@link Box}.
	 * @param maxHeight
	 *        Maximium height of the new {@link Box}.
	 * @param height
	 *        Concrete desired height of the new {@link Box}
	 * @param preserveAspectRatio
	 *        Whether the ration between width and height must be preserved.
	 * @param userObject
	 *        User object of the new fill box.
	 * @return The new positioned box.
	 */
	@SideEffectFree
	@Label("Explicit position")
	public static Box position(
			@Mandatory Box content,
			Double x,
			Double y,
			Double minWidth,
			Double maxWidth,
			Double width,
			Double minHeight,
			Double maxHeight,
			Double height,
			boolean preserveAspectRatio,
			Object userObject) {
		Sized sized = Sized.create().setPreserveAspectRatio(preserveAspectRatio);
		if (width != null) {
			// set width as min and max, eventually overridden by explicit values
			sized.setMinWidth(width).setMaxWidth(width);
		}
		if (minWidth != null) {
			sized.setMinWidth(minWidth);
		}
		if (maxWidth != null) {
			sized.setMaxWidth(maxWidth);
		}

		if (height != null) {
			// set height as min and max, eventually overridden by explicit values
			sized.setMinHeight(height).setMaxHeight(height);
		}
		if (minHeight != null) {
			sized.setMinHeight(minHeight);
		}
		if (maxHeight != null) {
			sized.setMaxHeight(maxHeight);
		}

		if (x != null) {
			sized.setDesiredX(x.doubleValue());
		}
		if (y != null) {
			sized.setDesiredY(y.doubleValue());
		}

		return sized
			.setContent(content)
				.setUserObject(userObject);
	}

	/**
	 * Factory for {@link Padding}.
	 * 
	 * @param content
	 *        The content to surround with padding.
	 * @param all
	 *        Padding for all sides.
	 * @param horizontal
	 *        Padding for both, left and right.
	 * @param vertical
	 *        Padding for both, top and bottom.
	 * @param top
	 *        Padding for the top side.
	 * @param left
	 *        Padding for the left side.
	 * @param right
	 *        Padding for the right side.
	 * @param bottom
	 *        Padding for the bottom side.
	 * @param cssClass
	 *        The css class for the padding box.
	 * @param userObject
	 *        User object of the new padding box.
	 * @return The new padding box.
	 */
	@SideEffectFree
	@Label("Create padding")
	public static Box padding(
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
	 * 
	 * <p>
	 * Positions the contents side by side.
	 * </p>
	 * 
	 * @param contents
	 *        The elements to position.
	 * @param gap
	 *        Gap between the single boxes.
	 * @param distribution
	 *        How to handle extra space.
	 * @param cssClass
	 *        The css class for the horizontal box.
	 * @param userObject
	 *        User object of the new horizontal box.
	 * @return The new horizontal box.
	 */
	@SideEffectFree
	@Label("Align horizontal")
	public static Box horizontal(
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
	 * 
	 * <p>
	 * Positions the contents on top of each other.
	 * </p>
	 * 
	 * @param contents
	 *        The elements to position.
	 * @param gap
	 *        Gap between the single boxes.
	 * @param distribution
	 *        How to handle extra space.
	 * @param cssClass
	 *        The css class for the horizontal box.
	 * @param userObject
	 *        User object of the new horizontal box.
	 * @return The new horizontal box.
	 */
	@SideEffectFree
	@Label("Align vertical")
	public static Box vertical(
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
	 * 
	 * <p>
	 * Positions four boxes around of a fifth box.
	 * </p>
	 * 
	 * @param center
	 *        Box to position others around.
	 * @param north
	 *        The box to position above the center.
	 * @param west
	 *        The box to position to the left of the center.
	 * @param east
	 *        The box to position to the right of the center.
	 * @param south
	 *        The box to position under the center.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create compass")
	public static CompassLayout compass(
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
	 * 
	 * @param contents
	 *        Each entry in the list represents a row in the grid; the list entry (itself a list)
	 *        contains the boxes top display in that row.
	 * @param gapX
	 *        The gap between the columns.
	 * @param gapY
	 *        The gap between the rows.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create Grid")
	public static Box grid(
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
	 * A layout in which boxes can be positioned explicitly.
	 * 
	 * <p>
	 * Use in combination with
	 * {@link #position(Box, Double, Double, Double, Double, Double, Double, Double, Double, boolean, Object)}.
	 * </p>
	 * 
	 * @param contents
	 *        Contents of the new box.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create positioning box")
	public static Box floating(
			@Mandatory List<? extends Box> contents,
			String cssClass,
			Object userObject) {
		return FloatingLayout.create().setNodes(contents).setCssClass(cssClass).setUserObject(userObject);
	}

	/**
	 * Factory for {@link Empty} boxes that produce gaps.
	 * 
	 * @param minWidth
	 *        Minimum width for the empty space.
	 * @param minHeight
	 *        Minimum height for the empty space.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Empty box")
	public static Box empty(
		double minWidth,
		double minHeight,
		String cssClass,
		Object userObject
	) {
		return Empty.create()
			.setMinWidth(minWidth)
			.setMinHeight(minHeight)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Factory for {@link Image}.
	 * 
	 * <p>
	 * Displays an image.
	 * </p>
	 * 
	 * @param data
	 *        Source data for the image.
	 * @param width
	 *        Width of the image.
	 * @param height
	 *        Height of the image.
	 * @param align
	 *        Where the image is aligned.
	 * @param scale
	 *        Scaling instruction for the image.
	 * @param orientation
	 *        Orientation of the image.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create image")
	public static Box image(
		@Mandatory Object data,
		Double width,
		Double height,
		ImageAlign align,
		ImageScale scale,
			ImageOrientation orientation,
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
		
		Image result = Image.create()
			.setImgWidth(imgWidth)
			.setImgHeight(imgHeight)
			.setHref(href);
		if (orientation != null) {
			result.setOrientation(orientation);
		}
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
	 * Creates a box that is user-selectable.
	 * 
	 * @param content
	 *        Actual content of the box.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Make selectable")
	public static Box selection(
			@Mandatory Box content,
		String cssClass,
		Object userObject
	) {
		return SelectableBox.create()
			.setContent(nonNull(content))
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}
	
	/**
	 * Creates box that responds to mouse click events.
	 * 
	 * @param content
	 *        The content box.
	 * @param clickHandler
	 *        The handler to execute when the mouse click event occurs.
	 * @param buttons
	 *        The buttons to react on.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("React on click")
	public static Box clickTarget(
			@Mandatory Box content,
			@Mandatory ClickHandler clickHandler,
			List<MouseButton> buttons,
			String cssClass,
			Object userObject
			) {
		return ClickTarget.create()
			.setContent(nonNull(content))
			.setClickHandler(clickHandler)
			.setButtons(buttons)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}
	
	/**
	 * Creates box that receives drop events.
	 * 
	 * @param content
	 *        The content box.
	 * @param dropHandler
	 *        The handler to execute when an element is dropped on thw box.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create drop region")
	public static Box dropRegion(
			@Mandatory Box content,
			@Mandatory ServerDropHandler dropHandler,
			String cssClass,
			Object userObject) {
		return DropRegion.create()
			.setContent(nonNull(content))
			.setDropHandler(dropHandler)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Creates box that displays a tooltip when the mouse hovers over its contents.
	 * 
	 * @param text
	 *        The tooltip to display
	 * @param content
	 *        The content box.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Add tooltip")
	public static Box tooltip(
			@Mandatory String text,
			@Mandatory Box content,
			String cssClass,
			Object userObject) {
		return Tooltip.create()
			.setText(text)
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
	 *        Vertical gap between children (for both siblings and non-siblings).
	 * @param sibblingGapY
	 *        Vertical gap between children if they belong to the same parent.
	 * @param subtreeGapY
	 *        Vertical gap between children if they belong to different subtrees.
	 * @param stroke
	 *        The stroke style for connections.
	 * @param strokeWidth
	 *        The stroke width used for connections.
	 * @param direction
	 *        The layout direction.
	 * @param compact
	 *        Whether a compact layout is preferred.
	 * @param parentAlign
	 *        Factor to determine the placement of the parent node relative to it's children. With a
	 *        value of zero the center of the parent node is placed at the same Y coordinate as the
	 *        first of it's children. With a value of 1.0, the parent is aligned to its last child.
	 *        A value in between, places the parent corresponding to the ratio between the first and
	 *        the last child.
	 * @param parentOffset
	 *        Offset to add to the parent Y coordinate after the alignment operation based on parent
	 *        ratio.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        An arbitrary object to associate with the graphics element.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create tree")
	public static Box tree(
		@Mandatory List<? extends Box> nodes, 
		@Mandatory List<? extends TreeConnection> connections,
		@DoubleDefault(40) double gapX, 
		Double gapY,
		Double sibblingGapY,
		Double subtreeGapY,
		DiagramDirection direction,
		@StringDefault("black") String stroke, 
		@DoubleDefault(1) double strokeWidth, 
		boolean compact,
		@DoubleDefault(0.5)
			double parentAlign,
		@DoubleDefault(0)
		double parentOffset,
		String cssClass,
		Object userObject 
	) {
		TreeLayout result = TreeLayout.create()
			.setNodes(nodes.stream().filter(Objects::nonNull).toList())
			.setConnections(connections.stream().filter(Objects::nonNull).toList())
			.setGapX(gapX)
			.setDirection(direction)
			.setStrokeStyle(stroke)
			.setThickness(strokeWidth)
			.setCompact(compact)
			.setParentAlign(parentAlign)
			.setParentOffset(parentOffset)
			.setCssClass(cssClass)
			.setUserObject(userObject);

		if (gapY != null) {
			result.setSibblingGapY(gapY);
			result.setSubtreeGapY(gapY);
		}
		if (sibblingGapY != null) {
			result.setSibblingGapY(sibblingGapY);
		}
		if (subtreeGapY != null) {
			result.setSubtreeGapY(subtreeGapY);
		}

		return result;
	}

	/**
	 * Creates a connection for a tree layout.
	 * 
	 * @param parent
	 *        The parent node or connector that should be connected.
	 * @param child
	 *        The child node or connector to connect to the given child.
	 * @param thickness
	 *        the optional stroke width used for this connection. Default is taken from the tree
	 *        layout.
	 * @param strokeStyle
	 *        The optional stroke style for this connection. Default is taken from the tree layout.
	 * @param dashes
	 *        An optional dashes array for rendering the edge. Default is to use a solid stroke.
	 * @param decorations
	 *        Additional decorations to place on the edge.
	 * 
	 * @return the new connection.
	 */
	@SideEffectFree
	@Label("Create connection")
	public static TreeConnection connection(
		@Mandatory Object parent,
		@Mandatory Object child,
		Double thickness,
		String strokeStyle,
		List<Double> dashes, 
		List<? extends EdgeDecoration> decorations
	) {
		if (parent == null) {
			return null;
		}
		if (dashes == null) {
			dashes = Collections.emptyList();
		}
		return TreeConnection.create()
			.setParent(asConnector(parent))
			.setChild(asConnector(child))
			.setThickness(thickness)
			.setStrokeStyle(strokeStyle)
			.setDashes(dashes)
			.setDecorations(decorations);
	}

	private static TreeConnector asConnector(Object node) {
		return node instanceof TreeConnector connector ? connector : TreeConnector.create().setAnchor((Box) node);
	}

	/**
	 * A connector to attach to a node to connect.
	 * 
	 * @param content
	 *        The content to render as decoration.
	 * @param linePosition
	 *        A ratio of the length of the edge that determines the position where the content is
	 *        placed on the edge. A value of 0.0 places the content at the start position of the
	 *        edge. A value of 1.0 places the content at the end position of the edge.
	 * @param offsetPosition
	 *        The position of the content box that is matched with the line position.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        An arbitrary object to associate with the graphics element.
	 * 
	 * @return The new decoration.
	 */
	@SideEffectFree
	@Label("Create edge decoration")
	public static EdgeDecoration decoration(
			Box content,
			String cssClass,
			Object userObject,
			double linePosition,
			OffsetPosition offsetPosition) {
		return EdgeDecoration.create()
			.setContent(content)
			.setLinePosition(linePosition)
			.setOffsetPosition(offsetPosition)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * A connector to attach to a node to connect.
	 * 
	 * @param anchor
	 *        The connected box.
	 * @param pos
	 *        Position where the connector is displayed.
	 * @param symbol
	 *        The symbol to display.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create connector")
	public static TreeConnector connector(
		@Mandatory Box anchor,
		@DoubleDefault(0.5) double pos,
		ConnectorSymbol symbol,
		String cssClass,
		Object userObject
	) {
		if (anchor == null) {
			return null;
		}
		return TreeConnector.create()
			.setAnchor(anchor)
			.setConnectPosition(pos)
			.setSymbol(symbol)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	private static Box nonNull(Box content) {
		return content == null ? Empty.create() : content;
	}

	/**
	 * Creates a 2D {@link Point}.
	 * 
	 * @param x
	 *        X position of the point.
	 * @param y
	 *        Y position of the point.
	 * @return The new {@link Point}.
	 */
	@SideEffectFree
	@Label("2D point")
	public static Point point(double x, double y) {
		return Point.create().setX(x).setY(y);
	}

	private static PolygonalChain polygonalChain(
			List<Point> points,
			boolean closed,
			String fillStyle,
			String stroke,
			double thickness,
			List<Double> dashes,
			String cssClass,
			Object userObject) {
		if (dashes == null) {
			dashes = Collections.emptyList();
		}
		return PolygonalChain.create()
			.setPoints(points)
			.setClosed(closed)
			.setFillStyle(fillStyle)
			.setStrokeStyle(stroke)
			.setThickness(thickness)
			.setDashes(dashes)
			.setCssClass(cssClass)
			.setUserObject(userObject);

	}

	/**
	 * Creates a polygon, i.e. a closed polygonal chain.
	 * 
	 * @param points
	 *        The corners of the polygon.
	 * @param fillStyle
	 *        The style how the enclosed area is filled.
	 * @param stroke
	 *        The style of the stroke.
	 * @param thickness
	 *        The width of the stroke.
	 * @param dashes
	 *        Length of dashes for a dashed border.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create polygon")
	public static PolygonalChain polygon(
			List<Point> points,
			@StringDefault("none") String fillStyle,
			@StringDefault("black") @ScriptConversion(ToStyle.class) String stroke,
			@DoubleDefault(1) double thickness,
			List<Double> dashes,
			String cssClass,
			Object userObject) {
		return polygonalChain(points, true, fillStyle, stroke, thickness, dashes, cssClass, userObject);
	}

	/**
	 * Creates a polygonal chain
	 * 
	 * @param points
	 *        The corners of the polygon.
	 * @param fillStyle
	 *        The style how the enclosed area is filled.
	 * @param stroke
	 *        The style of the stroke.
	 * @param thickness
	 *        The width of the stroke.
	 * @param dashes
	 *        Length of dashes for a dashed border.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Create poly line")
	public static PolygonalChain polyline(
			List<Point> points,
			@StringDefault("none") String fillStyle,
			@StringDefault("black") @ScriptConversion(ToStyle.class) String stroke,
			@DoubleDefault(1) double thickness,
			List<Double> dashes,
			String cssClass,
			Object userObject) {
		return polygonalChain(points, false, fillStyle, stroke, thickness, dashes, cssClass, userObject);
	}

	/**
	 * A {@link Box} whose contents are cut off when they become to large.
	 * 
	 * @param content
	 *        The contents to clip.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Clip content")
	public static ClipBox clipbox(
			@Mandatory Box content,
			String cssClass,
			Object userObject) {
		return ClipBox.create()
			.setContent(content)
			.setUserObject(userObject)
			.setClientId(cssClass)
			.setCssClass(cssClass);
	}

}

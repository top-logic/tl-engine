/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.callback.ClickHandler;
import com.top_logic.react.flow.callback.DiagramContextMenuProvider;
import com.top_logic.react.flow.data.Align;
import com.top_logic.react.flow.data.Alignment;
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttMilestone;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.server.axis.AxisProvider;
import com.top_logic.react.flow.server.axis.AxisProviderService;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.ClipBox;
import com.top_logic.react.flow.data.CompassLayout;
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.ContextMenu;
import com.top_logic.react.flow.data.Decoration;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DiagramDirection;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.Empty;
import com.top_logic.react.flow.data.Fill;
import com.top_logic.react.flow.data.FloatingLayout;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphLayout;
import com.top_logic.react.flow.data.GridLayout;
import com.top_logic.react.flow.data.HorizontalLayout;
import com.top_logic.react.flow.data.Image;
import com.top_logic.react.flow.data.ImageAlign;
import com.top_logic.react.flow.data.ImageOrientation;
import com.top_logic.react.flow.data.ImageScale;
import com.top_logic.react.flow.data.MouseButton;
import com.top_logic.react.flow.data.OffsetPosition;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Point;
import com.top_logic.react.flow.data.PolygonalChain;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Sized;
import com.top_logic.react.flow.data.SpaceDistribution;
import com.top_logic.react.flow.data.Stack;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.Tooltip;
import com.top_logic.react.flow.data.TreeConnection;
import com.top_logic.react.flow.data.TreeConnector;
import com.top_logic.react.flow.data.TreeLayout;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.server.handler.ServerDropHandler;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.operations.ScriptConversion;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * Factory for flow chart diagram elements.
 */
@ScriptPrefix("reactFlow")
public class FlowFactory extends TLScriptFunctions {
	
	private static final String FLOW_CORE_CSS = "/style/tl-flow-core.css";

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
			.setRoot(nonNull(root))
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
		double fontSize,
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
		return com.top_logic.react.flow.data.Border.create().setThickness(thickness).setStrokeStyle(stroke)
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

		contents = noNullContent(contents);

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

		contents = noNullContent(contents);

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
	 * Creates a box that displays a custom context menu.
	 * 
	 * @param content
	 *        Actual content of the box.
	 * @param menu
	 *        The context menu handler. Pass in the variable implicitly defined for the context menu
	 *        handler declared next to the diagram builder.
	 * @param cssClass
	 *        The css class for the new box.
	 * @param userObject
	 *        User object of the new box. Operations in the context menu operate on this object.
	 * @return The new box.
	 */
	@SideEffectFree
	@Label("Context menu")
	public static Box contextMenu(
			@Mandatory Box content,
			@Mandatory DiagramContextMenuProvider menu,
			String cssClass,
			Object userObject) {
		return ContextMenu.create()
			.setContent(nonNull(content))
			.setMenuProvider(menu)
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
	 * @param doubleClick
	 *        Whether to only react on double clicks.
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
			boolean doubleClick,
			String cssClass,
			Object userObject
			) {
		return ClickTarget.create()
			.setContent(nonNull(content))
			.setClickHandler(clickHandler)
			.setButtons(buttons)
			.setDoubleClick(doubleClick)
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
	 * Returns a list in which <code>null</code> was replaces by {@link Empty}.
	 * 
	 * <p>
	 * The given list is not modified. The return value may be the given list or a new one.
	 * </p>
	 */
	private static List<Box> noNullContent(List<Box> contents) {
		int i = 0;
		int size = contents.size();
		for (; i < size; i++) {
			if (contents.get(i) == null) {
				break;
			}
		}
		if (i == size) {
			// No null
			return contents;
		}
		List<Box> result = new ArrayList<>(size);
		result.addAll(contents.subList(0, i));
		result.add(Empty.create());
		i++;
		for (; i < size; i++) {
			result.add(nonNull(contents.get(i)));
		}
		return result;
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

	/**
	 * Converts a flow chart diagram to SVG binary data.
	 *
	 * <p>
	 * This function renders the diagram to a standalone SVG document. The resulting SVG can be
	 * saved, displayed, or converted to other formats (e.g., PDF using {@code pdfFile()}).
	 * </p>
	 *
	 * <p>
	 * Usage examples:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * // Basic usage with default settings
	 * flowToSvg(flowChart(flowText("Hello")))
	 *
	 * // With custom filename and text size
	 * flowToSvg(
	 *     flowChart(flowVbox(flowText("Header"), flowText("Content"))),
	 *     filename: "my-chart.svg",
	 *     textSize: 14.0
	 * )
	 *
	 * // With fixed dimensions
	 * flowToSvg($myDiagram, width: 800.0, height: 600.0)
	 *
	 * // Combined with PDF generation
	 * pdfFile(flowToSvg($diagram), "chart.pdf")
	 * </code>
	 * </pre>
	 *
	 * @param diagram
	 *        The diagram to render.
	 * @param filename
	 *        The filename for the resulting SVG file.
	 * @param textSize
	 *        The font size in points used for text measurement during layout.
	 * @param width
	 *        The fixed width of the SVG <code>viewBox</code>, or <code>null</code> for auto-sizing
	 *        based on content.
	 * @param height
	 *        The fixed height of the SVG <code>viewBox</code>, or <code>null</code> for auto-sizing
	 *        based on content.
	 * @return {@link BinaryData} containing the SVG document with content type
	 *         <code>"image/svg+xml"</code>.
	 */
	@SideEffectFree
	@Label("Export as SVG")
	public static BinaryData toSvg(
			@Mandatory Diagram diagram,
			@StringDefault("diagram.svg") String filename,
			@DoubleDefault(12.0) double textSize,
			Double width,
			Double height) {
		// Ensure .svg extension
		if (!filename.toLowerCase().endsWith(".svg")) {
			filename = filename + ".svg";
		}

		// Create render context for text measurement
		AWTContext context = new AWTContext((float) textSize);

		// Perform layout calculation
		diagram.layout(context);

		// Set viewBox dimensions
		Box root = diagram.getRoot();
		if (width != null && height != null) {
			diagram.setViewBoxWidth(width);
			diagram.setViewBoxHeight(height);
		} else if (width != null) {
			diagram.setViewBoxWidth(width);
			diagram.setViewBoxHeight(root.getHeight());
		} else if (height != null) {
			diagram.setViewBoxWidth(root.getWidth());
			diagram.setViewBoxHeight(height);
		} else {
			diagram.setViewBoxWidth(root.getWidth());
			diagram.setViewBoxHeight(root.getHeight());
		}

		// Ensure viewBox origin is set
		if (diagram.getViewBoxX() == 0 && diagram.getViewBoxY() == 0) {
			diagram.setViewBoxX(0);
			diagram.setViewBoxY(0);
		}

		// Render to SVG
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter) {
					boolean _svgStarted;
					@Override
					public void beginSvg() {
						super.beginSvg();

						_svgStarted = true;
					}

					@Override
					protected void endBeginTag() {
						super.endBeginTag();

						if (_svgStarted) {
							// Add default styles to the generated SVG.
							BinaryData styles = FileManager.getInstance().getDataOrNull(FLOW_CORE_CSS);
							if (styles == null) {
								Logger.warn("Missing PDF export styles: " + FLOW_CORE_CSS, FlowFactory.class);
							} else {
								tagWriter.beginTag("style");
								try (InputStream in = styles.getStream()) {
									StreamUtilities.copyReaderWriterContents(
										new InputStreamReader(in, StandardCharsets.UTF_8), tagWriter);
								} catch (IOException ex) {
									Logger.error("Failed to copy styles.", ex, FlowFactory.class);
								}
								tagWriter.endTag("style");
							}
							_svgStarted = false;
						}
					}
				}) {
			diagram.draw(svgWriter);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to generate SVG: " + ex.getMessage(), ex);
		}

		// Convert to binary data
		byte[] svgBytes = buffer.toString().getBytes(StandardCharsets.UTF_8);
		return BinaryDataFactory.createBinaryData(svgBytes, "image/svg+xml", filename);
	}

	/**
	 * Creates a graph layout that arranges nodes using hierarchical (layered) layout.
	 *
	 * @param nodes
	 *        The nodes to arrange.
	 * @param edges
	 *        The edges connecting nodes.
	 * @param layerGap
	 *        Gap between layers.
	 * @param nodeGap
	 *        Gap between nodes in the same layer.
	 * @param cssClass
	 *        The CSS class.
	 * @param userObject
	 *        User object.
	 * @return The new graph layout.
	 */
	@SideEffectFree
	@Label("Create graph layout")
	public static Box graphLayout(
			@Mandatory List<? extends Box> nodes,
			@Mandatory List<? extends GraphEdge> edges,
			@DoubleDefault(60) double layerGap,
			@DoubleDefault(30) double nodeGap,
			String cssClass,
			Object userObject) {
		return GraphLayout.create()
			.setNodes(nodes.stream().filter(Objects::nonNull).toList())
			.setEdges(edges.stream().filter(Objects::nonNull).toList())
			.setLayerGap(layerGap)
			.setNodeGap(nodeGap)
			.setCssClass(cssClass)
			.setUserObject(userObject);
	}

	/**
	 * Creates an edge for a graph layout.
	 *
	 * @param source
	 *        The source node.
	 * @param target
	 *        The target node.
	 * @param priority
	 *        Layout priority for cycle-breaking. Higher values prevent edge reversal. Use 3 for
	 *        inheritance (must always point upward), 2 for composition, 1 for normal references.
	 * @param sourceSymbol
	 *        Symbol at the source end (e.g. diamond for composition).
	 * @param targetSymbol
	 *        Symbol at the target end (e.g. filled arrow for inheritance).
	 * @param strokeStyle
	 *        Stroke color.
	 * @param thickness
	 *        Line thickness.
	 * @param dashes
	 *        Dash pattern.
	 * @param decorations
	 *        Edge decorations.
	 * @return The new edge.
	 */
	@SideEffectFree
	@Label("Create graph edge")
	public static GraphEdge graphEdge(
			@Mandatory Box source,
			@Mandatory Box target,
			int priority,
			ConnectorSymbol sourceSymbol,
			ConnectorSymbol targetSymbol,
			@StringDefault("black") String strokeStyle,
			@DoubleDefault(1) double thickness,
			List<Double> dashes,
			List<? extends EdgeDecoration> decorations,
			boolean selectable,
			Object userObject) {
		GraphEdge edge = GraphEdge.create()
			.setSource(source)
			.setTarget(target)
			.setPriority(priority)
			.setStrokeStyle(strokeStyle)
			.setThickness(thickness)
			.setSelectable(selectable);
		if (userObject != null) {
			edge.setUserObject(userObject);
		}
		if (sourceSymbol != null) {
			edge.setSourceSymbol(sourceSymbol);
		}
		if (targetSymbol != null) {
			edge.setTargetSymbol(targetSymbol);
		}
		if (dashes != null) {
			edge.setDashes(dashes);
		}
		if (decorations != null) {
			edge.setDecorations(decorations);
		}
		return edge;
	}

	/** Internal counter for ID generation (per JVM, stable across factory calls). */
	private static final java.util.concurrent.atomic.AtomicLong GANTT_ID_COUNTER =
		new java.util.concurrent.atomic.AtomicLong();

	private static String nextGanttId(String prefix) {
		return prefix + "-" + GANTT_ID_COUNTER.incrementAndGet();
	}

	@SideEffectFree
	@Label("Create Gantt row")
	public static GanttRow ganttRow(
			@Mandatory Object model,
			@Mandatory Box label,
			List<GanttRow> children) {
		GanttRow row = GanttRow.create()
			.setId(nextGanttId("gr"))
			.setUserObject(model)
			.setLabel(label);
		if (children != null) {
			row.setChildren(children);
		}
		return row;
	}

	@SideEffectFree
	@Label("Create Gantt span item")
	public static GanttSpan ganttSpan(
			@Mandatory Object model,
			@Mandatory GanttRow row,
			@Mandatory Box box,
			double start,
			double end,
			Boolean canMoveTime,
			Boolean canMoveRow,
			Boolean canResizeStart,
			Boolean canResizeEnd,
			Boolean canBeEdgeSource,
			Boolean canBeEdgeTarget) {
		GanttSpan span = GanttSpan.create()
			.setId(nextGanttId("gi"))
			.setUserObject(model)
			.setRowId(row.getId())
			.setBox(box)
			.setStart(start).setEnd(end);
		if (canMoveTime != null) span.setCanMoveTime(canMoveTime);
		if (canMoveRow != null) span.setCanMoveRow(canMoveRow);
		if (canResizeStart != null) span.setCanResizeStart(canResizeStart);
		if (canResizeEnd != null) span.setCanResizeEnd(canResizeEnd);
		if (canBeEdgeSource != null) span.setCanBeEdgeSource(canBeEdgeSource);
		if (canBeEdgeTarget != null) span.setCanBeEdgeTarget(canBeEdgeTarget);
		return span;
	}

	@SideEffectFree
	@Label("Create Gantt milestone item")
	public static GanttMilestone ganttMilestone(
			@Mandatory Object model,
			@Mandatory GanttRow row,
			@Mandatory Box box,
			double at,
			Boolean canMoveTime,
			Boolean canMoveRow,
			Boolean canBeEdgeSource,
			Boolean canBeEdgeTarget) {
		GanttMilestone milestone = GanttMilestone.create()
			.setId(nextGanttId("gi"))
			.setUserObject(model)
			.setRowId(row.getId())
			.setBox(box)
			.setAt(at);
		if (canMoveTime != null) milestone.setCanMoveTime(canMoveTime);
		if (canMoveRow != null) milestone.setCanMoveRow(canMoveRow);
		if (canBeEdgeSource != null) milestone.setCanBeEdgeSource(canBeEdgeSource);
		if (canBeEdgeTarget != null) milestone.setCanBeEdgeTarget(canBeEdgeTarget);
		return milestone;
	}

	@SideEffectFree
	@Label("Create Gantt edge")
	public static GanttEdge ganttEdge(
			@Mandatory Object model,
			@Mandatory GanttItem source,
			@Mandatory GanttEndpoint sourceEndpoint,
			@Mandatory GanttItem target,
			@Mandatory GanttEndpoint targetEndpoint,
			GanttEnforce enforce) {
		return GanttEdge.create()
			.setId(nextGanttId("ge"))
			.setUserObject(model)
			.setSourceItemId(source.getId()).setSourceEndpoint(sourceEndpoint)
			.setTargetItemId(target.getId()).setTargetEndpoint(targetEndpoint)
			.setEnforce(enforce != null ? enforce : GanttEnforce.STRICT);
	}

	@SideEffectFree
	@Label("Create Gantt line decoration")
	public static GanttLineDecoration ganttLineDeco(
			@Mandatory Object model,
			double at,
			String color,
			Box label,
			List<GanttRow> relevantFor,
			Boolean canMove) {
		GanttLineDecoration deco = GanttLineDecoration.create()
			.setId(nextGanttId("gd"))
			.setUserObject(model)
			.setAt(at)
			.setColor(color != null ? color : "#c02020")
			.setLabel(label);
		if (relevantFor != null) {
			java.util.List<String> ids = new java.util.ArrayList<>(relevantFor.size());
			for (GanttRow r : relevantFor) { ids.add(r.getId()); }
			deco.setRelevantFor(ids);
		}
		if (canMove != null) deco.setCanMove(canMove);
		return deco;
	}

	@SideEffectFree
	@Label("Create Gantt range decoration")
	public static GanttRangeDecoration ganttRangeDeco(
			@Mandatory Object model,
			double from,
			double to,
			String color,
			Box label,
			List<GanttRow> relevantFor,
			Boolean canMove,
			Boolean canResize) {
		GanttRangeDecoration deco = GanttRangeDecoration.create()
			.setId(nextGanttId("gd"))
			.setUserObject(model)
			.setFrom(from).setTo(to)
			.setColor(color != null ? color : "rgba(255, 220, 120, 0.35)")
			.setLabel(label);
		if (relevantFor != null) {
			java.util.List<String> ids = new java.util.ArrayList<>(relevantFor.size());
			for (GanttRow r : relevantFor) { ids.add(r.getId()); }
			deco.setRelevantFor(ids);
		}
		if (canMove != null) deco.setCanMove(canMove);
		if (canResize != null) deco.setCanResize(canResize);
		return deco;
	}

	@SideEffectFree
	@Label("Create Gantt axis")
	public static GanttAxis ganttAxis(
			@Mandatory String providerId,
			double rangeMin,
			double rangeMax,
			Double zoom) {
		double pixelsPerUnit = zoom != null ? zoom : 1.0;
		AxisProviderService svc = AxisProviderService.Module.INSTANCE.getImplementationInstance();
		AxisProvider provider = (svc != null) ? svc.lookup(providerId) : null;
		List<GanttTick> ticks = provider != null
			? provider.ticksFor(rangeMin, rangeMax, pixelsPerUnit)
			: java.util.Collections.emptyList();
		double snap = provider != null ? provider.snapGranularity(pixelsPerUnit) : 1.0;
		return GanttAxis.create()
			.setProviderId(providerId)
			.setRangeMin(rangeMin).setRangeMax(rangeMax)
			.setCurrentZoom(pixelsPerUnit).setSnapGranularity(snap)
			.setCurrentTicks(ticks);
	}

	@SideEffectFree
	@Label("Create Gantt layout")
	public static GanttLayout gantt(
			@Mandatory List<GanttRow> rootRows,
			@Mandatory List<GanttItem> items,
			List<GanttEdge> edges,
			List<GanttDecoration> decorations,
			@Mandatory GanttAxis axis) {
		GanttLayout layout = GanttLayout.create()
			.setRootRows(rootRows)
			.setItems(items)
			.setAxis(axis);
		if (edges != null) {
			layout.setEdges(edges);
		}
		if (decorations != null) {
			layout.setDecorations(decorations);
		}
		List<Box> contents = new java.util.ArrayList<>(items.size());
		for (GanttItem it : items) {
			if (it.getBox() != null) {
				contents.add(it.getBox());
			}
		}
		for (GanttRow root : rootRows) {
			addRowLabelsToContents(root, contents);
		}
		if (axis != null) {
			for (GanttTick tick : axis.getCurrentTicks()) {
				if (tick.getLabel() != null) {
					contents.add(tick.getLabel());
				}
			}
		}
		if (decorations != null) {
			for (GanttDecoration deco : decorations) {
				if (deco.getLabel() != null) {
					contents.add(deco.getLabel());
				}
			}
		}
		layout.setContents(contents);
		return layout;
	}

	private static void addRowLabelsToContents(GanttRow row, List<Box> contents) {
		Box label = row.getLabel();
		if (label != null) {
			contents.add(label);
		}
		for (GanttRow child : row.getChildren()) {
			addRowLabelsToContents(child, contents);
		}
	}

}

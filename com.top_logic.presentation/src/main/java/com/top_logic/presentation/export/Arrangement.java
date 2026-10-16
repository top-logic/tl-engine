/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.Bounds;
import com.top_logic.presentation.model.ContainerSpec;
import com.top_logic.presentation.model.CrossAlign;
import com.top_logic.presentation.model.ElementSpec;

/**
 * Computes the position and size of the content elements of a slide.
 *
 * <ul>
 * <li>An element with a position ({@link Bounds#isPositioned()}) is placed there. A width or
 * height it does not give extends to the right or bottom border of the content area of the slide
 * layout, a missing coordinate is the one of the content area.</li>
 * <li>All other elements are arranged one below the other in the content area, like the children
 * of a column.</li>
 * <li>A {@link ContainerSpec row or column} arranges its children in its bounds: Along its
 * direction, a child takes its own size (its width in a row, its height in a column), a child of
 * a column with automatic height the {@link #naturalHeight(ElementSpec, double, String) height of
 * its content}. All other children share the remaining space by their weights. If the children of
 * a column with own or automatic height need more space than the column has, the automatic
 * heights are reduced proportionally. Across the direction, a child takes its own size, otherwise
 * it is stretched to the size of the container, or in a row aligned other than
 * {@link CrossAlign#STRETCH stretched} it takes the height of its content, at most the height of
 * the row.</li>
 * <li>A child of a column is at most as wide as the column.</li>
 * </ul>
 *
 * <p>
 * The result are the bounds of all elements that are written to the slide, the containers
 * themselves are not part of it. Only a {@link ContainerSpec#split() split column} is part of the
 * result as a whole, since its children are placed while writing it, see
 * {@link ElementWriter}.
 * </p>
 */
public final class Arrangement {

	/**
	 * Measures the height an element needs for its content.
	 */
	@FunctionalInterface
	public interface HeightMeasure {

		/**
		 * The height in points the given element needs to show its content at the given width.
		 *
		 * @param element
		 *        An element that is no {@link ContainerSpec}.
		 * @param width
		 *        The width of the element in points.
		 * @param path
		 *        The path of the element in the description, for error messages.
		 */
		double naturalHeight(ElementSpec element, double width, String path);

	}

	/**
	 * An element with its computed bounds.
	 *
	 * @param element
	 *        The element, a {@link ContainerSpec} only if it is a {@link ContainerSpec#split() split
	 *        column}.
	 * @param bounds
	 *        The position and size of the element on the slide in points.
	 * @param path
	 *        The path of the element in the description, for error messages.
	 */
	public record Placed(ElementSpec element, Rectangle2D bounds, String path) {
		// Pure data.
	}

	private final double _gap;

	private final HeightMeasure _measure;

	/**
	 * Creates an {@link Arrangement}.
	 *
	 * @param gap
	 *        The distance between the elements of a row or column that does not give its own, and
	 *        between the elements arranged in the content area.
	 * @param measure
	 *        Measures the content of elements with automatic height.
	 */
	public Arrangement(double gap, HeightMeasure measure) {
		_gap = gap;
		_measure = measure;
	}

	/**
	 * Computes the bounds of the given content elements of a slide.
	 *
	 * @param content
	 *        The content elements of a slide.
	 * @param area
	 *        The content area of the slide layout.
	 * @param slidePath
	 *        The path of the slide in the description, for error messages.
	 * @return The elements to write with their bounds, in the order of the description.
	 */
	public List<Placed> arrange(List<ElementSpec> content, Rectangle2D area, String slidePath) {
		List<ElementSpec> flowing = new ArrayList<>();
		List<String> flowingPaths = new ArrayList<>();
		for (int n = 0; n < content.size(); n++) {
			ElementSpec element = content.get(n);
			if (!element.bounds().isPositioned()) {
				flowing.add(element);
				flowingPaths.add(elementPath(slidePath, n));
			}
		}
		List<Rectangle2D> flowingBounds = arrangeChildren(
			new ContainerSpec(Bounds.NONE, ContainerSpec.Direction.COLUMN, flowing, null, null, null), area,
			flowingPaths);

		List<Placed> result = new ArrayList<>();
		int flowingIndex = 0;
		for (int n = 0; n < content.size(); n++) {
			ElementSpec element = content.get(n);
			String path = elementPath(slidePath, n);
			Rectangle2D bounds = element.bounds().isPositioned()
				? positioned(element, area, path) : flowingBounds.get(flowingIndex++);
			place(element, bounds, path, result);
		}
		return result;
	}

	/**
	 * The path of the content element with the given index.
	 */
	public static String elementPath(String slidePath, int index) {
		return slidePath + "." + PresentationData.CONTENT + "[" + index + "]";
	}

	private Rectangle2D positioned(ElementSpec element, Rectangle2D area, String path) {
		Bounds bounds = element.bounds();
		double x = bounds.x() != null ? bounds.x().doubleValue() : area.getX();
		double y = bounds.y() != null ? bounds.y().doubleValue() : area.getY();
		double width = bounds.width() != null ? bounds.width().doubleValue() : Math.max(0, area.getMaxX() - x);
		double height;
		if (bounds.height() != null) {
			height = bounds.height().doubleValue();
		} else if (bounds.autoHeight()) {
			height = naturalHeight(element, width, path);
		} else {
			height = Math.max(0, area.getMaxY() - y);
		}
		return new Rectangle2D.Double(x, y, width, height);
	}

	/**
	 * Computes the bounds of the elements to write for the given element.
	 *
	 * @param element
	 *        The element, e.g. a row or column.
	 * @param bounds
	 *        The position and size of the element.
	 * @param path
	 *        The path of the element in the description, for error messages.
	 * @return The given element, or the elements of a container, with their bounds.
	 */
	public List<Placed> place(ElementSpec element, Rectangle2D bounds, String path) {
		List<Placed> result = new ArrayList<>();
		place(element, bounds, path, result);
		return result;
	}

	private void place(ElementSpec element, Rectangle2D bounds, String path, List<Placed> result) {
		if (element instanceof ContainerSpec container && !container.split()) {
			List<String> paths = childPaths(container, path);
			List<Rectangle2D> childBounds = arrangeChildren(container, bounds, paths);
			for (int n = 0; n < childBounds.size(); n++) {
				place(container.children().get(n), childBounds.get(n), paths.get(n), result);
			}
		} else {
			result.add(new Placed(element, bounds, path));
		}
	}

	/**
	 * The path of the child with the given index of a container.
	 */
	public static String childPath(String containerPath, int index) {
		return containerPath + "." + PresentationData.CHILDREN + "[" + index + "]";
	}

	private static List<String> childPaths(ContainerSpec container, String path) {
		List<String> result = new ArrayList<>();
		for (int n = 0; n < container.children().size(); n++) {
			result.add(childPath(path, n));
		}
		return result;
	}

	/**
	 * The height in points the given element needs to show its content at the given width.
	 *
	 * <p>
	 * A row needs the height of its highest child, a column the heights of its children plus the
	 * gaps. A child with own height needs this height.
	 * </p>
	 */
	public double naturalHeight(ElementSpec element, double width, String path) {
		if (!(element instanceof ContainerSpec container)) {
			return _measure.naturalHeight(element, width, path);
		}
		List<ElementSpec> children = container.children();
		List<String> paths = childPaths(container, path);
		if (container.direction() == ContainerSpec.Direction.ROW) {
			double[] widths = mainSizes(container, width, children.size(), n -> children.get(n).bounds().width());
			double result = 0;
			for (int n = 0; n < children.size(); n++) {
				result = Math.max(result, ownOrNatural(children.get(n), widths[n], paths.get(n)));
			}
			return result;
		}
		double result = gap(container) * Math.max(0, children.size() - 1);
		for (int n = 0; n < children.size(); n++) {
			ElementSpec child = children.get(n);
			result += ownOrNatural(child, crossSize(child.bounds().width(), width), paths.get(n));
		}
		return result;
	}

	private double ownOrNatural(ElementSpec element, double width, String path) {
		Double height = element.bounds().height();
		return height != null ? height.doubleValue() : naturalHeight(element, width, path);
	}

	private List<Rectangle2D> arrangeChildren(ContainerSpec container, Rectangle2D bounds, List<String> paths) {
		return container.direction() == ContainerSpec.Direction.ROW
			? arrangeRow(container, bounds, paths) : arrangeColumn(container, bounds, paths);
	}

	private List<Rectangle2D> arrangeRow(ContainerSpec container, Rectangle2D bounds, List<String> paths) {
		List<ElementSpec> children = container.children();
		int count = children.size();
		double gap = gap(container);
		double[] widths = mainSizes(container, bounds.getWidth(), count, n -> children.get(n).bounds().width());
		CrossAlign align = align(container);

		List<Rectangle2D> result = new ArrayList<>(count);
		double x = bounds.getX();
		for (int n = 0; n < count; n++) {
			ElementSpec child = children.get(n);
			Bounds childBounds = child.bounds();
			double height;
			if (childBounds.height() != null) {
				height = childBounds.height().doubleValue();
			} else if (childBounds.autoHeight() || align != CrossAlign.STRETCH) {
				// The content element is fitted into the height of the row.
				height = Math.min(naturalHeight(child, widths[n], paths.get(n)), bounds.getHeight());
			} else {
				height = bounds.getHeight();
			}
			double y = crossOffset(align, bounds.getY(), bounds.getHeight(), height);
			result.add(new Rectangle2D.Double(x, y, widths[n], height));
			x += widths[n] + gap;
		}
		return result;
	}

	private List<Rectangle2D> arrangeColumn(ContainerSpec container, Rectangle2D bounds, List<String> paths) {
		List<ElementSpec> children = container.children();
		int count = children.size();
		double gap = gap(container);
		double available = Math.max(0, bounds.getHeight() - gap * Math.max(0, count - 1));

		double[] widths = new double[count];
		double[] heights = new double[count];
		boolean[] auto = new boolean[count];
		double fixed = 0;
		double automatic = 0;
		double totalWeight = 0;
		for (int n = 0; n < count; n++) {
			Bounds childBounds = children.get(n).bounds();
			widths[n] = crossSize(childBounds.width(), bounds.getWidth());
			if (childBounds.height() != null) {
				heights[n] = childBounds.height().doubleValue();
				fixed += heights[n];
			} else if (childBounds.autoHeight()) {
				heights[n] = naturalHeight(children.get(n), widths[n], paths.get(n));
				auto[n] = true;
				automatic += heights[n];
			} else {
				totalWeight += container.weight(n);
			}
		}
		if (automatic > 0 && fixed + automatic > available) {
			// The content elements are fitted into the reduced heights.
			double factor = Math.max(0, available - fixed) / automatic;
			for (int n = 0; n < count; n++) {
				if (auto[n]) {
					heights[n] *= factor;
				}
			}
			automatic = Math.max(0, available - fixed);
		}
		double rest = Math.max(0, available - fixed - automatic);
		for (int n = 0; n < count; n++) {
			Bounds childBounds = children.get(n).bounds();
			if (childBounds.height() == null && !childBounds.autoHeight()) {
				heights[n] = rest * container.weight(n) / totalWeight;
			}
		}

		CrossAlign align = align(container);
		List<Rectangle2D> result = new ArrayList<>(count);
		double y = bounds.getY();
		for (int n = 0; n < count; n++) {
			double x = crossOffset(align, bounds.getX(), bounds.getWidth(), widths[n]);
			result.add(new Rectangle2D.Double(x, y, widths[n], heights[n]));
			y += heights[n] + gap;
		}
		return result;
	}

	/**
	 * Computes the sizes of the children of a container along its direction.
	 *
	 * @param size
	 *        The size of the container along its direction.
	 * @param ownSize
	 *        The own size of the child with the given index, <code>null</code> if it has none.
	 */
	private double[] mainSizes(ContainerSpec container, double size, int count,
			IntFunction<Double> ownSize) {
		double available = Math.max(0, size - gap(container) * Math.max(0, count - 1));
		double[] result = new double[count];
		double fixed = 0;
		double totalWeight = 0;
		for (int n = 0; n < count; n++) {
			Double own = ownSize.apply(n);
			if (own != null) {
				result[n] = own.doubleValue();
				fixed += result[n];
			} else {
				totalWeight += container.weight(n);
			}
		}
		double rest = Math.max(0, available - fixed);
		for (int n = 0; n < count; n++) {
			if (ownSize.apply(n) == null) {
				result[n] = rest * container.weight(n) / totalWeight;
			}
		}
		return result;
	}

	/**
	 * The width of a child of a column: its own width, but at most the width of the column.
	 */
	static double crossSize(Double own, double containerSize) {
		return own != null ? Math.min(own.doubleValue(), containerSize) : containerSize;
	}

	/**
	 * The position of a child across the direction of its container.
	 */
	static double crossOffset(CrossAlign align, double start, double containerSize, double size) {
		switch (align) {
			case CENTER:
				return start + (containerSize - size) / 2;
			case END:
				return start + containerSize - size;
			default:
				return start;
		}
	}

	private double gap(ContainerSpec container) {
		return container.gap() != null ? container.gap().doubleValue() : _gap;
	}

	private static CrossAlign align(ContainerSpec container) {
		return container.align() != null ? container.align() : CrossAlign.STRETCH;
	}

}

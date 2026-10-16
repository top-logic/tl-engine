/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

/**
 * A row or column arranging its children.
 *
 * <p>
 * Along its direction, a child takes its own size (its width in a row, its height in a column) if
 * given. In a column, a child with {@link Bounds#autoHeight() automatic height} takes the height of
 * its content. All other children share the remaining space by their weights. Across the
 * direction, the children are aligned as given by {@link #align()}.
 * </p>
 *
 * <p>
 * The children have no position of their own, they are placed by the container.
 * </p>
 *
 * <p>
 * A column with {@link #split()} gives each child its own height or the height of its content and
 * continues on further slides child by child: The children that fit stay on the slide. A
 * table or a text with {@link TextSpec#split()} that does not fit shows the rows or paragraphs
 * that fit, any other child that does not fit moves to the next slide. A child that is higher than
 * the whole column on its own is reduced to the height of the column.
 * </p>
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param direction
 *        Whether the children are arranged side by side or one below the other.
 * @param children
 *        The arranged elements.
 * @param gap
 *        The distance between two children in points, <code>null</code> for the distance of the
 *        template profile.
 * @param weights
 *        The weight of each child, <code>null</code> for the weight of the children themselves.
 * @param align
 *        The alignment of the children across the direction, <code>null</code> for
 *        {@link CrossAlign#STRETCH}.
 * @param split
 *        Whether the children of a column that do not fit continue on further slides. Always
 *        <code>false</code> for a row.
 */
public record ContainerSpec(Bounds bounds, Direction direction, List<ElementSpec> children, Double gap,
		List<Double> weights, CrossAlign align, boolean split) implements ElementSpec {

	/**
	 * Creates a container whose children do not continue on further slides.
	 */
	public ContainerSpec(Bounds bounds, Direction direction, List<ElementSpec> children, Double gap,
			List<Double> weights, CrossAlign align) {
		this(bounds, direction, children, gap, weights, align, false);
	}

	/**
	 * The direction in which a {@link ContainerSpec} arranges its children.
	 */
	public enum Direction {

		/** The children are arranged side by side from left to right. */
		ROW,

		/** The children are arranged one below the other from top to bottom. */
		COLUMN;

	}

	/**
	 * The weight of the child with the given index in the space shared by the children without
	 * own size.
	 *
	 * <p>
	 * The weight given by the child itself wins over the {@link #weights()} of the container. The
	 * default weight is 1.
	 * </p>
	 */
	public double weight(int index) {
		Double own = children.get(index).bounds().weight();
		if (own != null) {
			return own.doubleValue();
		}
		return weights == null ? 1 : weights.get(index).doubleValue();
	}

}

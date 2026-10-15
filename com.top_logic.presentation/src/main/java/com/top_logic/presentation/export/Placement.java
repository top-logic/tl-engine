/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.Bounds;
import com.top_logic.presentation.model.ElementSpec;
import com.top_logic.util.error.TopLogicException;

/**
 * Computes the position and size of the content elements of a slide.
 *
 * <p>
 * An element with complete {@link ElementSpec#bounds() bounds} is placed there. A single element
 * with incomplete bounds fills the content area of the slide layout; the values it gives (e.g. a
 * height) take precedence over the area, a width or height not given extends to the right or
 * bottom border of the area. Several elements with incomplete bounds on one slide are an error.
 * </p>
 */
public final class Placement {

	private Placement() {
		// Static utility.
	}

	/**
	 * Computes the bounds of the given elements.
	 *
	 * @param elements
	 *        The content elements of a slide.
	 * @param area
	 *        The content area of the slide layout.
	 * @param path
	 *        The path of the slide in the description, for error messages.
	 * @return The bounds in points for each element, in the order of the elements.
	 */
	public static List<Rectangle2D> place(List<ElementSpec> elements, Rectangle2D area, String path) {
		List<Rectangle2D> result = new ArrayList<>(elements.size());
		String unpositioned = null;
		for (int n = 0; n < elements.size(); n++) {
			Bounds bounds = elements.get(n).bounds();
			if (bounds.isComplete()) {
				result.add(new Rectangle2D.Double(bounds.x(), bounds.y(), bounds.width(), bounds.height()));
				continue;
			}
			String elementPath = elementPath(path, n);
			if (unpositioned != null) {
				throw new TopLogicException(
					I18NConstants.ERROR_SEVERAL_UNPOSITIONED__PATH_OTHER.fill(elementPath, unpositioned));
			}
			unpositioned = elementPath;
			result.add(fill(bounds, area));
		}
		return result;
	}

	/**
	 * The path of the content element with the given index.
	 */
	public static String elementPath(String slidePath, int index) {
		return slidePath + "." + PresentationData.CONTENT + "[" + index + "]";
	}

	private static Rectangle2D fill(Bounds bounds, Rectangle2D area) {
		double x = bounds.x() != null ? bounds.x().doubleValue() : area.getX();
		double y = bounds.y() != null ? bounds.y().doubleValue() : area.getY();
		double width = bounds.width() != null ? bounds.width().doubleValue() : Math.max(0, area.getMaxX() - x);
		double height = bounds.height() != null ? bounds.height().doubleValue() : Math.max(0, area.getMaxY() - y);
		return new Rectangle2D.Double(x, y, width, height);
	}

}

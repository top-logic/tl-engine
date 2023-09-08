/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Mathematical utilities.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class MathUtil {

	/**
	 * Euclidian distance between two positions.
	 */
	public static double getEuclidianDistance(Position position1, Position position2) {
		double xDifference = position1.getX() - position2.getX();
		double yDifference = position1.getY() - position2.getY();

		return Math.sqrt(xDifference * xDifference + yDifference * yDifference);
	}

	/**
	 * Position of the point we get if we move position1 t along the line between position1
	 *         and position2.
	 */
	public static Position getRelativeClosePoint(Position position1, Position position2, double t) {
		Position relativeClosePoint = JavaScriptObject.createObject().cast();

		relativeClosePoint.setX((1 - t) * position1.getX() + t * position2.getX());
		relativeClosePoint.setY((1 - t) * position1.getY() + t * position2.getY());

		return relativeClosePoint;
	}

	/**
	 * @see #getRelativeClosePoint(Position, Position, double)
	 */
	public static Position getAbsoluteClosePoint(Position position1, Position position2, double absoluteDistance) {
		double euclidianDistance = getEuclidianDistance(position1, position2);

		return getRelativeClosePoint(position1, position2, absoluteDistance / euclidianDistance);
	}

}

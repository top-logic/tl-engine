/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.util;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.core.Canvas;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.Position;

/**
 * Utilities methods for diagramJS objects.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSObjectUtil {

	/**
	 * Checks if the given {@link JavaScriptObject} is diagramJS Label.
	 */
	public static boolean isLabel(JavaScriptObject object) {
		return JavaScriptObjectUtil.has(object, "labelTarget");
	}

	/**
	 * Checks if the given {@link JavaScriptObject} is diagramJS Shape.
	 */
	public static boolean isShape(JavaScriptObject object) {
		// TODO SFO .. connections could have children (labels..)
		return JavaScriptObjectUtil.has(object, "children") && !isConnection(object);
	}

	/**
	 * Checks if the given {@link JavaScriptObject} is diagramJS Connection.
	 */
	public static boolean isConnection(JavaScriptObject object) {
		return JavaScriptObjectUtil.has(object, "waypoints");
	}
	
	private static native JavaScriptObject getConstructor(JavaScriptObject object) /*-{
		return object.constructor;
	}-*/;

	/**
	 * Javascript function name polyfill for IE11.
	 */
	private static native String getFunctionName(JavaScriptObject func) /*-{
		if (!func.name) {
			var ret = func.toString();
			ret = ret.substr('function '.length);
			ret = ret.substr(0, ret.indexOf('('));

			func.name = ret;
		}

		return func.name;
	}-*/;

	/**
	 * Returns first item bottom-up in the type hirachy of {@code element} which is not a
	 *         {@link Label}.
	 */
	public static Base getNonLabelElement(Base element) {
		if (isLabel(element)) {
			return element.getParent();
		} else {
			return element;
		}
	}

	/**
	 * Center {@link Position} for all given {@link Base} elements for the given
	 *         {@link Canvas}.
	 */
	public static Position getCanvasCenter(Collection<Base> elements, Canvas canvas) {
		return getCenter(getAbsoluteBounds(canvas, elements), canvas.getSize());
	}

	private static Position getCenter(Collection<Bounds> elementsBounds, Dimension size) {
		Position center = getCenter(getPositions(elementsBounds));

		center.move(-size.getWidth() / 2, -size.getHeight() / 2);

		return center;
	}

	/**
	 * Get the absolute {@link Bounds} for the given {@link Base} elements. The elements BBox
	 * without the viewbox moving.
	 */
	public static Collection<Bounds> getAbsoluteBounds(Canvas canvas, Collection<Base> elements) {
		return elements.stream().map(getAbsoluteBoundsMapping(canvas)).collect(Collectors.toSet());
	}

	private static Function<? super Base, ? extends Bounds> getAbsoluteBoundsMapping(Canvas canvas) {
		return element -> {
			Bounds absoluteBBox = canvas.getAbsoluteBBox(element);

			absoluteBBox.setPosition(getAbsoluteElementPosition(canvas, absoluteBBox.getPosition()));

			return absoluteBBox;
		};
	}

	private static Position getAbsoluteElementPosition(Canvas canvas, Position position) {
		Position viewboxPosition = canvas.getViewbox().getPosition();

		position.move(viewboxPosition.getX(), viewboxPosition.getY());

		return position;
	}

	/**
	 * Get {@link Position}s for a {@link Collection} of element {@link Bounds}.
	 */
	public static Set<Position> getPositions(Collection<Bounds> elementsBounds) {
		return elementsBounds.stream().map(bound -> bound.getPosition()).collect(Collectors.toSet());
	}

	/**
	 * Get a simple center for a set of {@link Position}s.
	 */
	public static Position getCenter(Collection<Position> points) {
		Position center = JavaScriptObject.createObject().cast();

		double x = 0., y = 0.;

		for (Position point : points) {
			x += point.getX();
			y += point.getY();
		}

		center.setX(x / points.size());
		center.setY(y / points.size());

		return center;
	}

	/**
	 * Create {@link Bounds} for the given {@link Position} and {@link Dimension}.
	 */
	public static Bounds createBounds(Position position, Dimension dimension) {
		Bounds bounds = JavaScriptObject.createObject().cast();

		bounds.setPosition(position);
		bounds.setDimension(dimension);

		return bounds;
	}
}

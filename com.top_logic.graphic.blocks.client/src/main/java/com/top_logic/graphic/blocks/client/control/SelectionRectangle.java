/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import java.util.ArrayList;
import java.util.List;

import org.vectomatic.dom.svg.OMSVGMatrix;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.SelectableBox;

import elemental2.core.JsMath;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.NodeList;
import elemental2.dom.PointerEvent;
import jsinterop.base.Js;

/**
 * Selects the boxes of a diagram by a rectangle the user draws with the mouse.
 *
 * <p>
 * Dragging with {@code Ctrl} held spans a rectangle between the point the mouse button was pressed
 * and the point it is released. Releasing the button adds every selectable box lying completely
 * inside the rectangle to the selection, as a click with {@code Ctrl} on each of them would (see
 * {@link com.top_logic.graphic.flow.operations.DiagramOperations#addToSelection(Iterable)}). The
 * gesture is only offered in a diagram with {@link Diagram#isMultiSelect() multiple selection};
 * {@code Escape} drops it.
 * </p>
 *
 * <p>
 * Pressing {@code Ctrl} and releasing the button without moving the mouse further than
 * {@link #DRAG_THRESHOLD} is a click and is left to the click handlers of the diagram elements.
 * </p>
 */
class SelectionRectangle {

	/** CSS class of the rectangle drawn while dragging. */
	static final String RECTANGLE_CLASS = "tlSelectionRectangle";

	/** Distance in screen pixels the mouse must move until a press becomes a rectangle drag. */
	private static final double DRAG_THRESHOLD = 4.0;

	/** CSS selector of all elements of a diagram the user can select. */
	private static final String SELECTABLE_ELEMENTS = ".tlCanSelect, .tlSelected";

	private final JSDiagramControl _control;

	private final OMSVGSVGElement _svg;

	/**
	 * Drops the gesture on {@code Escape}, registered only while the gesture lasts.
	 *
	 * <p>
	 * Registered for the capture phase: the key handling of the page does not let a key event
	 * bubble up to the window.
	 * </p>
	 */
	private final EventListener _escape = new EventListener() {
		@Override
		public void handleEvent(Event evt) {
			KeyboardEvent event = (KeyboardEvent) evt;
			if ("Escape".equals(event.key)) {
				abort();
				event.stopPropagation();
				event.preventDefault();
			}
		}
	};

	/** Whether the button was pressed with {@code Ctrl}, but the mouse has not moved far yet. */
	private boolean _pending;

	/** Whether the rectangle is being drawn. */
	private boolean _active;

	/**
	 * Whether {@code Escape} dropped the gesture, which still lasts until the button is released.
	 */
	private boolean _aborted;

	/** The pointer that started the gesture. */
	private int _pointerId;

	/** Client coordinates of the point the button was pressed. */
	private double _startX, _startY;

	/** Client coordinates of the current mouse position. */
	private double _endX, _endY;

	/** The rectangle drawn while dragging. */
	private OMSVGRectElement _rectangle;

	/**
	 * Creates a {@link SelectionRectangle}.
	 */
	SelectionRectangle(JSDiagramControl control, OMSVGSVGElement svg) {
		_control = control;
		_svg = svg;
	}

	/**
	 * Whether a press with {@code Ctrl} may still become, or already is, a rectangle drag.
	 *
	 * <p>
	 * While this is the case, the native drag of the diagram, which pans it, must not start.
	 * </p>
	 */
	boolean isStarted() {
		return _pending || _active || _aborted;
	}

	/**
	 * Handles the pointer-down event.
	 *
	 * <p>
	 * The event is not consumed, since it may still turn out to be a click.
	 * </p>
	 */
	void onDown(PointerEvent event) {
		// A press always starts anew: an aborted gesture whose release went elsewhere is over.
		cancel();

		Diagram diagram = _control._diagram;
		if (diagram == null || !diagram.isMultiSelect() || event.button != 0 || !event.ctrlKey) {
			return;
		}
		_pending = true;
		_pointerId = event.pointerId;
		_startX = event.clientX;
		_startY = event.clientY;
		DomGlobal.window.addEventListener("keydown", _escape, true);
	}

	/**
	 * Handles the pointer-move event.
	 *
	 * @return Whether the event was consumed by drawing the rectangle.
	 */
	boolean onMove(PointerEvent event) {
		if (!isStarted() || event.pointerId != _pointerId) {
			return false;
		}
		if (_aborted) {
			// Nothing is drawn any more, but the gesture lasts until the release.
			return true;
		}
		_endX = event.clientX;
		_endY = event.clientY;
		if (_pending) {
			if (JsMath.abs(_endX - _startX) < DRAG_THRESHOLD && JsMath.abs(_endY - _startY) < DRAG_THRESHOLD) {
				return false;
			}
			start();
		}
		updateRectangle();
		return true;
	}

	/**
	 * Handles the pointer-up event.
	 *
	 * @return Whether the event ended a rectangle drag and was consumed.
	 */
	boolean onUp(PointerEvent event) {
		if (!isStarted() || event.pointerId != _pointerId) {
			return false;
		}
		if (_aborted) {
			// The release ends the gesture dropped by Escape and clicks nothing: a click on the
			// diagram background would give up the whole selection.
			cancel();
			suppressNextClick();
			return true;
		}
		if (_pending) {
			// A click, left to the elements.
			cancel();
			return false;
		}
		_endX = event.clientX;
		_endY = event.clientY;

		List<SelectableBox> enclosed = findEnclosed();
		cancel();
		suppressNextClick();

		_control._diagram.addToSelection(enclosed);
		return true;
	}

	/**
	 * Drops the gesture without changing the selection.
	 */
	void cancel() {
		DomGlobal.window.removeEventListener("keydown", _escape, true);
		_pending = false;
		_active = false;
		_aborted = false;
		Element svgElement = svgElement();
		if (svgElement.hasPointerCapture(_pointerId)) {
			svgElement.releasePointerCapture(_pointerId);
		}
		removeRectangle();
	}

	/**
	 * Drops the gesture on {@code Escape} without changing the selection.
	 *
	 * <p>
	 * The rectangle disappears, but the gesture lasts until the button is released, so that the
	 * release does not click the element under the mouse.
	 * </p>
	 */
	private void abort() {
		DomGlobal.window.removeEventListener("keydown", _escape, true);
		_pending = false;
		_active = false;
		_aborted = true;
		removeRectangle();
	}

	private void removeRectangle() {
		if (_rectangle != null) {
			if (_rectangle.getParentNode() != null) {
				_rectangle.getParentNode().removeChild(_rectangle);
			}
			_rectangle = null;
		}
	}

	private void start() {
		_pending = false;
		_active = true;

		// Receive the pointer events also when the mouse leaves the diagram. Captured only now: a
		// captured press is not a click on the element it started on.
		svgElement().setPointerCapture(_pointerId);

		_rectangle = _control._svgDoc.createSVGRectElement();
		_rectangle.setClassNameBaseVal(RECTANGLE_CLASS);
		// Keep the outline thin, whatever the zoom.
		_rectangle.setAttribute("vector-effect", "non-scaling-stroke");
		_svg.appendChild(_rectangle);
	}

	private void updateRectangle() {
		double[] start = clientToSvg(_startX, _startY);
		double[] end = clientToSvg(_endX, _endY);
		_rectangle.setAttribute("x", Double.toString(JsMath.min(start[0], end[0])));
		_rectangle.setAttribute("y", Double.toString(JsMath.min(start[1], end[1])));
		_rectangle.setAttribute("width", Double.toString(JsMath.abs(end[0] - start[0])));
		_rectangle.setAttribute("height", Double.toString(JsMath.abs(end[1] - start[1])));
	}

	/**
	 * The selectable boxes drawn completely inside the rectangle.
	 *
	 * <p>
	 * Compared in screen coordinates, so that every transformation between the diagram and the
	 * screen - zoom, pan, scrolled parts of the diagram - is taken into account.
	 * </p>
	 */
	private List<SelectableBox> findEnclosed() {
		double left = JsMath.min(_startX, _endX);
		double right = JsMath.max(_startX, _endX);
		double top = JsMath.min(_startY, _endY);
		double bottom = JsMath.max(_startY, _endY);

		List<SelectableBox> result = new ArrayList<>();
		NodeList<Element> candidates = svgElement().querySelectorAll(SELECTABLE_ELEMENTS);
		for (int n = 0; n < candidates.length; n++) {
			Element candidate = candidates.item(n);
			Object widget = JSDiagramControl.getAttachedWidget(candidate);
			if (!(widget instanceof SelectableBox)) {
				continue;
			}
			DOMRect bounds = candidate.getBoundingClientRect();
			if (bounds.width == 0 && bounds.height == 0) {
				// Not displayed.
				continue;
			}
			if (bounds.left >= left && bounds.right <= right && bounds.top >= top && bounds.bottom <= bottom) {
				result.add((SelectableBox) widget);
			}
		}
		return result;
	}

	/**
	 * Keeps the click the browser fires after the release from reaching the element under the
	 * mouse: the drag ends there, it does not click it.
	 */
	private void suppressNextClick() {
		Element svgElement = svgElement();
		EventListener[] suppress = new EventListener[1];
		suppress[0] = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				evt.stopPropagation();
				evt.preventDefault();
				svgElement.removeEventListener("click", suppress[0], true);
			}
		};
		svgElement.addEventListener("click", suppress[0], true);

		// The click follows the release immediately, if at all.
		DomGlobal.setTimeout(args -> svgElement.removeEventListener("click", suppress[0], true), 0);
	}

	private double[] clientToSvg(double clientX, double clientY) {
		OMSVGMatrix ctm = _svg.getScreenCTM();
		if (ctm == null) {
			return new double[] { clientX, clientY };
		}
		OMSVGPoint point = _svg.createSVGPoint();
		point.setX((float) clientX);
		point.setY((float) clientY);
		OMSVGPoint svgPoint = point.matrixTransform(ctm.inverse());
		return new double[] { svgPoint.getX(), svgPoint.getY() };
	}

	private Element svgElement() {
		return Js.cast(_svg.getElement());
	}

}

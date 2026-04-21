/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.control;

import java.util.List;

import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGMatrix;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import com.google.gwt.dom.client.Element;

import com.top_logic.react.flow.client.dom.DOMUtil;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.drag.DragController;

import elemental2.dom.DomGlobal;

/**
 * Manages a single drag session on a flow diagram.
 *
 * <p>
 * On pointer-down, detects whether the user wants to move or resize a box. Creates a clone for
 * WYSIWYG feedback, dims the original, and updates the clone position/size on each pointer-move
 * frame. On pointer-up, commits the drag; on ESC, cancels it.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class DragHandler {

	/** Distance in SVG units from a box edge to trigger resize instead of move. */
	private static final double EDGE_THRESHOLD = 4.0;

	/** CSS class applied to the original SVG element during drag. */
	static final String DRAG_SOURCE_CLASS = "tl-drag-source";

	/** ID of the SVG group that contains the drag clone. */
	private static final String CLONE_GROUP_ID = "tl-drag-clone";

	private final FlowDiagramClientControl _control;

	private final OMSVGSVGElement _svg;

	/** The controller that handles constraints and commit. */
	private DragController _controller;

	/** The original box being dragged (not the clone). */
	private Box _target;

	/** The direct child of the controller that contains or is the target. */
	private Box _controllerChild;

	/** Drag edge if resizing, {@code null} if moving. */
	private DragEdge _edge;

	/** Drop areas for this drag session. */
	private List<DropArea> _dropAreas;

	/** SVG coordinates at drag start. */
	private double _startSvgX, _startSvgY;

	/** Original box position at drag start. */
	private double _origX, _origY, _origW, _origH;

	/** Current clone position/size. */
	private double _cloneX, _cloneY, _cloneW, _cloneH;

	/** Whether a drag session is currently active. */
	private boolean _active;

	/** The SVG element of the original box (to dim/undim). */
	private OMSVGElement _originalSvgElement;

	/** The clone SVG group element. */
	private OMSVGGElement _cloneGroup;

	/**
	 * Creates a {@link DragHandler}.
	 */
	DragHandler(FlowDiagramClientControl control, OMSVGSVGElement svg) {
		_control = control;
		_svg = svg;
	}

	/**
	 * Whether a drag session is active.
	 */
	boolean isActive() {
		return _active;
	}

	/**
	 * Attempts to start a drag session from a pointer-down event.
	 *
	 * @param clientX
	 *        the pointer X in client (screen) coordinates.
	 * @param clientY
	 *        the pointer Y in client (screen) coordinates.
	 * @param targetElement
	 *        the DOM element that received the pointer-down event.
	 * @return {@code true} if drag started, {@code false} if no draggable box was found.
	 */
	boolean tryStart(double clientX, double clientY, elemental2.dom.Element targetElement) {
		if (_active) {
			return false;
		}

		// Convert client coordinates to SVG coordinates.
		double[] svgCoords = clientToSvg(clientX, clientY);
		double svgX = svgCoords[0];
		double svgY = svgCoords[1];

		// Walk up the DOM from the event target to find a Box.
		Box hitBox = findBoxFromElement(targetElement);
		if (hitBox == null) {
			return false;
		}

		// Walk up the Box parent tree to find a DragController.
		DragController controller = null;
		Box controllerChild = null;
		DragEdge edge = detectEdge(hitBox, svgX, svgY);

		Box current = hitBox;
		while (current != null) {
			Widget parent = current.getParent();
			if (parent instanceof DragController) {
				DragController candidate = (DragController) parent;
				if (edge != null && candidate.canResize(current, edge)) {
					controller = candidate;
					controllerChild = current;
					break;
				} else if (edge == null && candidate.canMove(current)) {
					controller = candidate;
					controllerChild = current;
					break;
				}
			}
			if (parent instanceof Box) {
				current = (Box) parent;
			} else {
				break;
			}
		}

		if (controller == null) {
			return false;
		}

		_controller = controller;
		_target = hitBox;
		_controllerChild = controllerChild;
		_edge = edge;
		_startSvgX = svgX;
		_startSvgY = svgY;

		// Use the controller child for position/size (it's the direct child of the controller).
		_origX = _controllerChild.getX();
		_origY = _controllerChild.getY();
		_origW = _controllerChild.getWidth();
		_origH = _controllerChild.getHeight();
		_cloneX = _origX;
		_cloneY = _origY;
		_cloneW = _origW;
		_cloneH = _origH;

		_dropAreas = controller.getDropAreas(_controllerChild);

		// Dim the original element.
		String clientId = _controllerChild.getClientId();
		if (clientId != null) {
			_originalSvgElement = _control._svgDoc.getElementById(clientId);
			if (_originalSvgElement != null) {
				DOMUtil.addClassName(_originalSvgElement.getElement(), DRAG_SOURCE_CLASS);
			}
		}

		// Create clone group overlay.
		createCloneOverlay();

		_active = true;

		DomGlobal.console.info("Drag started on box: " + clientId
			+ (edge != null ? " (resize " + edge.name() + ")" : " (move)"));
		return true;
	}

	/**
	 * Updates the drag based on a pointer-move event.
	 */
	void onMove(double clientX, double clientY) {
		if (!_active) {
			return;
		}

		double[] svgCoords = clientToSvg(clientX, clientY);
		double svgX = svgCoords[0];
		double svgY = svgCoords[1];

		double dx = svgX - _startSvgX;
		double dy = svgY - _startSvgY;

		if (_edge == null) {
			// Move mode: apply delta to original position, then constrain.
			double proposedX = _origX + dx;
			double proposedY = _origY + dy;
			double[] constrained = _controller.constrainMove(_controllerChild, proposedX, proposedY);
			_cloneX = constrained[0];
			_cloneY = constrained[1];
			_cloneW = _origW;
			_cloneH = _origH;
		} else {
			// Resize mode: move the dragged edge.
			switch (_edge) {
				case N: {
					double proposedEdge = _origY + dy;
					double constrained = _controller.constrainResize(_controllerChild, _edge, proposedEdge);
					_cloneY = constrained;
					_cloneH = (_origY + _origH) - constrained;
					_cloneX = _origX;
					_cloneW = _origW;
					break;
				}
				case S: {
					double proposedEdge = (_origY + _origH) + dy;
					double constrained = _controller.constrainResize(_controllerChild, _edge, proposedEdge);
					_cloneY = _origY;
					_cloneH = constrained - _origY;
					_cloneX = _origX;
					_cloneW = _origW;
					break;
				}
				case W: {
					double proposedEdge = _origX + dx;
					double constrained = _controller.constrainResize(_controllerChild, _edge, proposedEdge);
					_cloneX = constrained;
					_cloneW = (_origX + _origW) - constrained;
					_cloneY = _origY;
					_cloneH = _origH;
					break;
				}
				case E: {
					double proposedEdge = (_origX + _origW) + dx;
					double constrained = _controller.constrainResize(_controllerChild, _edge, proposedEdge);
					_cloneX = _origX;
					_cloneW = constrained - _origX;
					_cloneY = _origY;
					_cloneH = _origH;
					break;
				}
			}
		}

		updateCloneOverlay();
	}

	/**
	 * Commits the drag on pointer-up.
	 */
	void commit() {
		if (!_active) {
			return;
		}

		DomGlobal.console.info("Drag committed: x=" + _cloneX + ", y=" + _cloneY
			+ ", w=" + _cloneW + ", h=" + _cloneH);

		_controller.commitDrag(_controllerChild, _cloneX, _cloneY, _cloneW, _cloneH);
		cleanup();
	}

	/**
	 * Cancels the drag (e.g., ESC key).
	 */
	void cancel() {
		if (!_active) {
			return;
		}

		DomGlobal.console.info("Drag cancelled.");

		_controller.cancelDrag(_controllerChild);
		cleanup();
	}

	private void cleanup() {
		// Un-dim the original.
		if (_originalSvgElement != null) {
			DOMUtil.removeClassName(_originalSvgElement.getElement(), DRAG_SOURCE_CLASS);
			_originalSvgElement = null;
		}

		// Remove clone overlay.
		removeCloneOverlay();

		_active = false;
		_controller = null;
		_target = null;
		_controllerChild = null;
		_edge = null;
		_dropAreas = null;
	}

	/**
	 * Detects which edge of the box is near the pointer, or {@code null} if the pointer is in
	 * the interior.
	 */
	private DragEdge detectEdge(Box box, double svgX, double svgY) {
		// Compute the absolute position of the box by walking up the parent tree
		// and accumulating translate offsets. This is needed because box.getX()/getY()
		// are relative to the parent.
		double absX = 0;
		double absY = 0;
		Box current = box;
		while (current != null) {
			absX += current.getX();
			absY += current.getY();
			Widget parent = current.getParent();
			if (parent instanceof Box) {
				current = (Box) parent;
			} else {
				break;
			}
		}

		double w = box.getWidth();
		double h = box.getHeight();

		double relX = svgX - absX;
		double relY = svgY - absY;

		// Check proximity to each edge.
		boolean nearLeft = relX < EDGE_THRESHOLD;
		boolean nearRight = (w - relX) < EDGE_THRESHOLD;
		boolean nearTop = relY < EDGE_THRESHOLD;
		boolean nearBottom = (h - relY) < EDGE_THRESHOLD;

		// Prefer horizontal edges (E/W) for Gantt charts since resize is typically left/right.
		if (nearLeft) {
			return DragEdge.W;
		}
		if (nearRight) {
			return DragEdge.E;
		}
		if (nearTop) {
			return DragEdge.N;
		}
		if (nearBottom) {
			return DragEdge.S;
		}
		return null;
	}

	/**
	 * Walks up the DOM from the given element to find a Box by matching clientIds in the
	 * diagram model.
	 */
	private Box findBoxFromElement(elemental2.dom.Element element) {
		elemental2.dom.Element current = element;
		while (current != null) {
			String id = current.id;
			if (id != null && !id.isEmpty()) {
				Widget widget = findWidgetById(id, _control._diagram.getRoot());
				if (widget instanceof Box) {
					return (Box) widget;
				}
			}
			current = current.parentElement;
		}
		return null;
	}

	/**
	 * Recursively searches for a Widget with the given clientId.
	 */
	private Widget findWidgetById(String clientId, Widget widget) {
		if (widget == null) {
			return null;
		}
		if (clientId.equals(widget.getClientId())) {
			return widget;
		}
		// Traverse children. Boxes that are Layouts or Decorations contain children.
		Widget found = searchChildren(clientId, widget);
		return found;
	}

	/**
	 * Search children of the given widget for the target clientId.
	 */
	private native Widget searchChildren(String clientId, Widget widget) /*-{
		// msgbuf objects expose properties directly. Walk all property values
		// looking for objects with a clientId.
		if (!widget) return null;

		// Use the Java-based visitor pattern: iterate through all own properties
		// looking for child Widgets.
		var props = Object.keys(widget);
		for (var i = 0; i < props.length; i++) {
			var val = widget[props[i]];
			if (val && typeof val === 'object') {
				if (Array.isArray(val)) {
					for (var j = 0; j < val.length; j++) {
						var item = val[j];
						if (item && item.clientId !== undefined) {
							var result = this.@com.top_logic.react.flow.client.control.DragHandler::findWidgetById(Ljava/lang/String;Lcom/top_logic/react/flow/data/Widget;)(clientId, item);
							if (result) return result;
						}
					}
				} else if (val.clientId !== undefined) {
					var result = this.@com.top_logic.react.flow.client.control.DragHandler::findWidgetById(Ljava/lang/String;Lcom/top_logic/react/flow/data/Widget;)(clientId, val);
					if (result) return result;
				}
			}
		}
		return null;
	}-*/;

	/**
	 * Converts client (screen) coordinates to SVG diagram coordinates using the
	 * SVG element's CTM (current transformation matrix).
	 */
	private double[] clientToSvg(double clientX, double clientY) {
		OMSVGPoint point = _svg.createSVGPoint();
		point.setX((float) clientX);
		point.setY((float) clientY);

		OMSVGMatrix ctm = _svg.getScreenCTM();
		if (ctm != null) {
			OMSVGMatrix inverse = ctm.inverse();
			OMSVGPoint svgPoint = point.matrixTransform(inverse);
			return new double[] { svgPoint.getX(), svgPoint.getY() };
		}

		// Fallback: use viewBox-based calculation.
		return new double[] { clientX, clientY };
	}

	/**
	 * Creates the clone overlay: a semi-transparent rectangle showing where the box would land.
	 */
	private void createCloneOverlay() {
		_cloneGroup = _control._svgDoc.createSVGGElement();
		_cloneGroup.setId(CLONE_GROUP_ID);
		_cloneGroup.setAttribute("pointer-events", "none");
		_svg.appendChild(_cloneGroup);

		updateCloneOverlay();
	}

	/**
	 * Updates the clone overlay rectangle to reflect the current clone position/size.
	 */
	private void updateCloneOverlay() {
		if (_cloneGroup == null) {
			return;
		}

		// Remove old children.
		while (_cloneGroup.getFirstChild() != null) {
			_cloneGroup.removeChild(_cloneGroup.getFirstChild());
		}

		// Draw a simple rectangle at the clone position with a dashed border.
		org.vectomatic.dom.svg.OMSVGRectElement rect = _control._svgDoc.createSVGRectElement(
			(float) _cloneX, (float) _cloneY, (float) _cloneW, (float) _cloneH, 2f, 2f);
		rect.setAttribute("fill", "rgba(66,133,244,0.2)");
		rect.setAttribute("stroke", "#4285F4");
		rect.setAttribute("stroke-width", "1.5");
		rect.setAttribute("stroke-dasharray", "4,3");
		_cloneGroup.appendChild(rect);
	}

	/**
	 * Removes the clone overlay from the SVG.
	 */
	private void removeCloneOverlay() {
		if (_cloneGroup != null) {
			_cloneGroup.getElement().removeFromParent();
			_cloneGroup = null;
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.drag;

import java.util.List;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;

/**
 * Interface for layouts that support interactive drag-and-drop of their child boxes.
 *
 * <p>
 * A box is draggable if walking up its parent tree finds a parent that implements
 * {@link DragController} and returns {@code true} from {@link #canMove} or
 * {@link #canResize} for its direct child. The drag system calls the controller's
 * methods during the drag lifecycle.
 * </p>
 *
 * <p>
 * All methods run on the client (GWT-compiled). They must be synchronous and must
 * not initiate server communication. Layouts that need server-computed drop areas
 * should pre-populate them in the model before sending to the client.
 * </p>
 */
public interface DragController {

	/** Whether this box may be moved by dragging its body. */
	boolean canMove(Box box);

	/** Whether this box may be resized by dragging the given edge. */
	boolean canResize(Box box, DragEdge edge);

	/**
	 * Valid drop areas for this box during a drag. Called once at drag-start; the result
	 * is cached for the duration of the drag.
	 *
	 * <p>
	 * Implementations may compute areas from the model (e.g., Gantt row geometry) or
	 * read pre-computed areas from a model field (e.g., server-populated drop areas).
	 * </p>
	 */
	List<DropArea> getDropAreas(Box box);

	/**
	 * Constrain a proposed absolute position during a move drag. Called every frame.
	 *
	 * @param box
	 *        the box being dragged (original, not the clone)
	 * @param proposedX
	 *        proposed X from mouse offset
	 * @param proposedY
	 *        proposed Y from mouse offset
	 * @return two-element array {@code {correctedX, correctedY}}
	 */
	double[] constrainMove(Box box, double proposedX, double proposedY);

	/**
	 * Constrain a proposed absolute edge position during a resize drag. Called every frame.
	 *
	 * @param box
	 *        the box being resized (original)
	 * @param edge
	 *        which edge is being dragged
	 * @param proposedEdgePos
	 *        proposed position of that edge
	 * @return corrected edge position
	 */
	double constrainResize(Box box, DragEdge edge, double proposedEdgePos);

	/**
	 * Commit the drag result to the model. Called on successful drop (mouseup).
	 *
	 * <p>
	 * The box still has its original position/size. The final values come as parameters.
	 * The controller compares with the originals to determine what changed (move vs.
	 * resize, which edge, row change) and writes the appropriate model mutations.
	 * </p>
	 */
	void commitDrag(Box box, double finalX, double finalY,
		double finalWidth, double finalHeight);

	/**
	 * Drag cancelled (ESC or pointer left the diagram). No model changes.
	 * Implementations should clean up any cached state (e.g., drop area maps).
	 */
	void cancelDrag(Box box);

	/**
	 * Converts SVG root coordinates to the layout coordinate space of this controller's
	 * children. Override this when the controller applies a viewport transform (e.g. scroll)
	 * that shifts the visual position of items relative to their layout coordinates.
	 *
	 * @return two-element array {@code {layoutX, layoutY}}
	 */
	default double[] svgToLayout(double svgX, double svgY) {
		return new double[] { svgX, svgY };
	}

	// -----------------------------------------------------------------------
	// Viewport pan support (drag-to-pan on empty space)
	// -----------------------------------------------------------------------

	/**
	 * Whether the controller supports viewport panning. If {@code true}, the drag
	 * system falls back to panning when no draggable item is found under the cursor.
	 */
	default boolean canPan() {
		return false;
	}

	/**
	 * Called when a viewport pan starts (pointer down on empty space, past threshold).
	 *
	 * @param svgX
	 *        Start position in SVG coordinates.
	 * @param svgY
	 *        Start position in SVG coordinates.
	 */
	default void startPan(double svgX, double svgY) {
	}

	/**
	 * Called on each frame during a viewport pan.
	 *
	 * @param svgX
	 *        Current position in SVG coordinates.
	 * @param svgY
	 *        Current position in SVG coordinates.
	 */
	default void panTo(double svgX, double svgY) {
	}

	/**
	 * Called when the viewport pan ends (pointer up).
	 */
	default void endPan() {
	}

	/**
	 * Callback for applying pan-related SVG transform updates.
	 */
	interface PanRenderer {
		/** Sets the {@code transform} attribute on the element with the given ID. */
		void setTranslate(String elementId, double tx, double ty);
	}

	/**
	 * Applies the current scroll state as SVG transforms. Called by the drag system
	 * after {@link #panTo} to update the visual representation without a full re-layout.
	 *
	 * @param renderer
	 *        Callback for DOM manipulation (provided by the client-side drag handler).
	 */
	default void renderPan(PanRenderer renderer) {
	}
}

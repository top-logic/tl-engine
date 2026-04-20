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
}

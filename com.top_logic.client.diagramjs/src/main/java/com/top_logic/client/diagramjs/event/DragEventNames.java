/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.event;

/**
 * Dragging diagramjs event names.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DragEventNames extends CommonEventNames {

	/**
	 * General drag operation.
	 */
	public static final String DRAG = "drag";

	/**
	 * Connect drap operation.
	 */
	public static final String CONNECT_DRAG = "connect";

	/**
	 * Lifecycle suffix for the drag end phase.
	 */
	public static final String DRAG_END_SUFFIX = "end";

	/**
	 * Lifecycle suffix for the drag move phase.
	 */
	public static final String DRAG_MOVE_SUFFIX = "move";

	/**
	 * Lifecycle suffix for the drag start phase.
	 */
	public static final String DRAG_START_SUFFIX = "start";

	/**
	 * Lifecycle suffix for the drag initialization.
	 */
	public static final String DRAG_INIT_SUFFIX = "init";

	/**
	 * Event triggered when a connect dragging is initialized.
	 */
	public static final String DRAG_CONNECT_INIT_EVENT = CONNECT_DRAG + EVENT_NAME_SEPARATOR + DRAG_INIT_SUFFIX;

	/**
	 * Event triggered when a dragging is initialized.
	 */
	public static final String DRAG_INIT_EVENT = DRAG + EVENT_NAME_SEPARATOR + DRAG_INIT_SUFFIX;

	/**
	 * Event triggered when a connect dragging is started.
	 */
	public static final String DRAG_CONNECT_START_EVENT = CONNECT_DRAG + EVENT_NAME_SEPARATOR + DRAG_START_SUFFIX;

	/**
	 * Event triggered when a dragging is started.
	 */
	public static final String DRAG_START_EVENT = DRAG + EVENT_NAME_SEPARATOR + DRAG_START_SUFFIX;

	/**
	 * Event triggered when a connect dragging is moved.
	 */
	public static final String DRAG_CONNECT_MOVE_EVENT = CONNECT_DRAG + EVENT_NAME_SEPARATOR + DRAG_MOVE_SUFFIX;

	/**
	 * Event triggered when a dragging is moved.
	 */
	public static final String DRAG_MOVE_EVENT = DRAG + EVENT_NAME_SEPARATOR + DRAG_MOVE_SUFFIX;

	/**
	 * Event triggered when a connect dragging is finished.
	 */
	public static final String DRAG_CONNECT_END_EVENT = CONNECT_DRAG + EVENT_NAME_SEPARATOR + DRAG_END_SUFFIX;

	/**
	 * Event triggered when a dragging is finished.
	 */
	public static final String DRAG_END_EVENT = DRAG + EVENT_NAME_SEPARATOR + DRAG_END_SUFFIX;
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Dragging objects out of one React control and dropping them on another.
 *
 * <p>
 * A control offering its rows for dragging implements {@link
 * com.top_logic.layout.react.control.dnd.DragSourceControl}, and one accepting a drop is configured
 * with a {@link com.top_logic.layout.react.control.dnd.DropTarget}. Which of the two the drop
 * command reaches is the receiving control; it resolves the gesture through this package's arguments
 * and announces a {@link com.top_logic.layout.react.control.dnd.DropEvent} carrying business
 * objects.
 * </p>
 *
 * <p>
 * Between the two controls travels nothing but client-side identity: the id of the control the drag
 * started in, the keys of its dragged rows, and a type tag classifying them. The dragged objects are
 * resolved by the source control, the target object by the receiving one, so neither end has to
 * trust a wire value with designating an object, and a source and a target need know nothing of each
 * other beyond the tag. The tag is also what the client decides on while a drag is still moving: it
 * highlights a control whose accepted tags the drag matches, and the server decides the drop itself
 * once it arrives.
 * </p>
 */
package com.top_logic.layout.react.control.dnd;

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Drag and drop declared in a view: the {@code <drag>} and {@code <drop>} configuration an element
 * offers, and the bindings that apply it to the generic drag-and-drop seam of the React controls
 * ({@link com.top_logic.layout.react.control.dnd.DragSourceControl},
 * {@link com.top_logic.layout.react.control.dnd.DropTarget}).
 *
 * <p>
 * What an element drags are the items it displays, and what a drop is made on is either the
 * element as a whole or one of its items; the element decides what its items are, the
 * configuration and the bindings here are the same for every element.
 * </p>
 *
 * @see com.top_logic.layout.view.dnd.DragConfig
 * @see com.top_logic.layout.view.dnd.DropConfig
 * @see com.top_logic.layout.view.dnd.DropBinding
 * @see com.top_logic.layout.view.dnd.DragSourceBinding
 */
package com.top_logic.layout.view.dnd;

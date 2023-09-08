/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collection;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The class {@link Drag} provides drag'n drop source elements of type T.
 * 
 * <p>
 * If an {@link AbstractControlBase control} shall serve as {@link Drag} it has to register the
 * {@link DragAndDropCommand} as {@link ControlCommand}.
 * </p>
 * 
 * <p>
 * To enable the client-side drag'n drop functionality, the following attributes must be given on
 * the {@link Drag} source elements:
 * </p>
 * 
 * <dl>
 * <dt>{@link HTMLConstants#TL_DRAG_N_DROP}</dt>
 * <dd>
 * The client-side IDs of potential {@link Drop} targets must be added to the element serving as
 * {@link Drag} source, see {@link DNDUtil#writeDNDInfo(TagWriter, FrameScope, Collection)}.</dd>
 * 
 * <dt>{@link HTMLConstants#TL_TYPE_ATTR}</dt>
 * <dd>A JavaScript reference to the prototype object providing functions
 * <code>createDrag(event, element)</code>, and
 * <code>createDrop(clientDragInfo, event, element)</code>. See <code>Draggable</code> in
 * <code>dragAndDrop.js</code>.</dd>
 * </dl>
 * 
 * @param <T>
 *        The type of objects provided by this {@link Drag} to be exchanged via drag'n drop.
 * 
 * @see Drop
 * @see DNDUtil#writeDNDInfo(TagWriter, FrameScope, Collection)
 * @see HTMLConstants#TL_DRAG_N_DROP
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Drag<T> {

	/**
	 * Notifies this {@link Drag} source that a drag'n drop operation should be performed to the
	 * {@link Drop} identified by the given ID.
	 * 
	 * @param dropId
	 *        The ID of the {@link Drop} which is the target.
	 * @param dragInfo
	 *        The client-side IDs of the dragged elements.
	 * @param dropInfo
	 *        Arguments understood by the {@link Drop} target to handle
	 *        {@link Drop#notifyDrop(Object, Object)}. Typically these arguments are just forwarded
	 *        to the target {@link Drop}.
	 */
	void notifyDrag(String dropId, Object dragInfo, Object dropInfo);

}

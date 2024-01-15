/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen;

import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Scope that can be observed for changes.
 * <p>
 * If a {@link ModelListener} is registered in multiple ways which match, it is still notified only
 * once. Example: A listener which is registered for a specific object and for its type, or if it is
 * registered for multiple types which match.
 * </p>
 * 
 * @implNote You may get a {@link ModelScope} through {@link FrameScope#getModelScope()}.
 * 
 * @see DisplayContext#getExecutionScope()
 * @see ControlScope#getFrameScope()
 * @see FrameScope#getModelScope()
 */
public interface ModelScope {

	/**
	 * Observes all objects for all kinds of changes.
	 *
	 * @param listener
	 *        The listener to inform, if a change occurs.
	 * @return Whether the given listener was (newly) added.
	 */
	boolean addModelListener(ModelListener listener);

	/**
	 * Observes all objects of the given type for all kinds of changes.
	 * <p>
	 * This includes all objects of subtypes, recursively.
	 * </p>
	 *
	 * @param type
	 *        The type of objects to observe.
	 * @param listener
	 *        The listener to inform, if a change occurs.
	 * @return Whether the given listener was (newly) added.
	 */
	boolean addModelListener(TLStructuredType type, ModelListener listener);

	/**
	 * Observes the given object for changes (updates and deletion).
	 *
	 * @param object
	 *        The observed object.
	 * @param listener
	 *        The listener to inform, if the given object is changed.
	 * @return Whether the given listener was (newly) added.
	 */
	boolean addModelListener(TLObject object, ModelListener listener);

	/**
	 * Removes the given listener that was added with {@link #addModelListener(ModelListener)}
	 * before.
	 *
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given listener was added with {@link #addModelListener(ModelListener)}.
	 */
	boolean removeModelListener(ModelListener listener);

	/**
	 * Removes the given listener that was added with
	 * {@link #addModelListener(TLStructuredType, ModelListener)} before.
	 * <p>
	 * It is not possible to register a listener for a type and than use this method to unregister
	 * for one of its sub types to exclude updates for them.
	 * </p>
	 *
	 * @param type
	 *        The type of observed objects.
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given listener was observing the given type.
	 */
	boolean removeModelListener(TLStructuredType type, ModelListener listener);

	/**
	 * Removes the given listener that was added with
	 * {@link #addModelListener(TLObject, ModelListener)} before.
	 *
	 * @param object
	 *        The observed object.
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given listener was observing this model before.
	 */
	boolean removeModelListener(TLObject object, ModelListener listener);

}

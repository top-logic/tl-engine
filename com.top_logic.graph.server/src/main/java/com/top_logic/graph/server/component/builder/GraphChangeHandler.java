/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.component.builder;

import com.top_logic.graph.common.model.GraphModel;

/**
 * Interface for listening for model changes that affect the graph display.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphChangeHandler {

	/**
	 * The given object was created. Update the {@link GraphModel} accordingly if necessary.
	 * 
	 * @param graph
	 *        Never null.
	 * @param newObject
	 *        Never null.
	 */
	default void handleModelCreated(GraphModel graph, Object newObject) {
		// Ignore.
	}

	/**
	 * The given object has changed. Update the {@link GraphModel} accordingly if necessary.
	 * <p>
	 * It is unspecified how the object has changed.
	 * </p>
	 * 
	 * @param graph
	 *        Never null.
	 * @param changedObject
	 *        Never null.
	 */
	default void handleModelChanged(GraphModel graph, Object changedObject) {
		// Ignore.
	}

	/**
	 * The given object was deleted. Update the {@link GraphModel} accordingly if necessary.
	 * 
	 * @param graph
	 *        Never null.
	 * @param deletedObject
	 *        Never null.
	 */
	default void handleModelDeleted(GraphModel graph, Object deletedObject) {
		// Ignore.
	}

}

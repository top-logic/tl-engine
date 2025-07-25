/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.Set;

import com.top_logic.mig.html.SelectionModel;

/**
 * An event sent after the selection in a {@link SelectionModel} has changed.
 * 
 * @see SelectionListener
 */
public interface SelectionEvent<T> {

	/**
	 * The {@link SelectionModel} that sent the event.
	 */
	SelectionModel<T> getSender();

	/**
	 * The objects that were selected before the select operation.
	 */
	Set<? extends T> getOldSelection();

	/**
	 * The objects that are selected after the select operation.
	 */
	Set<? extends T> getNewSelection();

	/**
	 * The objects have to be updated due to the selection change.
	 */
	Set<?> getUpdatedObjects();

}

/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

/**
 * Observable {@link #getMode() mode} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModeModel {

	/**
	 * Mode constant describing that the owner of a {@link ModeModel} can be
	 * modified by user interactions.
	 */
	public static final int EDIT_MODE = 1;
	
	/**
	 * Mode constant describing that the owner of a {@link ModeModel} cannot be
	 * modified by user interactions, but displays itself in a way that gives
	 * visual feedback that it could potentially be edited.
	 */
	public static final int DISABLED_MODE = 2;
	
	/**
	 * Mode constant describing that the owner of a {@link ModeModel} is
	 * displayed in a read-only fashion without any visual feedback that it
	 * could potentially be edited.
	 */
	public static final int IMMUTABLE_MODE = 3;
	
	/**
	 * Mode constant describing that the owner of a {@link ModeModel} is not
	 * being displayed.
	 */
	public static final int INVISIBLE_MODE = 4;

	/**
	 * The current mode.
	 * 
	 * @return One of {@link #EDIT_MODE}, {@link #DISABLED_MODE},
	 *         {@link #IMMUTABLE_MODE}, or {@link #INVISIBLE_MODE}.
	 */
	int getMode();
	
	/**
	 * Adds the given {@link ModeModelListener} to this model. 
	 */
	void addModeModelListener(ModeModelListener listener);
	
	/**
	 * Removes the given {@link ModeModelListener} to this model. 
	 */
	void removeModeModelListener(ModeModelListener listener);
	
}

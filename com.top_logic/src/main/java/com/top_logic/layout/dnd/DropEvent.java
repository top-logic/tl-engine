/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Event data containing information about a drop of a drag-and-drop operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DropEvent {

	private DndData _data;

	/**
	 * Creates a {@link DropEvent}.
	 *
	 * @param data
	 *        See {@link #getData()}.
	 */
	public DropEvent(DndData data) {
		_data = Objects.requireNonNull(data);
	}

	/**
	 * The source model from which the drag data originated from.
	 * 
	 * @see #getData()
	 */
	public Object getSource() {
		return _data.getSource().getDragSourceModel();
	}

	/**
	 * The data being dragged from the drag source.
	 * 
	 * @see #getSource()
	 */
	public Collection<Object> getData() {
		return _data.getDragData();
	}

	/**
	 * The drag target model on which the drop happened.
	 */
	public abstract Object getTarget();

	/**
	 * A {@link Function} determining a scripting name for the drag data.
	 * 
	 * <p>
	 * The input argument for the function must be the dragged data to create a {@link ModelName}
	 * for.
	 * </p>
	 * 
	 * <p>
	 * Must only be used when {@link ScriptingRecorder#isRecordingActive() recording is enabled}.
	 * </p>
	 * 
	 * @see DndData#getDragDataName()
	 */
	public Function<Object, ModelName> getDragDataName() {
		return _data.getDragDataName();
	}

}

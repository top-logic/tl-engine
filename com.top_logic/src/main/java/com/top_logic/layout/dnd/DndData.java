/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import java.util.Collection;
import java.util.function.Function;

import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Value holder for the drag'n drop data being sent in a drop operation.
 * 
 * @see DnD#getDndData(com.top_logic.layout.DisplayContext, java.util.Map)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DndData {

	private final DragSourceSPI _source;

	private final Collection<Object> _dragData;

	private final Function<ModelName, ModelName> _dragDataName;

	/**
	 * Creates a {@link DndData}.
	 *
	 * @param source
	 *        See {@link #getSource()}.
	 * @param dragData
	 *        See {@link #getDragData()}.
	 * @param dragDataName
	 *        See {@link #getDragDataName()}.
	 */
	public DndData(DragSourceSPI source, Collection<Object> dragData, Function<ModelName, ModelName> dragDataName) {
		_source = source;
		_dragData = dragData;
		_dragDataName = dragDataName;
	}

	/**
	 * The source (control) of the drag operation.
	 */
	public DragSourceSPI getSource() {
		return _source;
	}

	/**
	 * The objects being dragged.
	 */
	public Collection<Object> getDragData() {
		return _dragData;
	}

	/**
	 * A {@link Function} determining a scripting name for the drag data.
	 * 
	 * <p>
	 * The input argument for the function must be a {@link ModelName} for {@link #getSource()}, or
	 * <code>null</code> if no such name exists.
	 * </p>
	 * 
	 * <p>
	 * Must only be used when {@link ScriptingRecorder#isRecordingActive() recording is enabled}.
	 * </p>
	 */
	public Function<ModelName, ModelName> getDragDataName() {
		return _dragDataName;
	}

}

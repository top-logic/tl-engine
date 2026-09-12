/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@code dropObjects} command: drops the objects with the given {@link ModelName identities} on
 * the row with the given identity.
 *
 * <p>
 * This is the replay-stable form of a {@link DropArguments drop}: it names the business objects
 * themselves instead of the client-side keys the live gesture carried, so a recorded step resolves
 * again in a later session, after sorting, filtering, and without the control the objects were
 * dragged out of. The {@link DropEvent} a replayed drop announces therefore names no source
 * control.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans — each {@link ModelName} renders through its own
 * label, i.e. by the object's readable identity.
 * </p>
 */
@Label("Drop {objects} on '{targetObject}'")
public interface DropObjectsArguments extends ReactCommand {

	/** @see #getObjects() */
	String OBJECTS = "objects";

	/** @see #getTargetObject() */
	String TARGET_OBJECT = "targetObject";

	/** @see #getPosition() */
	String POSITION = DropArguments.POSITION;

	/**
	 * The business identities of the dropped objects.
	 */
	@Name(OBJECTS)
	List<ModelName> getObjects();

	/**
	 * The business identity of the row the objects were dropped on, or {@code null} when they were
	 * dropped on the control as a whole.
	 */
	@Name(TARGET_OBJECT)
	@Nullable
	ModelName getTargetObject();

	/** @see #getTargetObject() */
	void setTargetObject(ModelName value);

	/**
	 * Where the drop happened relative to the target row.
	 *
	 * @implNote One of the names {@link DropPosition#wireName()} transmits a position under.
	 */
	@Name(POSITION)
	String getPosition();

	/** @see #getPosition() */
	void setPosition(String value);

}

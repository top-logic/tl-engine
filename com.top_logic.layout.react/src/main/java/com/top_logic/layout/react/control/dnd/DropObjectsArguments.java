/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@code dropObjects} command: drops the objects with the given {@link ModelName identities} at
 * a {@link DropLocation} whose reference objects are named the same way.
 *
 * <p>
 * This is the replay-stable form of a {@link DropArguments drop}: it names the business objects
 * themselves instead of the client-side keys the live gesture carried, and the location the drop
 * was applied at instead of the place the pointer was at, so a recorded step resolves again in a
 * later session, after sorting, filtering, and without the control the objects were dragged out
 * of. The {@link DropEvent} a replayed drop announces therefore names no source control.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans — each {@link ModelName} renders through its own
 * label, i.e. by the object's readable identity.
 * </p>
 */
@Label("Drop {objects} ({mode})")
public interface DropObjectsArguments extends ReactCommand {

	/** @see #getObjects() */
	String OBJECTS = "objects";

	/** @see #getKind() */
	String KIND = "kind";

	/** @see #getMode() */
	String MODE = "mode";

	/** @see #getTargetObject() */
	String TARGET_OBJECT = "targetObject";

	/** @see #getParent() */
	String PARENT = "parent";

	/** @see #getBefore() */
	String BEFORE = "before";

	/**
	 * The business identities of the dropped objects.
	 */
	@Name(OBJECTS)
	List<ModelName> getObjects();

	/**
	 * The kind the objects were dragged as, absent for a drag without a kind.
	 *
	 * @implNote The {@link DragSourceControl#dragKind()} of the control the objects were dragged
	 *           out of when the drop was recorded.
	 */
	@Name(KIND)
	@Nullable
	String getKind();

	/** @see #getKind() */
	void setKind(String value);

	/**
	 * The mode of the operation the drop was applied by.
	 *
	 * @implNote One of the names {@link DropMode#wireName()} transmits a mode under.
	 */
	@Name(MODE)
	@Mandatory
	String getMode();

	/** @see #getMode() */
	void setMode(String value);

	/**
	 * The business identity of the item the objects were dropped onto, for a drop onto an item.
	 *
	 * @implNote The {@link DropLocation.Onto#target()} of the location.
	 */
	@Name(TARGET_OBJECT)
	@Nullable
	ModelName getTargetObject();

	/** @see #getTargetObject() */
	void setTargetObject(ModelName value);

	/**
	 * The business identity of the item the objects were inserted among the children of, for an
	 * insertion; absent for an insertion into a flat list.
	 *
	 * @implNote The {@link DropLocation.Insert#parent()} of the location.
	 */
	@Name(PARENT)
	@Nullable
	ModelName getParent();

	/** @see #getParent() */
	void setParent(ModelName value);

	/**
	 * The business identity of the item the objects were inserted before, for an insertion; absent
	 * for an insertion at the end.
	 *
	 * @implNote The {@link DropLocation.Insert#before()} of the location.
	 */
	@Name(BEFORE)
	@Nullable
	ModelName getBefore();

	/** @see #getBefore() */
	void setBefore(ModelName value);

}

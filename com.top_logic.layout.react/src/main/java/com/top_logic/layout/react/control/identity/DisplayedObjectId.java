/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.json.SimpleJsonListBinding;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ScriptingControl;

/**
 * Marks a property of a {@link ReactCommand} argument interface that carries the
 * {@link ObjectIdentities id} of an object a control displays - a {@link String} property for one
 * object, a {@link java.util.List} of {@link String} for several.
 *
 * <p>
 * The script recorder replaces each such id by the business key of its object, in the same
 * property; a replay resolves the key back to the id of the object in the replaying session before
 * it dispatches the command. Both translations use the {@link ScriptingControl#objectIdentities()
 * object identities} of the control displaying the objects, see {@link ObjectIdTranslation}: the
 * target control of the command, or the control another argument names, see {@link #control()}.
 * </p>
 *
 * <p>
 * A list-valued property is bound as a JSON array with
 * {@link com.top_logic.basic.config.json.JsonBinding}({@link SimpleJsonListBinding}.class), so a
 * key - which is JSON itself - keeps its commas in the arguments of a step.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DisplayedObjectId {

	/**
	 * The name of the argument property that names the control displaying the objects, empty for
	 * the target control of the command.
	 *
	 * <p>
	 * The named property is a {@link String} holding a reference of a control of the same window,
	 * as {@link ControlReferences#controlOf(String) the window resolves it}: its control id, as the browser
	 * sends it, or its semantic address. The script recorder replaces the reference by the address
	 * of the control, a replay resolves the address back to the control and dispatches its id. A
	 * reference designating no control of the window leaves the ids of the objects as they are.
	 * </p>
	 *
	 * <p>
	 * Example: a drop names the dragged objects by their ids in the control the drag started in,
	 * which another argument names.
	 * </p>
	 */
	String control() default "";

}

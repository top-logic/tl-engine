/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import com.top_logic.layout.react.control.ScriptingControl;

/**
 * How the headless projection of a control identifies the objects the control displays.
 *
 * <p>
 * The mode is passed to the {@link ScriptingControl#scriptingScalarState(ProjectionMode) projection
 * of a control} and handed on to the identity helpers that put an object's identity into the
 * projection, so a control builds its projection once for both modes.
 * </p>
 */
public enum ProjectionMode {

	/**
	 * The observation an agent acts on: an object is identified by the id the control displays it
	 * under - the id its commands take -, accompanied by its label.
	 *
	 * <p>
	 * Only a control offering {@link ScriptingControl#objectIdentities() object identities} projects
	 * ids; the objects of any other control are identified by their key as in {@link #KEY}.
	 * </p>
	 */
	AGENT,

	/**
	 * The session-independent form an assertion is recorded and checked in: an object is identified
	 * by its business key, an object without one by its id.
	 */
	KEY;

	/**
	 * Whether the projection identifies the objects of a control by their ids in this mode.
	 *
	 * @param identities
	 *        The {@link ScriptingControl#objectIdentities() object identities} of the control,
	 *        {@code null} if it offers none.
	 */
	public boolean projectsIds(ObjectIdentities identities) {
		return this == AGENT && identities != null;
	}
}

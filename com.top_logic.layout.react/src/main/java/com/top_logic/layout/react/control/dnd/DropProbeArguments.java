/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Typed arguments of the command a client sends while a drag hovers a {@link DropTarget}, asking
 * whether a drop right there would be accepted.
 *
 * <p>
 * The drop in question is named exactly like a {@link DropArguments drop}. In addition, the probe
 * names the drag it belongs to and carries an identifier of its own, under which the control
 * answers the {@link DropVerdict verdict}. A probe decides nothing and changes nothing.
 * </p>
 */
public interface DropProbeArguments extends DropArguments {

	/** @see #getDrag() */
	String DRAG = "drag";

	/** @see #getProbe() */
	String PROBE = "probe";

	/**
	 * Client-side identifier of the running drag.
	 *
	 * <p>
	 * The verdicts a control answers belong to one drag; a probe of another drag discards those of
	 * the previous one.
	 * </p>
	 */
	@Name(DRAG)
	@Mandatory
	String getDrag();

	/**
	 * Client-side identifier of this probe, under which the verdict is answered.
	 */
	@Name(PROBE)
	@Mandatory
	String getProbe();

}

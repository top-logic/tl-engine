/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.AbstractControlBase;

/**
 * The class {@link IdentifierSourceProxy} is an {@link IdentifierSource} based on some
 * {@link AbstractControlBase}. An {@link IdentifierSourceProxy} can be used if the
 * {@link FrameScope} of the {@link ControlScope} of some {@link Control} shall be used but the
 * {@link Control} is not attached yet.
 * <p>
 * <b>Attention:</b> It is necessary to ensure that the control is attached as soon an ID is
 * requested by this {@link IdentifierSource}
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IdentifierSourceProxy implements IdentifierSource {

	/** the control whose FrameScope shall be used as {@link IdentifierSource}*/
	private final AbstractControlBase control;

	/**
	 * Creates a {@link IdentifierSourceProxy} whose {@link #createNewID()}
	 * method based on the given <code>control</code>.
	 */
	public IdentifierSourceProxy(AbstractControlBase control) {
		this.control = control;
	}

	/**
	 * Calling this method creates a new ID based on the {@link FrameScope} of the
	 * {@link ControlScope} of the {@link #control} of this instance.
	 * 
	 * @see com.top_logic.layout.IdentifierSource#createNewID()
	 */
	@Override
	public String createNewID() {
		return control.getScope().getFrameScope().createNewID();
	}

}

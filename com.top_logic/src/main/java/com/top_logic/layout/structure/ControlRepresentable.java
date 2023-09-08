/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.layout.Control;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutComponent} that is directly displayed by a control.
 * 
 * <p>
 * Components implementing this probably also want to override
 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getComponentControlProvider()} and
 * add the annotation {@link ControlRepresentableCP}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ControlRepresentable {

	/**
	 * This method returns a {@link Control} which is used to render the contents of this
	 * {@link ControlRepresentable}, e.g. the {@link ContentControl} renders the content of some
	 * {@link ControlRepresentable} using this method.
	 * <p>
	 * It is neither ensured that subsequent calls of this method returns equal nor different
	 * controls.
	 * </p>
	 * 
	 * @return a {@link Control} to use for rendering. Never <code>null</code>.
	 */
	public Control getRenderingControl();
	

}

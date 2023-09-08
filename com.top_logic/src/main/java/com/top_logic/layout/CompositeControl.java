/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;

/**
 * Instances of {@link CompositeControl} are {@link Control}s which provide a list of inner
 * {@link Control}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompositeControl extends Control {

	/**
	 * This method returns the list of children of this {@link CompositeControl}.
	 * 
	 * @return a list of {@link Control}s. never <code>null</code>.
	 */
	public List<? extends HTMLFragment> getChildren();

}

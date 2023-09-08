/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the title of an object.
 * 
 * @see WindowModel#TITLE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TitleChangedListener extends PropertyListener {

	/**
	 * Handles change of the title of the given object.
	 * 
	 * @param sender
	 *        Object whose title changed.
	 * @param oldTitle
	 *        Former title.
	 * @param newTitle
	 *        New title.
	 */
	void handleTitleChanged(Object sender, HTMLFragment oldTitle, HTMLFragment newTitle);
}


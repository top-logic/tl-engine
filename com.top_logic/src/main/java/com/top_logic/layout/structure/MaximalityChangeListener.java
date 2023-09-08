/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyListener;

/**
 * The class {@link MaximalityChangeListener} is a listener interface for informing something that
 * the maximality of an object has changed.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface MaximalityChangeListener extends PropertyListener {

	/**
	 * This method is called if the maximality of the given sender is changed to
	 * <code>maximal</code>.
	 */
	void maximalityChanged(Object sender, boolean maximal);

}

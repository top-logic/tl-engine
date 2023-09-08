/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Interface to be implemented by {@link LayoutComponent} when the GUI must be updates manually.
 * 
 * <p>
 * It is not intended to make a simple redraw like in {@link LayoutComponent#invalidate()}, but also
 * updates states which is needed for display.
 * </p>
 *
 * <p>
 * Note: Use this interface rarely, because actually the state should be updated automatically due
 * to model event handling.
 * </p>
 * 
 * @author <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public interface Updatable {

	/**
	 * Updates the state used to create GUI and redraws the component.
	 */
	void update();
    
}


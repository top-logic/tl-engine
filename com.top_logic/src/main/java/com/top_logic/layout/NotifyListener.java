/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * The class {@link NotifyListener} can be implemented from some kind of
 * listener which have to do some work if it will be added as listener to a
 * model or removed as listener from a model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface NotifyListener {

	/**
	 * This method must be called to inform the listener that is was attached as
	 * listener to <code>aModel</code>.
	 */
	public void notifyAttachedTo(Object aModel);

	/**
	 * This method must be called to inform the listener that is was detached as
	 * listener from <code>aModel</code>.
	 */
	public void notifyDetachedFrom(Object aModel);
	
}

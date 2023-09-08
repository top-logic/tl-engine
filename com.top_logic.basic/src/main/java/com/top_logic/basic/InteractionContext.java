/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.thread.UnboundListener;

/**
 * Representation of a one interaction on the server.
 * 
 * <p>
 * Represents a server request.
 * </p>
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InteractionContext extends TypedAnnotatable {

	/**
	 * Returns the {@link SessionContext} in which the interaction is done.
	 */
	SessionContext getSessionContext();

	/**
	 * Sets the new value of {@link #getSessionContext()}
	 * 
	 * @param session
	 *        New value of {@link #getSessionContext()}
	 */
	void installSessionContext(SessionContext session);

	/**
	 * Returns the {@link SubSessionContext} in which this interaction is done.
	 */
	SubSessionContext getSubSessionContext();

	/**
	 * Sets the new value of {@link #getSubSessionContext()}
	 * 
	 * @param subSession
	 *        New value of {@link #getSubSessionContext()}
	 */
	void installSubSessionContext(SubSessionContext subSession);

	/**
	 * Invalidates this {@link InteractionContext}.
	 * 
	 * <p>
	 * This interaction can not longer be used when it is invalid.
	 * </p>
	 */
	void invalidate();

	/**
	 * Registers the given {@link UnboundListener}
	 * 
	 * @param l
	 *        The listener to be informed about removing this {@link InteractionContext} from its
	 *        thread.
	 * 
	 * @since 5.7.5
	 */
	void addUnboundListener(UnboundListener l);

}
